package com.neu.mtinv.mapper;

import com.neu.mtinv.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface UserMapper {
    User findByUsername(@Param("username") String username);

    String usernameExist(String username);

    void addUser(String username, String password, String realname, String email, String phone);

    void addUserRole(String user_id, String role_id);

    List<User> getUsers();

    void deleteUser(String user_id);

    void deleteUserRole(String user_id);
}
