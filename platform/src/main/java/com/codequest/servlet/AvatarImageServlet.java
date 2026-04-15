package com.codequest.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.codequest.dao.UserDAO;
import com.codequest.model.User;

/**
 * 头像读取控制器，将磁盘头像文件转换为 HTTP 可访问资源。
 * Author: 张雨泽
 */
@WebServlet("/avatar/image")
public class AvatarImageServlet extends HttpServlet {

    private static final String PROJECT_ROOT_PROPERTY = "codequest.project.root";
    private static final String AVATAR_DIR_NAME = "uploads/avatars";
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        User freshUser;
        try {
            freshUser = userDAO.findById(loginUser.getId());
        } catch (Exception ex) {
            throw new ServletException("读取头像信息失败。", ex);
        }

        if (freshUser == null || freshUser.getAvatarUrl() == null || freshUser.getAvatarUrl().trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File avatarFile = resolveAvatarFile(freshUser.getAvatarUrl().trim());
        if (avatarFile == null || !avatarFile.exists() || !avatarFile.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = Files.probeContentType(avatarFile.toPath());
        if (contentType == null || !contentType.startsWith("image/")) {
            contentType = guessImageContentType(avatarFile.getName());
        }

        resp.setContentType(contentType);
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setDateHeader("Expires", 0);
        resp.setContentLengthLong(avatarFile.length());

        try (InputStream in = new FileInputStream(avatarFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                resp.getOutputStream().write(buffer, 0, len);
            }
            resp.getOutputStream().flush();
        }
    }

    private File resolveAvatarFile(String avatarPath) throws ServletException {
        File candidate = new File(avatarPath);
        if (candidate.isAbsolute()) {
            if (candidate.exists()) {
                return candidate;
            }

            // 兼容历史绝对路径失效场景：回退到当前 WebApp 头像目录按文件名查找。
            String fileName = candidate.getName();
            if (fileName != null && !fileName.trim().isEmpty()) {
                File fallback = resolveFromWebRoot(AVATAR_DIR_NAME + "/" + fileName);
                if (fallback != null && fallback.exists()) {
                    return fallback;
                }

                File root = resolveProjectRoot();
                if (root != null) {
                    File fallbackFromRoot = new File(new File(root, AVATAR_DIR_NAME), fileName);
                    if (fallbackFromRoot.exists()) {
                        return fallbackFromRoot;
                    }
                }
            }

            return candidate;
        }

        String normalized = avatarPath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        File fromWebRoot = resolveFromWebRoot(normalized);
        if (fromWebRoot != null && fromWebRoot.exists()) {
            return fromWebRoot;
        }

        File root = resolveProjectRoot();
        if (root != null) {
            File fileFromRoot = new File(root, normalized);
            if (fileFromRoot.exists()) {
                return fileFromRoot;
            }

            File fileFromAvatarDir = new File(new File(root, AVATAR_DIR_NAME), new File(normalized).getName());
            if (fileFromAvatarDir.exists()) {
                return fileFromAvatarDir;
            }
        }

        return candidate;
    }

    private File resolveFromWebRoot(String normalizedPath) {
        if (normalizedPath == null || normalizedPath.trim().isEmpty()) {
            return null;
        }
        String realPath = getServletContext().getRealPath("/" + normalizedPath);
        if (realPath == null || realPath.trim().isEmpty()) {
            return null;
        }
        return new File(realPath);
    }

    private File resolveProjectRoot() throws ServletException {
        String configuredRoot = System.getProperty(PROJECT_ROOT_PROPERTY);
        if (configuredRoot != null && !configuredRoot.trim().isEmpty()) {
            return new File(configuredRoot.trim());
        }

        try {
            File classesDir = new File(AvatarImageServlet.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File target = classesDir;
            if (target.isFile()) {
                target = target.getParentFile();
            }
            while (target != null && !new File(target, "pom.xml").exists()) {
                target = target.getParentFile();
            }
            if (target != null) {
                return target;
            }
        } catch (URISyntaxException ex) {
            throw new ServletException("解析项目根目录失败。", ex);
        }

        return new File(System.getProperty("user.dir", "."));
    }

    private String guessImageContentType(String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/jpeg";
    }
}