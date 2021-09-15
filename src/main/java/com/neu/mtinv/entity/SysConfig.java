package com.neu.mtinv.entity;

import lombok.Data;

@Data
public class SysConfig {
    private String eqim_ip;
    private String eqim_user;
    private String eqim_pass = "";
    private String eqim_sid;
    private String catalog_minE;
    private String catalog_maxE;
    private String catalog_minN;
    private String catalog_maxN;
    private String catalog_minM;
    private String catalog_maxM;
    private String catalog_jg;
    private String show_minE;
    private String show_maxE;
    private String show_minN;
    private String show_maxN;
    private String show_minM;
    private String show_maxM;
    private String show_jg;
    private String seed_bef;
    private String seed_time;
    private String mtinv_minL;
    private String mtinv_maxL;
    private String mtinv_minD;
    private String mtinv_maxD;
    private String eqim_doSend;
    private String eqim_sendM;
    private String eqim_sendIP;
    private String eqim_sendPort;
    private String eqim_sendUser;
    private String eqim_sendPass;
    private String eqim_sendCname;
    private String eqim_sendSname;
    private String cal_wait_time;
}
