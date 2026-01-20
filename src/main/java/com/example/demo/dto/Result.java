package com.example.demo.dto;

import lombok.Data;

/**
 * 统一响应结果类
 * @param <T> 返回数据的类型
 */
@Data
public class Result<T> {
    private Integer code;  // 状态码: 200=成功, 400=失败, 401=未授权, 500=服务器错误
    private String message;  // 提示信息
    private T data;  // 返回的数据
    
    // 成功响应(无数据)
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }
    
    // 成功响应(有数据)
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }
    
    // 成功响应(自定义消息)
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    
    // 失败响应
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setMessage(message);
        return result;
    }
    
    // 失败响应(自定义状态码)
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
