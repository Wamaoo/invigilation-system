//处理业务逻辑（相当于 “业务处理部门”）
package com.invigilation.invigilation.service;

import com.invigilation.invigilation.entity.ConflictApplication;
import java.util.List;

public interface ConflictApplicationService {
    // 提交冲突申请
    boolean submitApplication(ConflictApplication application);

    // 查询所有申请
    List<ConflictApplication> getAllApplications();

    // 处理申请（同意/拒绝）
    boolean handleApplication(Integer id, Integer status);

    // 按状态查询申请（用于Dashboard查待审核）
    List<ConflictApplication> getApplyListByStatus(Integer status);

    // 根据ID查询申请
    ConflictApplication getById(Integer id);
}