package com.codequest.dao;

import java.sql.Connection;
import java.sql.SQLException;

import com.codequest.util.JDBCUtils;

/**
 * DAO 基类，统一提供数据库连接获取能力。
 * Author: 张雨泽
 */
public abstract class BaseDAO {

    protected Connection getConnection() throws SQLException {
        return JDBCUtils.getConnection();
    }
}