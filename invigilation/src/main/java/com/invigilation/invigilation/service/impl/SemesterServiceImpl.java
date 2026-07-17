package com.invigilation.invigilation.service.impl;

import com.invigilation.invigilation.entity.Semester;
import com.invigilation.invigilation.mapper.SemesterMapper;
import com.invigilation.invigilation.service.SemesterService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SemesterServiceImpl implements SemesterService {
    @Resource
    private SemesterMapper semesterMapper;

    @Override
    public List<Semester> getAll() {
        return semesterMapper.selectAll();
    }

    @Override
    public Semester getById(Integer id) {
        return semesterMapper.selectById(id);
    }

    @Override
    public Semester getCurrent() {
        return semesterMapper.selectCurrent();
    }

    @Override
    public boolean add(Semester semester) {
        semester.setCreateTime(LocalDateTime.now());
        semester.setUpdateTime(LocalDateTime.now());
        if (semester.getIsCurrent() == null) semester.setIsCurrent(0);
        return semesterMapper.insert(semester) > 0;
    }

    @Override
    public boolean update(Semester semester) {
        semester.setUpdateTime(LocalDateTime.now());
        return semesterMapper.updateById(semester) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return semesterMapper.deleteById(id) > 0;
    }

    @Override
    public boolean setCurrent(Integer id) {
        semesterMapper.clearCurrent();
        Semester semester = new Semester();
        semester.setId(id);
        semester.setIsCurrent(1);
        semester.setUpdateTime(LocalDateTime.now());
        return semesterMapper.updateById(semester) > 0;
    }
}
