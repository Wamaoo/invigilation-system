package com.invigilation.invigilation.service.impl;

import com.invigilation.invigilation.entity.TeacherInvigilation;
import com.invigilation.invigilation.mapper.TeacherInvigilationMapper;
import com.invigilation.invigilation.service.TeacherInvigilationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeacherInvigilationServiceImpl implements TeacherInvigilationService {

    @Resource
    private TeacherInvigilationMapper teacherInvigilationMapper;

    @Override
    public List<TeacherInvigilation> getByTeacherId(String teacherId) {
        return teacherInvigilationMapper.selectByTeacherId(teacherId);
    }

    @Override
    public List<TeacherInvigilation> getByArrangeId(String arrangeId) {
        return teacherInvigilationMapper.selectByArrangeId(arrangeId);
    }

    @Override
    public void addTeacher(TeacherInvigilation record) {
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        teacherInvigilationMapper.insert(record);
    }

    @Override
    public void batchAddTeachers(List<TeacherInvigilation> list) {
        LocalDateTime now = LocalDateTime.now();
        for (TeacherInvigilation ti : list) {
            ti.setCreateTime(now);
            ti.setUpdateTime(now);
        }
        teacherInvigilationMapper.batchInsert(list);
    }

    @Override
    public void removeByArrangeId(String arrangeId) {
        teacherInvigilationMapper.deleteByArrangeId(arrangeId);
    }

    @Override
    public void removeByTeacherId(String teacherId) {
        teacherInvigilationMapper.deleteByTeacherId(teacherId);
    }
}
