package com.neu.mtinv.shiro;

import com.neu.mtinv.entity.Role;
import com.neu.mtinv.entity.User;
import com.neu.mtinv.mapper.RoleMapper;
import com.neu.mtinv.service.LoginService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

/*自定义Realm用于查询用户的角色喝权限信息并保存到权限管理器*/
public class CustomRealm extends AuthorizingRealm {
    @Autowired
    private LoginService loginService;
    @Autowired
    private RoleMapper roleMapper;

    //执行授权逻辑
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        String username = (String) principalCollection.getPrimaryPrincipal();
        User user = loginService.findByUsername(username);
        Role role = roleMapper.findRoleByUsername(user.getUsername());

        //添加角色
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.addRole(role.getRole_name());

        return simpleAuthorizationInfo;
    }

    //执行认证逻辑
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //在Post请求时会先认证，再到请求
        if (authenticationToken.getPrincipal() == null) {
            return null;
        }

        //获取用户信息
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String username = token.getUsername();
        User user = loginService.findByUsername(username);
        if (user == null) {
            return null;
        } else {
            //判断密码
            return new SimpleAuthenticationInfo(username, user.getPassword(), getName());
        }
    }
}
