package com.hlw.yupipictureend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hlw.yupipictureend.model.dto.user.UserQueryRequest;
import com.hlw.yupipictureend.vo.LoginUserVO;
import com.hlw.yupipictureend.vo.UserVO;
import generator.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lwh
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-02-02 14:31:16
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);


    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    LoginUserVO getLoginUserVO(User user);

    User getLoginUser(HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
}
