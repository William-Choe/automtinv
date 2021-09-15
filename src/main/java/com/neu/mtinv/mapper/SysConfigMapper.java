package com.neu.mtinv.mapper;

import com.neu.mtinv.entity.SysConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Component
public interface SysConfigMapper {
    SysConfig getSysconfig();

    void setEqimDB(@Param("ip") String ip, @Param("user") String user,
                   @Param("pass") String pass, @Param("sid") String sid);

    void setEqimCatalog(@Param("mine") String mine, @Param("maxe") String maxe,
                        @Param("minn") String minn, @Param("maxn") String maxn,
                        @Param("minm") String minm, @Param("maxm") String maxm,
                        @Param("jg") String jg);

    void setEqimShow(@Param("mine") String mine, @Param("maxe") String maxe,
                     @Param("minn") String minn, @Param("maxn") String maxn,
                     @Param("minm") String minm, @Param("maxm") String maxm,
                     @Param("jg") String jg);

    void setEqimSendSwitch(@Param("doSend") String doSend);

    void setEqimSendPara(@Param("send_m") String send_m, @Param("send_ip") String send_ip,
                         @Param("send_port") String send_port, @Param("send_user") String send_user,
                         @Param("send_pass") String send_pass, @Param("send_cname") String send_cname,
                         @Param("send_sname") String send_sname);

    void setSeedConfig(@Param("sbef") String sbef, @Param("st") String st);

    void setMtConfig(@Param("minl") String minl, @Param("maxl") String maxl,
                     @Param("mind") String mind, @Param("maxd") String maxd);

    void setCalWaitTime(@Param("cal_wait_time") String cal_wait_time);

    void updateShowMsg(@Param("showMsg") String showMsg);

    String getShowMsg();
}
