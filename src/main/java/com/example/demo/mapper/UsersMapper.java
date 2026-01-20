package com.example.demo.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Users;
import org.apache.ibatis.annotations.*;

// Mapper 层负责与数据库进行交互，核心依赖 MyBatis 注解与 Spring 扫描注解，无需 xml 配置
// 把 springboot 获得的属性类再绑定给到 mybatis-plus ，然后绑定的这个接口给其他层调用
@Mapper
public interface UsersMapper extends BaseMapper<Users> {
    // ------- 空闲包间页面 -------
    // 展示空闲包间总数 -- 前面有，直接复用
    // 这里的功能实现均较为复杂，放在其他层创建查询。
}

/*
 * @Mapper：注解是 MyBatis 框架中的一个重要标识，
 * 它定义了 Mapper 接口，用于与数据库交互。使用这个注解可以简化数据库操作代码，并提供一些优势，
 * 如自动生成 SQL、类型安全性等。
 * */
