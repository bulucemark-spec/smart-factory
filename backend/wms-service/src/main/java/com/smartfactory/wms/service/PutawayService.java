package com.smartfactory.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.common.exception.BusinessException;
import com.smartfactory.common.exception.ErrorCode;
import com.smartfactory.common.util.SeqNo;
import com.smartfactory.wms.dto.DisposeDto;
import com.smartfactory.wms.dto.InspectionCallbackDto;
import com.smartfactory.wms.entity.Inventory;
import com.smartfactory.wms.entity.Location;
import com.smartfactory.wms.entity.PutawayTask;
import com.smartfactory.wms.mapper.InventoryMapper;
import com.smartfactory.wms.mapper.LocationMapper;
import com.smartfactory.wms.mapper.PutawayTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PutawayService {

    private final PutawayTaskMapper taskMapper;
    private final LocationMapper locationMapper;
    private final InventoryMapper inventoryMapper;
    private final InventorySupport support;

    /** IQC 合格：为待检批次生成上架任务 */
    @Transactional(rollbackFor = Exception.class)
    public void onPassed(InspectionCallbackDto dto) {
        Inventory pending = findByMaterialBatch(dto.getMaterialId(), dto.getBatchNo(), "PENDING");
        if (pending == null) {
            return;
        }
        PutawayTask task = new PutawayTask();
        task.setTaskNo(SeqNo.next("PA"));
        task.setWarehouseId(pending.getWarehouseId());
        task.setMaterialId(dto.getMaterialId());
        task.setMaterialName(pending.getMaterialName());
        task.setBatchNo(dto.getBatchNo());
        task.setQty(dto.getQty() != null ? dto.getQty() : pending.getQty());
        task.setFromLocationId(pending.getLocationId());
        task.setStatus("NEW");
        taskMapper.insert(task);
    }

    /** 推荐可用存储库位 */
    public List<Location> recommend(Long warehouseId) {
        return locationMapper.selectList(new LambdaQueryWrapper<Location>()
                .eq(Location::getWarehouseId, warehouseId)
                .eq(Location::getLocationType, "STORAGE")
                .eq(Location::getStatus, 1)
                .orderByAsc(Location::getAisle)
                .orderByAsc(Location::getLayer)
                .last("limit 20"));
    }

    /** 执行上架：待检 → 目标库位合格库存 */
    @Transactional(rollbackFor = Exception.class)
    public void execute(Long taskId, Long toLocationId, String operator) {
        PutawayTask task = taskMapper.selectById(taskId);
        if (task == null || !"NEW".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "上架任务状态非法或不存在");
        }
        Location to = locationMapper.selectById(toLocationId);
        if (to == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "目标库位不存在");
        }

        Inventory from = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getLocationId, task.getFromLocationId())
                .eq(Inventory::getMaterialId, task.getMaterialId())
                .eq(Inventory::getBatchNo, task.getBatchNo())
                .eq(Inventory::getStatus, "PENDING")
                .last("limit 1"));
        if (from == null || inventoryMapper.deductAny(from.getId(), task.getQty()) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "待检库存不足，无法上架");
        }

        Inventory target = support.findOrCreate(to.getWarehouseId(), to.getId(),
                task.getMaterialId(), from.getMaterialCode(), task.getMaterialName(),
                task.getBatchNo(), "QUALIFIED", from.getProductionDate());
        inventoryMapper.addQty(target.getId(), task.getQty());

        task.setToLocationId(to.getId());
        task.setToLocationCode(to.getCode());
        task.setStatus("DONE");
        task.setOperatorName(operator);
        task.setFinishTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    /** 检验不合格：冻结批次 */
    @Transactional(rollbackFor = Exception.class)
    public void onFreeze(InspectionCallbackDto dto) {
        Inventory inv = findByMaterialBatch(dto.getMaterialId(), dto.getBatchNo(), "PENDING");
        if (inv == null) {
            inv = findByMaterialBatch(dto.getMaterialId(), dto.getBatchNo(), "QUALIFIED");
        }
        if (inv != null) {
            inventoryMapper.changeStatus(inv.getId(), "FROZEN");
        }
    }

    /** OQC 合格：成品批次放行 */
    @Transactional(rollbackFor = Exception.class)
    public void onRelease(InspectionCallbackDto dto) {
        Inventory inv = findByMaterialBatch(dto.getMaterialId(), dto.getBatchNo(), "PENDING");
        if (inv != null) {
            inventoryMapper.changeStatus(inv.getId(), "QUALIFIED");
        }
    }

    /** MRB 处置 */
    @Transactional(rollbackFor = Exception.class)
    public void dispose(DisposeDto dto) {
        Inventory inv = findByMaterialBatch(dto.getMaterialId(), dto.getBatchNo(), "FROZEN");
        if (inv == null) {
            inv = findByMaterialBatch(dto.getMaterialId(), dto.getBatchNo(), "PENDING");
        }
        if (inv == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应批次库存");
        }
        switch (dto.getDisposition()) {
            case "SCRAP" -> inventoryMapper.changeStatus(inv.getId(), "SCRAPPED");
            case "CONCESSION", "REWORK", "SORT" -> inventoryMapper.changeStatus(inv.getId(), "QUALIFIED");
            case "RETURN" -> inventoryMapper.deductAny(inv.getId(),
                    dto.getQty() != null ? dto.getQty() : inv.getQty());
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "未知处置方式");
        }
    }

    private Inventory findByMaterialBatch(Long materialId, String batchNo, String status) {
        return inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getMaterialId, materialId)
                .eq(Inventory::getBatchNo, batchNo)
                .eq(Inventory::getStatus, status)
                .gt(Inventory::getQty, 0)
                .last("limit 1"));
    }
}
