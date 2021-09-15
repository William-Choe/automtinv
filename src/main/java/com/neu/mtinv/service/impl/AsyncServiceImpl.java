package com.neu.mtinv.service.impl;


import com.neu.mtinv.mapper.MtinfoMapper;
import com.neu.mtinv.service.AsyncService;
import com.neu.mtinv.util.Mtinv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class AsyncServiceImpl implements AsyncService {
    @Resource
    private Mtinv mtinv;
    @Resource
    private MtinfoMapper mtinfoMapper;

    @Async
    @Override
    public void runMultiFilterTask(String mtinv_id, String lon, String lat, String depth, String time, String magnitude, String distance_min, String distance_max, String filters, String seedName) throws Exception {
        try {
            Map<String, String> map = mtinv.doMath_manual_batch(lon, lat, depth, time, magnitude, distance_min, distance_max, filters.split("/"), seedName);
            mtinfoMapper.updateMultiFilterData(mtinv_id, map.get("time"), map.get("lat"), map.get("lon"),
                    map.get("depth"), map.get("magnitude"), map.get("distance_min"), map.get("distance_max"), map.get("filter_min"),
                    map.get("filter_max"), map.get("upTimeTemp"), map.get("endTimeTemp"), map.get("resultPath"),
                    map.get("result_file"), map.get("result_datetime"), map.get("result_lon"), map.get("result_lat"),
                    map.get("result_depth"), map.get("result_m"), map.get("result_s1"), map.get("result_r1"),
                    map.get("result_d1"), map.get("result_s2"), map.get("result_r2"), map.get("result_d2"), map.get("result_pvr"),
                    map.get("tarPath"), map.get("resultList"), map.get("bestResultInfo"));
        } catch (Exception e) {
            log.error("error occurs: ", e);
            mtinfoMapper.deleteMtinfo(mtinv_id);
            throw new IOException("计算失败！");
        }
    }

    @Async
    @Override
    public void runBatchTask(Map<String, Map<String, String>> paras, Map<String, String> resultMap) throws Exception {
        for (Map.Entry<String, Map<String, String>> entry : paras.entrySet()) {
            String event = entry.getKey();
            String mtinv_id = resultMap.get(event);
            Map<String, String> value = entry.getValue();
            log.info("Start Computing Event：" + event);

            String lon = value.get("经度");
            String lat = value.get("纬度");
            String depth = value.get("深度");
            String time = value.get("时间");
            String magnitude = value.get("震级");
            String distance_min = value.get("最小震中距");
            String distance_max = value.get("最大震中距");
            String filter_min = value.get("最小滤波参数");
            String filter_max = value.get("最大滤波参数");
            String location_cname = value.get("震中位置");
            String seedName = value.get("Seed文件名");

            Map<String, String> map;
            try {
                map = mtinv.doMath_manual_batch(lon, lat, depth, time, magnitude, distance_min, distance_max, filter_min, filter_max, seedName);
            } catch (Exception e) {
                mtinfoMapper.deleteMtinfo(mtinv_id);
                continue;
            }
            mtinfoMapper.updateBatchData(mtinv_id, map.get("time"), map.get("lat"), map.get("lon"),
                    map.get("depth"), map.get("magnitude"), map.get("distance_min"), map.get("distance_max"), map.get("filter_min"),
                    map.get("filter_max"), map.get("upTimeTemp"), map.get("endTimeTemp"), map.get("resultPath"),
                    map.get("result_file"), map.get("result_datetime"), map.get("result_lon"), map.get("result_lat"),
                    map.get("result_depth"), map.get("result_m"), map.get("result_s1"), map.get("result_r1"),
                    map.get("result_d1"), map.get("result_s2"), map.get("result_r2"), map.get("result_d2"), map.get("result_pvr"),
                    map.get("rootPath"), map.get("tarPath"), map.get("resultList"), location_cname);
        }
    }
}
