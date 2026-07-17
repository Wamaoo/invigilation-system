package com.invigilation.invigilation.service;

import com.invigilation.invigilation.entity.ExamSubject;
import java.util.List;

public interface ExamSubjectService {
    // 获取考试科目列表（支持筛选）
    List<ExamSubject> getExamList(String examName, String major, String examType, String semesterId);
    List<ExamSubject> getExamListByPage(String examName, String major, String examType, String semesterId, int offset, int size);
    long getExamTotal(String examName, String major, String examType, String semesterId);
    // 新增考试科目
    boolean addExam(ExamSubject examSubject);
    // 编辑考试科目
    boolean editExam(ExamSubject examSubject);
    // 删除考试科目
    boolean deleteExam(String subjectId);
}