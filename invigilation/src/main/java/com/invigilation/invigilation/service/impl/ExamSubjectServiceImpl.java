package com.invigilation.invigilation.service.impl;

import com.invigilation.invigilation.entity.ExamSubject;
import com.invigilation.invigilation.mapper.ExamSubjectMapper;
import com.invigilation.invigilation.service.ExamSubjectService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExamSubjectServiceImpl implements ExamSubjectService {
    @Resource
    private ExamSubjectMapper examSubjectMapper;

    @Override
    public List<ExamSubject> getExamList(String examName, String major, String examType, String semesterId) {
        return examSubjectMapper.selectByCondition(examName, major, examType, semesterId);
    }

    @Override
    public List<ExamSubject> getExamListByPage(String examName, String major, String examType, String semesterId, int offset, int size) {
        return examSubjectMapper.selectByConditionWithPage(examName, major, examType, semesterId, offset, size);
    }

    @Override
    public long getExamTotal(String examName, String major, String examType, String semesterId) {
        return examSubjectMapper.countByCondition(examName, major, examType, semesterId);
    }

    @Override
    public boolean addExam(ExamSubject examSubject) {
        //自动填充创建/更新时间
        examSubject.setCreateTime(LocalDateTime.now());
        examSubject.setUpdateTime(LocalDateTime.now());
        return examSubjectMapper.insert(examSubject) > 0;
    }

    @Override
    public boolean editExam(ExamSubject examSubject) {
        //编辑时更新修改时间
        examSubject.setUpdateTime(LocalDateTime.now());
        return examSubjectMapper.updateById(examSubject) > 0;
    }

    @Override
    public boolean deleteExam(String subjectId) {
        //根据科目ID删除
        return examSubjectMapper.deleteById(subjectId) > 0;
    }
}