package com.invigilation.invigilation.service.impl;

import com.invigilation.invigilation.entity.ConflictApplication;
import com.invigilation.invigilation.mapper.ConflictApplicationMapper;
import com.invigilation.invigilation.service.ConflictApplicationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConflictApplicationServiceImpl implements ConflictApplicationService {

    @Resource
    private ConflictApplicationMapper conflictApplicationMapper;

    @Override
    public boolean submitApplication(ConflictApplication application) {
        application.setStatus(0); // 0-待审核
        application.setCreateTime(LocalDateTime.now());
        return conflictApplicationMapper.insert(application) > 0;
    }

    @Override
    public List<ConflictApplication> getAllApplications() {
        return conflictApplicationMapper.selectAll();
    }

    @Override
    public boolean handleApplication(Integer id, Integer status) {
        // 1. 先更新状态
        int updateCount = conflictApplicationMapper.updateStatus(id, status);
        // 2. 审核后删除该申请
        if (updateCount > 0) {
            return conflictApplicationMapper.deleteById(id) > 0;
        }
        return false;
    }

    @Override
    public ConflictApplication getById(Integer id) {
        return conflictApplicationMapper.selectById(id);
    }

    @Override
    public List<ConflictApplication> getApplyListByStatus(Integer status) {
        // 手动过滤待审核申请
        List<ConflictApplication> allList = conflictApplicationMapper.selectAll();
        return allList.stream()
                .filter(item -> status.equals(item.getStatus()))
                .toList();
    }
}