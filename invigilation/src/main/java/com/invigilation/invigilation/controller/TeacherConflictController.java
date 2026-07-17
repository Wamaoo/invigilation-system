package com.invigilation.invigilation.controller;

import com.invigilation.invigilation.entity.ConflictApplication;
import com.invigilation.invigilation.service.ConflictApplicationService;
import com.invigilation.invigilation.util.Result;
import com.invigilation.invigilation.websocket.NotificationWebSocketHandler;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/conflict")
public class TeacherConflictController {
    @Resource
    private ConflictApplicationService conflictApplicationService;

    @Resource
    private NotificationWebSocketHandler notificationWebSocketHandler;

    @PostMapping("/apply")
    public Result<String> submitConflictApply(@RequestBody ConflictApplication application) {
        try {
            boolean success = conflictApplicationService.submitApplication(application);
            if (success) {
                // 实时通知管理员
                notificationWebSocketHandler.notifyAdmin(
                        "新的冲突申请",
                        "教师 " + application.getTeacherName() + "（" + application.getTeacherId() + "）提交了冲突申请，安排ID: " + application.getArrangeId()
                );
            }
            return success ? Result.success("申请提交成功") : Result.error("申请提交失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("申请提交异常：" + e.getMessage());
        }
    }
}