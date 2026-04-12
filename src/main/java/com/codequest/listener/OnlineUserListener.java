package com.codequest.listener;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * 在线人数监听器，用于维护应用级在线会话数。
 * Author: 张雨泽
 */
public class OnlineUserListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        ServletContext context = se.getSession().getServletContext();
        // 多会话并发创建时，使用同步块避免在线人数丢计数。
        synchronized (context) {
            Object value = context.getAttribute("onlineCount");
            int current = value instanceof Integer ? (Integer) value : 0;
            context.setAttribute("onlineCount", current + 1);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        ServletContext context = se.getSession().getServletContext();
        // 销毁场景下保证计数不小于 0，防止出现负数。
        synchronized (context) {
            Object value = context.getAttribute("onlineCount");
            int current = value instanceof Integer ? (Integer) value : 0;
            int next = Math.max(0, current - 1);
            context.setAttribute("onlineCount", next);
        }
    }
}
