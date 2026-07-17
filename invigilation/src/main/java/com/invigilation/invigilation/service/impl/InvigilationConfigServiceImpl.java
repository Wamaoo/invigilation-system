package com.invigilation.invigilation.service.impl;

import com.invigilation.invigilation.entity.ConflictInfo;
import com.invigilation.invigilation.entity.InvigilationConfig;
import com.invigilation.invigilation.entity.TeacherInfo;
import com.invigilation.invigilation.entity.TeacherInvigilation;
import com.invigilation.invigilation.mapper.InvigilationConfigMapper;
import com.invigilation.invigilation.mapper.TeacherInfoMapper;
import com.invigilation.invigilation.mapper.TeacherInvigilationMapper;
import com.invigilation.invigilation.mapper.TeacherSubjectMapper;
import com.invigilation.invigilation.service.InvigilationConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class InvigilationConfigServiceImpl implements InvigilationConfigService {
    @Resource
    private InvigilationConfigMapper invigilationConfigMapper;

    @Resource
    private TeacherInvigilationMapper teacherInvigilationMapper;

    @Resource
    private TeacherInfoMapper teacherInfoMapper;

    @Resource
    private TeacherSubjectMapper teacherSubjectMapper;

    @Override
    public List<InvigilationConfig> getConfigList(String examName, String classroom, String semesterId) {
        return enrichTeachers(invigilationConfigMapper.selectByCondition(examName, classroom, null, null, semesterId));
    }

    @Override
    public List<InvigilationConfig> getConfigList(String examName, String classroom, String startDate, String endDate, String semesterId) {
        return enrichTeachers(invigilationConfigMapper.selectByCondition(examName, classroom, startDate, endDate, semesterId));
    }

    @Override
    public boolean addConfig(InvigilationConfig config) {
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        return invigilationConfigMapper.insert(config) > 0;
    }

    @Override
    public boolean editConfig(InvigilationConfig config) {
        config.setUpdateTime(LocalDateTime.now());
        return invigilationConfigMapper.updateById(config) > 0;
    }

    @Override
    public boolean deleteConfig(String arrangeId) {
        teacherInvigilationMapper.deleteByArrangeId(arrangeId);
        return invigilationConfigMapper.deleteById(arrangeId) > 0;
    }

    @Override
    public List<InvigilationConfig> getConfigListByIds(List<String> arrangeIds) {
        return enrichTeachers(invigilationConfigMapper.selectByIds(arrangeIds));
    }

    @Override
    public List<InvigilationConfig> getConfigListByPage(String examName, String classroom,
                                                         String startDate, String endDate,
                                                         String semesterId,
                                                         int offset, int size) {
        return enrichTeachers(invigilationConfigMapper.selectByConditionWithPage(
                examName, classroom, startDate, endDate, semesterId, offset, size));
    }

    @Override
    public long getConfigTotal(String examName, String classroom, String startDate, String endDate, String semesterId) {
        return invigilationConfigMapper.countByCondition(examName, classroom, startDate, endDate, semesterId);
    }

    @Override
    public boolean updateInvigilationStatus(String arrangeId, String status) {
        InvigilationConfig config = new InvigilationConfig();
        config.setArrangeId(arrangeId);
        config.setStatus(status);
        config.setUpdateTime(LocalDateTime.now());
        return invigilationConfigMapper.updateById(config) > 0;
    }

    // ====== 从 teacher_invigilation 表查询并填充教师信息 ======
    @Override
    public List<InvigilationConfig> enrichTeachers(List<InvigilationConfig> list) {
        if (list == null) return List.of();
        for (InvigilationConfig config : list) {
            List<TeacherInvigilation> tis = teacherInvigilationMapper.selectByArrangeId(config.getArrangeId());
            List<String> ids = new ArrayList<>();
            List<String> names = new ArrayList<>();
            for (TeacherInvigilation ti : tis) {
                ids.add(ti.getTeacherId());
                TeacherInfo info = teacherInfoMapper.selectByTeacherId(ti.getTeacherId());
                names.add(info != null ? info.getTeacherName() : ti.getTeacherId());
            }
            config.setTeacherIds(String.join(",", ids));
            config.setTeacherNames(String.join(",", names));
        }
        return list;
    }

    // ====== 实时考试状态计算 ======
    @Override
    public InvigilationConfig enrichStatus(InvigilationConfig config) {
        if (config == null) return null;
        if (config.getExamDate() == null || config.getEndTime() == null) return config;

        try {
            LocalDate examDate = LocalDate.parse(config.getExamDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            // MySQL TIME 可能返回 "HH:mm" 或 "HH:mm:ss"，兼容两者
            String endTimeStr = config.getEndTime();
            LocalTime endTime;
            try {
                endTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
            } catch (DateTimeParseException e) {
                endTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
            }
            LocalDateTime endDateTime = LocalDateTime.of(examDate, endTime);

            if (LocalDateTime.now().isAfter(endDateTime)) {
                config.setStatus("已结束");
            }
        } catch (Exception ignored) {
        }
        return config;
    }

    @Override
    public List<InvigilationConfig> enrichStatusList(List<InvigilationConfig> list) {
        if (list == null) return List.of();
        for (InvigilationConfig config : list) {
            enrichStatus(config);
        }
        return list;
    }

    // ====== 智能冲突检测 ======
    @Override
    public List<ConflictInfo> checkConflicts(String examDate, String startTime, String endTime,
                                              String classroom, List<String> teacherIds,
                                              String excludeArrangeId) {
        List<ConflictInfo> conflicts = new ArrayList<>();
        if (examDate == null || startTime == null || endTime == null) return conflicts;

        List<InvigilationConfig> allConfigs = invigilationConfigMapper.selectByCondition(null, null, null, null, null);

        for (InvigilationConfig existing : allConfigs) {
            if (excludeArrangeId != null && excludeArrangeId.equals(existing.getArrangeId())) {
                continue;
            }
            if (existing.getExamDate() == null || existing.getStartTime() == null || existing.getEndTime() == null) {
                continue;
            }
            if (!examDate.equals(existing.getExamDate())) continue;

            if (!isTimeOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime())) continue;

            if (classroom != null && !classroom.isBlank()
                    && classroom.equals(existing.getClassroom())) {
                String detail = String.format("教室 %s 在 %s %s-%s 已被占用（%s）",
                        classroom, examDate, startTime, endTime, existing.getArrangeId());
                conflicts.add(ConflictInfo.classroomConflict(detail, existing.getArrangeId()));
            }

            if (teacherIds != null && !teacherIds.isEmpty()) {
                List<TeacherInvigilation> existTeachers = teacherInvigilationMapper.selectByArrangeId(existing.getArrangeId());
                Set<String> existTeacherIds = existTeachers.stream()
                        .map(TeacherInvigilation::getTeacherId)
                        .collect(Collectors.toSet());

                for (String nt : teacherIds) {
                    if (existTeacherIds.contains(nt)) {
                        TeacherInfo ti = teacherInfoMapper.selectByTeacherId(nt);
                        String tName = ti != null ? ti.getTeacherName() : nt;
                        String detail = String.format("教师 %s（%s）在 %s %s-%s 已有监考安排（%s）",
                                tName, nt, examDate, startTime, endTime, existing.getArrangeId());
                        conflicts.add(ConflictInfo.teacherConflict(detail, existing.getArrangeId(), nt, tName));
                    }
                }
            }
        }
        return conflicts;
    }

    private boolean isTimeOverlap(String aStart, String aEnd, String bStart, String bEnd) {
        return aStart.compareTo(bEnd) < 0 && aEnd.compareTo(bStart) > 0;
    }

    // ====== 自动排课推荐（排除任课教师） ======
    @Override
    public List<String> autoSuggestTeachers(String examDate, String startTime, String endTime,
                                             int requiredCount, String excludeArrangeId,
                                             String subjectId) {
        List<TeacherInfo> allTeachers = teacherInfoMapper.selectByCondition(null, null);
        if (allTeachers.isEmpty()) return List.of();

        // 查询该科目的任课教师，排考时排除
        Set<String> subjectTeachers = new HashSet<>();
        if (subjectId != null && !subjectId.isBlank()) {
            subjectTeachers.addAll(teacherSubjectMapper.selectTeacherIdsBySubjectId(subjectId));
        }

        List<Map<String, Object>> workloadList = invigilationConfigMapper.countTeacherWorkload();
        Map<String, Long> workloadMap = new HashMap<>();
        for (Map<String, Object> row : workloadList) {
            String tid = (String) row.get("teacher_id");
            Long cnt = row.get("cnt") != null ? ((Number) row.get("cnt")).longValue() : 0L;
            workloadMap.put(tid, cnt);
        }

        List<TeacherInfo> available = allTeachers.stream()
                .filter(t -> !subjectTeachers.contains(t.getTeacherId()))
                .filter(t -> {
                    long conflictCount = invigilationConfigMapper.countTeacherConflictsInTimeRange(
                            t.getTeacherId(), examDate, startTime, endTime, excludeArrangeId);
                    return conflictCount == 0;
                })
                .sorted(Comparator.comparingLong(t -> workloadMap.getOrDefault(t.getTeacherId(), 0L)))
                .collect(Collectors.toList());

        return available.stream()
                .limit(requiredCount)
                .map(TeacherInfo::getTeacherId)
                .collect(Collectors.toList());
    }
}
