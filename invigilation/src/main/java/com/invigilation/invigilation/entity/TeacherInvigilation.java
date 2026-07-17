package com.invigilation.invigilation.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeacherInvigilation {
    private Long id;
    private String teacherId;
    private String arrangeId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}