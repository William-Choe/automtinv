package com.neu.mtinv.mapper;

import com.neu.mtinv.entity.Mtinfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public interface MtinfoMapper {
    List<Mtinfo> getMtinfoByType(String type);

    List<Mtinfo> getMtinfoByTypeAndLimit(String type, String limit);

    Mtinfo getMtinfoById(String id);

    void reMathSet(String result_file, String result_s1, String result_d1,
                   String result_r1, String result_s2, String result_d2,
                   String result_r2, String result_pvr, String id);

    String getResultPathById(String id);

    void deleteMtinfo(String id);

    void insertData(String cataid, String o_time, String lat,
                    String lon, String depth, String m, String location_cname,
                    String dist_min, String dist_max, String filter_min,
                    String filter_max, String m_time, String r_time,
                    String result_path, String result_file, String result_time,
                    String result_lon, String result_lat, String result_depth,
                    String result_m, String result_s1, String result_d1,
                    String result_r1, String result_s2, String result_d2,
                    String result_r2, String result_pvr);

    void insertDataManual(String userid, String o_time, String lat,
                          String lon, String depth, String m,
                          String dist_min, String dist_max, String filter_min,
                          String filter_max, String m_time, String r_time,
                          String result_path, String result_file, String result_time,
                          String result_lon, String result_lat, String result_depth,
                          String result_m, String result_s1, String result_d1,
                          String result_r1, String result_s2, String result_d2,
                          String result_r2, String result_pvr);

    String getLastId();

    void insertDataManualInitial(String userid);

    void updateMultiFilterData(String id, String o_time, String lat, String lon, String depth, String m,
                               String dist_min, String dist_max, String filter_min, String filter_max, String m_time, String r_time,
                               String result_path, String result_file, String result_time, String result_lon, String result_lat, String result_depth,
                               String result_m, String result_s1, String result_d1, String result_r1, String result_s2, String result_d2,
                               String result_r2, String result_pvr, String tar_path, String result_list, String best_result_info);

    void updateBatchData(String id, String o_time, String lat, String lon, String depth, String m,
                               String dist_min, String dist_max, String filter_min, String filter_max, String m_time, String r_time,
                               String result_path, String result_file, String result_time, String result_lon, String result_lat, String result_depth,
                               String result_m, String result_s1, String result_d1, String result_r1, String result_s2, String result_d2,
                               String result_r2, String result_pvr, String root_path, String tar_path, String result_list, String location_cname);

    String checkProgress(String id);

    String checkID(String id);

    Map<String, String> getMultiFilterResult(String id);

    Map<String, String> getBatchResult(String id);
}