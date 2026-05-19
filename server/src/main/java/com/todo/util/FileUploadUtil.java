package com.todo.util;

import com.todo.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
        if (file.isEmpty()) {
            throw new BizException(400, "文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BizException(400, "不支持的文件类型");
        }
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".") ?
                original.substring(original.lastIndexOf(".")).toLowerCase() : "";
        if (!List.of(".jpg", ".jpeg", ".png", ".gif", ".webp").contains(ext)) {
            throw new BizException(400, "不支持的文件格式，仅支持 jpg/png/gif/webp");
        }
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
