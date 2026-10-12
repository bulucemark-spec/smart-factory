package com.smartfactory.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 业务单号生成：前缀 + 时间戳 + 随机数
 */
public final class SeqNo {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private SeqNo() {
    }

    public static String next(String prefix) {
        return prefix + FMT.format(LocalDateTime.now())
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
