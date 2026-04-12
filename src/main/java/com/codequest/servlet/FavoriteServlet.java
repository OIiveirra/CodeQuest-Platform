package com.codequest.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.codequest.dao.FavoriteDAO;
import com.codequest.model.User;

/**
 * 收藏切换控制器，支持 Ajax 收藏/取消收藏。
 */
@WebServlet("/favorite/toggle")
public class FavoriteServlet extends HttpServlet {

    private final FavoriteDAO favoriteDAO = new FavoriteDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getId() == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"success\":false,\"message\":\"请先登录\"}");
            return;
        }

        Long questionId = parseLong(req.getParameter("questionId"));
        if (questionId == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"题目ID无效\"}");
            return;
        }

        try {
            boolean favorited = favoriteDAO.toggleFavorite(loginUser.getId(), questionId);
            String message = favorited ? "收藏成功" : "已取消收藏";
            resp.getWriter().write("{\"success\":true,\"favorited\":" + favorited + ",\"message\":\"" + message + "\"}");
        } catch (SQLException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\":false,\"message\":\"操作失败，请稍后重试\"}");
        }
    }

    private Long parseLong(String value) {
        try {
            return value == null || value.trim().isEmpty() ? null : Long.valueOf(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }
}
