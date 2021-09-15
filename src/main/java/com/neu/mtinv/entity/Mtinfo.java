package com.neu.mtinv.entity;

import lombok.Data;

@Data
public class Mtinfo {
    private String id;
    private String type;
    private String userid;
    private String cataid;
    private String o_time;
    private String lat;
    private String lon;
    private String depth;
    private String m;
    private String location_cname;
    private String dist_min;
    private String dist_max;
    private String filter_min;
    private String filter_max;
    private String m_time;
    private String r_time;
    private String re_time;
    private String result_path;
    private String result_file;
    private String result_time;
    private String result_lon;
    private String result_lat;
    private String result_depth;
    private String result_m;
    private String result_s1;
    private String result_d1;
    private String result_r1;
    private String result_s2;
    private String result_d2;
    private String result_r2;
    private String result_pvr;
    private String CENC_flag;
    private String CENC_time;
    private String math_time;
    private String jg = " ";
    private String root_path;
    private String tar_path;
    private String result_list;
    private String best_result_info;
}
