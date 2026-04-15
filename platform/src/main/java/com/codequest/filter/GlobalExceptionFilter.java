package com.codequest.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 全局异常过滤器，统一捕获未处理异常并跳转 500 页面。
 * Author: 张雨泽
 */
public class GlobalExceptionFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        try {
            chain.doFilter(request, response);
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (resp.isCommitted()) {
                throw new ServletException(ex);
            }

            req.setAttribute("errorMessage", ex.getMessage() == null ? "系统发生未知异常。" : ex.getMessage());
            req.setAttribute("errorType", ex.getClass().getSimpleName());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            RequestDispatcher dispatcher = req.getRequestDispatcher("/500.jsp");
            dispatcher.forward(req, resp);
        }
    }

    @Override
    public void destroy() {
    }
}