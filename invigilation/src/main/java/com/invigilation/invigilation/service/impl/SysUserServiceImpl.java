package com.invigilation.invigilation.service.impl;

import com.invigilation.invigilation.entity.SysUser;
import com.invigilation.invigilation.mapper.SysUserMapper;
import com.invigilation.invigilation.service.SysUserService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl implements SysUserService {
    @Resource
    private SysUserMapper sysUserMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public SysUser getByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    @Override
    public boolean login(String username, String password, String role) {
        SysUser user = getByUsername(username);
        if (user == null) return false;
        // 兼容旧数据：如果密码是明文（未以 $2a$ 开头），走明文比较并自动升级
        if (!user.getPassword().startsWith("$2a$")) {
            if (password.equals(user.getPassword())) {
                // 自动升级为 BCrypt 哈希
                user.setPassword(encoder.encode(password));
                sysUserMapper.updatePassword(username, user.getPassword());
                return role.equals(user.getRole());
            }
            return false;
        }
        return encoder.matches(password, user.getPassword()) && role.equals(user.getRole());
    }

    @Override
    public boolean addUser(SysUser sysUser) {
        sysUser.setPassword(encoder.encode(sysUser.getPassword()));
        return sysUserMapper.insert(sysUser) > 0;
    }

    @Override
    public boolean updatePassword(String username, String oldPassword, String newPassword) {
        SysUser user = getByUsername(username);
        if (user == null) return false;
        // 兼容旧数据的明文密码
        boolean matched;
        if (!user.getPassword().startsWith("$2a$")) {
            matched = oldPassword.equals(user.getPassword());
        } else {
            matched = encoder.matches(oldPassword, user.getPassword());
        }
        if (!matched) return false;
        return sysUserMapper.updatePassword(username, encoder.encode(newPassword)) > 0;
    }
}
