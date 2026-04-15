package com.codequest.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.codequest.util.JDBCUtils;

/**
 * 应用启动监听器，负责初始化和销毁全局数据库连接池。
 */
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        JDBCUtils.initDataSource();
        printStartupBanner();
        System.out.println("[CodeQuest] Database connection pool initialized successfully.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JDBCUtils.closeDataSource();
        System.out.println("[CodeQuest] Database connection pool closed safely.");
    }

    private void printStartupBanner() {
        System.out.println("""

   ###############################################################
   ##                                                           ##
   ##                 CODEQUEST SYSTEM STARTED                  ##
   ##                                                           ##
   ###############################################################

                     CodeQuest System Started

                """);
    }
}