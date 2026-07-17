package com.invigilation.invigilation.controller;

import com.invigilation.invigilation.entity.ConflictInfo;
import com.invigilation.invigilation.entity.InvigilationConfig;
import com.invigilation.invigilation.entity.TeacherInvigilation;
import com.invigilation.invigilation.mapper.InvigilationConfigMapper;
import com.invigilation.invigilation.service.InvigilationConfigService;
import com.invigilation.invigilation.service.TeacherInvigilationService;
import com.invigilation.invigilation.util.IdGenerator;
import com.invigilation.invigilation.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/invigilation")
public class InvigilationConfigController {
    @Resource
    private InvigilationConfigService invigilationConfigService;

    @Resource
    private TeacherInvigilationService teacherInvigilationService;

    @Resource
    private InvigilationConfigMapper invigilationConfigMapper;

    @GetMapping("/next-id")
    public Result<String> getNextId() {
        List<String> ids = invigilationConfigMapper.selectAllArrangeIds();
        return Result.success(IdGenerator.generateNextId("ARRANGE", ids));
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getConfigList(
            @RequestParam(required = false) String examName,
            @RequestParam(required = false) String classroom,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String semesterId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        int offset = (page - 1) * size;
        List<InvigilationConfig> list = invigilationConfigService.getConfigListByPage(
                examName, classroom, startDate, endDate, semesterId, offset, size);
        invigilationConfigService.enrichStatusList(list);
        long total = invigilationConfigService.getConfigTotal(examName, classroom, startDate, endDate, semesterId);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<Void> addConfig(@RequestBody InvigilationConfig config) {
        boolean ok = invigilationConfigService.addConfig(config);
        if (ok && config.getArrangeId() != null) {
            // 同步保存 teacher_invigilation 关联
            saveTeacherRelations(config.getArrangeId(), config.getTeacherIds());
        }
        return ok ? Result.success() : Result.error("新增失败");
    }

    @PutMapping("/edit")
    public Result<Void> editConfig(@RequestBody InvigilationConfig config) {
        boolean ok = invigilationConfigService.editConfig(config);
        if (ok && config.getArrangeId() != null) {
            // 重建 teacher_invigilation 关联
            teacherInvigilationService.removeByArrangeId(config.getArrangeId());
            saveTeacherRelations(config.getArrangeId(), config.getTeacherIds());
        }
        return ok ? Result.success() : Result.error("编辑失败");
    }

    @DeleteMapping("/delete/{arrangeId}")
    public Result<Void> deleteConfig(@PathVariable String arrangeId) {
        return invigilationConfigService.deleteConfig(arrangeId) ? Result.success() : Result.error("删除失败");
    }

    @PostMapping("/import")
    public Result<String> importConfigs(@RequestBody List<InvigilationConfig> configList) {
        int insert = 0, update = 0, fail = 0;
        StringBuilder failMsg = new StringBuilder();
        for (int i = 0; i < configList.size(); i++) {
            InvigilationConfig c = configList.get(i);
            if (c.getArrangeId() == null || c.getArrangeId().trim().isEmpty()) {
                fail++; failMsg.append("第").append(i + 1).append("条:安排ID为空;");
                continue;
            }
            try {
                InvigilationConfig existing = invigilationConfigMapper.selectByArrangeId(c.getArrangeId());
                if (existing != null) {
                    existing.setExamName(c.getExamName());
                    existing.setClassroom(c.getClassroom());
                    existing.setExamDate(c.getExamDate());
                    existing.setStartTime(c.getStartTime());
                    existing.setEndTime(c.getEndTime());
                    existing.setExamDuration(c.getExamDuration());
                    existing.setStatus(c.getStatus());
                    existing.setRemark(c.getRemark());
                    invigilationConfigService.editConfig(existing);
                    update++;
                } else {
                    invigilationConfigService.addConfig(c);
                    insert++;
                }
            } catch (Exception e) {
                fail++;
                failMsg.append("第").append(i + 1).append("条:").append(e.getMessage()).append(";");
            }
        }
        String msg = "导入完成：新增 " + insert + " 条，更新 " + update + " 条，失败 " + fail + " 条";
        if (failMsg.length() > 0) msg += "。" + failMsg;
        return Result.success(msg);
    }

    @GetMapping("/check-conflicts")
    public Result<List<ConflictInfo>> checkConflicts(
            @RequestParam String examDate,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam String classroom,
            @RequestParam String teacherIds,
            @RequestParam(required = false) String excludeArrangeId) {
        List<String> ids = teacherIds == null || teacherIds.isBlank()
                ? List.of()
                : Arrays.stream(teacherIds.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        List<ConflictInfo> conflicts = invigilationConfigService.checkConflicts(
                examDate, startTime, endTime, classroom, ids, excludeArrangeId);
        return Result.success(conflicts);
    }

    /** 自动排课推荐（排除任课教师） */
    @GetMapping("/auto-suggest")
    public Result<List<String>> autoSuggestTeachers(
            @RequestParam String examDate,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "2") Integer requiredCount,
            @RequestParam(required = false) String excludeArrangeId,
            @RequestParam(required = false) String subjectId) {
        List<String> suggested = invigilationConfigService.autoSuggestTeachers(
                examDate, startTime, endTime, requiredCount, excludeArrangeId, subjectId);
        return Result.success(suggested);
    }

    @PostMapping("/update-status")
    public Result<String> updateStatus(@RequestBody InvigilationConfig config) {
        boolean success = invigilationConfigService.updateInvigilationStatus(config.getArrangeId(), config.getStatus());
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }

    // ====== 内部工具方法 ======
    private void saveTeacherRelations(String arrangeId, String teacherIds) {
        if (teacherIds == null || teacherIds.isBlank()) return;
        List<TeacherInvigilation> list = Arrays.stream(teacherIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(id -> {
                    TeacherInvigilation ti = new TeacherInvigilation();
                    ti.setTeacherId(id);
                    ti.setArrangeId(arrangeId);
                    return ti;
                })
                .collect(Collectors.toList());
        if (!list.isEmpty()) {
            teacherInvigilationService.batchAddTeachers(list);
        }
    }
}
