package com.invigilation.invigilation.service;

import com.invigilation.invigilation.entity.ConflictInfo;
import com.invigilation.invigilation.entity.InvigilationConfig;
import java.util.List;
import java.util.Map;

public interface InvigilationConfigService {
    List<InvigilationConfig> getConfigList(String examName, String classroom, String semesterId);

    List<InvigilationConfig> getConfigList(String examName, String classroom, String startDate, String endDate, String semesterId);
    boolean addConfig(InvigilationConfig config);
    boolean editConfig(InvigilationConfig config);
    boolean deleteConfig(String arrangeId);
    List<InvigilationConfig> getConfigListByIds(List<String> arrangeIds);

    List<InvigilationConfig> getConfigListByPage(String examName, String classroom,
                                                  String startDate, String endDate,
                                                  String semesterId,
                                                  int offset, int size);

    long getConfigTotal(String examName, String classroom, String startDate, String endDate, String semesterId);

    boolean updateInvigilationStatus(String arrangeId, String status);

    List<ConflictInfo> checkConflicts(String examDate, String startTime, String endTime,
                                       String classroom, List<String> teacherIds,
                                       String excludeArrangeId);

    InvigilationConfig enrichStatus(InvigilationConfig config);
    List<InvigilationConfig> enrichStatusList(List<InvigilationConfig> list);

    /** 从 teacher_invigilation 表填充教师ID和姓名 */
    List<InvigilationConfig> enrichTeachers(List<InvigilationConfig> list);

    /** 自动排课：根据考试信息推荐可用教师（排除任课教师） */
    List<String> autoSuggestTeachers(String examDate, String startTime, String endTime,
                                      int requiredCount, String excludeArrangeId,
                                      String subjectId);
}
