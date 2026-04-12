package com.codequest.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.codequest.util.InterviewSessionUtils;

/**
 * 面试上下文管理控制器，用于清空当前面试会话。
 */
@WebServlet("/interview/session")
public class InterviewSessionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        InterviewSessionUtils.clear(session);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
