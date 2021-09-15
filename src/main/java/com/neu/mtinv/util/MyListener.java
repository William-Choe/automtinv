package com.neu.mtinv.util;

import com.neu.mtinv.entity.SysConfig;
import com.neu.mtinv.mapper.LastCatalogMapper;
import com.neu.mtinv.mapper.MtinfoMapper;
import com.neu.mtinv.mapper.SysConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.*;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@WebListener
public class MyListener implements ServletContextListener {
    @Resource
	private SysConfigMapper sysConfigMapper;

    @Resource
	private MtinfoMapper mtinfoMapper;

    @Resource
	private LastCatalogMapper lastCatalogMapper;

    @Resource
	private Mtinv mtinv;

    @Resource
    private UserDate ud;

	private static ScheduledExecutorService ee = Executors.newScheduledThreadPool(1);

	public void contextInitialized(ServletContextEvent arg0) {
		ee.scheduleWithFixedDelay(() -> {
			try{
				int temp = 0;
				String lastCatalog_eqim = "";
				String lastCatalog_mtinv = "";
				String cataid = "";
				String save_time = "";
				String o_time = "";
				String lat = "";
				String lon = "";
				String m = "";
				String depth = "";
				String seedName = "";
				String localName = "";

				SysConfig config = sysConfigMapper.getSysconfig();
				String startTimeTemp = ud.getStrDate();

				//step 1
				String jgt = "";
				String jg = config.getCatalog_jg();
				String[] jgList = jg.split(",");

                for (String s : jgList) {
                    jgt = jgt + " Cata_id LIKE '" + s + "%' OR";
                }

				if (jgt.length() > 0) {
					jgt = jgt.substring(0, jgt.length() - 2);
				}

				MysqlCon mc_eqim = new MysqlCon(config.getEqim_ip(), config.getEqim_user(), config.getEqim_pass(), config.getEqim_sid());
				String sql = "select MAX(Save_time) from catalog where Lon>=" + config.getCatalog_minE() + " and Lon<=" + config.getCatalog_maxE() +
                        " and Lat>=" + config.getCatalog_minN() + " and Lat<=" + config.getCatalog_maxN() + " and m>=" + config.getCatalog_minM() +
                        " and m<=" + config.getCatalog_maxM() + " and (" + jgt + ")";
				ResultSet rs = mc_eqim.executeQuery(sql);
				if (rs.next()) {
					lastCatalog_eqim = rs.getString(1);
				}

				save_time = lastCatalog_eqim.replaceAll("-", "");
				save_time = save_time.replaceAll(":", "");
				save_time = save_time.replaceAll("-", "");
				save_time = save_time.replaceAll(" ", "");
				save_time = save_time.substring(0, save_time.length() - 2);
				mc_eqim.close();

				lastCatalog_mtinv = lastCatalogMapper.getEqTime();
				if (!save_time.equals(lastCatalog_mtinv)) {
                    log.info("Listener -> Find new event!");
					temp = 1;
					lastCatalogMapper.updateEqTime(save_time);
					log.info("Listener -> Update EQTime " + save_time);
				}

				//step 2
				if (temp == 1) {
					MysqlCon mc_eqim2 = new MysqlCon(config.getEqim_ip(), config.getEqim_user(), config.getEqim_pass(), config.getEqim_sid());
					String sql2 = "select * from catalog where Save_time='" + save_time + "'";
					ResultSet rs2 = mc_eqim2.executeQuery(sql2);
					if (rs2.next()) {
						cataid = rs2.getString("Cata_id");
						o_time = rs2.getString("O_time");
						lat = rs2.getString("Lat");
						lon = rs2.getString("Lon");
						m = rs2.getString("M");
						depth = rs2.getString("Depth");
						localName = rs2.getString("Location_cname");

						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//小写的mm表示的是分钟
						Date o_time_date = sdf.parse(o_time);
						Calendar now = Calendar.getInstance();
						now.setTime(o_time_date);
						now.set(Calendar.SECOND, now.get(Calendar.SECOND) - Integer.parseInt(config.getSeed_bef()));
						String o_time_bef = sdf.format(now.getTime());
						seedName = o_time_bef.replaceAll("-", "");
						seedName = seedName.replaceAll(":", "");
						seedName = seedName.replaceAll(" ", "");
						seedName = seedName + ".seed";

						// 发震后等待Xmin开始计算
						double cal_wait_time = Double.parseDouble(config.getCal_wait_time()) * 60 * 1000;
						Date current_time = new Date();
						long time_dif = (current_time.getTime() - o_time_date.getTime()) ;
						if (time_dif < cal_wait_time) {
							long wait_time = new Double(cal_wait_time - time_dif).longValue();
							Thread.sleep(wait_time);
						}

						log.info("MyListener -> 创建export.sh: " + o_time_bef);
						File cho = new File("/jopens/AutoExport/export.sh");
						cho.createNewFile();
						String filein_cho = "#!/bin/bash\n";
						filein_cho += "sh /jopens/AutoExport/AutoExportSeedVolume.sh -catalog '"
								+ o_time_bef + "' " + lat + " " + " " + lon + " " + depth + " " + m + " " + config.getSeed_time() + "\n";
						RandomAccessFile mm_cho;
						try {
							mm_cho = new RandomAccessFile(cho, "rw");
							mm_cho.writeBytes(filein_cho);
							mm_cho.close();
						} catch (IOException e1) {
							log.error("MyListener -> error occurs: ", e1);
						}

                        log.info("MyListener -> 执行export.sh:");
						String tep;
						String command = "bash /jopens/AutoExport/export.sh";
						Process pp = Runtime.getRuntime().exec(command);
						BufferedReader reader = new BufferedReader(new InputStreamReader(pp.getInputStream()));
						while ((tep = reader.readLine()) != null) {
							log.info(tep);
						}
					}
					mc_eqim2.close();
				}


				//step 3
				if (temp == 1) {
					log.info("MyListener -> 开始计算: " + localName + " " + seedName);
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

					// TO  CENC
					String endTimeTemp = ud.getStrDate();
					String doSend = config.getEqim_doSend();

					// dosend = 1表示开启发送
					if (doSend.equals("1")) {
						double send_m = Double.parseDouble(config.getEqim_sendM());

						if (Double.parseDouble(m) >= send_m || Double.parseDouble(result_m) >= send_m) {
							SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							Date st = dfs.parse("1970-01-01 08:00:00");
							Date ot = dfs.parse(o_time);
							Date nt = dfs.parse(endTimeTemp);
							long chatime = (ot.getTime() - st.getTime()) / 1000;
							long jstime = (nt.getTime() - ot.getTime()) / 1000 / 60 / 60;
							log.info("MyListener -> 计算时间：" + jstime);

							if (jstime < 2) {
								// 命令 发震时刻 经度 纬度 深度 震级 中文地名 IP地址 端口号 用户名也即台网代码 密码 台网中文名 台网英文名
                                log.info("MyListener -> 发送至EQIM");
								RunSH runsh = new RunSH();
								runsh.rsh("send2eqim " + chatime + " " + lon + " " + lat + " " + result_depth + " " + result_m + " " + localName + " "
										+ config.getEqim_sendIP() + " " + config.getEqim_sendPort() + " " + config.getEqim_sendUser()
										+ " " + config.getEqim_sendPass() + " " + config.getEqim_sendCname() + " " + config.getEqim_sendSname());
							}
						}
					}

					// 添加数据
					mtinfoMapper.insertData(cataid, o_time, lat, lon, depth, m, localName, config.getMtinv_minD(), config.getMtinv_maxD(),
							config.getMtinv_minL(), config.getMtinv_maxL(), startTimeTemp, endTimeTemp, resultPath, result_file, result_time, result_lon, result_lat,
							result_depth, result_m, result_s1, result_d1, result_r1, result_s2, result_d2, result_r2, result_pvr);
				}
			}catch(Exception e){
				log.error("MyListener -> error occurs: ", e);
			}
		}, 0, 10, TimeUnit.SECONDS);
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		ee.shutdown();
	}
}
