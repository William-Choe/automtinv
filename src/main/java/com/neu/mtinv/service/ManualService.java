package com.neu.mtinv.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface ManualService {
    String upload(MultipartFile file) throws IOException;

    void uploadMulti(MultipartFile[] files) throws IOException;

    String uploadPara(MultipartFile file) throws IOException;

    Map<String, String> manualDo(HttpServletRequest request, String lon, String lat, String depth, String time, String magnitude,
                                 String distance_min, String distance_max, String filter_min, String filter_max, String seedName, String userId) throws Exception;

    Map<String, Map<String, String>> manualDoBatch(String paraFilePath, String userId) throws Exception;

    Map<String, String> manualDoMultiFilter(HttpServletRequest request, String lon, String lat, String depth, String time, String magnitude,
                                            String distance_min, String distance_max, String filters, String seedName, String userId) throws Exception;

    String manualDoMultiFilterStart(String lon, String lat, String depth, String time, String magnitude,
                                    String distance_min, String distance_max, String filters, String seedName, String userId) throws Exception;

    Map<String, String> manualDoMultiFilterResult(String mtinv_id);

    Map<String, String> manualDoBatchStart(String paraFilePath, String userId) throws Exception;

    Map<String, String> manualDoBatchResult(String mtinv_id);

    String manualDoCheckProgress(String mtinv_id);

    void downloadResult(HttpServletResponse response, String filePath) throws IOException;

    void downloadResultBatch(HttpServletResponse response, String files) throws IOException;

    String getStation(String rootPath);

    Map<String, String> reMath(String rootPath, String id, String uncheckedStation, String checkedStation, String filter_min, String filter_max);
}
