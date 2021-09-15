package com.neu.mtinv.service;

import java.util.Map;

public interface AsyncService {
    void runMultiFilterTask(String mtinv_id, String lon, String lat, String depth, String time, String magnitude, String distance_min, String distance_max, String filters, String seedName) throws Exception;

    void runBatchTask(Map<String, Map<String, String>> paras, Map<String, String> resultMap) throws Exception;
}
