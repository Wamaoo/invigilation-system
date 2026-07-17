package com.invigilation.invigilation.controller;

import com.invigilation.invigilation.util.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Resource
    private ResourceLoader resourceLoader;

    /** 上传文件，返回可访问的URL路径 */
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        try {
            // 生成唯一文件名，防止覆盖
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String savedName = UUID.randomUUID().toString().replace("-", "") + ext;

            // 保存到 uploads/conflict/ 目录
            String dateDir = "conflict";
            Path savePath = Paths.get(uploadDir, dateDir);
            Files.createDirectories(savePath);
            File saveFile = savePath.resolve(savedName).toFile();
            file.transferTo(saveFile);

            // 返回可访问的URL路径（带 /invigilation 前缀以匹配代理规则）
            String fileUrl = "/invigilation/api/files/" + dateDir + "/" + savedName;
            return Result.success(fileUrl);
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    /** 文件下载/预览 */
    @GetMapping("/files/{dir}/{filename}")
    public void serveFile(@PathVariable String dir, @PathVariable String filename,
                          HttpServletResponse response) {
        try {
            Path filePath = Paths.get(uploadDir, dir, filename).normalize();
            File file = filePath.toFile();
            if (!file.exists()) {
                response.setStatus(404);
                response.getWriter().write("文件不存在");
                return;
            }

            // 根据扩展名设置Content-Type
            String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf(".")).toLowerCase() : "";
            String contentType = switch (ext) {
                case ".jpg", ".jpeg" -> "image/jpeg";
                case ".png" -> "image/png";
                case ".gif" -> "image/gif";
                case ".bmp" -> "image/bmp";
                case ".pdf" -> "application/pdf";
                case ".doc" -> "application/msword";
                case ".docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                case ".xls" -> "application/vnd.ms-excel";
                case ".xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                case ".zip" -> "application/zip";
                case ".rar" -> "application/x-rar-compressed";
                default -> "application/octet-stream";
            };

            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
            Files.copy(filePath, response.getOutputStream());
            response.getOutputStream().flush();
        } catch (IOException e) {
            response.setStatus(500);
        }
    }
}
