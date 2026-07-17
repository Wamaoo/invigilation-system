package com.invigilation.invigilation.controller;

import com.invigilation.invigilation.entity.SysUser;
import com.invigilation.invigilation.service.SysUserService;
import com.invigilation.invigilation.util.JwtUtil;
import com.invigilation.invigilation.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {
    @Resource
    private SysUserService sysUserService;

    @Resource
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> loginParam) {
        String username = loginParam.get("username");
        String password = loginParam.get("password");
        String role = loginParam.get("role");

        // 1. 查询用户
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            return Result.error("用户名不存在");
        }

        // 2. 校验角色
        if (!user.getRole().equals(role)) {
            return Result.error("用户身份不匹配");
        }

        // 3. 密码校验（BCrypt 兼容旧数据）
        if (!sysUserService.login(username, password, role)) {
            return Result.error("密码错误");
        }

        // 4. 生成 JWT Token 并返回
        String token = jwtUtil.generateToken(username, role);
        return Result.success(Map.of(
                "username", user.getUsername(),
                "role", user.getRole(),
                "token", token
        ));
    }
}
