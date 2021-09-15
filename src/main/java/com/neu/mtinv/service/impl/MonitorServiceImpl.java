package com.neu.mtinv.service.impl;

import com.neu.mtinv.entity.Mtinfo;
import com.neu.mtinv.entity.SysConfig;
import com.neu.mtinv.mapper.MtinfoMapper;
import com.neu.mtinv.mapper.SysConfigMapper;
import com.neu.mtinv.service.MonitorService;
import com.neu.mtinv.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.*;

@Slf4j
@Service
public class MonitorServiceImpl implements MonitorService {
    @Resource
    private SysConfigMapper sysConfigMapper;

    @Resource
    private MtinfoMapper mtinfoMapper;

    @Resource
    private ReadTXT readTXT;

    @Override
    public List<Map> getCatalog() {
        SysConfig config = sysConfigMapper.getSysconfig();
        String lon_min = config.getShow_minE();
        String lon_max = config.getShow_maxE();
        String lat_min = config.getShow_minN();
        String lat_max = config.getShow_maxN();
        String m_min = config.getShow_minM();
        String m_max = config.getShow_maxM();
        String jg_r = config.getShow_jg();

        List<Map> list = new ArrayList<>();
        String jg = "";
        String o_time = "";
        String locat = "";
        String lon_s = "";
        String lat_s = "";
        String depth_s = "";
        String m_s = "";
        String resultPath = "";
        String resultFile = "";
        String id = "";

        try {
            MysqlCon mc = new MysqlCon(config.getEqim_ip(),config.getEqim_user(),config.getEqim_pass(),config.getEqim_sid());

            String sql = "";
            String jgt = "";
            if (jg_r.length() > 0) {
                String[] jgList = jg_r.split(",");
                for (int i = 0; i < jgList.length; i++) {
                    jgt = jgt + " Cata_id LIKE '" + jgList[i] + "%' OR";
                }
                if (jgt.length() > 0) {
                    jgt = jgt.substring(0, jgt.length() - 2);
                }
            }
            if (jgt.length() > 0) {
                sql = "select * from catalog where Location_cname!='测试' and Lat>=" + lat_min + " and Lat<=" + lat_max + " and Lon>=" + lon_min + " and Lon<=" + lon_max + " and M>=" + m_min + " and M<=" + m_max + " and (" + jgt + ") order by O_time desc limit 100";
            } else {
                sql = "select * from catalog where Location_cname!='测试' and Lat>=" + lat_min + " and Lat<=" + lat_max + " and Lon>=" + lon_min + " and Lon<=" + lon_max + " and M>=" + m_min + " and M<=" + m_max + "  order by O_time desc limit 100";
            }

            ResultSet rs = mc.executeQuery(sql);
            log.info(sql);

            List<Mtinfo> mtinfoList = mtinfoMapper.getMtinfoByTypeAndLimit("auto", "100");

            while(rs.next()){
                jg = rs.getString("Cata_id").substring(0,2);
                o_time = rs.getString("O_time");
                locat = rs.getString("Location_cname");

                double lon = rs.getDouble("Lon");
                DecimalFormat df1 = new DecimalFormat("###.0");
                lon_s = df1.format(lon);

                double lat = rs.getDouble("Lat");
                DecimalFormat df2 = new DecimalFormat("##.0");
                lat_s = df2.format(lat);

                double depth = rs.getDouble("Depth");
                DecimalFormat df3 = new DecimalFormat("####.0");
                depth_s = df3.format(depth);


                double m = rs.getDouble("M");
                DecimalFormat df4 = new DecimalFormat("#.0");
                m_s = df4.format(m);

                String backid = rs.getString("Cata_id");
                for (Mtinfo mtinfo : mtinfoList) {
                    String backid_mt = mtinfo.getCataid();
                    if(backid.equals(backid_mt)){
                        resultPath = mtinfo.getResult_path();
                        resultFile = mtinfo.getResult_file();
                        id = mtinfo.getId();
                    }
                }

                Map<String, Object> map = new HashMap<>();
                map.put("id", id);
                map.put("jg", jg);
                map.put("o_time", Timestamp.valueOf(o_time));
                map.put("location_cname", locat);
                map.put("lon", lon_s);
                map.put("lat", lat_s);
                map.put("depth", depth_s);
                map.put("m", m_s);
                map.put("result_path", resultPath);
                map.put("result_file", resultFile);

                list.add(map);
                resultPath = "";
                resultFile = "";
                id = "";
            }


            mc.close();
            return list;
        } catch (SQLException e) {
            log.error("error occurs: ", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> getResult(String id, String resultFile) throws IOException {
        Mtinfo mtinfo = mtinfoMapper.getMtinfoById(id);

        Map<String, Object> map = new HashMap<>();
        map.put("resultS1", mtinfo.getResult_s1());
        map.put("resultD1", mtinfo.getResult_d1());
        map.put("resultR1", mtinfo.getResult_r1());
        map.put("resultS2", mtinfo.getResult_s2());
        map.put("resultD2", mtinfo.getResult_d2());
        map.put("resultR2", mtinfo.getResult_r2());

        List l = readTXT.readtxt(resultFile);
        map.put("detail", l);

        String[] ra = resultFile.split("/");
        int le = ra[ra.length - 1].length() + ra[ra.length - 2].length() + 1;
        String MTINVPath = resultFile.substring(0, resultFile.length() - le);

        String gmtPath = MTINVPath + "gmtmap.jpg";
        String mecaPath = resultFile.substring(0, resultFile.length() - le) + "/meca.jpg";

        List<String> imgList = readTXT.getResultImg(resultFile);
        Collections.sort(imgList);

        map.put("gmtmapUrl", gmtPath);
        map.put("mecaUrl", mecaPath);
        map.put("imagesUrl", imgList);

        return map;
    }

    @Override
    public byte[] getImage(String imageUrl) throws IOException {
        System.out.println(imageUrl);
        File file = new File(imageUrl);
        FileInputStream inputStream = new FileInputStream(file);
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes, 0, inputStream.available());

        return bytes;
    }
}
