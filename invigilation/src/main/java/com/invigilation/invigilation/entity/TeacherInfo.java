//教师信息表
package com.invigilation.invigilation.entity;

import lombok.Data;
import java.time.LocalDateTime;
// Lombok注解：自动生成get/set/toString等方法
@Data
public class TeacherInfo {
    private String teacherId;
    private String teacherName;
    private String department;
    private String phone;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}