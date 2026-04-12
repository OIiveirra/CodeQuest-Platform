package com.codequest.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codequest.dto.Result;
import com.codequest.service.UserService;

/**
 * 注册控制器，负责新用户注册流程。
 * Author: 张雨泽
 */
public class RegisterServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = trim(req.getParameter("username"));
        String password = trim(req.getParameter("password"));

        if (username.isEmpty() || password.isEmpty()) {
            req.setAttribute("error", "用户名和密码不能为空。");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        Result<Void> result = userService.register(username, password);
        if (!result.isSuccess()) {
            req.setAttribute("error", result.getMessage());
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/login?registered=true");
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
