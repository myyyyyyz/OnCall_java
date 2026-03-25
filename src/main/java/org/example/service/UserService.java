package org.example.service;

import org.example.dto.User;
import org.example.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 创建的用户
     */
    @Transactional
    public User createUser(User user) {
        userMapper.insert(user);
        return user;
    }

    /**
     * 根据 ID 查询用户
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    public User getUserByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 是否更新成功
     */
    @Transactional
    public boolean updateUser(User user) {
        return userMapper.update(user) > 0;
    }

    /**
     * 删除用户
     *
     * @param id 用户 ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean deleteUser(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }

    /**
     * 根据用户类型查询用户
     *
     * @param userType 用户类型
     * @return 用户列表
     */
    public List<User> getUsersByUserType(String userType) {
        return userMapper.selectByUserType(userType);
    }
}
