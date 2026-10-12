package com.smartfactory.qms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.common.result.Result;
import com.smartfactory.qms.entity.InspectionItem;
import com.smartfactory.qms.entity.InspectionStandard;
import com.smartfactory.qms.mapper.InspectionItemMapper;
import com.smartfactory.qms.mapper.InspectionStandardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/qms/standards")
@RequiredArgsConstructor
public class StandardController {

    private final InspectionStandardMapper standardMapper;
    private final InspectionItemMapper itemMapper;

    @GetMapping
    public Result<List<InspectionStandard>> list() {
        return Result.ok(standardMapper.selectList(
                new LambdaQueryWrapper<InspectionStandard>().orderByDesc(InspectionStandard::getId)));
    }

    @PostMapping
    public Result<Long> save(@RequestBody InspectionStandard standard) {
        if (standard.getId() == null) {
            standardMapper.insert(standard);
        } else {
            standardMapper.updateById(standard);
            itemMapper.delete(new LambdaQueryWrapper<InspectionItem>()
                    .eq(InspectionItem::getStandardId, standard.getId()));
        }
        if (standard.getItems() != null) {
            for (InspectionItem item : standard.getItems()) {
                item.setId(null);
                item.setStandardId(standard.getId());
                itemMapper.insert(item);
            }
        }
        return Result.ok(standard.getId());
    }

    @GetMapping("/{id}/detail")
    public Result<InspectionStandard> detail(@PathVariable Long id) {
        InspectionStandard standard = standardMapper.selectById(id);
        if (standard != null) {
            standard.setItems(itemMapper.selectList(new LambdaQueryWrapper<InspectionItem>()
                    .eq(InspectionItem::getStandardId, id)
                    .orderByAsc(InspectionItem::getSequence)));
        }
        return Result.ok(standard);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        standardMapper.deleteById(id);
        return Result.ok();
    }
}
