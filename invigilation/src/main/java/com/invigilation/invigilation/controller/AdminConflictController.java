//接收前端请求，返回响应
package com.invigilation.invigilation.controller;

import com.invigilation.invigilation.entity.ConflictApplication;
import com.invigilation.invigilation.service.ConflictApplicationService;
import com.invigilation.invigilation.service.InvigilationConfigService;
import com.invigilation.invigilation.util.Result;
import com.invigilation.invigilation.websocket.NotificationWebSocketHandler;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/conflict")
public class AdminConflictController {
    @Resource
    private ConflictApplicationService conflictApplicationService;

    @Resource
    private InvigilationConfigService invigilationConfigService;

    @Resource
    private NotificationWebSocketHandler notificationWebSocketHandler;

    // 查询所有冲突申请
    @GetMapping("/list")
    public Result<List<ConflictApplication>> getConflictList() {
        List<ConflictApplication> list = conflictApplicationService.getAllApplications();
        return Result.success(list);
    }

    //查询待审核申请（适配前端Dashboard的请求）
    @GetMapping("/apply/list")
    public Result<List<ConflictApplication>> getPendingApplyList(@RequestParam Integer status) {
        List<ConflictApplication> list = conflictApplicationService.getApplyListByStatus(status);
        return Result.success(list);
    }

    // 处理申请（同意/拒绝）+ 同步更新监考状态
    @PutMapping("/handle")
    public Result<String> handleConflictApply(
            @RequestParam Integer id,
            @RequestParam Integer status,
            @RequestParam(required = false) String arrangeId,
            @RequestParam(required = false) String teacherId,
            @RequestParam(required = false) String teacherName) {
        try {
            boolean success = conflictApplicationService.handleApplication(id, status);
            if (!success) {
                return Result.error("处理申请失败");
            }

            if (status == 1 && arrangeId != null && !arrangeId.isEmpty()) {
                boolean statusSuccess = invigilationConfigService.updateInvigilationStatus(arrangeId, "冲突预警");
                if (!statusSuccess) {
                    return Result.error("申请处理成功，但监考状态更新失败");
                }
            }

            // 实时通知教师
            String tid = teacherId;
            if (tid == null) {
                ConflictApplication app = conflictApplicationService.getById(id);
                if (app != null) tid = app.getTeacherId();
            }
            if (tid != null) {
                String msg = status == 1
                        ? "您的冲突申请已通过，相关安排已标记为冲突预警"
                        : "您的冲突申请已被拒绝";
                notificationWebSocketHandler.notifyTeacher(tid, "冲突申请审核结果", msg);
            }

            return Result.success("处理成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("处理异常: " + e.getMessage());
        }
    }

    // 适配前端的audit接口（前端用post，这里兼容）
    @PostMapping("/apply/audit")
    public Result<String> auditApply(@RequestBody ConflictApplication apply) {
        return handleConflictApply(apply.getId(), apply.getStatus(), apply.getArrangeId(), apply.getTeacherId(), apply.getTeacherName());
    }
}