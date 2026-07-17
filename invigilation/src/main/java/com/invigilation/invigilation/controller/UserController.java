package com.invigilation.invigilation.controller;

import com.invigilation.invigilation.entity.SysUser;
import com.invigilation.invigilation.service.SysUserService;
import com.invigilation.invigilation.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user")
public class UserController {
    @Resource
    private SysUserService sysUserService;

    @PostMapping("/add")
    public Result<Void> addUser(@RequestBody SysUser sysUser) {
        //调用自定义的addUser方法
        boolean success = sysUserService.addUser(sysUser);
        return success ? Result.success() : Result.error("新增用户失败");
    }
}