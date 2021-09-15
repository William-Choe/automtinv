package com.neu.mtinv.entity;

import lombok.Data;

@Data
public class Do {
    private String lon;
    private String lat;
    private String depth;
    private String time;
    private String magnitude;
    private String distance_min;
    private String distance_max;
    private String filter_min;
    private String filter_max;
    private String institution;
    private String location;
    private String seedName;
    private String userId;
    private String filters;
}
