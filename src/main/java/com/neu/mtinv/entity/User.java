package com.neu.mtinv.entity;

import lombok.Data;

@Data
public class User {
    private String id;
    private String username;
    private String password;
    private String realname;
    private String email;
    private String phone;
    private Role role;
    private String role_id;
}
