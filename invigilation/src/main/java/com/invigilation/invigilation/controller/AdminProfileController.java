package com.invigilation.invigilation.controller;

import com.invigilation.invigilation.service.SysUserService;
import com.invigilation.invigilation.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminProfileController {

    @Resource
    private SysUserService sysUserService;

    /** 管理员修改密码 */
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
