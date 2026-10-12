package com.smartfactory.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.wms.entity.Inventory;
import com.smartfactory.wms.mapper.InventoryMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 库存通用支撑：查找/创建、FIFO 候选与分配
 */
@Component
@RequiredArgsConstructor
public class InventorySupport {

    private final InventoryMapper inventoryMapper;

    public Inventory findOrCreate(Long warehouseId, Long locationId, Long materialId,
                                  String materialCode, String materialName,
                                  String batchNo, String status, LocalDate productionDate) {
        Inventory inv = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getLocationId, locationId)
                .eq(Inventory::getMaterialId, materialId)
                .eq(Inventory::getBatchNo, batchNo)
                .eq(Inventory::getStatus, status));
        if (inv != null) {
            return inv;
        }
        inv = new Inventory();
        inv.setWarehouseId(warehouseId);
        inv.setLocationId(locationId);
        inv.setMaterialId(materialId);
        inv.setMaterialCode(materialCode);
        inv.setMaterialName(materialName);
        inv.setBatchNo(batchNo);
        inv.setQty(BigDecimal.ZERO);
        inv.setStatus(status);
        inv.setProductionDate(productionDate);
        inventoryMapper.insert(inv);
        return inv;
    }

    /** FIFO 合格批次，按效期、生产日期、id 升序 */
    public List<Inventory> fifoCandidates(Long warehouseId, Long materialId) {
        LambdaQueryWrapper<Inventory> w = new LambdaQueryWrapper<>();
        w.eq(Inventory::getMaterialId, materialId)
                .eq(Inventory::getStatus, "QUALIFIED")
                .gt(Inventory::getQty, BigDecimal.ZERO);
        if (warehouseId != null) {
            w.eq(Inventory::getWarehouseId, warehouseId);
        }
        w.orderByAsc(Inventory::getExpiryDate)
                .orderByAsc(Inventory::getProductionDate)
                .orderByAsc(Inventory::getId);
        return inventoryMapper.selectList(w);
    }

    /** 按 FIFO 分配 need，返回 (库存, 占用量) 列表；返回总量 < need 即存在短缺 */
    public List<Allocation> allocate(Long warehouseId, Long materialId, BigDecimal need) {
        List<Allocation> result = new ArrayList<>();
        BigDecimal remain = need;
        for (Inventory inv : fifoCandidates(warehouseId, materialId)) {
            if (remain.signum() <= 0) {
                break;
            }
            BigDecimal take = inv.getQty().min(remain);
            if (take.signum() > 0) {
                result.add(new Allocation(inv, take));
                remain = remain.subtract(take);
            }
        }
        return result;
    }

    @Data
    @AllArgsConstructor
    public static class Allocation {
        private Inventory inventory;
        private BigDecimal qty;
    }
}
