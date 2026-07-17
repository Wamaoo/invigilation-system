package com.invigilation.invigilation.controller;

import com.invigilation.invigilation.entity.Semester;
import com.invigilation.invigilation.service.SemesterService;
import com.invigilation.invigilation.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/semester")
public class SemesterController {

    @Resource
    private SemesterService semesterService;

    @GetMapping("/list")
    public Result<List<Semester>> list() {
        return Result.success(semesterService.getAll());
    }

    @GetMapping("/current")
    public Result<Semester> current() {
        return Result.success(semesterService.getCurrent());
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Semester semester) {
        boolean success = semesterService.add(semester);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping("/edit")
    public Result<String> edit(@RequestBody Semester semester) {
        boolean success = semesterService.update(semester);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        boolean success = semesterService.delete(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PutMapping("/set-current/{id}")
    public Result<String> setCurrent(@PathVariable Integer id) {
        boolean success = semesterService.setCurrent(id);
        return success ? Result.success("切换成功") : Result.error("切换失败");
    }
}
