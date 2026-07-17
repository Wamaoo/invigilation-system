// 定义数据结构（对应数据库表，相当于 “数据模板”）
package com.invigilation.invigilation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data // 需引入lombok依赖，简化get/set
public class ConflictApplication {
    /** 申请ID */
    private Integer id;

    /** 教师工号（如T008） */
    private String teacherId;

    /** 教师姓名 */
    private String teacherName;

    /** 监考安排ID */
    private String arrangeId;

    /** 申请原因 */
    private String reason;

    /** 材料文件路径 */
    private String fileUrl;

    /** 状态：0-待审核 1-已同意 2-已拒绝 */
    private Integer status;

    /** 申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}