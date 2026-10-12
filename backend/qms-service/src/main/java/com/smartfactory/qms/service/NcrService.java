package com.smartfactory.qms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.common.exception.BusinessException;
import com.smartfactory.common.exception.ErrorCode;
import com.smartfactory.common.util.SeqNo;
import com.smartfactory.qms.dto.DisposeDto;
import com.smartfactory.qms.entity.Capa;
import com.smartfactory.qms.entity.InspectionTask;
import com.smartfactory.qms.entity.Ncr;
import com.smartfactory.qms.feign.WmsClient;
import com.smartfactory.qms.mapper.CapaMapper;
import com.smartfactory.qms.mapper.NcrMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NcrService {

    private final NcrMapper ncrMapper;
    private final CapaMapper capaMapper;
    private final WmsClient wmsClient;

    @Transactional(rollbackFor = Exception.class)
    public Ncr createFromInspection(InspectionTask task, String defectDesc) {
        Ncr ncr = new Ncr();
        ncr.setNcrNo(SeqNo.next("NCR"));
        ncr.setSourceType(task.getInspectionType());
        ncr.setSourceNo(task.getTaskNo());
        ncr.setMaterialId(task.getMaterialId());
        ncr.setMaterialCode(task.getMaterialCode());
        ncr.setMaterialName(task.getMaterialName());
        ncr.setBatchNo(task.getBatchNo());
        ncr.setDefectDesc(defectDesc);
        ncr.setLevel("MAJOR");
        ncr.setStatus("OPEN");
        ncr.setCreatorName(task.getInspectorName());
        ncrMapper.insert(ncr);
        return ncr;
    }

    /** MRB 评审：确定处置方式，联动 WMS，并自动建立 CAPA */
    @Transactional(rollbackFor = Exception.class)
    public void mrb(Long ncrId, String disposition, String comment) {
        Ncr ncr = ncrMapper.selectById(ncrId);
        if (ncr == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "NCR 不存在");
        }
        if (!"OPEN".equals(ncr.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "NCR 已评审，禁止重复操作");
        }
        ncr.setDisposition(disposition);
        ncr.setMrbComment(comment);
        ncr.setStatus("REVIEWED");
        ncrMapper.updateById(ncr);

        DisposeDto dispose = new DisposeDto();
        dispose.setMaterialId(ncr.getMaterialId());
        dispose.setBatchNo(ncr.getBatchNo());
        dispose.setDisposition(disposition);
        try {
            wmsClient.dispose(dispose);
        } catch (Exception e) {
            log.warn("WMS 处置回调失败 ncr={} : {}", ncr.getNcrNo(), e.getMessage());
        }

        Capa capa = new Capa();
        capa.setCapaNo(SeqNo.next("CAPA"));
        capa.setNcrId(ncr.getId());
        capa.setStatus("OPEN");
        capa.setCreateTime(LocalDateTime.now());
        capaMapper.insert(capa);
    }

    @Transactional(rollbackFor = Exception.class)
    public void closeNcr(Long ncrId) {
        Ncr ncr = ncrMapper.selectById(ncrId);
        if (ncr != null && !"CLOSED".equals(ncr.getStatus())) {
            ncr.setStatus("CLOSED");
            ncr.setCloseTime(LocalDateTime.now());
            ncrMapper.updateById(ncr);
        }
    }

    public Ncr detail(Long id) {
        return ncrMapper.selectById(id);
    }

    public List<Ncr> list(String status) {
        LambdaQueryWrapper<Ncr> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(Ncr::getStatus, status);
        w.orderByDesc(Ncr::getId);
        return ncrMapper.selectList(w);
    }
}
