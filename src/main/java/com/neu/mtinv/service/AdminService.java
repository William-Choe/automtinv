package com.neu.mtinv.service;

import com.neu.mtinv.entity.Mtinfo;
import com.neu.mtinv.entity.Role;
import com.neu.mtinv.entity.SysConfig;
import com.neu.mtinv.entity.User;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface AdminService {
    SysConfig getSysConfig();

    void setEqimDB(String ip, String user, String pass, String sid);

    void setEqimCatalog(String mine, String maxe, String minn, String maxn, String minm, String maxm, String jg);

    void setEqimShow(String mine, String maxe, String minn, String maxn, String minm, String maxm, String jg);

    void setEqimSendSwitch(String doSend);

    void setEqimSendPara(String send_m, String send_ip, String send_port, String send_user, String send_pass, String send_cname, String sned_sname);

    void setSeedConfig(String sbef, String st);

    void setMtConfig( String minl, String maxl, String mind, String maxd);

    void setCalWaitTime(String cal_wait_time);

    List<Mtinfo> getMtinfo(String type);

    String uploadAuto(MultipartFile file) throws IOException;

    Map<String, String> autoDo(String jd, String wd, String sd, String tt, String m,
                                 String distance_s, String distance_e, String lb_s, String lb_e, String jg, String location, String seedName) throws Exception;

    void deleteCatalog(String id);

    void busuan(String catalog_id) throws Exception;

    List<Map> getCatalogBS();

    List<Role> getRoles();

    List<User> getUsers();

    boolean addUser(String username, String password, String realname, String email, String phone, String role_id) throws Exception;

    void deleteUser(String user_id);

    void updateShowMsg(String showMsg);

    String getShowMsg();
}
