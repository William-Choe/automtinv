package com.neu.mtinv.service.impl;

import com.neu.mtinv.entity.*;
import com.neu.mtinv.mapper.MtinfoMapper;
import com.neu.mtinv.mapper.RoleMapper;
import com.neu.mtinv.mapper.SysConfigMapper;
import com.neu.mtinv.mapper.UserMapper;
import com.neu.mtinv.service.AdminService;
import com.neu.mtinv.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {
    @Resource
    private SysConfigMapper sysConfigMapper;

    @Resource
    private MtinfoMapper mtinfoMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private Mtinv mtinv;

    @Override
    public SysConfig getSysConfig() {
        return sysConfigMapper.getSysconfig();
    }

    @Override
    public void setEqimDB(String ip, String user, String pass, String sid) {
        sysConfigMapper.setEqimDB(ip, user, pass, sid);
    }

    @Override
    public void setEqimSendSwitch(String doSend) {
        sysConfigMapper.setEqimSendSwitch(doSend);
    }

    @Override
    public void setEqimSendPara(String send_m, String send_ip, String send_port, String send_user, String send_pass, String send_cname, String sned_sname) {
        sysConfigMapper.setEqimSendPara(send_m, send_ip, send_port, send_user, send_pass, send_cname, sned_sname);
    }

    @Override
    public void setEqimCatalog(String mine, String maxe, String minn, String maxn, String minm, String maxm, String jg) {
        sysConfigMapper.setEqimCatalog(mine, maxe, minn, maxn, minm, maxm, jg);
    }

    @Override
    public void setEqimShow(String mine, String maxe, String minn, String maxn, String minm, String maxm, String jg) {
        sysConfigMapper.setEqimShow(mine, maxe, minn, maxn, minm, maxm, jg);
    }

    @Override
    public void setSeedConfig(String sbef, String st) {
        sysConfigMapper.setSeedConfig(sbef, st);
    }

    @Override
    public void setMtConfig(String minl, String maxl, String mind, String maxd) {
        sysConfigMapper.setMtConfig(minl, maxl, mind, maxd);
    }

    @Override
    public void setCalWaitTime(String cal_wait_time) {
        sysConfigMapper.setCalWaitTime(cal_wait_time);
    }

    @Override
    public List<Mtinfo> getMtinfo(String type) {
        List<Mtinfo> mtinfoList = mtinfoMapper.getMtinfoByType(type);

        for (Mtinfo mtinfo : mtinfoList) {
            if (type.equals("auto")) {
                mtinfo.setJg(mtinfo.getCataid().substring(0, 2));
            }

            String o_time = mtinfo.getO_time();
            if (type.equals("auto") && o_time.length() >= 21) {
                mtinfo.setO_time(o_time.substring(0, o_time.length() - 2));
            } else if (type.equals("manual")) {
                mtinfo.setO_time(o_time);
            }

            mtinfo.setLon(new DecimalFormat("###.0").format(Double.parseDouble(mtinfo.getLon())));
            mtinfo.setLat(new DecimalFormat("##.0").format(Double.parseDouble(mtinfo.getLat())));
            mtinfo.setDepth(new DecimalFormat("####.0").format(Double.parseDouble(mtinfo.getDepth())));
            mtinfo.setM(new DecimalFormat("#.0").format(Double.parseDouble(mtinfo.getM())));

            String m_time = mtinfo.getM_time();
            String r_time = mtinfo.getR_time();

            if (m_time != null && r_time != null) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date m_date = df.parse(m_time);
                    Date r_date = df.parse(r_time);

                    long c_time = (r_date.getTime() - m_date.getTime()) / 1000;
                    mtinfo.setMath_time(String.valueOf(c_time));
                } catch (ParseException e) {
                    log.error("error occurs: ", e);
                }
            }

            String[] arr = mtinfo.getResult_path().split("/");
            mtinfo.setRoot_path("/" + arr[1] + "/" + arr[2] + "/" + arr[3] + "/");
        }

        return mtinfoList;
    }

    @Override
    public String uploadAuto(MultipartFile file) throws IOException {
        String computePath = "/autoMTInv/autoCompute/";
        String fileName= file.getOriginalFilename();
        String[] fileNameArr = fileName.split("\\.");

        if (!fileNameArr[fileNameArr.length - 1].toLowerCase().equals("seed")) {
            throw new IOException("上传失败，请上传seed类型文件");
        }

        String seedName = new Date().getTime() + ".seed";

        new File(computePath).mkdir();
        File dest = new File(computePath + seedName);
        file.transferTo(dest);

        return computePath + seedName;
    }

    @Override
    public Map<String, String> autoDo(String lon, String lat, String depth, String time, String magnitude, String distance_min, String distance_max, String filter_min, String filter_max, String institution, String location, String seedName) throws Exception {
        Map<String, String> map = mtinv.doMath_auto(lon, lat, depth, time, magnitude, distance_min, distance_max, filter_min, filter_max, institution, location, seedName);

        mtinfoMapper.insertData(map.get("cataid"), map.get("o_time"), map.get("lat"), map.get("lon"), map.get("depth"), map.get("m"),
                map.get("location"), map.get("mind"), map.get("maxd"), map.get("minl"), map.get("maxl"),map.get("startTimeTemp"), map.get("endTimeTemp"),
                map.get("resultPath"), map.get("resultFile"), map.get("resultTime"), map.get("resultLon"), map.get("resultLat"),
                map.get("resultDepth"), map.get("result_m"), map.get("result_s1"), map.get("result_d1"), map.get("result_r1"),
                map.get("result_s2"), map.get("result_d2"), map.get("result_r2"), map.get("result_pvr"));

        String mtinv_id = mtinfoMapper.getLastId();

        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("resultList", map.get("resultList"));
        returnMap.put("mtinv_id", mtinv_id);
        returnMap.put("rootPath", map.get("rootPath"));

        return returnMap;
    }

    @Override
    public void deleteCatalog(String id) {
        //删除catalog目录
        String resultPath = mtinfoMapper.getResultPathById(id);
        System.out.println(resultPath);
        String rootPath = resultPath.substring(0, resultPath.length() - 13);
        FileUtil fu = new FileUtil();
        fu.deleteDirectory(rootPath);

        //删除数据
        mtinfoMapper.deleteMtinfo(id);
    }

    @Override
    public List<Map> getCatalogBS() {
        SysConfig config = getSysConfig();
        UserDate ud = new UserDate();
        String fd = ud.getDateBefore(60);

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

        try {
            MysqlCon mc = new MysqlCon(config.getEqim_ip(),config.getEqim_user(),config.getEqim_pass(),config.getEqim_sid());
            String sql;
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

            // select * from catalog where O_time>=date_format('2021-05-22', '%Y-%m-%d') and Lat >= 16 and Lat <= 55 and Lon >= 70 and Lon <= 135a and M>=3 order by O_time desc
            if (jgt.length() > 0) {
                sql = "select * from catalog where O_time>=date_format('" + fd + "','%Y-%m-%d')  and Location_cname!='测试' and Lat>="
                        + lat_min + " and Lat<=" + lat_max + " and Lon>=" + lon_min + " and Lon<=" + lon_max + " and M>=" + m_min
                        + " and M<=" + m_max + " and (" + jgt + ") order by O_time desc ";
            } else {
                sql = "select * from catalog where O_time>=date_format('" + fd + "','%Y-%m-%d')  and Location_cname!='测试' and Lat>="
                        + lat_min + " and Lat<=" + lat_max + " and Lon>=" + lon_min + " and Lon<=" + lon_max + " and M>=" + m_min
                        + " and M<=" + m_max + "  order by O_time desc ";
            }
            ResultSet rs = mc.executeQuery(sql);
            List<Mtinfo> mtinfoList = mtinfoMapper.getMtinfoByType("auto");
            boolean tof;
            while(rs.next()){
                tof = true;
                jg = rs.getString("Cata_id").substring(0,2);

                o_time = rs.getString("O_time");
                o_time = o_time.substring(0, o_time.length() - 2);

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
                    if (backid.equals(backid_mt)) {
                        tof = false;
                        break;
                    }
                }

                if(tof){
                    Map<String, String> map = new HashMap<>();
                    map.put("id", backid);
                    map.put("jg", jg);
                    map.put("oTime", o_time);
                    map.put("location", locat);
                    map.put("lon", lon_s);
                    map.put("lat", lat_s);
                    map.put("depth", depth_s);
                    map.put("m", m_s);

                    list.add(map);
                }
            }
            mc.close();
            return list;
        } catch (SQLException e) {
            log.error("error occurs: ", e);
            return null;
        }
    }

    @Override
    public void busuan(String catalog_id) throws Exception {
        SysConfig config = getSysConfig();

        String cataid = "";
        String o_time = "";
        String lat = "";
        String lon = "";
        String m = "";
        String depth = "";
        String seedName = "";
        String localName = "";

        UserDate ud = new UserDate();
        String startTimeTemp = ud.getStrDate();

        // step 1
        MysqlCon mc_eqim = new MysqlCon(config.getEqim_ip(), config.getEqim_user(), config.getEqim_pass(), config.getEqim_sid());
        String sql2 = "select * from catalog where  Cata_id='" + catalog_id + "'";
        ResultSet rs2 = mc_eqim.executeQuery(sql2);
        if (rs2.next()) {
            cataid = rs2.getString("Cata_id");
            o_time = rs2.getString("O_time");
            lat = rs2.getString("Lat");
            lon = rs2.getString("Lon");
            m = rs2.getString("M");
            depth = rs2.getString("Depth");
            localName = rs2.getString("Location_cname");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date o_time_date = sdf.parse(o_time);
            Calendar now = Calendar.getInstance();
            now.setTime(o_time_date);
            now.set(Calendar.SECOND, now.get(Calendar.SECOND) - Integer.parseInt(config.getSeed_bef()));
            String o_time_bef = sdf.format(now.getTime());
            seedName = o_time_bef.replaceAll("-","");
            seedName = seedName.replaceAll(":","");
            seedName = seedName.replaceAll(" ","");
            seedName = seedName + ".seed";

            log.info("创建export.sh");
            File cho = new File("/jopens/AutoExport/export.sh");
            cho.createNewFile();
            String filein_cho = "#!/bin/bash\n" ;
            filein_cho += "sh /jopens/AutoExport/AutoExportSeedVolume.sh -catalog '"
                    + o_time_bef + "' " + lat + " " + " " + lon + " " + depth + " " + m + " " + config.getSeed_time() + "\n";
            RandomAccessFile mm_cho = null;
            try {
                mm_cho = new RandomAccessFile(cho, "rw");
                mm_cho.writeBytes(filein_cho);
                mm_cho.close();
            } catch (IOException e1) {
                log.error("error occurs: ", e1);
            }

            log.info("执行export.sh");
            String tep;
            String command ="sh /jopens/AutoExport/export.sh";
            Process pp = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(pp.getInputStream()));
            while ((tep = reader.readLine()) != null) {
                log.info(tep);
            }
        }
        mc_eqim.close();

        // step 2
        String para = mtinv.doMath_busuan(localName, seedName, lat, lon, m, o_time, depth);

        String[] paraArray = para.split(";");
        String result_file = paraArray[0];
        String result_riqi = paraArray[1];
        String result_sj = paraArray[2];
        String result_lat = paraArray[3];
        String result_lon = paraArray[4];
        String result_depth = paraArray[5];
        String result_m = paraArray[6];
        String result_s1 = paraArray[7];
        String result_d1 = paraArray[8];
        String result_r1 = paraArray[9];
        String result_s2 = paraArray[10];
        String result_d2 = paraArray[11];
        String result_r2 = paraArray[12];
        String result_pvr = paraArray[13];
        String resultPath = paraArray[14];
        String result_time = result_riqi + " " + result_sj;
        String endTimeTemp = ud.getStrDate();

        mtinfoMapper.insertData(cataid, o_time, lat, lon, depth, m, localName, config.getMtinv_minD(), config.getMtinv_maxD(),
                config.getMtinv_minL(), config.getMtinv_maxL(), startTimeTemp, endTimeTemp, resultPath, result_file, result_time,
                result_lon, result_lat, result_depth, result_m, result_s1, result_d1, result_r1, result_s2, result_d2, result_r2, result_pvr);

    }

    @Override
    public List<Role> getRoles() {
        return roleMapper.getRoles();
    }

    @Override
    public List<User> getUsers() {
        return  userMapper.getUsers();
    }

    @Override
    public boolean addUser(String username, String password, String realname, String email, String phone, String role_id){
        if (userMapper.usernameExist(username) != null) {
            return false;
        }

        userMapper.addUser(username, password, realname, email, phone);
        userMapper.addUserRole(userMapper.findByUsername(username).getId(), role_id);
        return true;
    }
    
    @Override
    public void deleteUser(String user_id) {
        userMapper.deleteUser(user_id);
        userMapper.deleteUserRole(user_id);
    }

    @Override
    public void updateShowMsg(String showMsg) {
        sysConfigMapper.updateShowMsg(showMsg);
    }

    @Override
    public String getShowMsg() {
        return sysConfigMapper.getShowMsg();
    }
}
