package com.neu.mtinv.controller;

import com.neu.mtinv.entity.Do;
import com.neu.mtinv.entity.ReMath;
import com.neu.mtinv.service.ManualService;
import com.neu.mtinv.entity.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/manual")
public class ManualController {
    @Resource
    private ManualService manualService;

    /*单文件上传*/
    @RequiresRoles(value = {"admin", "user"}, logical = Logical.OR)
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public Response upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Response.isFail().msg("上传失败，请选择文件");
        }
        String filePath = manualService.upload(file);
        return Response.isSuccess().data(filePath);
    }

    /*多文件上传*/
    @RequiresRoles(value = {"admin", "user"}, logical = Logical.OR)
    @RequestMapping(value = "/upload/multi", method = RequestMethod.POST)
    public Response uploadMulti(@RequestParam("file") MultipartFile[] files) throws IOException {
        if (files.length == 0) {
            return Response.isFail().msg("上传失败，请选择文件");
        }
        manualService.uploadMulti(files);
        return Response.isSuccess();
    }

    /*批量计算参数文件上传*/
    @RequiresRoles(value = {"admin", "user"}, logical = Logical.OR)
    @RequestMapping(value = "/upload/para", method = RequestMethod.POST)
    public Response uploadPara(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Response.isFail().msg("上传失败，请选择文件");
        }
        String paraFilePath = manualService.uploadPara(file);
        return Response.isSuccess().data(paraFilePath);
    }

    /*手动计算，单次*/
    @RequiresRoles(value = {"admin", "user"}, logical = Logical.OR)
    @RequestMapping(value = "/do", method = RequestMethod.POST)
    public Response mutualDo(HttpServletRequest request, @RequestBody Do doPara) throws Exception {
        Map<String, String> map = manualService.manualDo(request, doPara.getLon(), doPara.getLat(), doPara.getDepth(), doPara.getTime(), doPara.getMagnitude(),
                doPara.getDistance_min(), doPara.getDistance_max(), doPara.getFilter_min(), doPara.getFilter_max(), doPara.getSeedName(),doPara.getUserId());
        return Response.isSuccess().data(map);
    }

    /*手动计算，多套滤波参数
    * filters: 使用'/'拼接参数：0.02/0.08/0.03/0.07/0.04/0.06 (min/max/min/max/min/max)
    * */
    @RequiresRoles(value = {"admin", "user"}, logical = Logical.OR)
    @RequestMapping(value = "/do/multi-filter", method = RequestMethod.POST)
    public Response mutualDoMultiFilter(HttpServletRequest request, @RequestBody Do doPara) throws Exception {
        Map<String, String> result = manualService.manualDoMultiFilter(request, doPara.getLon(), doPara.getLat(), doPara.getDepth(), doPara.getTime(), doPara.getMagnitude(),
                doPara.getDistance_min(), doPara.getDistance_max(), doPara.getFilters(), doPara.getSeedName(),doPara.getUserId());
        return Response.isSuccess().data(result);
    }

    /*
     * 多套滤波参数接口拆分为异步计算：
     * 1. 将计算更改为异步任务
     * 2. 接口一：/multi-filter/start
     *   2.1 向mtinv表中插入一行空数据
     *   2.2 获取mtinv_id
     *   2.3 运行异步任务（任务结束后将结果update到该行）
     *   2.4 返回mtinv_id
     * 3. 接口二：/multi-filter/progress, para: mtinv_id
     *   3.1 检查该mtinv_id行数据是否被更新
     *   3.2 若为空值返回0，若有结果返回1
     * 4. 接口三：/multi-filter/result, para: mtinv_id
     *   4.1 返回tarPath, resultList, mtinv_id, bestResultInfo
     * */
    @RequiresRoles(value = {"admin", "user"}, logical = Logical.OR)
    @RequestMapping(value = "/do/progress", method = RequestMethod.GET)
    public Response manualDoCheckStatus(@RequestParam("mtinv_id") String mtinv_id) {
        String status = manualService.manualDoCheckProgress(mtinv_id);
        return Response.isSuccess().data(status);
    }

    @RequiresRoles(value = {"admin", "user"}, logical = Logical.OR)
    @RequestMapping(value = "/do/multi-filter/start", method = RequestMethod.POST)
    public Response manualDoMultiFilterStart(@RequestBody Do doPara) throws Exception {
        String mtinv_id = manualService.manualDoMultiFilterStart(doPara.getLon(), doPara.getLat(), doPara.getDepth(), doPara.getTime(), doPara.getMagnitude(),
                doPara.getDistance_min(), doPara.getDistance_max(), doPara.getFilters(), doPara.getSeedName(),doPara.getUserId());
        return Response.isSuccess().data(mtinv_id);
    }

    @RequiresRoles(value = {"admin", "user"}, logical = Logical.OR)
    @RequestMapping(value = "/do/multi-filter/result", method = RequestMethod.GET)
    public Response manualDoMultiFilterResult(@RequestParam("mtinv_id") String mtinv_id) {
        Map<String, String> result = manualService.manualDoMultiFilterResult(mtinv_id);
        return Response.isSuccess().data(result);
    }

    /*批量计算，异步调用*/
    @RequiresRoles(value = {"admin", "user"}, logical = Logical.OR)
    @RequestMapping(value = "/do/batch/start", method = RequestMethod.POST)
    public Response mutualDoBatchStart(@RequestBody Map<String, String> para) throws Exception {
        Map<String, String> result = manualService.manualDoBatchStart(para.get("paraFilePath"), para.get("userId"));
        return Response.isSuccess().data(result);
    }

    @RequiresRoles(value = {"admin", "user"}, logical = Logical.OR)
    @RequestMapping(value = "/do/batch/result", method = RequestMethod.GET)
    public Response manualDoBatchResult(@RequestParam("mtinv_id") String mtinv_id) {
        Map<String, String> result = manualService.manualDoBatchResult(mtinv_id);
        return Response.isSuccess().data(result);
    }

    /*批量计算多次地震*/
    @RequiresRoles(value = {"admin", "user"}, logical = Logical.OR)
    @RequestMapping(value = "/do/batch", method = RequestMethod.POST)
    public Response mutualDoBatch(@RequestBody Map<String, String> para) throws Exception {
        Map<String, Map<String, String>> result = manualService.manualDoBatch(para.get("paraFilePath"), para.get("userId"));
        return Response.isSuccess().data(result);
    }

    @RequestMapping(value = "/downloadResult", method = RequestMethod.GET)
    public Response downloadResult(HttpServletResponse response, @RequestParam(name = "filePath") String filePath) throws IOException {
        manualService.downloadResult(response, filePath);
        return Response.isSuccess();
    }

    @RequestMapping(value = "/downloadResult/batch", method = RequestMethod.GET)
    public Response downloadResultBatch(HttpServletResponse response, @RequestParam(name = "files") String files) throws IOException {
        manualService.downloadResultBatch(response, files);
        return Response.isSuccess();
    }

    @RequiresRoles(value = {"admin", "user"}, logical = Logical.OR)
    @RequestMapping(value = "/reMath/getStation", method = RequestMethod.GET)
    public Response getStation(@RequestParam(name = "rootPath") String rootPath) {
        String stations = manualService.getStation(rootPath);
        return Response.isSuccess().data(stations);
    }

    @RequiresRoles(value = {"admin", "user"}, logical = Logical.OR)
    @RequestMapping(value = "/reMath", method = RequestMethod.POST)
    public Response reMath(@RequestBody ReMath reMathPara) {
        Map<String, String> map = manualService.reMath(reMathPara.getRootPath(), reMathPara.getId(), reMathPara.getUncheckedStation(),
                reMathPara.getCheckedStation(), reMathPara.getFilter_min(), reMathPara.getFilter_max());
        return Response.isSuccess().data(map);
    }
}