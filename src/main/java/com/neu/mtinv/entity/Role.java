package com.neu.mtinv.entity;

import lombok.Data;

import java.util.List;

@Data
public class Role {
    private String id;
    private String role_name;
    private List<Permission> permissions;
}
