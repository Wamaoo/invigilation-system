package com.invigilation.invigilation.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InvigilationConfig {
    private String arrangeId;          // 安排ID
    private String subjectId;          // 关联 exam_subject.subject_id
    private String examName;           // 考试科目
    private String classroom;          // 考场
    private String examDate;           // 考试日期 "2026-01-10"
    private String startTime;          // 开始时间 "09:00"
    private String endTime;            // 结束时间 "11:00"
    private Integer requiredTeachers;  // 所需监考人数
    private String examDuration;       // 考试时长
    private String semesterId;         // 学期ID
    /** 请求用字段（不映射数据库）：逗号分隔教师ID */
    private String teacherIds;
    /** 请求用字段（不映射数据库）：逗号分隔教师姓名 */
    private String teacherNames;
    private String status;             // 状态（已安排/冲突预警）
    private String remark;             // 备注
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 兼容前端展示：组合 examDate + startTime + endTime */
    public String getExamTime() {
        if (examDate != null && startTime != null && endTime != null) {
            return examDate + " " + startTime + "-" + endTime;
        }
        return null;
    }
}