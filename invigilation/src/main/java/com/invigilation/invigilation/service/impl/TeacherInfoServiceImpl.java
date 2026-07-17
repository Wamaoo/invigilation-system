//实现类
package com.invigilation.invigilation.service.impl;

import com.invigilation.invigilation.entity.TeacherInfo;
import com.invigilation.invigilation.mapper.TeacherInfoMapper;
import com.invigilation.invigilation.service.TeacherInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
// 标记这是Service组件，Spring会自动管理
@Service
public class TeacherInfoServiceImpl implements TeacherInfoService {
    @Resource
    private TeacherInfoMapper teacherInfoMapper;
//查询教师列表
    @Override
    public List<TeacherInfo> getTeacherList(String teacherName, String department) {
        return teacherInfoMapper.selectByCondition(teacherName, department);
    }

    @Override
    public List<TeacherInfo> getTeacherListByPage(String teacherName, String department, int offset, int size) {
        return teacherInfoMapper.selectByConditionWithPage(teacherName, department, offset, size);
    }

    @Override
    public long getTeacherTotal(String teacherName, String department) {
        return teacherInfoMapper.countByCondition(teacherName, department);
    }
//新增教师
    @Override
    public boolean addTeacher(TeacherInfo teacherInfo) {
        teacherInfo.setCreateTime(LocalDateTime.now());
        teacherInfo.setUpdateTime(LocalDateTime.now());
        return teacherInfoMapper.insert(teacherInfo) > 0;
    }
//编辑教师
    @Override
    public boolean editTeacher(TeacherInfo teacherInfo) {
        teacherInfo.setUpdateTime(LocalDateTime.now());
        return teacherInfoMapper.updateById(teacherInfo) > 0;
    }

    @Override
    public boolean deleteTeacher(String teacherId) {
        return teacherInfoMapper.deleteById(teacherId) > 0;
    }

    @Override
    public TeacherInfo getByTeacherId(String teacherId) {
        return teacherInfoMapper.selectByTeacherId(teacherId);
    }
}