package com.campus.errand.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/upload")
public class FileUploadController {

    // 上传文件保存路径
    private static final String UPLOAD_PATH = "uploads";

    /**
     * 上传图片
     */
    @PostMapping("/image")
    public Map<String, Object> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestAttribute(value = "currentUserId", required = false) Long userId,
            HttpServletRequest request) {

        try {
            if (file.isEmpty()) {
                return Map.of("code", 400, "msg", "文件为空", "data", null);
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Map.of("code", 400, "msg", "只支持图片文件", "data", null);
            }

            // 验证文件大小（5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return Map.of("code", 400, "msg", "文件大小不能超过5MB", "data", null);
            }

            // 创建上传目录
            String realPath = request.getServletContext().getRealPath(UPLOAD_PATH);
            File uploadDir = new File(realPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + UUID.randomUUID() + suffix;

            // 保存文件
            File dest = new File(uploadDir, fileName);
            file.transferTo(dest);

            // 构建文件URL
            String fileUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + request.getContextPath() + "/" + UPLOAD_PATH + "/" + fileName;

            log.info("图片上传成功: {}, 上传人: {}", fileUrl, userId);

            // 返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("url", fileUrl);
            data.put("name", originalFilename);
            data.put("size", file.getSize());

            return Map.of("code", 200, "msg", "上传成功", "data", data);

        } catch (IOException e) {
            log.error("上传图片异常", e);
            return Map.of("code", 500, "msg", "上传失败：" + e.getMessage(), "data", null);
        } catch (Exception e) {
            log.error("上传图片异常", e);
            return Map.of("code", 500, "msg", "上传失败：系统错误", "data", null);
        }
    }

    /**
     * 批量上传图片
     */
    @PostMapping("/images")
    public Map<String, Object> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestAttribute(value = "currentUserId", required = false) Long userId,
            HttpServletRequest request) {

        try {
            if (files == null || files.isEmpty()) {
                return Map.of("code", 400, "msg", "文件为空", "data", null);
            }

            if (files.size() > 9) {
                return Map.of("code", 400, "msg", "最多上传9张图片", "data", null);
            }

            List<Map<String, Object>> imageList = new ArrayList<>();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // 验证文件类型
                    String contentType = file.getContentType();
                    if (contentType != null && contentType.startsWith("image/")) {
                        // 验证文件大小（5MB）
                        if (file.getSize() <= 5 * 1024 * 1024) {
                            // 创建上传目录
                            String realPath = request.getServletContext().getRealPath(UPLOAD_PATH);
                            File uploadDir = new File(realPath);
                            if (!uploadDir.exists()) {
                                uploadDir.mkdirs();
                            }

                            // 生成文件名
                            String originalFilename = file.getOriginalFilename();
                            String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
                            String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + UUID.randomUUID() + suffix;

                            // 保存文件
                            File dest = new File(uploadDir, fileName);
                            file.transferTo(dest);

                            // 构建文件URL
                            String fileUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                                    + request.getContextPath() + "/" + UPLOAD_PATH + "/" + fileName;

                            Map<String, Object> imageInfo = new HashMap<>();
                            imageInfo.put("url", fileUrl);
                            imageInfo.put("name", originalFilename);
                            imageInfo.put("size", file.getSize());

                            imageList.add(imageInfo);
                        }
                    }
                }
            }

            log.info("批量上传图片成功: {}张, 上传人: {}", imageList.size(), userId);

            return Map.of("code", 200, "msg", "上传成功", "data", imageList);

        } catch (Exception e) {
            log.error("批量上传图片异常", e);
            return Map.of("code", 500, "msg", "上传失败：" + e.getMessage(), "data", null);
        }
    }

    /**
     * 删除图片
     */
    @DeleteMapping("/image")
    public Map<String, Object> deleteImage(
            @RequestParam("url") String url,
            @RequestAttribute(value = "currentUserId", required = false) Long userId,
            HttpServletRequest request) {

        try {
            // 从URL中提取文件名
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            String realPath = request.getServletContext().getRealPath(UPLOAD_PATH);
            File file = new File(realPath, fileName);

            if (file.exists() && file.delete()) {
                log.info("删除图片成功: {}, 操作人: {}", fileName, userId);
                return Map.of("code", 200, "msg", "删除成功", "data", null);
            } else {
                return Map.of("code", 400, "msg", "文件不存在或删除失败", "data", null);
            }

        } catch (Exception e) {
            log.error("删除图片异常", e);
            return Map.of("code", 500, "msg", "删除失败：" + e.getMessage(), "data", null);
        }
    }
}
