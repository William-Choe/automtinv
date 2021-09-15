package com.neu.mtinv.mapper;

import com.neu.mtinv.entity.Role;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface RoleMapper {
    Role findRoleByUsername(@Param("username") String username);

//    Role findPermissionByRoleId(@Param("roleId") Integer roleId);

    List<Role> getRoles();
}