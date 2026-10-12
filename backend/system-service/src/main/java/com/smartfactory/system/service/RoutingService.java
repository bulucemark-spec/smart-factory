package com.smartfactory.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.common.exception.BusinessException;
import com.smartfactory.common.exception.ErrorCode;
import com.smartfactory.system.entity.Routing;
import com.smartfactory.system.entity.RoutingOperation;
import com.smartfactory.system.entity.WorkCenter;
import com.smartfactory.system.mapper.RoutingMapper;
import com.smartfactory.system.mapper.RoutingOperationMapper;
import com.smartfactory.system.mapper.WorkCenterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutingService {

    private final RoutingMapper routingMapper;
    private final RoutingOperationMapper operationMapper;
    private final WorkCenterMapper workCenterMapper;

    public Routing detail(Long id) {
        Routing routing = routingMapper.selectById(id);
        if (routing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工艺路线不存在");
        }
        List<RoutingOperation> ops = operationMapper.selectList(
                new LambdaQueryWrapper<RoutingOperation>()
                        .eq(RoutingOperation::getRoutingId, id)
                        .orderByAsc(RoutingOperation::getSequence));
        fillWorkCenter(ops);
        routing.setOperations(ops);
        return routing;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveWithOperations(Routing routing) {
        if (routing.getId() == null) {
            routingMapper.insert(routing);
        } else {
            routingMapper.updateById(routing);
            operationMapper.delete(
                    new LambdaQueryWrapper<RoutingOperation>().eq(RoutingOperation::getRoutingId, routing.getId()));
        }
        if (routing.getOperations() != null) {
            for (RoutingOperation op : routing.getOperations()) {
                op.setId(null);
                op.setRoutingId(routing.getId());
                operationMapper.insert(op);
            }
        }
        return routing.getId();
    }

    private void fillWorkCenter(List<RoutingOperation> ops) {
        if (ops == null || ops.isEmpty()) {
            return;
        }
        Set<Long> ids = ops.stream().map(RoutingOperation::getWorkCenterId).collect(Collectors.toSet());
        Map<Long, WorkCenter> map = workCenterMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(WorkCenter::getId, w -> w));
        for (RoutingOperation op : ops) {
            WorkCenter w = map.get(op.getWorkCenterId());
            if (w != null) {
                op.setWorkCenterCode(w.getCode());
                op.setWorkCenterName(w.getName());
            }
        }
    }
}
