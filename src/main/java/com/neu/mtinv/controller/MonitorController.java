package com.neu.mtinv.controller;

import com.neu.mtinv.service.AdminService;
import com.neu.mtinv.service.MonitorService;
import com.neu.mtinv.entity.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/monitor")
public class MonitorController {
    @Resource
    private MonitorService monitorService;

    @Resource
    private AdminService adminService;

    @RequestMapping(value = "/getCatalog", method = RequestMethod.GET)
    public Response getCatalog() {
        return Response.isSuccess().data(monitorService.getCatalog());
    }

    @RequestMapping(value = "/getMtinfo", method = RequestMethod.GET)
    public Response getMtinfo() {
        return Response.isSuccess().data(adminService.getMtinfo("auto"));
    }

    /*震源机制解*/
    @RequestMapping(value = "/getResult", method = RequestMethod.GET)
    public Response getResult(String id, String resultFile) throws IOException {
        return Response.isSuccess().data(monitorService.getResult(id, resultFile));
    }

    /*波形拟合图*/
    @RequestMapping(value = "/image", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(String imageUrl) throws IOException {
        return monitorService.getImage(imageUrl);
    }
}