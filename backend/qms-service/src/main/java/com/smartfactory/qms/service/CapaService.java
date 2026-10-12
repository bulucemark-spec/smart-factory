package com.smartfactory.qms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.common.exception.BusinessException;
import com.smartfactory.common.exception.ErrorCode;
import com.smartfactory.qms.entity.Capa;
import com.smartfactory.qms.mapper.CapaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CapaService {

    private final CapaMapper capaMapper;
    private final NcrService ncrService;

    /** 责任人执行纠正/预防措施 */
    @Transactional(rollbackFor = Exception.class)
    public void execute(Capa input) {
        Capa capa = capaMapper.selectById(input.getId());
        if (capa == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "CAPA 不存在");
        }
        if ("CLOSED".equals(capa.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "CAPA 已关闭");
        }
        capa.setRootCause(input.getRootCause());
        capa.setCorrectiveAction(input.getCorrectiveAction());
        capa.setPreventiveAction(input.getPreventiveAction());
        capa.setOwner(input.getOwner());
        capa.setOwnerName(input.getOwnerName());
        capa.setDueDate(input.getDueDate());
        capa.setStatus("IN_PROGRESS");
        capaMapper.updateById(capa);
    }

    /** 质量部验证；通过则关闭 CAPA 与关联 NCR，不通过退回 */
    @Transactional(rollbackFor = Exception.class)
    public void verify(Long capaId, String verifyResult, Boolean passed, String verifierName) {
        Capa capa = capaMapper.selectById(capaId);
        if (capa == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "CAPA 不存在");
        }
        if (!"IN_PROGRESS".equals(capa.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "CAPA 尚未执行或已关闭");
        }
        capa.setVerifyResult(verifyResult);
        capa.setVerifierName(verifierName);
        if (passed != null && passed) {
            capa.setStatus("CLOSED");
            capa.setCloseTime(LocalDateTime.now());
            capaMapper.updateById(capa);
            ncrService.closeNcr(capa.getNcrId());
        } else {
            capa.setStatus("IN_PROGRESS");
            capaMapper.updateById(capa);
        }
    }

    public Capa detail(Long id) {
        return capaMapper.selectById(id);
    }

    public List<Capa> list(String status) {
        LambdaQueryWrapper<Capa> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(Capa::getStatus, status);
        w.orderByDesc(Capa::getId);
        return capaMapper.selectList(w);
    }
}
