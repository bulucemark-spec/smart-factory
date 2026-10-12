package com.smartfactory.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.common.util.SeqNo;
import com.smartfactory.wms.entity.Location;
import com.smartfactory.wms.entity.Receipt;
import com.smartfactory.wms.entity.ReceiptItem;
import com.smartfactory.wms.entity.Inventory;
import com.smartfactory.wms.feign.QmsClient;
import com.smartfactory.wms.feign.dto.InspectionTaskRequest;
import com.smartfactory.wms.mapper.InventoryMapper;
import com.smartfactory.wms.mapper.LocationMapper;
import com.smartfactory.wms.mapper.ReceiptItemMapper;
import com.smartfactory.wms.mapper.ReceiptMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptMapper receiptMapper;
    private final ReceiptItemMapper itemMapper;
    private final LocationMapper locationMapper;
    private final InventoryMapper inventoryMapper;
    private final InventorySupport inventorySupport;
    private final QmsClient qmsClient;

    @Transactional(rollbackFor = Exception.class)
    public String receive(Receipt receipt) {
        receipt.setReceiptNo(SeqNo.next("RC"));
        receipt.setStatus("DONE");
        receipt.setReceiveTime(LocalDateTime.now());
        receiptMapper.insert(receipt);

        Location recvLoc = locationMapper.selectOne(new LambdaQueryWrapper<Location>()
                .eq(Location::getWarehouseId, receipt.getWarehouseId())
                .eq(Location::getLocationType, "RECEIVING"));
        Long recvLocId = recvLoc != null ? recvLoc.getId() : null;

        for (ReceiptItem item : receipt.getItems()) {
            item.setReceiptId(receipt.getId());
            item.setStatus("PENDING");
            item.setLocationId(recvLocId);
            if (!StringUtils.hasText(item.getBatchNo())) {
                item.setBatchNo(SeqNo.next("B"));
            }
            itemMapper.insert(item);

            Inventory inv = inventorySupport.findOrCreate(
                    receipt.getWarehouseId(), recvLocId,
                    item.getMaterialId(), item.getMaterialCode(), item.getMaterialName(),
                    item.getBatchNo(), "PENDING", item.getProductionDate());
            inventoryMapper.addQty(inv.getId(), item.getQty());

            pushIqc(receipt, item);
        }
        return receipt.getReceiptNo();
    }

    private void pushIqc(Receipt receipt, ReceiptItem item) {
        InspectionTaskRequest req = new InspectionTaskRequest();
        req.setInspectionType("IQC");
        req.setSourceType("RECEIPT");
        req.setSourceNo(receipt.getReceiptNo());
        req.setMaterialId(item.getMaterialId());
        req.setMaterialCode(item.getMaterialCode());
        req.setMaterialName(item.getMaterialName());
        req.setBatchNo(item.getBatchNo());
        req.setSupplierId(receipt.getSupplierId());
        req.setSupplierName(receipt.getSupplierName());
        req.setQty(item.getQty());
        try {
            qmsClient.createInspectionTask(req);
        } catch (Exception e) {
            log.warn("IQC 任务推送失败 sourceNo={} : {}", receipt.getReceiptNo(), e.getMessage());
        }
    }

    public Receipt detail(Long id) {
        Receipt r = receiptMapper.selectById(id);
        if (r != null) {
            r.setItems(itemMapper.selectList(
                    new LambdaQueryWrapper<ReceiptItem>().eq(ReceiptItem::getReceiptId, id)));
        }
        return r;
    }

    public List<Receipt> list() {
        return receiptMapper.selectList(new LambdaQueryWrapper<Receipt>().orderByDesc(Receipt::getId));
    }
}
