package com.invigilation.invigilation.util;

import lombok.Data;
//统一返回结果类
@Data
public class Result<T> {
    private int code;//状态码：200成功，500失败
    private String msg;//提示信息
    private T data;//数据

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }
}