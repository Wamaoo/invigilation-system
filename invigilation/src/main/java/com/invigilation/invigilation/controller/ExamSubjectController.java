package com.invigilation.invigilation.controller;

import com.invigilation.invigilation.entity.ExamSubject;
import com.invigilation.invigilation.mapper.ExamSubjectMapper;
import com.invigilation.invigilation.service.ExamSubjectService;
import com.invigilation.invigilation.util.IdGenerator;
import com.invigilation.invigilation.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/exam")
public class ExamSubjectController {
    @Resource
    private ExamSubjectService examSubjectService;

    @Resource
    private ExamSubjectMapper examSubjectMapper;

    /** 获取下一个可用的科目ID（自动生成 EXAM + 3位数字） */
    @GetMapping("/next-id")
    public Result<String> getNextId() {
        List<String> ids = examSubjectMapper.selectAllExamIds();
        return Result.success(IdGenerator.generateNextId("EXAM", ids));
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getExamList(
            @RequestParam(required = false) String examName,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String examType,
            @RequestParam(required = false) String semesterId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        int offset = (page - 1) * size;
        List<ExamSubject> list = examSubjectService.getExamListByPage(examName, major, examType, semesterId, offset, size);
        long total = examSubjectService.getExamTotal(examName, major, examType, semesterId);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<Void> addExam(@RequestBody ExamSubject examSubject) {
        return examSubjectService.addExam(examSubject) ? Result.success() : Result.error("新增失败");
    }

    @PutMapping("/edit")
    public Result<Void> editExam(@RequestBody ExamSubject examSubject) {
        return examSubjectService.editExam(examSubject) ? Result.success() : Result.error("编辑失败");
    }

    @DeleteMapping("/delete/{subjectId}")
    public Result<Void> deleteExam(@PathVariable String subjectId) {
        return examSubjectService.deleteExam(subjectId) ? Result.success() : Result.error("删除失败");
    }

    /** Excel 导入考试科目数据（已存在则更新，不存在则新增） */
    @PostMapping("/import")
    public Result<String> importExams(@RequestBody List<ExamSubject> examList) {
        int insert = 0, update = 0, fail = 0;
        StringBuilder failMsg = new StringBuilder();
        for (int i = 0; i < examList.size(); i++) {
            ExamSubject e = examList.get(i);
            if (e.getSubjectId() == null || e.getSubjectId().trim().isEmpty()) {
                fail++; failMsg.append("第").append(i + 1).append("条:科目ID为空;");
                continue;
            }
            try {
                ExamSubject existing = examSubjectMapper.selectBySubjectId(e.getSubjectId());
                if (existing != null) {
                    existing.setExamName(e.getExamName());
                    existing.setGrade(e.getGrade());
                    existing.setMajor(e.getMajor());
                    existing.setExamType(e.getExamType());
                    examSubjectService.editExam(existing);
                    update++;
                } else {
                    examSubjectService.addExam(e);
                    insert++;
                }
            } catch (Exception ex) {
                fail++;
                failMsg.append("第").append(i + 1).append("条:").append(ex.getMessage()).append(";");
            }
        }
        String msg = "导入完成：新增 " + insert + " 条，更新 " + update + " 条，失败 " + fail + " 条";
        if (failMsg.length() > 0) msg += "。" + failMsg;
        return Result.success(msg);
    }
}