//接收前端请求
package com.invigilation.invigilation.controller;

import com.invigilation.invigilation.entity.SysUser;
import com.invigilation.invigilation.entity.TeacherInfo;
import com.invigilation.invigilation.service.SysUserService;
import com.invigilation.invigilation.service.TeacherInfoService;
import com.invigilation.invigilation.service.TeacherInvigilationService;
import com.invigilation.invigilation.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//标记这是REST接口控制器
@RestController
// 定义接口前缀：所有该类的接口都以/api/admin/teacher开头
@RequestMapping("/api/admin/teacher")
public class TeacherController {
    // 注入Service（Spring自动提供实例）
    @Resource
    private TeacherInfoService teacherInfoService;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private TeacherInvigilationService teacherInvigilationService;

    @GetMapping("/list")
    public Result<Map<String, Object>> getTeacherList(
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        int offset = (page - 1) * size;
        List<TeacherInfo> list = teacherInfoService.getTeacherListByPage(teacherName, department, offset, size);
        long total = teacherInfoService.getTeacherTotal(teacherName, department);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<Void> addTeacher(@RequestBody TeacherInfo teacherInfo) {
        // 调用Service新增，返回成功/失败结果
        return teacherInfoService.addTeacher(teacherInfo) ? Result.success() : Result.error("新增失败");
    }

    @PutMapping("/edit")
    public Result<Void> editTeacher(@RequestBody TeacherInfo teacherInfo) {
        return teacherInfoService.editTeacher(teacherInfo) ? Result.success() : Result.error("编辑失败");
    }

    @DeleteMapping("/delete/{teacherId}")
    public Result<Void> deleteTeacher(@PathVariable String teacherId) {
        // 检查该教师是否有未结束的监考安排
        List<com.invigilation.invigilation.entity.TeacherInvigilation> list = teacherInvigilationService.getByTeacherId(teacherId);
        if (!list.isEmpty()) {
            return Result.error("该教师仍有监考安排，请先删除相关安排再删除教师");
        }
        return teacherInfoService.deleteTeacher(teacherId) ? Result.success() : Result.error("删除失败");
    }

    /** Excel 导入教师数据（已存在则更新，不存在则新增，同时创建登录账号） */
    @PostMapping("/import")
    public Result<String> importTeachers(@RequestBody List<TeacherInfo> teacherList) {
        int insert = 0, update = 0, fail = 0;
        StringBuilder failMsg = new StringBuilder();
        for (int i = 0; i < teacherList.size(); i++) {
            TeacherInfo t = teacherList.get(i);
            // 跳过空 ID
            if (t.getTeacherId() == null || t.getTeacherId().trim().isEmpty()) {
                fail++;
                failMsg.append("第").append(i + 1).append("条:教师工号为空;");
                continue;
            }
            try {
                TeacherInfo existing = teacherInfoService.getByTeacherId(t.getTeacherId());
                if (existing != null) {
                    // 已存在 → 更新
                    existing.setTeacherName(t.getTeacherName());
                    existing.setDepartment(t.getDepartment());
                    existing.setPhone(t.getPhone());
                    teacherInfoService.editTeacher(existing);
                    update++;
                } else {
                    // 不存在 → 新增 teacher_info
                    teacherInfoService.addTeacher(t);
                    // 同时创建 sys_user 登录账号（密码统一 123456）
                    SysUser user = new SysUser();
                    user.setUsername(t.getTeacherId());
                    user.setPassword("123456");
                    user.setRole("teacher");
                    try { sysUserService.addUser(user); } catch (Exception ignored) {}
                    insert++;
                }
            } catch (Exception e) {
                fail++;
                failMsg.append("第").append(i + 1).append("条(").append(t.getTeacherId()).append("):").append(e.getMessage()).append(";");
            }
        }
        String msg = "导入完成：新增 " + insert + " 条，更新 " + update + " 条，失败 " + fail + " 条";
        if (failMsg.length() > 0) msg += "。" + failMsg;
        return Result.success(msg);
    }

    // 根据工号查教师
    @GetMapping("/get-by-id")
    public Result<TeacherInfo> getTeacherById(@RequestParam String teacherId) {
        TeacherInfo teacher = teacherInfoService.getByTeacherId(teacherId);
        return teacher != null ? Result.success(teacher) : Result.error("教师信息不存在");
    }

}
