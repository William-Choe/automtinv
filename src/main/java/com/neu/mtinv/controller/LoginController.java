package com.neu.mtinv.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.neu.mtinv.entity.User;
import com.neu.mtinv.entity.Response;
import com.neu.mtinv.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
public class LoginController {
    @Resource
    private LoginService loginService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Response login(@RequestBody User user) {
        //添加用户认证信息
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(user.getUsername(), user.getPassword());

        try {
            subject.login(usernamePasswordToken);
        } catch (AuthenticationException e) {
            log.warn("user：" + user.getUsername() + " username/password is wrong");
            return Response.isFail().msg("username/password is wrong");
        } catch (AuthorizationException e) {
            log.warn("user：" + user.getUsername() + " no permission");
            return Response.isFail().msg("no permission");
        }

        log.info("user：" + user.getUsername() + " login successful");
        return Response.isSuccess().data(loginService.findByUsername(user.getUsername()));
    }

    @RequestMapping(value = "/code", method = RequestMethod.GET)
    public Response getCode(HttpServletResponse response, HttpSession session){
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100,4,20);
        session.setAttribute("code", lineCaptcha.getCode());

        try {
            ServletOutputStream outputStream = response.getOutputStream();
            lineCaptcha.write(outputStream);
            outputStream.close();
        } catch (IOException e) {
            return Response.isFail().msg(e.toString());
        }

        return Response.isSuccess();
    }

    @RequestMapping(value = "/code/verify", method = RequestMethod.POST)
    public Response verifyCode(@RequestBody Map<String, String> params, HttpSession session) {
        String code = params.get("code").toLowerCase();
        String sessionCheckCode = (String) session.getAttribute("code");

        if (sessionCheckCode.toLowerCase().equals(code)){
            return Response.isSuccess().msg("Verification code is correct!");
        }else {
            return Response.isFail().msg("Verification code is wrong!");
        }
    }
}
