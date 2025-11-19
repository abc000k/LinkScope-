package com.nageoffer.shortlink.admin.service;

import com.nageoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户响应DTO
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 检查用户名是否不存在（可注册）
     * @param username 用户名
     * @return true-用户名不存在（可注册）；false-用户名已存在
     */
    Boolean hasUsername(String username);

    /**
     * 用户注册
     * @param requestParam 用户注册请求DTO
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 更新用户信息
     * @param requestParam 用户更新请求DTO
     */
    void update(UserUpdateReqDTO requestParam);

    /**
     * 用户登录
     * @param requestParam 用户登录请求DTO
     * @return 登录响应DTO（包含token）
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 检查用户登录状态（token有效性）
     * @param username 用户名
     * @param token 登录令牌
     * @return true-登录有效；false-登录无效
     */
    Boolean checkLogin(String username, String token);

    /**
     * 用户登出
     * @param username 用户名
     * @param token 登录令牌
     */
    void logout(String username, String token);
}