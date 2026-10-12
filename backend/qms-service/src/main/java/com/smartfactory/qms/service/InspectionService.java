package com.smartfactory.qms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.common.context.UserContext;
import com.smartfactory.common.exception.BusinessException;
import com.smartfactory.common.exception.ErrorCode;
import com.smartfactory.common.util.SeqNo;
import com.smartfactory.qms.dto.InspectionCallbackDto;
import com.smartfactory.qms.dto.InspectionTaskCreateDto;
import com.smartfactory.qms.dto.ResultInput;
import com.smartfactory.qms.dto.SubmitResultDto;
import com.smartfactory.qms.entity.InspectionItem;
import com.smartfactory.qms.entity.InspectionResult;
import com.smartfactory.qms.entity.InspectionStandard;
import com.smartfactory.qms.entity.InspectionTask;
import com.smartfactory.qms.feign.WmsClient;
import com.smartfactory.qms.mapper.InspectionItemMapper;
import com.smartfactory.qms.mapper.InspectionResultMapper;
import com.smartfactory.qms.mapper.InspectionStandardMapper;
import com.smartfactory.qms.mapper.InspectionTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InspectionService {

    private final InspectionTaskMapper taskMapper;
    private final InspectionStandardMapper standardMapper;
    private final InspectionItemMapper itemMapper;
    private final InspectionResultMapper resultMapper;
    private final NcrService ncrService;
    private final WmsClient wmsClient;

    /** 内部：WMS 收货 / MES 完工 → 建立检验任务 */
    @Transactional(rollbackFor = Exception.class)
    public String createTask(InspectionTaskCreateDto dto) {
        InspectionStandard std = findStandard(dto.getMaterialId(), dto.getSupplierId(), dto.getInspectionType());
        InspectionTask task = new InspectionTask();
        task.setTaskNo(SeqNo.next(dto.getInspectionType()));
        task.setInspectionType(dto.getInspectionType());
        task.setSourceType(dto.getSourceType());
        task.setSourceNo(dto.getSourceNo());
        task.setMaterialId(dto.getMaterialId());
        task.setMaterialCode(dto.getMaterialCode());
        task.setMaterialName(dto.getMaterialName());
        task.setBatchNo(dto.getBatchNo());
        task.setSupplierId(dto.getSupplierId());
        task.setSupplierName(dto.getSupplierName());
        task.setSampleSize(std != null ? std.getSampleSize() : 5);
        task.setStatus("PENDING");
        taskMapper.insert(task);
        return task.getTaskNo();
    }

    /** 录入实测结果并自动判定 */
    @Transactional(rollbackFor = Exception.class)
    public void submit(SubmitResultDto dto, UserContext inspector) {
        InspectionTask task = taskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "检验任务不存在");
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "检验任务已判定，禁止重复提交");
        }

        InspectionStandard std = findStandard(task.getMaterialId(), task.getSupplierId(), task.getInspectionType());
        Map<Long, InspectionItem> itemMap = std != null ? loadItems(std.getId()) : new HashMap<>();

        resultMapper.delete(new LambdaQueryWrapper<InspectionResult>().eq(InspectionResult::getTaskId, task.getId()));

        List<String> ngItems = new ArrayList<>();
        for (ResultInput in : dto.getResults()) {
            InspectionItem item = itemMap.get(in.getItemId());
            String judge = judge(item, in);

            InspectionResult result = new InspectionResult();
            result.setTaskId(task.getId());
            result.setItemId(in.getItemId());
            result.setItemName(item != null ? item.getName() : null);
            result.setMeasuredValue(in.getMeasuredValue());
            result.setJudge(judge);
            result.setRemark(in.getRemark());
            resultMapper.insert(result);

            if ("NG".equals(judge)) {
                ngItems.add((item != null ? item.getName() : "检验项") + " 不合格");
            }
        }

        task.setInspector(inspector != null ? inspector.getUserId() : null);
        task.setInspectorName(inspector != null ? inspector.getUsername() : null);
        task.setInspectTime(LocalDateTime.now());

        if (ngItems.isEmpty()) {
            task.setStatus("QUALIFIED");
            task.setConclusion("全部项目合格");
            taskMapper.updateById(task);
            onQualified(task);
        } else {
            task.setStatus("UNQUALIFIED");
            task.setConclusion("不合格项：" + String.join("；", ngItems));
            taskMapper.updateById(task);
            onUnqualified(task, String.join("；", ngItems));
        }
    }

    private String judge(InspectionItem item, ResultInput in) {
        if (item != null && item.getUpperLimit() != null && in.getMeasuredValue() != null) {
            BigDecimal v = in.getMeasuredValue();
            boolean ok = (item.getLowerLimit() == null || v.compareTo(item.getLowerLimit()) >= 0)
                    && (item.getUpperLimit() == null || v.compareTo(item.getUpperLimit()) <= 0);
            return ok ? "OK" : "NG";
        }
        return in.getJudge() != null ? in.getJudge() : "OK";
    }

    private void onQualified(InspectionTask task) {
        InspectionCallbackDto cb = baseCallback(task);
        cb.setPassed(true);
        try {
            if ("OQC".equals(task.getInspectionType())) {
                wmsClient.release(cb);
            } else {
                wmsClient.inspectionPassed(cb);
            }
        } catch (Exception e) {
            log.warn("WMS 合格回调失败 task={} : {}", task.getTaskNo(), e.getMessage());
        }
    }

    private void onUnqualified(InspectionTask task, String desc) {
        InspectionCallbackDto cb = baseCallback(task);
        cb.setPassed(false);
        try {
            wmsClient.freeze(cb);
        } catch (Exception e) {
            log.warn("WMS 冻结回调失败 task={} : {}", task.getTaskNo(), e.getMessage());
        }
        ncrService.createFromInspection(task, desc);
    }

    private InspectionCallbackDto baseCallback(InspectionTask task) {
        InspectionCallbackDto cb = new InspectionCallbackDto();
        cb.setInspectionType(task.getInspectionType());
        cb.setSourceNo(task.getSourceNo());
        cb.setMaterialId(task.getMaterialId());
        cb.setBatchNo(task.getBatchNo());
        return cb;
    }

    public Map<String, Object> taskDetail(Long id) {
        InspectionTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "检验任务不存在");
        }
        InspectionStandard std = findStandard(task.getMaterialId(), task.getSupplierId(), task.getInspectionType());
        List<InspectionItem> items = std != null
                ? itemMapper.selectList(new LambdaQueryWrapper<InspectionItem>()
                        .eq(InspectionItem::getStandardId, std.getId())
                        .orderByAsc(InspectionItem::getSequence))
                : List.of();
        List<InspectionResult> results = resultMapper.selectList(
                new LambdaQueryWrapper<InspectionResult>().eq(InspectionResult::getTaskId, id));
        Map<String, Object> map = new HashMap<>();
        map.put("task", task);
        map.put("items", items);
        map.put("results", results);
        return map;
    }

    public List<InspectionTask> list(String status, String type) {
        LambdaQueryWrapper<InspectionTask> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(InspectionTask::getStatus, status);
        if (type != null) w.eq(InspectionTask::getInspectionType, type);
        w.orderByDesc(InspectionTask::getId);
        return taskMapper.selectList(w);
    }

    private InspectionStandard findStandard(Long materialId, Long supplierId, String type) {
        LambdaQueryWrapper<InspectionStandard> w = new LambdaQueryWrapper<>();
        w.eq(InspectionStandard::getMaterialId, materialId)
                .eq(InspectionStandard::getInspectionType, type);
        if (supplierId != null) {
            w.and(q -> q.eq(InspectionStandard::getSupplierId, supplierId)
                    .or().isNull(InspectionStandard::getSupplierId));
        }
        w.orderByDesc(InspectionStandard::getId).last("limit 1");
        return standardMapper.selectOne(w);
    }

    private Map<Long, InspectionItem> loadItems(Long standardId) {
        return itemMapper.selectList(new LambdaQueryWrapper<InspectionItem>()
                        .eq(InspectionItem::getStandardId, standardId)
                        .orderByAsc(InspectionItem::getSequence)).stream()
                .collect(Collectors.toMap(InspectionItem::getId, i -> i, (a, b) -> a));
    }
}
