package com.neu.mtinv.entity;

import java.io.Serializable;

public class Response implements Serializable {
    private int status; //状态码，200：成功，500：失败
    private String msg; //描述信息
    private Object data; //服务端数据

    public static Response isSuccess() {
        return new Response().status(200).msg("Success!");
    }

    public static Response isFail() {
        return new Response().status(500).msg("Fail!");
    }

    public Response msg(String msg){
        this.setMsg(msg);
        return this;
    }

    public Response msg(Throwable e) {
        this.setMsg(e.toString());
        return this;
    }

    public Response data(Object data) {
        this.setData(data);
        return this;
    }

    public Response status(int status) {
        this.setStatus(status);
        return this;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}