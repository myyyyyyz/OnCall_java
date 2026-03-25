package org.example.service;

import org.example.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security 用户详情服务
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUserByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }

        // 构建用户角色列表
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (user.getUserType() != null) {
            // 将 user_type 转换为 Spring Security 角色格式
            String role = "ROLE_" + user.getUserType().toUpperCase();
            authorities.add(new SimpleGrantedAuthority(role));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
