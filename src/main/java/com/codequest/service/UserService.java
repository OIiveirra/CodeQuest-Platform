package com.codequest.service;

import java.sql.SQLException;

import com.codequest.dao.UserDAO;
import com.codequest.dto.Result;
import com.codequest.model.User;
import com.codequest.util.MD5Utils;

/**
 * 用户业务服务层，负责登录与注册流程编排。
 * Author: 张雨泽
 */
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public Result<User> login(String username, String rawPassword) {
        String finalUsername = trim(username);
        String finalPassword = trim(rawPassword);
        if (finalUsername.isEmpty() || finalPassword.isEmpty()) {
            return Result.failure(400, "请输入用户名和密码。");
        }

        try {
            // 数据库存储的是 MD5 值，登录时需先做同规则加密。
            User user = userDAO.findByUsernameAndPassword(finalUsername, MD5Utils.md5(finalPassword));
            if (user == null) {
                return Result.failure(401, "用户名或密码错误。");
            }
            return Result.success(user);
        } catch (SQLException ex) {
            return Result.failure(500, "登录失败，请稍后重试。");
        }
    }

    public Result<Void> register(String username, String rawPassword) {
        String finalUsername = trim(username);
        String finalPassword = trim(rawPassword);
        if (finalUsername.isEmpty() || finalPassword.isEmpty()) {
            return Result.failure(400, "用户名和密码不能为空。");
        }

        try {
            // 注册前先校验用户名唯一性，避免数据库唯一约束异常。
            if (userDAO.existsByUsername(finalUsername)) {
                return Result.failure(409, "用户名已存在，请更换一个用户名。");
            }
            userDAO.createUser(finalUsername, MD5Utils.md5(finalPassword));
            return Result.success(null);
        } catch (SQLException ex) {
            return Result.failure(500, "注册失败，请稍后重试。");
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
