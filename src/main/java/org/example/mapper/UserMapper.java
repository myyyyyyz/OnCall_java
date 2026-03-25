package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.dto.User;

import java.util.List;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper {

    /**
     * 插入用户
     *
     * @param user 用户信息
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 根据 ID 查询用户
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    User selectByPhone(@Param("phone") String phone);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 影响行数
     */
    int update(User user);

    /**
     * 删除用户
     *
     * @param id 用户 ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    List<User> selectAll();

    /**
     * 根据用户类型查询用户
     *
     * @param userType 用户类型
     * @return 用户列表
     */
    List<User> selectByUserType(@Param("userType") String userType);
}
