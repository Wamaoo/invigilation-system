package com.invigilation.invigilation.util;

import java.util.List;
import java.util.stream.Collectors;

public class IdGenerator {

    /**
     * 生成下一个可用ID
     * 例如：前缀 "EXAM"，已有 ["EXAM001","EXAM003"] → 返回 "EXAM002"
     * @param prefix ID前缀（如 "EXAM"、"ARRANGE"）
     * @param existingIds 数据库中已有的所有ID列表
     * @return 下一个可用的ID字符串
     */
    public static String generateNextId(String prefix, List<String> existingIds) {
        // 提取所有已用的数字序号
        List<Integer> usedNumbers = existingIds.stream()
                .filter(id -> id != null && id.startsWith(prefix))
                .map(id -> {
                    try {
                        return Integer.parseInt(id.substring(prefix.length()));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .filter(n -> n > 0)
                .sorted()
                .collect(Collectors.toList());

        // 从1开始找最小的未使用序号
        int nextNum = 1;
        for (int num : usedNumbers) {
            if (num == nextNum) {
                nextNum++;
            } else if (num > nextNum) {
                break; // 找到了空缺
            }
        }

        return prefix + String.format("%03d", nextNum);
    }
}
