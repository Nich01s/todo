package com.todo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileUploadUtil {

    private final Path uploadPath;

    public FileUploadUtil(@Value("${app.upload.avatar-dir:uploads/avatars}") String avatarDir) {
        this.uploadPath = Paths.get(avatarDir);
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录", e);
        }
    }

    public String uploadAvatar(MultipartFile file) {
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".") ?
                original.substring(original.lastIndexOf(".")) : ".png";
        String filename = UUID.randomUUID() + ext;
        try {
            Path target = uploadPath.resolve(filename);
            file.transferTo(target.toFile());
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }
}
