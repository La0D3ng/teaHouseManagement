package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private RoleBasedAuthFilter roleBasedAuthFilter;

    /**
     * Spring Security 配置，实现基于角色的权限控制
     * 
     * 权限说明：
     * - manager: 管理员，拥有最高权限
     * - staff: 员工，可以管理房间信息
     * - customer: 客户，可以预订和评价
     * 
     * 认证方式：通过JWT Token识别用户身份
     * - 前端登录成功后会收到JWT Token
     * - 后续请求需在Header中携带：Authorization: Bearer <token>
     * - Token包含用户ID、角色等信息，经过加密签名，无法伪造
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // 禁用 CSRF 保护，便于测试
            .cors(cors -> {})  // 启用 CORS 支持，使用 CorsFilter Bean 配置
            .authorizeHttpRequests(authz -> authz
                // Swagger文档访问（无需认证）
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html"
                ).permitAll()
                
                // 静态资源访问（无需认证）
                .requestMatchers("/avatars/**", "/rooms/images/**").permitAll()
                
                // 所有人都可以访问的接口
                .requestMatchers(
                    "/users/login",
                    "/users/changePassword",
                    "/users/sendCode",
                    "/users/forgetPassword",
                    "/users/updateAvatarUrl",
                    "/users/getUserInfo/{userId}",
                    "/rooms/getRoomTotal",
                    "/rooms/getAvalibleRoomTotal",
                    "/rooms/getOccupiedRoomToal",
                    "/rooms/getAllRooms",
                    "/reservations/getCurrentMonthTotalReservations",
                    "/reservations/getTodayTotalReservations",
                    "/reservations/checkTimeSlot",
                    "/reservations/getTimeSlotStatus/{roomId}",
                    "/reservations/getAvailableRoomInfo/{roomId}",
                    "/reservations/getReservationsInfo",
                    "/roomReviews/showAllReviews/{userId}",
                    "/reservations/searchAvailableRooms",
                    "/users/uploadAvatar"
                ).permitAll()
                
                // 仅customer可以注册和取消订单
                .requestMatchers("/users/register").permitAll()
                
                // 仅manager可以访问
                .requestMatchers(
                    "/users/updatePrivileges",
                    "/usersPrivileges/getUserPrivilegesInfo/{userId}",
                    "/rooms/addRooms",
                    "/rooms/deleteRooms",
                    "/usersPrivileges/getAllUsersPrivileges"
                ).hasRole("manager")
                
                // manager和staff可以访问
                .requestMatchers(
                    "/rooms/updateRooms",
                    "/rooms/updateRoomStatus",
                    "/rooms/uploadRoomImage"
                ).hasAnyRole("manager", "staff")
                
                // 仅customer可以访问
                .requestMatchers(
                    "/reservations/addReservation",
                    "/reservations/updateReservation",
                    "/reservations/updateReservationStatus",
                    "/roomReviews/addReview",
                    "/roomReviews/deleteReview/{reviewId}",
                    "/roomReviews/updateReview",
                    "/reservations/cancelReservation"
                ).hasRole("customer")
                
                .anyRequest().authenticated()  // 其他请求需要认证
            )
            .addFilterBefore(roleBasedAuthFilter, UsernamePasswordAuthenticationFilter.class)  // 添加自定义认证过滤器
            .httpBasic(httpBasic -> httpBasic.disable())  // 禁用 HTTP Basic 认证
            .formLogin(form -> form.disable());  // 禁用表单登录
            
        return http.build();
    }
}
