package com.invigilation.invigilation.entity;

import lombok.Data;

@Data
public class ConflictInfo {
    /** 冲突类型：teacher-教师冲突，classroom-教室冲突 */
    private String type;
    /** 冲突描述 */
    private String detail;
    /** 冲突的安排ID */
    private String conflictArrangeId;
    /** 教师冲突时的教师ID */
    private String teacherId;
    /** 教师冲突时的教师姓名 */
    private String teacherName;

    public static ConflictInfo teacherConflict(String detail, String conflictArrangeId, String teacherId, String teacherName) {
        ConflictInfo ci = new ConflictInfo();
        ci.setType("teacher");
        ci.setDetail(detail);
        ci.setConflictArrangeId(conflictArrangeId);
        ci.setTeacherId(teacherId);
        ci.setTeacherName(teacherName);
        return ci;
    }

    public static ConflictInfo classroomConflict(String detail, String conflictArrangeId) {
        ConflictInfo ci = new ConflictInfo();
        ci.setType("classroom");
        ci.setDetail(detail);
        ci.setConflictArrangeId(conflictArrangeId);
        return ci;
    }
}
