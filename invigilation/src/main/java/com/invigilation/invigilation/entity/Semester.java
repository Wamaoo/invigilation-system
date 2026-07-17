package com.invigilation.invigilation.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Semester {
    private Integer id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer isCurrent;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
