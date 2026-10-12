package com.smartfactory.mes.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.mes.entity.Genealogy;
import com.smartfactory.mes.mapper.GenealogyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenealogyService {

    private final GenealogyMapper genealogyMapper;

    /** 正向：成品批次 → 所用子件批次 */
    public List<Genealogy> backward(String parentBatch) {
        return genealogyMapper.selectList(new LambdaQueryWrapper<Genealogy>()
                .eq(Genealogy::getParentBatch, parentBatch));
    }

    /** 反向：子件批次 → 用于哪些成品批次 */
    public List<Genealogy> forward(String childBatch) {
        return genealogyMapper.selectList(new LambdaQueryWrapper<Genealogy>()
                .eq(Genealogy::getChildBatch, childBatch));
    }
}
