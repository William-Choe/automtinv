package com.neu.mtinv.service.impl;

import com.neu.mtinv.entity.User;
import com.neu.mtinv.mapper.UserMapper;
import com.neu.mtinv.service.LoginService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LoginServiceImpl implements LoginService {
    @Resource
    private UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
}
