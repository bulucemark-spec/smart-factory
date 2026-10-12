package com.smartfactory.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.common.exception.BusinessException;
import com.smartfactory.common.exception.ErrorCode;
import com.smartfactory.common.util.SeqNo;
import com.smartfactory.wms.dto.FinishedInboundDto;
import com.smartfactory.wms.dto.PickItemDto;
import com.smartfactory.wms.dto.PickWaveDto;
import com.smartfactory.wms.entity.Inventory;
import com.smartfactory.wms.entity.Location;
import com.smartfactory.wms.entity.PickTask;
import com.smartfactory.wms.entity.PickWave;
import com.smartfactory.wms.mapper.InventoryMapper;
import com.smartfactory.wms.mapper.LocationMapper;
import com.smartfactory.wms.mapper.PickTaskMapper;
import com.smartfactory.wms.mapper.PickWaveMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PickService {

    private final PickWaveMapper waveMapper;
    private final PickTaskMapper taskMapper;
    private final LocationMapper locationMapper;
    private final InventoryMapper inventoryMapper;
    private final InventorySupport support;

    /** MES 下发工单：按 BOM 生成领料波次，FIFO 分配批次 */
    @Transactional(rollbackFor = Exception.class)
    public String createWave(PickWaveDto dto) {
        PickWave wave = new PickWave();
        wave.setWaveNo(SeqNo.next("WV"));
        wave.setSourceType("WORK_ORDER");
        wave.setSourceId(dto.getSourceId());
        wave.setSourceNo(dto.getSourceNo());
        wave.setWarehouseId(dto.getWarehouseId() != null ? dto.getWarehouseId() : 1L);
        wave.setStatus("RELEASED");
        waveMapper.insert(wave);

        for (PickItemDto item : dto.getItems()) {
            List<InventorySupport.Allocation> allocations =
                    support.allocate(wave.getWarehouseId(), item.getMaterialId(), item.getQty());
            for (InventorySupport.Allocation a : allocations) {
                Inventory inv = a.getInventory();
                Location loc = locationMapper.selectById(inv.getLocationId());
                PickTask task = new PickTask();
                task.setWaveId(wave.getId());
                task.setMaterialId(item.getMaterialId());
                task.setMaterialCode(item.getMaterialCode());
                task.setMaterialName(item.getMaterialName());
                task.setBatchNo(inv.getBatchNo());
                task.setLocationId(inv.getLocationId());
                task.setLocationCode(loc != null ? loc.getCode() : String.valueOf(inv.getLocationId()));
                task.setQty(a.getQty());
                task.setPickedQty(BigDecimal.ZERO);
                task.setStatus("NEW");
                taskMapper.insert(task);
            }
            BigDecimal allocated = allocations.stream()
                    .map(InventorySupport.Allocation::getQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (allocated.compareTo(item.getQty()) < 0) {
                throw new BusinessException(3011,
                        "物料 " + item.getMaterialCode() + " 合格库存不足，缺 "
                                + item.getQty().subtract(allocated));
            }
        }
        return wave.getWaveNo();
    }

    /** 执行整波拣货：条件扣减合格库存 */
    @Transactional(rollbackFor = Exception.class)
    public void executeWave(Long waveId, String operator) {
        PickWave wave = waveMapper.selectById(waveId);
        if (wave == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "波次不存在");
        }
        List<PickTask> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<PickTask>().eq(PickTask::getWaveId, waveId));
        for (PickTask task : tasks) {
            if (!"NEW".equals(task.getStatus())) {
                continue;
            }
            Inventory inv = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getLocationId, task.getLocationId())
                    .eq(Inventory::getMaterialId, task.getMaterialId())
                    .eq(Inventory::getBatchNo, task.getBatchNo())
                    .eq(Inventory::getStatus, "QUALIFIED")
                    .last("limit 1"));
            if (inv == null || inventoryMapper.deductQty(inv.getId(), task.getQty()) == 0) {
                throw new BusinessException(3011, "库存不足：" + task.getMaterialName());
            }
            task.setPickedQty(task.getQty());
            task.setStatus("PICKED");
            task.setOperatorName(operator);
            task.setPickTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }
        wave.setStatus("PICKED");
        waveMapper.updateById(wave);
    }

    /** MES 线边交接：按工单号查询已拣明细 */
    public List<PickTask> resultBySourceNo(String sourceNo) {
        PickWave wave = waveMapper.selectOne(new LambdaQueryWrapper<PickWave>()
                .eq(PickWave::getSourceNo, sourceNo)
                .orderByDesc(PickWave::getId)
                .last("limit 1"));
        if (wave == null) {
            return List.of();
        }
        return taskMapper.selectList(
                new LambdaQueryWrapper<PickTask>().eq(PickTask::getWaveId, wave.getId()));
    }

    /** MES 完工：成品入成品仓（待 OQC） */
    @Transactional(rollbackFor = Exception.class)
    public void finishedInbound(FinishedInboundDto dto) {
        Long warehouseId = dto.getWarehouseId() != null ? dto.getWarehouseId() : 3L;
        Location loc = locationMapper.selectOne(new LambdaQueryWrapper<Location>()
                .eq(Location::getWarehouseId, warehouseId)
                .eq(Location::getLocationType, "STORAGE")
                .orderByAsc(Location::getId)
                .last("limit 1"));
        Long locationId = loc != null ? loc.getId() : null;
        Inventory inv = support.findOrCreate(warehouseId, locationId,
                dto.getMaterialId(), dto.getMaterialCode(), dto.getMaterialName(),
                dto.getBatchNo(), "PENDING", null);
        inventoryMapper.addQty(inv.getId(), dto.getQty());
    }
}
