package com.smartfactory.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.common.exception.BusinessException;
import com.smartfactory.common.exception.ErrorCode;
import com.smartfactory.common.util.SeqNo;
import com.smartfactory.wms.entity.Inventory;
import com.smartfactory.wms.entity.Location;
import com.smartfactory.wms.entity.Outbound;
import com.smartfactory.wms.entity.OutboundItem;
import com.smartfactory.wms.mapper.InventoryMapper;
import com.smartfactory.wms.mapper.LocationMapper;
import com.smartfactory.wms.mapper.OutboundItemMapper;
import com.smartfactory.wms.mapper.OutboundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboundService {

    private final OutboundMapper outboundMapper;
    private final OutboundItemMapper itemMapper;
    private final LocationMapper locationMapper;
    private final InventoryMapper inventoryMapper;
    private final InventorySupport support;

    @Transactional(rollbackFor = Exception.class)
    public String create(Outbound outbound) {
        outbound.setOutboundNo(SeqNo.next("OB"));
        outbound.setType(outbound.getType() != null ? outbound.getType() : "SALES");
        outbound.setStatus("NEW");
        outboundMapper.insert(outbound);

        for (OutboundItem item : outbound.getItems()) {
            List<InventorySupport.Allocation> allocations =
                    support.allocate(outbound.getWarehouseId(), item.getMaterialId(), item.getQty());
            BigDecimal allocated = allocations.stream()
                    .map(InventorySupport.Allocation::getQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (allocated.compareTo(item.getQty()) < 0) {
                throw new BusinessException(3011,
                        "物料 " + item.getMaterialCode() + " 合格库存不足，缺 "
                                + item.getQty().subtract(allocated));
            }
            for (InventorySupport.Allocation a : allocations) {
                Inventory inv = a.getInventory();
                Location loc = locationMapper.selectById(inv.getLocationId());
                OutboundItem oi = new OutboundItem();
                oi.setOutboundId(outbound.getId());
                oi.setMaterialId(item.getMaterialId());
                oi.setMaterialCode(item.getMaterialCode());
                oi.setMaterialName(item.getMaterialName());
                oi.setBatchNo(inv.getBatchNo());
                oi.setQty(a.getQty());
                oi.setPickedQty(BigDecimal.ZERO);
                oi.setLocationCode(loc != null ? loc.getCode() : null);
                itemMapper.insert(oi);
            }
        }
        return outbound.getOutboundNo();
    }

    /** 确认发运：按批次条件扣减库存 */
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long id, String operator) {
        Outbound outbound = outboundMapper.selectById(id);
        if (outbound == null || !"NEW".equals(outbound.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "出库单状态非法或不存在");
        }
        List<OutboundItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<OutboundItem>().eq(OutboundItem::getOutboundId, id));
        for (OutboundItem item : items) {
            Inventory inv = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getMaterialId, item.getMaterialId())
                    .eq(Inventory::getBatchNo, item.getBatchNo())
                    .eq(Inventory::getStatus, "QUALIFIED")
                    .gt(Inventory::getQty, 0)
                    .orderByAsc(Inventory::getId)
                    .last("limit 1"));
            if (inv == null || inventoryMapper.deductQty(inv.getId(), item.getQty()) == 0) {
                throw new BusinessException(3011,
                        "库存不足，无法出库 batch=" + item.getBatchNo());
            }
            item.setPickedQty(item.getQty());
            itemMapper.updateById(item);
        }
        outbound.setStatus("SHIPPED");
        outbound.setShipTime(LocalDateTime.now());
        outbound.setOperatorName(operator);
        outboundMapper.updateById(outbound);
    }

    public Outbound detail(Long id) {
        Outbound ob = outboundMapper.selectById(id);
        if (ob != null) {
            ob.setItems(itemMapper.selectList(
                    new LambdaQueryWrapper<OutboundItem>().eq(OutboundItem::getOutboundId, id)));
        }
        return ob;
    }

    public List<Outbound> list() {
        return outboundMapper.selectList(
                new LambdaQueryWrapper<Outbound>().orderByDesc(Outbound::getId));
    }
}
