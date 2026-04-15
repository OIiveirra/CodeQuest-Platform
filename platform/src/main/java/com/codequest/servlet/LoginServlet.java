package com.codequest.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codequest.dto.Result;
import com.codequest.model.User;
import com.codequest.service.UserService;

/**
 * 登录控制器，负责登录校验与会话写入。
 * Author: 张雨泽
 */
public class LoginServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = trim(req.getParameter("username"));
        String password = trim(req.getParameter("password"));

        if (username.isEmpty() || password.isEmpty()) {
            req.setAttribute("error", "请输入用户名和密码。");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        Result<User> result = userService.login(username, password);
        if (!result.isSuccess() || result.getData() == null) {
            req.setAttribute("error", result.getMessage());
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        req.getSession(true).setAttribute("loginUser", result.getData());
        resp.sendRedirect(req.getContextPath() + "/");
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
