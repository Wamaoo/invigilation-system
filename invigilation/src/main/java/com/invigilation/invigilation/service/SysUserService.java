package com.invigilation.invigilation.service;

import com.invigilation.invigilation.entity.SysUser;

public interface SysUserService {
    SysUser getByUsername(String username);
    boolean login(String username, String password, String role);

    //添加用户方法
    boolean addUser(SysUser sysUser);

    // 修改密码
    boolean updatePassword(String username, String oldPassword, String newPassword);
}