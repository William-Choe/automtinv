package com.neu.mtinv.service;

import com.neu.mtinv.entity.User;

public interface LoginService {
    User findByUsername(String username);
}
