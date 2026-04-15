package com.codequest.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.codequest.dao.UserDAO;
import com.codequest.model.User;

/**
 * 头像上传控制器，负责上传校验、落盘与数据库地址更新。
 * Author: 张雨泽
 */
@WebServlet({"/avatar/upload", "/uploadAvatar"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 2 * 1024 * 1024, maxRequestSize = 3 * 1024 * 1024)
public class AvatarUploadServlet extends HttpServlet {

    private static final String PROJECT_ROOT_PROPERTY = "codequest.project.root";
    private static final String AVATAR_DIR_NAME = "uploads/avatars";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Part avatarPart;
        try {
            avatarPart = req.getPart("avatar");
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "上传请求格式错误。请使用 multipart/form-data。");
            return;
        }

        if (avatarPart == null || avatarPart.getSize() <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "请选择要上传的头像文件。");
            return;
        }

        String submittedFileName = safeFileName(avatarPart.getSubmittedFileName());
        String contentType = avatarPart.getContentType() == null ? "" : avatarPart.getContentType().toLowerCase(Locale.ROOT);
        String lowerName = submittedFileName.toLowerCase(Locale.ROOT);

        boolean isJpg = lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || "image/jpeg".equals(contentType);
        boolean isPng = lowerName.endsWith(".png") || "image/png".equals(contentType);

        if (!isJpg && !isPng) {
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "仅支持 jpg/png 格式头像。");
            return;
        }

        File dir = resolveAvatarDirectory();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new ServletException("头像目录创建失败: " + dir.getAbsolutePath());
        }

        String extension = isPng ? ".png" : ".jpg";
        String targetName = loginUser.getId() + extension;
        File targetFile = new File(dir, targetName);

        try {
            try (InputStream inputStream = avatarPart.getInputStream();
                 OutputStream outputStream = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            // 存储相对路径，避免容器重建或切换运行模式后绝对路径失效。
            String avatarPath = AVATAR_DIR_NAME + "/" + targetName;
            new UserDAO().updateAvatar(loginUser.getId(), avatarPath);

            // 同步会话中的用户信息，避免页面读取到旧头像地址。
            loginUser.setAvatarUrl(avatarPath);
            session.setAttribute("loginUser", loginUser);

            resp.sendRedirect(req.getContextPath() + "/profile");
        } catch (Exception ex) {
            throw new ServletException("头像上传失败，请稍后重试。", ex);
        }
    }

    private String safeFileName(String fileName) {
        if (fileName == null) {
            return "";
        }
        int slash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        return slash >= 0 ? fileName.substring(slash + 1) : fileName;
    }

    private File resolveAvatarDirectory() throws ServletException {
        File fromWebRoot = findAvatarDirFromWebRoot();
        if (fromWebRoot != null) {
            return fromWebRoot;
        }

        // 允许通过 JVM 参数显式指定项目根目录：-Dcodequest.project.root=...
        String configuredRoot = System.getProperty(PROJECT_ROOT_PROPERTY);
        if (configuredRoot != null && !configuredRoot.trim().isEmpty()) {
            return new File(configuredRoot.trim(), AVATAR_DIR_NAME);
        }

        File fromCodeSource = findProjectRootFromCodeSource();
        if (fromCodeSource != null) {
            return new File(fromCodeSource, AVATAR_DIR_NAME);
        }

        File fromWebApp = findProjectRootFromWebApp();
        if (fromWebApp != null) {
            return new File(fromWebApp, AVATAR_DIR_NAME);
        }

        // 兜底：确保不抛 500，目录落到当前工作目录。
        File userDir = new File(System.getProperty("user.dir", "."));
        return new File(userDir, AVATAR_DIR_NAME);
    }

    private File findAvatarDirFromWebRoot() {
        ServletContext context = getServletContext();
        if (context == null) {
            return null;
        }

        String realPath = context.getRealPath("/" + AVATAR_DIR_NAME);
        if (realPath == null || realPath.trim().isEmpty()) {
            return null;
        }
        return new File(realPath);
    }

    private File findProjectRootFromCodeSource() throws ServletException {
        try {
            File classesDir = new File(AvatarUploadServlet.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File projectRoot = classesDir;
            if (classesDir.isFile()) {
                projectRoot = classesDir.getParentFile();
            }

            File target = projectRoot;
            while (target != null && !new File(target, "pom.xml").exists()) {
                target = target.getParentFile();
            }

            return target;
        } catch (URISyntaxException ex) {
            throw new ServletException("解析头像目录失败。", ex);
        }
    }

    private File findProjectRootFromWebApp() {
        ServletContext context = getServletContext();
        if (context == null) {
            return null;
        }

        String realPath = context.getRealPath("/");
        if (realPath == null || realPath.trim().isEmpty()) {
            return null;
        }

        File target = new File(realPath);
        while (target != null && !new File(target, "pom.xml").exists()) {
            target = target.getParentFile();
        }
        return target;
    }
}
