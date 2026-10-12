package com.smartfactory.mes.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.common.context.UserContext;
import com.smartfactory.common.exception.BusinessException;
import com.smartfactory.common.exception.ErrorCode;
import com.smartfactory.common.result.Result;
import com.smartfactory.common.util.SeqNo;
import com.smartfactory.mes.dto.FinishedInboundDto;
import com.smartfactory.mes.dto.InspectionTaskCreateDto;
import com.smartfactory.mes.dto.PickTask;
import com.smartfactory.mes.dto.PickWaveDto;
import com.smartfactory.mes.dto.ReportDto;
import com.smartfactory.mes.entity.Genealogy;
import com.smartfactory.mes.entity.MaterialTxn;
import com.smartfactory.mes.entity.WorkOrder;
import com.smartfactory.mes.entity.WorkReport;
import com.smartfactory.mes.feign.QmsClient;
import com.smartfactory.mes.feign.WmsClient;
import com.smartfactory.mes.mapper.GenealogyMapper;
import com.smartfactory.mes.mapper.MaterialTxnMapper;
import com.smartfactory.mes.mapper.WorkOrderMapper;
import com.smartfactory.mes.mapper.WorkReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final WorkReportMapper workReportMapper;
    private final MaterialTxnMapper materialTxnMapper;
    private final GenealogyMapper genealogyMapper;
    private final WmsClient wmsClient;
    private final QmsClient qmsClient;

    @Transactional(rollbackFor = Exception.class)
    public WorkOrder create(WorkOrder wo) {
        wo.setOrderNo(SeqNo.next("WO"));
        wo.setStatus("CREATED");
        wo.setQtyCompleted(BigDecimal.ZERO);
        wo.setQtyDefect(BigDecimal.ZERO);
        wo.setQtyScrap(BigDecimal.ZERO);
        workOrderMapper.insert(wo);
        return wo;
    }

    /** 下达：状态 RELEASED，并向 WMS 发起领料波次（items 由前端按 BOM×计划量展开） */
    @Transactional(rollbackFor = Exception.class)
    public void release(Long id, PickWaveDto wave) {
        WorkOrder wo = must(id);
        assertStatus(wo, "CREATED");
        wo.setStatus("RELEASED");
        workOrderMapper.updateById(wo);

        if (wave != null && wave.getItems() != null && !wave.getItems().isEmpty()) {
            wave.setSourceId(wo.getId());
            wave.setSourceNo(wo.getOrderNo());
            try {
                wmsClient.createPickWave(wave);
            } catch (Exception e) {
                log.warn("WMS 领料波次创建失败 wo={} : {}", wo.getOrderNo(), e.getMessage());
            }
        }
    }

    /** 线边交接：拉取 WMS 拣货结果，回写投料记录与谱系（子件批次） */
    @Transactional(rollbackFor = Exception.class)
    public void receiveMaterials(Long id) {
        WorkOrder wo = must(id);
        assertStatus(wo, "RELEASED", "PICKED");

        List<PickTask> tasks = List.of();
        try {
            Result<List<PickTask>> r = wmsClient.pickResult(wo.getOrderNo());
            if (r != null && r.getData() != null) {
                tasks = r.getData();
            }
        } catch (Exception e) {
            log.warn("WMS 拣货结果查询失败 wo={} : {}", wo.getOrderNo(), e.getMessage());
        }
        if (tasks.isEmpty()) {
            throw new BusinessException(ErrorCode.CONFLICT, "WMS 尚未完成拣货，请先在 WMS 执行拣货");
        }

        LocalDateTime now = LocalDateTime.now();
        for (PickTask t : tasks) {
            MaterialTxn txn = new MaterialTxn();
            txn.setWorkOrderId(wo.getId());
            txn.setOrderNo(wo.getOrderNo());
            txn.setMaterialId(t.getMaterialId());
            txn.setMaterialCode(t.getMaterialCode());
            txn.setMaterialName(t.getMaterialName());
            txn.setBatchNo(t.getBatchNo());
            txn.setQty(t.getQty());
            txn.setType("ISSUE");
            txn.setCreateTime(now);
            materialTxnMapper.insert(txn);

            Genealogy g = new Genealogy();
            g.setWorkOrderId(wo.getId());
            g.setChildBatch(t.getBatchNo());
            g.setMaterialId(t.getMaterialId());
            g.setMaterialCode(t.getMaterialCode());
            g.setQty(t.getQty());
            genealogyMapper.insert(g);
        }
        wo.setStatus("PICKED");
        workOrderMapper.updateById(wo);
    }

    @Transactional(rollbackFor = Exception.class)
    public void start(Long id) {
        WorkOrder wo = must(id);
        assertStatus(wo, "PICKED", "RELEASED");
        wo.setActualStart(LocalDateTime.now());
        wo.setStatus("STARTED");
        workOrderMapper.updateById(wo);
    }

    /** 报工：写报工记录，累计合格/不良/报废数量 */
    @Transactional(rollbackFor = Exception.class)
    public void report(ReportDto dto, UserContext user) {
        WorkOrder wo = must(dto.getWorkOrderId());
        assertStatus(wo, "STARTED", "RUNNING");

        WorkReport r = new WorkReport();
        r.setWorkOrderId(wo.getId());
        r.setOrderNo(wo.getOrderNo());
        r.setOperationSeq(dto.getOperationSeq());
        r.setOperationName(dto.getOperationName());
        r.setWorkstationId(dto.getWorkstationId());
        r.setUserId(user != null ? user.getUserId() : null);
        r.setUserName(user != null ? user.getUsername() : null);
        r.setGoodQty(nz(dto.getGoodQty()));
        r.setDefectQty(nz(dto.getDefectQty()));
        r.setScrapQty(nz(dto.getScrapQty()));
        r.setDefectCode(dto.getDefectCode());
        r.setReportTime(LocalDateTime.now());
        workReportMapper.insert(r);

        wo.setQtyCompleted(nz(wo.getQtyCompleted()).add(r.getGoodQty()));
        wo.setQtyDefect(nz(wo.getQtyDefect()).add(r.getDefectQty()));
        wo.setQtyScrap(nz(wo.getQtyScrap()).add(r.getScrapQty()));
        wo.setStatus("RUNNING");
        workOrderMapper.updateById(wo);
    }

    /** 完工：生成成品批次、回填谱系、WMS 成品入库、QMS 建立 OQC */
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        WorkOrder wo = must(id);
        assertStatus(wo, "STARTED", "RUNNING");
        if (wo.getQtyCompleted() == null || wo.getQtyCompleted().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "尚无合格报工数量，无法完工");
        }

        String batch = SeqNo.next("FG");
        wo.setFinishedBatch(batch);

        List<Genealogy> children = genealogyMapper.selectList(new LambdaQueryWrapper<Genealogy>()
                .eq(Genealogy::getWorkOrderId, wo.getId())
                .isNull(Genealogy::getParentBatch));
        for (Genealogy g : children) {
            g.setParentBatch(batch);
            genealogyMapper.updateById(g);
        }

        FinishedInboundDto inbound = new FinishedInboundDto();
        inbound.setOrderNo(wo.getOrderNo());
        inbound.setMaterialId(wo.getProductMaterialId());
        inbound.setMaterialCode(wo.getProductCode());
        inbound.setMaterialName(wo.getProductName());
        inbound.setBatchNo(batch);
        inbound.setQty(wo.getQtyCompleted());
        try {
            wmsClient.finishedInbound(inbound);
        } catch (Exception e) {
            log.warn("WMS 成品入库失败 wo={} : {}", wo.getOrderNo(), e.getMessage());
        }

        InspectionTaskCreateDto oqc = new InspectionTaskCreateDto();
        oqc.setInspectionType("OQC");
        oqc.setSourceType("WORK_ORDER");
        oqc.setSourceNo(wo.getOrderNo());
        oqc.setMaterialId(wo.getProductMaterialId());
        oqc.setMaterialCode(wo.getProductCode());
        oqc.setMaterialName(wo.getProductName());
        oqc.setBatchNo(batch);
        oqc.setQty(wo.getQtyCompleted());
        try {
            qmsClient.createInspectionTask(oqc);
        } catch (Exception e) {
            log.warn("QMS OQC 任务创建失败 wo={} : {}", wo.getOrderNo(), e.getMessage());
        }

        wo.setStatus("STOCKED");
        wo.setActualEnd(LocalDateTime.now());
        workOrderMapper.updateById(wo);
    }

    @Transactional(rollbackFor = Exception.class)
    public void close(Long id) {
        WorkOrder wo = must(id);
        assertStatus(wo, "STOCKED");
        wo.setStatus("CLOSED");
        workOrderMapper.updateById(wo);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        WorkOrder wo = must(id);
        assertStatus(wo, "CREATED", "RELEASED", "PICKED");
        wo.setStatus("CANCELLED");
        workOrderMapper.updateById(wo);
    }

    public WorkOrder detail(Long id) {
        return must(id);
    }

    public List<WorkOrder> list(String status) {
        LambdaQueryWrapper<WorkOrder> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(WorkOrder::getStatus, status);
        w.orderByDesc(WorkOrder::getId);
        return workOrderMapper.selectList(w);
    }

    public List<WorkReport> reports(Long workOrderId) {
        return workReportMapper.selectList(new LambdaQueryWrapper<WorkReport>()
                .eq(WorkReport::getWorkOrderId, workOrderId)
                .orderByDesc(WorkReport::getReportTime));
    }

    private WorkOrder must(Long id) {
        WorkOrder wo = workOrderMapper.selectById(id);
        if (wo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工单不存在");
        }
        return wo;
    }

    private void assertStatus(WorkOrder wo, String... allowed) {
        for (String a : allowed) {
            if (a.equals(wo.getStatus())) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.CONFLICT,
                "当前状态[" + wo.getStatus() + "]不允许该操作，允许状态：" + Arrays.toString(allowed));
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
