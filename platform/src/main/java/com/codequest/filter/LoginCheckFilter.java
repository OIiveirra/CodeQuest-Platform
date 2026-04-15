package com.codequest.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.codequest.model.User;

/**
 * 登录校验过滤器，拦截受保护路径。
 * Author: 张雨泽
 */
public class LoginCheckFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("loginUser");
        if (user != null) {
            chain.doFilter(request, response);
            return;
        }

        // 统一回到登录页，避免未授权访问受保护资源。
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }

    @Override
    public void destroy() {
    }
}
