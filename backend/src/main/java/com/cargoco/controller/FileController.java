package com.cargoco.controller;

import com.cargoco.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        List<String> allowedSuffixes = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");
        if (!allowedSuffixes.contains(suffix)) {
            return Result.error("只支持上传图片文件（jpg/jpeg/png/gif/webp/bmp）");
        }
        // 限制大小 10MB
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error("文件大小不能超过10MB");
        }

        try {
            // 按日期创建子目录
            String datePath = new java.text.SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String dirPath = uploadPath + datePath + "/";
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String newFilename = UUID.randomUUID().toString().replace("-", "") + suffix;
            File dest = new File(dirPath + newFilename).getAbsoluteFile();
            file.transferTo(dest);

            String url = urlPrefix + datePath + "/" + newFilename;
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            data.put("name", originalFilename);
            return Result.success("上传成功", data);
        } catch (IOException e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}
