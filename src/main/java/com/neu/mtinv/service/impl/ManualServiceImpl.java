package com.neu.mtinv.service.impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.neu.mtinv.mapper.MtinfoMapper;
import com.neu.mtinv.service.AsyncService;
import com.neu.mtinv.service.ManualService;
import com.neu.mtinv.util.Mtinv;
import com.neu.mtinv.util.MtinvFactory;
import com.neu.mtinv.util.ReMath;
import com.neu.mtinv.util.RunSH;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class ManualServiceImpl implements ManualService {
    @Resource
    private MtinfoMapper mtinfoMapper;
    @Resource
    private ReMath reMath;
    @Resource
    private Mtinv mtinv;
    @Resource
    private Gson gson;
    @Resource
    private AsyncService asyncService;
    @Resource
    private MtinvFactory mtinvFactory;
    @Resource
    private RunSH runsh;

    /*单文件上传*/
    @Override
    public String upload(MultipartFile file) throws IOException {
        String computePath = "/autoMTInv/compute/";
        String fileName= file.getOriginalFilename();
        String[] fileNameArr = fileName.split("\\.");

        if (!fileNameArr[fileNameArr.length - 1].toLowerCase().equals("seed")) {
            throw new IOException("上传失败，请上传seed类型文件");
        }

        new File(computePath).mkdir();
        File dest = new File(computePath + fileName);
        file.transferTo(dest);

        return fileName;
    }

    /*多文件上传*/
    @Override
    public void uploadMulti(MultipartFile[] files) throws IOException {
        String computePath = "/autoMTInv/compute/";

        for (MultipartFile file : files) {
            String fileName= file.getOriginalFilename();
            String[] fileNameArr = fileName.split("\\.");

            if (!fileNameArr[fileNameArr.length - 1].toLowerCase().equals("seed")) {
                throw new IOException("上传失败，请上传seed类型文件");
            }

            new File(computePath).mkdir();
            File dest = new File(computePath + fileName);
            file.transferTo(dest);
        }
    }

    /*批量计算参数文件上传*/
    @Override
    public String uploadPara(MultipartFile file) throws IOException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = "para-" + df.format(new Date()) + ".json";
        String filePath = "/autoMTInv/compute/" + fileName;

        // 读取txt文件，将参数提取出来存至map
        Map<String, Map<String, String>> jsonPara = new LinkedHashMap<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String lineTxt;
        while ((lineTxt = br.readLine()) != null) {
            if (lineTxt.startsWith("#")) {
                continue;
            }

            String[] paras = lineTxt.split("\\s+");
            String event_id = paras[0];
            Map<String, String> event_para = new LinkedHashMap<>();
            event_para.put("经度", paras[3]);
            event_para.put("纬度", paras[4]);
            event_para.put("深度", paras[5]);
            event_para.put("时间", paras[6] + " " + paras[7]);
            event_para.put("震级", paras[8]);
            event_para.put("最小震中距", paras[9]);
            event_para.put("最大震中距", paras[10]);
            event_para.put("最小滤波参数", paras[11]);
            event_para.put("最大滤波参数", paras[12]);
            event_para.put("机构名", paras[2]);
            event_para.put("震中位置", paras[1]);
            event_para.put("Seed文件名", paras[13]);
            jsonPara.put(event_id, event_para);
        }
        br.close();

        // 将参数转存为json文件
        String jsonFileContent = gson.toJson(jsonPara);
        BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
        bw.write(jsonFileContent);
        bw.close();
        return filePath;
    }

    /*单次手动计算*/
    @Override
    public Map<String, String> manualDo(HttpServletRequest request, String lon, String lat, String depth, String time, String magnitude,
                                        String distance_min, String distance_max, String filter_min, String filter_max, String seedName, String userId) throws Exception {

        Map<String, String> returnMap = new HashMap<>();
        try {
            Map<String, String> map = mtinv.doMath_manual(request, lon, lat, depth, time, magnitude, distance_min, distance_max, filter_min, filter_max, seedName);
            mtinfoMapper.insertDataManual(userId, map.get("time"), map.get("lat"), map.get("lon"),
                    map.get("depth"), map.get("magnitude"), map.get("distance_min"), map.get("distance_max"), map.get("filter_min"),
                    map.get("filter_max"), map.get("upTimeTemp"), map.get("endTimeTemp"), map.get("resultPath"),
                    map.get("result_file"), map.get("result_datetime"), map.get("result_lon"), map.get("result_lat"),
                    map.get("result_depth"), map.get("result_m"), map.get("result_s1"), map.get("result_r1"),
                    map.get("result_d1"), map.get("result_s2"), map.get("result_r2"), map.get("result_d2"), map.get("result_pvr"));
            String mtinv_id = mtinfoMapper.getLastId();

            returnMap.put("tarPath", map.get("tarPath"));
            returnMap.put("resultList", map.get("resultList"));
            returnMap.put("mtinv_id", mtinv_id);
            returnMap.put("rootPath", map.get("rootPath"));
        } catch (Exception e) {
            log.error("error occurs: ", e);
            throw new IOException("计算失败！");
        }
        return returnMap;
    }

    /*批量计算，同步计算*/
    @Override
    public Map<String, Map<String, String>> manualDoBatch(String paraFilePath, String userId) throws Exception {
        // 读取参数配置文件
        JSON paraJson = JSONUtil.readJSON(new File(paraFilePath), StandardCharsets.UTF_8);
        String paraJsonStr = JSONUtil.toJsonStr(paraJson);
        Map<String, Map<String, String>> paras = gson.fromJson(paraJsonStr, Map.class);
        Map<String, Map<String, String>> resultMap = new LinkedHashMap<>();

        // 获取compute路径下全部seed文件名
        File seedFile = new File("/autoMTInv/compute/");
        String[] files = seedFile.list();
        StringBuilder seedFiles = new StringBuilder();
        if (files != null) {
            for (String file : files) {
                if (file.toLowerCase().endsWith(".seed")) {
                    seedFiles.append(file).append(";");
                }
            }
        }

        // 检查参数文件中Seed文件名是否存在
        for (Map<String, String> value : paras.values()) {
            String seedName = value.get("Seed文件名");
            if (!seedFiles.toString().contains(seedName)) {
                log.error(seedName + "文件不存在，请确认是否上传该文件或参数文件是否填写错误！");
                throw new IOException(seedName + "文件不存在，请确认是否上传该文件或参数文件是否填写错误！");
            }
        }

        // 批量计算
        for (Map.Entry<String, Map<String, String>> entry : paras.entrySet()) {
            String event = entry.getKey();
            Map<String, String> value = entry.getValue();
            log.info("开始计算事件：" + event);

            String lon = value.get("经度");
            String lat = value.get("纬度");
            String depth = value.get("深度");
            String time = value.get("时间");
            String magnitude = value.get("震级");
            String distance_min = value.get("最小震中距");
            String distance_max = value.get("最大震中距");
            String filter_min = value.get("最小滤波参数");
            String filter_max = value.get("最大滤波参数");
            String institution = value.get("机构");
            String location = value.get("震中位置");
            String seedName = value.get("Seed文件名");
            Map<String, String> result = mtinv.doMath_manual_batch(lon, lat, depth, time, magnitude, distance_min, distance_max, filter_min, filter_max, seedName);

            mtinfoMapper.insertDataManual(userId, result.get("time"), result.get("lat"), result.get("lon"),
                    result.get("depth"), result.get("magnitude"), result.get("distance_min"), result.get("distance_max"), result.get("filter_min"),
                    result.get("filter_max"), result.get("upTimeTemp"), result.get("endTimeTemp"), result.get("resultPath"),
                    result.get("result_file"), result.get("result_datetime"), result.get("result_lon"), result.get("result_lat"),
                    result.get("result_depth"), result.get("result_m"), result.get("result_s1"), result.get("result_r1"),
                    result.get("result_d1"), result.get("result_s2"), result.get("result_r2"), result.get("result_d2"), result.get("result_pvr"));
            String mtinv_id = mtinfoMapper.getLastId();

            Map<String, String> map = new HashMap<>();
            map.put("tarPath", result.get("tarPath"));
            map.put("resultList", result.get("resultList"));
            map.put("mtinv_id", mtinv_id);
            map.put("rootPath", result.get("rootPath"));
            resultMap.put(event, map);
        }
        return resultMap;
    }

    /*多套滤波参数计算，同步计算*/
    @Override
    public Map<String, String> manualDoMultiFilter(HttpServletRequest request, String lon, String lat, String depth, String time, String magnitude,
                                                   String distance_min, String distance_max, String filters, String seedName, String userId) throws Exception {
        Map<String, String> map = mtinv.doMath_manual_batch(lon, lat, depth, time, magnitude, distance_min, distance_max, filters.split("/"), seedName);
        mtinfoMapper.insertDataManual(userId, map.get("time"), map.get("lat"), map.get("lon"),
                map.get("depth"), map.get("magnitude"), map.get("distance_min"), map.get("distance_max"), map.get("filter_min"),
                map.get("filter_max"), map.get("upTimeTemp"), map.get("endTimeTemp"), map.get("resultPath"),
                map.get("result_file"), map.get("result_datetime"), map.get("result_lon"), map.get("result_lat"),
                map.get("result_depth"), map.get("result_m"), map.get("result_s1"), map.get("result_r1"),
                map.get("result_d1"), map.get("result_s2"), map.get("result_r2"), map.get("result_d2"), map.get("result_pvr"));
        String mtinv_id = mtinfoMapper.getLastId();

        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("tarPath", map.get("tarPath"));
        returnMap.put("resultList", map.get("resultList"));
        returnMap.put("mtinv_id", mtinv_id);
        returnMap.put("bestResultInfo", map.get("bestResultInfo"));
        return returnMap;
    }

    /*检查计算进度*/
    @Override
    public String manualDoCheckProgress(String mtinv_id) {
        //若计算失败，这一条目数据将会被删除，也就没有此ID了
        String checkID = mtinfoMapper.checkID(mtinv_id);
        if (checkID.equals("0")) {
            return "-1";
        }

        String status = mtinfoMapper.checkProgress(mtinv_id);
        return status == null ? "0" : "1";
    }

    /*多套滤波参数计算，异步调用*/
    @Override
    public String manualDoMultiFilterStart(String lon, String lat, String depth, String time, String magnitude, String distance_min, String distance_max, String filters, String seedName, String userId) throws Exception {
        // 向数据表中插入初始数据
        mtinfoMapper.insertDataManualInitial(userId);
        // 获取上一步插入行mtinv_id
        String mtinv_id = mtinfoMapper.getLastId();
        // 异步调用计算线程
        asyncService.runMultiFilterTask(mtinv_id, lon, lat, depth, time, magnitude, distance_min, distance_max, filters, seedName);

        return mtinv_id;
    }

    /*多套滤波参数计算，获取结果*/
    @Override
    public Map<String, String> manualDoMultiFilterResult(String mtinv_id) {
        return mtinfoMapper.getMultiFilterResult(mtinv_id);
    }

    /*批量计算，异步调用*/
    @Override
    public Map<String, String> manualDoBatchStart(String paraFilePath, String userId) throws Exception {
        // 读取参数配置文件
        JSON paraJson = JSONUtil.readJSON(new File(paraFilePath), StandardCharsets.UTF_8);
        String paraJsonStr = JSONUtil.toJsonStr(paraJson);
        Map<String, Map<String, String>> paras = gson.fromJson(paraJsonStr, Map.class);
        Map<String, String> resultMap = new LinkedHashMap<>();

        // 获取compute路径下全部seed文件名
        File seedFile = new File("/autoMTInv/compute/");
        String[] files = seedFile.list();
        StringBuilder seedFiles = new StringBuilder();
        if (files != null) {
            for (String file : files) {
                if (file.toLowerCase().endsWith(".seed")) {
                    seedFiles.append(file).append(";");
                }
            }
        }

        // 检查参数文件中Seed文件名是否存在
        for (Map<String, String> value : paras.values()) {
            String seedName = value.get("Seed文件名");
            if (!seedFiles.toString().contains(seedName)) {
                log.error(seedName + "文件不存在，请确认是否上传该文件或参数文件是否填写错误！");
                throw new IOException(seedName + "文件不存在，请确认是否上传该文件或参数文件是否填写错误！");
            }
        }

        // 将各个事件对应的mtinv_id存入resultMap
        for (String event: paras.keySet()) {
            mtinfoMapper.insertDataManualInitial(userId);
            String mtinv_id = mtinfoMapper.getLastId();
            resultMap.put(event, mtinv_id);
        }

        // 异步调用批量计算线程
        asyncService.runBatchTask(paras, resultMap);
        return resultMap;
    }

    /*批量计算，获取结果*/
    @Override
    public Map<String, String> manualDoBatchResult(String mtinv_id) {
        return mtinfoMapper.getBatchResult(mtinv_id);
    }

    /*下载计算结果*/
    @Override
    public void downloadResult(HttpServletResponse response, String filePath) throws IOException {
        response.setContentType("text/html");

        File file = new File(filePath);
        if (!file.exists()) {
            log.error(file.getAbsolutePath() + " 文件不存在!");
            throw new IOException(file.getAbsolutePath() + " 文件不存在！");
        }

        FileInputStream fileInputStream;
        ServletOutputStream ou;

        fileInputStream = new FileInputStream(file);
        ou = response.getOutputStream();
        if (filePath.length() > 0) {
            response.setContentType("application/x-msdownload");
            response.setHeader("Content-Disposition", "attachment; filename=result.tar");

            int fileLen = fileInputStream.available();
            byte[] a = new byte[fileLen];
            fileInputStream.read(a);
            ou.write(a);
            fileInputStream.close();
            ou.close();
        }
    }

    /*批量下载*/
    @Override
    public void downloadResultBatch(HttpServletResponse response, String files) throws IOException {
        String[] fileList = files.split(",");
        String timeStr = mtinvFactory.create_batch_tar(fileList);
        String command = "bash /autoMTInv/compute/tmp/" + timeStr + ".sh";
        runsh.rsh(command);
        String filePath = "/autoMTInv/compute/tmp/" + timeStr + ".tar";
        downloadResult(response, filePath);
    }

    /*获取某一次计算参与的站台*/
    @Override
    public String getStation(String rootPath) {
        return reMath.getStations(rootPath);
    }

    /*更改计算参数，重新计算*/
    @Override
    public Map<String, String> reMath(String rootPath, String id, String uncheckedStation, String checkedStation, String filter_min, String filter_max) {
        String fileTime;
        String[] rootPathA = rootPath.split("/");
        String fileTimeS = rootPathA[rootPathA.length - 1];
        fileTime = fileTimeS.split("_")[0];

        reMath.remath(rootPath, checkedStation, uncheckedStation, filter_min, filter_max, id);

        String resultList = "";
        String resultPath = rootPath + "MTINV/result/";
        File resultFile = new File(resultPath);
        String[] rf = resultFile.list();
        Arrays.sort(rf);
        for (String s : rf) {
            if (s.substring(s.length() - 3).equals("jpg")) {
                resultList = resultList + resultPath + s + ",";
            }
        }
        if (resultList.length() > 0) {
            resultList = resultList.substring(0, resultList.length() - 1);
        }
        String tarPath = rootPath + "MTINV/" + fileTime + ".tar";

        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("resultList", resultList);
        returnMap.put("tar", tarPath);
        return returnMap;
    }
}
