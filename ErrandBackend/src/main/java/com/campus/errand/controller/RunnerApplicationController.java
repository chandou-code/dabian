package com.campus.errand.controller;

import com.campus.errand.entity.RunnerApplication;
import com.campus.errand.service.RunnerApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 跑腿员申请控制器
 */
@Slf4j
@RestController
@RequestMapping("/runner/applications")
public class RunnerApplicationController {

    @Autowired
    private RunnerApplicationService applicationService;

    /**
     * 提交跑腿员申请
     */
    @PostMapping("/submit")
    public Map<String, Object> submitApplication(@RequestBody RunnerApplication application, HttpServletRequest request) {
        try {
            // 从请求中获取当前用户ID
            Long userId = (Long) request.getAttribute("currentUserId");
            if (userId == null) {
                return Map.of("code", 401, "msg", "用户未认证", "data", null);
            }

            // 设置用户ID
            application.setUserId(userId);

            // 提交申请
            boolean result = applicationService.submitApplication(application);

            if (result) {
                return Map.of("code", 200, "msg", "申请提交成功", "data", application);
            } else {
                return Map.of("code", 500, "msg", "申请提交失败", "data", null);
            }
        } catch (Exception e) {
            log.error("提交申请失败", e);
            return Map.of("code", 500, "msg", "申请提交失败：" + e.getMessage(), "data", null);
        }
    }

    /**
     * 获取申请列表（管理员）
     */
    @GetMapping("/list")
    public Map<String, Object> getApplicationList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        try {
            // 检查用户权限（这里简化处理，实际应该检查是否为管理员）
            String role = (String) request.getAttribute("currentRole");
            if (!"admin".equals(role)) {
                return Map.of("code", 403, "msg", "无权限访问", "data", null);
            }

            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("status", status);
            params.put("page", page);
            params.put("pageSize", pageSize);

            // 获取申请列表
            List<RunnerApplication> applications = applicationService.getApplicationList(params);
            long total = applicationService.getApplicationCount(params);

            // 构建响应
            Map<String, Object> data = new HashMap<>();
            data.put("list", applications);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);

            return Map.of("code", 200, "msg", "success", "data", data);
        } catch (Exception e) {
            log.error("获取申请列表失败", e);
            return Map.of("code", 500, "msg", "获取失败：" + e.getMessage(), "data", null);
        }
    }

    /**
     * 获取申请详情
     */
    @GetMapping("/detail/{id}")
    public Map<String, Object> getApplicationDetail(@PathVariable Long id) {
        try {
            RunnerApplication application = applicationService.getApplicationById(id);
            if (application != null) {
                return Map.of("code", 200, "msg", "success", "data", application);
            } else {
                return Map.of("code", 404, "msg", "申请不存在", "data", null);
            }
        } catch (Exception e) {
            log.error("获取申请详情失败", e);
            return Map.of("code", 500, "msg", "获取失败：" + e.getMessage(), "data", null);
        }
    }

    /**
     * 批准申请
     */
    @PutMapping("/approve/{id}")
    public Map<String, Object> approveApplication(
            @PathVariable Long id,
            @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        try {
            // 检查用户权限
            String role = (String) request.getAttribute("currentRole");
            if (!"admin".equals(role)) {
                return Map.of("code", 403, "msg", "无权限操作", "data", null);
            }

            Long reviewerId = (Long) request.getAttribute("currentUserId");
            String comment = params.getOrDefault("comment", "");

            // 批准申请
            boolean result = applicationService.approveApplication(id, reviewerId, comment);

            if (result) {
                return Map.of("code", 200, "msg", "批准成功", "data", null);
            } else {
                return Map.of("code", 500, "msg", "批准失败", "data", null);
            }
        } catch (Exception e) {
            log.error("批准申请失败", e);
            return Map.of("code", 500, "msg", "批准失败：" + e.getMessage(), "data", null);
        }
    }

    /**
     * 拒绝申请
     */
    @PutMapping("/reject/{id}")
    public Map<String, Object> rejectApplication(
            @PathVariable Long id,
            @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        try {
            // 检查用户权限
            String role = (String) request.getAttribute("currentRole");
            if (!"admin".equals(role)) {
                return Map.of("code", 403, "msg", "无权限操作", "data", null);
            }

            Long reviewerId = (Long) request.getAttribute("currentUserId");
            String comment = params.getOrDefault("comment", "");

            // 拒绝申请
            boolean result = applicationService.rejectApplication(id, reviewerId, comment);

            if (result) {
                return Map.of("code", 200, "msg", "拒绝成功", "data", null);
            } else {
                return Map.of("code", 500, "msg", "拒绝失败", "data", null);
            }
        } catch (Exception e) {
            log.error("拒绝申请失败", e);
            return Map.of("code", 500, "msg", "拒绝失败：" + e.getMessage(), "data", null);
        }
    }

    /**
     * 上传学生证照片
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadStudentIdPhoto(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Map.of("code", 400, "msg", "请选择文件", "data", null);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + suffix;

            // 保存文件（实际项目中应该使用云存储或配置文件路径）
            String uploadPath = "D:/uploads/";
            File directory = new File(uploadPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File dest = new File(uploadPath + filename);
            file.transferTo(dest);

            // 返回文件URL
            String fileUrl = "/uploads/" + filename;
            return Map.of("code", 200, "msg", "上传成功", "data", Map.of("url", fileUrl));
        } catch (IOException e) {
            log.error("上传文件失败", e);
            return Map.of("code", 500, "msg", "上传失败：" + e.getMessage(), "data", null);
        }
    }
}
