package com.smartfactory.qms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.qms.entity.SpcRecord;
import com.smartfactory.qms.mapper.SpcRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpcService {

    private final SpcRecordMapper spcMapper;

    @Transactional(rollbackFor = Exception.class)
    public void add(SpcRecord record) {
        LocalDateTime now = LocalDateTime.now();
        if (record.getRecordTime() == null) {
            record.setRecordTime(now);
        }
        record.setCreateTime(now);
        spcMapper.insert(record);
    }

    /** 控制图：数据点 + 均值/标准差/UCL/LCL */
    public Map<String, Object> chart(String characteristic) {
        List<SpcRecord> records = load(characteristic);
        double[] values = records.stream().mapToDouble(r -> r.getValue().doubleValue()).toArray();
        double mean = mean(values);
        double sigma = std(values, mean);

        Map<String, Object> map = new HashMap<>();
        map.put("records", records);
        map.put("mean", round(mean));
        map.put("sigma", round(sigma));
        map.put("ucl", round(mean + 3 * sigma));
        map.put("lcl", round(mean - 3 * sigma));
        return map;
    }

    /** 过程能力 Cp/Cpk（需给定规格上下限） */
    public Map<String, Object> cpk(String characteristic, double lsl, double usl) {
        double[] values = load(characteristic).stream()
                .mapToDouble(r -> r.getValue().doubleValue()).toArray();
        double mean = mean(values);
        double sigma = std(values, mean);

        double cp = sigma == 0 ? 0 : (usl - lsl) / (6 * sigma);
        double cpu = sigma == 0 ? 0 : (usl - mean) / (3 * sigma);
        double cpl = sigma == 0 ? 0 : (mean - lsl) / (3 * sigma);

        Map<String, Object> map = new HashMap<>();
        map.put("mean", round(mean));
        map.put("sigma", round(sigma));
        map.put("cp", round(cp));
        map.put("cpk", round(Math.min(cpu, cpl)));
        map.put("cpu", round(cpu));
        map.put("cpl", round(cpl));
        return map;
    }

    private List<SpcRecord> load(String characteristic) {
        return spcMapper.selectList(new LambdaQueryWrapper<SpcRecord>()
                .eq(SpcRecord::getCharacteristic, characteristic)
                .orderByAsc(SpcRecord::getRecordTime));
    }

    private double mean(double[] v) {
        if (v.length == 0) return 0;
        double sum = 0;
        for (double d : v) sum += d;
        return sum / v.length;
    }

    private double std(double[] v, double mean) {
        if (v.length < 2) return 0;
        double sum = 0;
        for (double d : v) sum += (d - mean) * (d - mean);
        return Math.sqrt(sum / (v.length - 1));
    }

    private double round(double d) {
        return Math.round(d * 10000.0) / 10000.0;
    }
}
