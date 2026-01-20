package com.example.demo.config;

import com.example.demo.entity.Users;
import com.example.demo.mapper.UsersMapper;
import com.example.demo.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * 基于JWT的认证过滤器
 * 从请求头Authorization中获取JWT Token，验证并设置用户权限
 */
@Component
public class RoleBasedAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UsersMapper usersMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 从请求头获取Authorization（格式：Bearer <token>）
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 提取Token
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            
            // 验证Token
            if (token != null && jwtUtil.validateToken(token)) {
                // 从Token中获取用户信息
                Long userId = jwtUtil.getUserIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                
                if (userId != null && role != null) {
                    // 验证用户是否仍然存在且状态正常
                    Users user = usersMapper.selectById(userId);
                    
                    if (user != null && user.getUserStatus() == 1) {
                        // 验证Token中的角色是否与数据库一致（防止权限变更后旧Token仍有效）
                        if (user.getRole().name().equals(role)) {
                            // 将角色转换为Spring Security的权限格式（ROLE_开头）
                            String roleWithPrefix = "ROLE_" + role;
                            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleWithPrefix);
                            
                            // 创建认证对象并设置到SecurityContext中
                            UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(user, null, Collections.singletonList(authority));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
