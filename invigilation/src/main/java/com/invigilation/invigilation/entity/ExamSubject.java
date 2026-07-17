package com.invigilation.invigilation.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamSubject {
    private String subjectId;
    private String examName;
    private String grade;
    private String major;
    private String examType;
    private String semesterId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}