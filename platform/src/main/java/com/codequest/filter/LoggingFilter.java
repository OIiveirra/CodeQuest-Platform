package com.codequest.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 请求耗时日志过滤器。
 */
public class LoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        long start = System.currentTimeMillis();
        HttpServletRequest req = (HttpServletRequest) request;
        String uri = req.getRequestURI();
        String method = req.getMethod();

        System.out.println("[REQ-START] method=" + method + ", uri=" + uri + ", start=" + start);
        try {
            chain.doFilter(request, response);
        } finally {
            long end = System.currentTimeMillis();
            long cost = end - start;
            System.out.println("[REQ-END] method=" + method + ", uri=" + uri + ", end=" + end + ", costMs=" + cost);
        }
    }

    @Override
    public void destroy() {
    }
}
