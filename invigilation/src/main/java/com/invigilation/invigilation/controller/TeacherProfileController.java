package com.invigilation.invigilation.controller;

import com.invigilation.invigilation.entity.TeacherInfo;
import com.invigilation.invigilation.service.SysUserService;
import com.invigilation.invigilation.service.TeacherInfoService;
import com.invigilation.invigilation.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 教师端个人信息控制器（独立于 admin 的后台管理）
 * 路径前缀：/api/teacher/info
 */
@RestController
@RequestMapping("/api/teacher/info")
public class TeacherProfileController {

    @Resource
    private TeacherInfoService teacherInfoService;

    @Resource
    private SysUserService sysUserService;

    /** 查询教师个人信息 */
    @GetMapping("/get-by-id")
    public Result<TeacherInfo> getTeacherInfoById(@RequestParam String teacherId) {
        TeacherInfo teacher = teacherInfoService.getByTeacherId(teacherId);
        return teacher != null ? Result.success(teacher) : Result.error("教师信息不存在");
    }

    /** 修改个人信息（姓名、部门、电话），工号不可改 */
    @PutMapping("/update")
    public Result<Void> updateTeacherInfo(@RequestBody TeacherInfo teacherInfo) {
        TeacherInfo existing = teacherInfoService.getByTeacherId(teacherInfo.getTeacherId());
        if (existing == null) return Result.error("教师不存在");
        existing.setTeacherName(teacherInfo.getTeacherName());
        existing.setDepartment(teacherInfo.getDepartment());
        existing.setPhone(teacherInfo.getPhone());
        return teacherInfoService.editTeacher(existing) ? Result.success() : Result.error("更新失败");
    }

    /** 修改密码 */
    @PostMapping("/update-password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (username == null || oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }
        return sysUserService.updatePassword(username, oldPassword, newPassword)
                ? Result.success() : Result.error("原密码错误或用户不存在");
    }
}
