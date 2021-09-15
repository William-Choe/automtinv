package com.neu.mtinv.controller;

import com.neu.mtinv.entity.Do;
import com.neu.mtinv.entity.SysConfig;
import com.neu.mtinv.entity.User;
import com.neu.mtinv.service.AdminService;
import com.neu.mtinv.service.ManualService;
import com.neu.mtinv.entity.Response;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Resource
    private AdminService adminService;
    @Resource
    private ManualService manualService;


    @RequiresRoles("admin")
    @RequestMapping(value = "/getSysconfig", method = RequestMethod.GET)
    public Response getSysConfig() {
        return Response.isSuccess().data(adminService.getSysConfig());
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/setEqimDB", method = RequestMethod.PUT)
    public Response setEqimDB(@RequestBody SysConfig sysConfig) {
        adminService.setEqimDB(sysConfig.getEqim_ip(), sysConfig.getEqim_user(), sysConfig.getEqim_pass(), sysConfig.getEqim_sid());

        return Response.isSuccess();
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/setEqimCatalog", method = RequestMethod.PUT)
    public Response setEqimCatalog(@RequestBody SysConfig sysConfig) {
        adminService.setEqimCatalog(sysConfig.getCatalog_minE(), sysConfig.getCatalog_maxE(),
                sysConfig.getCatalog_minN(), sysConfig.getCatalog_maxN(),
                sysConfig.getCatalog_minM(), sysConfig.getCatalog_maxM(),
                sysConfig.getCatalog_jg());

        return Response.isSuccess();
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/setEqimShow", method = RequestMethod.PUT)
    public Response setEqimShow(@RequestBody SysConfig sysConfig) {
        adminService.setEqimShow(sysConfig.getShow_minE(), sysConfig.getShow_maxE(),
                sysConfig.getShow_minN(), sysConfig.getShow_maxN(),
                sysConfig.getShow_minM(), sysConfig.getShow_maxM(),
                sysConfig.getShow_jg());

        return Response.isSuccess();
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/setEqimSendSwitch", method = RequestMethod.PUT)
    public Response setEqimSendSwitch(@RequestBody SysConfig sysConfig) {
        adminService.setEqimSendSwitch(sysConfig.getEqim_doSend());

        return Response.isSuccess();
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/setEqimSendPara", method = RequestMethod.PUT)
    public Response setEqimSendPara(@RequestBody SysConfig sysConfig) {
        adminService.setEqimSendPara(sysConfig.getEqim_sendM(), sysConfig.getEqim_sendIP(), sysConfig.getEqim_sendPort(),
                sysConfig.getEqim_sendUser(), sysConfig.getEqim_sendPass(), sysConfig.getEqim_sendCname(), sysConfig.getEqim_sendSname());

        return Response.isSuccess();
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/setSeedConfig", method = RequestMethod.PUT)
    public Response setSeedConfig(@RequestBody SysConfig sysConfig) {
        adminService.setSeedConfig(sysConfig.getSeed_bef(), sysConfig.getSeed_time());

        return Response.isSuccess();
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/setMtConfig", method = RequestMethod.PUT)
    public Response setMtConfig(@RequestBody SysConfig sysConfig) {
        adminService.setMtConfig(sysConfig.getMtinv_minL(), sysConfig.getMtinv_maxL(),
                sysConfig.getMtinv_minD(), sysConfig.getMtinv_maxD());

        return Response.isSuccess();
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/setCalWaitTime", method = RequestMethod.PUT)
    public Response setCalWaitTime(@RequestBody SysConfig sysConfig) {
        adminService.setCalWaitTime(sysConfig.getCal_wait_time());

        return Response.isSuccess();
    }

    /*type = auto or manual*/
    @RequiresRoles("admin")
    @RequestMapping(value = "/getMtinfo/{type}", method = RequestMethod.GET)
    public Response getMtinfo(@PathVariable("type") String type) {
        return Response.isSuccess().data(adminService.getMtinfo(type));
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/uploadAuto", method = RequestMethod.POST)
    public Response upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Response.isFail().msg("上传失败，请选择文件");
        }

        String filePath = adminService.uploadAuto(file);
        return Response.isSuccess().data(filePath);
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/autoDo", method = RequestMethod.POST)
    public Response autoDo(@RequestBody Do doPara) throws Exception {
        Map map = adminService.autoDo(doPara.getLon(), doPara.getLat(), doPara.getDepth(), doPara.getTime(), doPara.getMagnitude(),
                doPara.getDistance_min(), doPara.getDistance_max(), doPara.getFilter_min(), doPara.getFilter_max(),
                doPara.getInstitution(), URLDecoder.decode(doPara.getLocation(),"utf-8"), doPara.getSeedName());

        return Response.isSuccess().data(map);
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/deleteCatalog/{id}", method = RequestMethod.DELETE)
    public Response deleteCatalog(@PathVariable("id") String id) {
        adminService.deleteCatalog(id);

        return Response.isSuccess();
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/downloadResult", method = RequestMethod.GET)
    public Response downloadResult(HttpServletResponse response, @RequestParam(name = "filePath") String filePath) throws IOException {
        manualService.downloadResult(response, filePath);

        return Response.isSuccess();
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/getBSCatalog", method = RequestMethod.GET)
    public Response getCatalogBS() {
        return Response.isSuccess().data(adminService.getCatalogBS());
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/busuan", method = RequestMethod.POST)
    public Response busuan(@RequestParam("catalog_id") String catalog_id) throws Exception {
        adminService.busuan(catalog_id);

        return Response.isSuccess();
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/getUsers", method = RequestMethod.GET)
    public Response getUsers() {
        return Response.isSuccess().data(adminService.getUsers());
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/getRoles", method = RequestMethod.GET)
    public Response getRoles() {
        return Response.isSuccess().data(adminService.getRoles());
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/addUser", method = RequestMethod.POST)
    public Response addUser(@RequestBody User user) throws Exception {
        if (adminService.addUser(user.getUsername(), user.getPassword(), user.getRealname(), user.getEmail(), user.getPhone(), user.getRole_id())) {
            return Response.isSuccess();
        } else {
            return Response.isFail().msg("username exists!");
        }
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/deleteUser", method = RequestMethod.DELETE)
    public Response deleteUser(@RequestParam("user_id") String user_id) {
        adminService.deleteUser(user_id);

        return Response.isSuccess();
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/updateShowMsg", method = RequestMethod.POST)
    public Response updateShowMsg(@RequestBody Map<String, String> params) {
        adminService.updateShowMsg(params.get("showMsg"));
        return Response.isSuccess();
    }

    @RequestMapping(value = "/getShowMsg", method = RequestMethod.GET)
    public Response getShowMsg() {
        return Response.isSuccess().data(adminService.getShowMsg());
    }
}