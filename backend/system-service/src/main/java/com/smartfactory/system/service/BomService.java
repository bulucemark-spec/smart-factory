package com.smartfactory.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.common.exception.BusinessException;
import com.smartfactory.common.exception.ErrorCode;
import com.smartfactory.system.entity.Bom;
import com.smartfactory.system.entity.BomItem;
import com.smartfactory.system.entity.Material;
import com.smartfactory.system.mapper.BomItemMapper;
import com.smartfactory.system.mapper.BomMapper;
import com.smartfactory.system.mapper.MaterialMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BomService {

    private final BomMapper bomMapper;
    private final BomItemMapper itemMapper;
    private final MaterialMapper materialMapper;

    public Bom detail(Long id) {
        Bom bom = bomMapper.selectById(id);
        if (bom == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "BOM 不存在");
        }
        List<BomItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<BomItem>()
                        .eq(BomItem::getBomId, id)
                        .orderByAsc(BomItem::getSequence));
        fillMaterial(items);
        bom.setItems(items);
        return bom;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveWithItems(Bom bom) {
        if (bom.getId() == null) {
            bomMapper.insert(bom);
        } else {
            bomMapper.updateById(bom);
            itemMapper.delete(new LambdaQueryWrapper<BomItem>().eq(BomItem::getBomId, bom.getId()));
        }
        if (bom.getItems() != null) {
            for (BomItem item : bom.getItems()) {
                item.setId(null);
                item.setBomId(bom.getId());
                itemMapper.insert(item);
            }
        }
        return bom.getId();
    }

    private void fillMaterial(List<BomItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> ids = items.stream().map(BomItem::getMaterialId).collect(Collectors.toSet());
        Map<Long, Material> map = materialMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Material::getId, m -> m));
        for (BomItem item : items) {
            Material m = map.get(item.getMaterialId());
            if (m != null) {
                item.setMaterialCode(m.getCode());
                item.setMaterialName(m.getName());
                item.setUnit(m.getUnit());
            }
        }
    }
}
