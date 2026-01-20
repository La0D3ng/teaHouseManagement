package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.UsersPrivileges;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserPrivilegesMapper extends BaseMapper<UsersPrivileges> {
    // ------- 权限管理页面 -------
    // 获取管理员总数
    @Select("SELECT COUNT(user_id) FROM userprivileges WHERE role = 'manager'")
    Long getManagerCount();

    // 获取员工总数
    @Select("SELECT COUNT(user_id) FROM userprivileges WHERE role = 'staff'")
    Long getStaffCount();

    // 获取用户总数
    @Select("SELECT COUNT(user_id) FROM userprivileges WHERE role = 'customer'")
    Long getCustomerCount();
    
    // 获取所有用户权限信息
    @Select("SELECT * FROM userprivileges ORDER BY user_id")
    java.util.List<UsersPrivileges> getAllUsersPrivileges();
}
