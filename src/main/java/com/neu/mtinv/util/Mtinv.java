package com.neu.mtinv.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class Mtinv {
	@Resource
	private UserDate ud;

	@Resource
	private MtinvFactory mtinvFactory;

	@Resource
	private RunSH runsh;

	@Resource
	private FileUtil fu;

	/*补算模块计算*/
	public String doMath_busuan(String localName, String seedName, String lat, String lon, String magnitude, String o_time, String depth) throws ParseException, IOException{
		String computePath = "/autoMTInv/autoCompute/";
		String rootPath = "";
		String RAWDATAPath = "";
		String SACPath = "";
		String MTINVPath = "";
		String resultPath = "";

		String saveurl_old = "";
		String saveurl_new = "";

		String distance_min = "";
		String distance_max = "";
		String filter_min = "";
		String filter_max = "";
		String command = "";

		double m_dou = Double.parseDouble(magnitude);
		if (m_dou <= 3.5) {
			distance_min = "10";
			distance_max = "100";
			filter_min = "0.02";
			filter_max = "0.09";
		}
		if (m_dou <= 4 & m_dou > 3.5) {
			distance_min = "50";
			distance_max = "300";
			filter_min = "0.02";
			filter_max = "0.08";
		}
		if (m_dou <= 5 & m_dou > 4) {
			distance_min = "100";
			distance_max = "300";
			filter_min = "0.02";
			filter_max = "0.08";
		}
		if (m_dou > 5) {
			distance_min = "150";
			distance_max = "350";
			filter_min = "0.02";
			filter_max = "0.08";
		}

		//国际时间
		String UTCTime = "";
		UTCTime = ud.getHours(o_time, -8);

		//创建文件夹
		String fileTime = UTCTime.substring(0, 4) + UTCTime.substring(5, 7) +
				UTCTime.substring(8, 10) + UTCTime.substring(11, 13) +
				UTCTime.substring(14, 16) + UTCTime.substring(17, 19);

		rootPath = computePath + fileTime + "/";
		new File(rootPath).mkdir();
		RAWDATAPath = rootPath + "RAWDATA/";
		new File(RAWDATAPath).mkdir();
		SACPath = rootPath + "SAC/";
		new File(SACPath).mkdir();
		MTINVPath = rootPath + "MTINV/";
		new File(MTINVPath).mkdir();

		//创建日志文件
		File logFile = new File(rootPath + "logfile.txt");
		logFile.createNewFile();
		//logFile写入
		BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), "UTF-8")); // 指定编码格式，以免读取时中文字符异常

		//把seed文件复制到RAWDATA文件夹里
		saveurl_old = "/jopens/AutoExport/seed/" + seedName;
		saveurl_new = RAWDATAPath + seedName;
		fu.fileChannelCopy(new File(saveurl_old), new File(saveurl_new));

		saveurl_new = "/jopens/AutoExport/" + seedName;
		fu.fileChannelCopy(new File(saveurl_old), new File(saveurl_new));

		//儒略日
		String OTCTime = "";
		int yy = Integer.parseInt(UTCTime.substring(0,4));
		int mm = Integer.parseInt(UTCTime.substring(5,7));
		int dd = Integer.parseInt(UTCTime.substring(8,10));
		OTCTime = "" + ud.getDate(yy, mm, dd);

		// jieya.sh
		log.info("Create File jieya.sh");
		logWriter.append(ud.getStrDate() + ": 创建jieya.sh \n\r");
		mtinvFactory.create_jieya(RAWDATAPath, seedName);

		log.info("Run jieya.sh");
		logWriter.append(ud.getStrDate() + ": 执行jieya.sh \n\r");
		command ="bash /jopens/AutoExport/jieya.sh";
		runsh.runAndLog(command, logWriter);

		/**
		 *	步骤一、进行seed波形的解压及头文件的改写
		 *
		 *	步骤二、挑选符合震中距的台站
		 *
		 *	步骤三、进行格林函数脚本的准备、计算
		 *
		 */
		/*************************************第3步，创建步骤一的脚本文件***********************************************/

		//①	addinfounpackmtinv.sh
		log.info("Create File addinfounpackmtinv.sh");
		logWriter.append(ud.getStrDate() + ": 创建addinfounpackmtinv.sh \n\r");
		mtinvFactory.create_addinfounpackmtinv_auto(RAWDATAPath, lon, lat, depth, seedName);

		//②	cho.sm
		log.info("Create File cho.sm");
		logWriter.append(ud.getStrDate() + ": 创建cho.sm \n\r");
		mtinvFactory.create_cho(RAWDATAPath, UTCTime, OTCTime, lon, lat, depth);

		//③	cutdata.cmd
		log.info("Create File cutdata.cmd");
		logWriter.append(ud.getStrDate() + ": 创建cutdata.cmd \n\r");
		mtinvFactory.create_cutdata(RAWDATAPath);

		//④	syn.sh
		log.info("Create File syn.sh");
		logWriter.append(ud.getStrDate() + ": 创建syn.sh \n\r");
		mtinvFactory.create_syn(RAWDATAPath);

		//⑤	unpack.csh
		log.info("Create File unpack.csh");
		logWriter.append(ud.getStrDate() + ": 创建unpack.csh \n\r");
		mtinvFactory.create_unpack_auto(RAWDATAPath);

		/*************************************第3步，创建步骤一的脚本文件(结束)***********************************************/


		/*************************************第4步，执行步骤一的脚本文件***********************************************/

		// 1 addinfounpackmtinv.sh
		log.info("Run addinfounpackmtinv.sh");
		logWriter.append(ud.getStrDate() + ": 执行addinfounpackmtinv.sh \n\r");
		command = "bash " + RAWDATAPath + "addinfounpackmtinv.sh";
		runsh.runAndLog(command, logWriter);


		// 2 cutdata.cmd
		log.info("Run cutdata.cmd");
		logWriter.append(ud.getStrDate() + ": 执行cutdata.cmd \n\r");
		command = "bash " + RAWDATAPath + "cutdata.cmd";
		runsh.runAndLog(command, logWriter);


		// 3 syn.sh
		log.info("Run syn.sh");
		logWriter.append(ud.getStrDate() + ": 执行syn.sh \n\r");
		command = "bash " + RAWDATAPath + "syn.sh";
		runsh.runAndLog(command, logWriter);

		/*************************************第4步，执行步骤一的脚本文件（结束）***********************************************/


		/*************************************第5步，创建步骤二脚本文件***********************************************/

		//①	IDODIST.sh
		log.info("Create File IDODIST.sh");
		logWriter.append(ud.getStrDate() + ": 创建IDODIST.sh \n\r");
		mtinvFactory.create_IDODISH(SACPath, RAWDATAPath, distance_min, distance_max);


		//②	NETST.sh
		log.info("Create File NETST.sh");
		logWriter.append(ud.getStrDate() + ": 创建NETST.sh \n\r");
		mtinvFactory.create_NETST(SACPath);

		/*************************************第5步，创建步骤二脚本文件（结束）***********************************************/


		/*************************************第6步，执行步骤二脚本文件***********************************************/

		// 1 IDODIST.sh
		log.info("Run IDODIST.sh");
		logWriter.append(ud.getStrDate() + ": 执行IDODIST.sh \n\r");
		command = "bash " + SACPath + "IDODIST.sh";
		runsh.runAndLog(command, logWriter);

		// 2 NETST.Sh
		log.info("Run NETST.sh");
		logWriter.append(ud.getStrDate() + ": 执行NETST.Sh \n\r");
		command = "bash " + SACPath + "NETST.sh";
		runsh.runAndLog(command, logWriter);

		/*************************************第6步，执行步骤二脚本文件（结束）***********************************************/


		/*************************************jiancha***********************************************/
		boolean ifsca = false;
		File sacFile = new File(SACPath);
		String[] fileName = sacFile.list();
		for (int i = 0; i < fileName.length; i++) {
			String sac = fileName[i].substring(fileName[i].length() - 3);
			if (sac.equals("SAC")) {
				ifsca = true;
			}
		}
		if (!ifsca) {
			//delete file
			File rfile = new File(rootPath);
			fu.deleteAllFilesOfDir(rfile);
			return "";
		}
		/*************************************jiancha（结束）***********************************************/



		/*************************************第7步，创建步骤三脚本文件***********************************************/

		//创建makeglib.csh
		log.info("Create File makeglib.csh");
		logWriter.append(ud.getStrDate()+": 创建makeglib.csh \n\r");
		mtinvFactory.create_makeglib_auto(MTINVPath, RAWDATAPath, SACPath, UTCTime, lat, lon, filter_min, filter_max);

		//创建tar.sh
		log.info("Create File tar.sh");
		logWriter.append(ud.getStrDate()+": 创建tar.sh \n\r");
		mtinvFactory.create_tar(MTINVPath, fileTime);

		/***************************************************************************************************************

		 2017-05-05
		 取速度模型
		 生成  GETPOINT.sh  并执行

		 ***************************************************************************************************************/
		//1、创建 GETPOINT.sh
		log.info("Create File GETPOINT.sh");
		logWriter.append(ud.getStrDate() + ": 创建GETPOINT.sh \n\r");
		mtinvFactory.create_GETPOINT(lon, lat);

		//2、执行 GETPOINT.sh
		log.info("Run GETPOINT.sh");
		logWriter.append(ud.getStrDate() + ": 执行GETPOINT.sh \n\r");
		command = "bash /autoMTInv/modeldb/crust2.0/GETPOINT.sh";
		runsh.runAndLog(command, logWriter);

		//3、读取outcr
		String se1 = "";
		String se2 = "";
		String se3 = "";
		String se4 = "";
		String he1 = "";
		String he2 = "";
		String he3 = "";
		String he4 = "";
		String uc1 = "";
		String uc2 = "";
		String uc3 = "";
		String uc4 = "";
		String mc1 = "";
		String mc2 = "";
		String mc3 = "";
		String mc4 = "";
		String lc1 = "";
		String lc2 = "";
		String lc3 = "";
		String lc4 = "";
		File outcr = new File("/autoMTInv/modeldb/crust2.0/outcr");
		InputStreamReader read0 = new InputStreamReader(new FileInputStream(outcr));
		BufferedReader bufferedReader0 = new BufferedReader(read0);
		String lineTxt0 = null;
		while ((lineTxt0 = bufferedReader0.readLine()) != null) {
			if (lineTxt0.indexOf("soft sed") > 0) {
				String se = lineTxt0;
				se = se.replaceAll(" ", "");
				String sel[] = se.split("\\.");
				se1 = sel[0] + "." + sel[1].substring(0, 2);
				se2 = sel[1].substring(4, sel[1].length()) + "." + sel[2].substring(0, 2);
				se3 = sel[2].substring(4, sel[2].length()) + "." + sel[3].substring(0, 2);
				se4 = sel[3].substring(4, sel[3].length()) + "." + sel[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("hard sed") > 0) {
				String he = lineTxt0;
				he = he.replaceAll(" ", "");
				String hel[] = he.split("\\.");
				he1 = hel[0] + "." + hel[1].substring(0, 2);
				he2 = hel[1].substring(4, hel[1].length()) + "." + hel[2].substring(0, 2);
				he3 = hel[2].substring(4, hel[2].length()) + "." + hel[3].substring(0, 2);
				he4 = hel[3].substring(4, hel[3].length()) + "." + hel[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("upper crust") > 0) {
				String uc = lineTxt0;
				uc = uc.replaceAll(" ", "");
				String ucl[] = uc.split("\\.");
				uc1 = ucl[0] + "." + ucl[1].substring(0, 2);
				uc2 = ucl[1].substring(4, ucl[1].length()) + "." + ucl[2].substring(0, 2);
				uc3 = ucl[2].substring(4, ucl[2].length()) + "." + ucl[3].substring(0, 2);
				uc4 = ucl[3].substring(4, ucl[3].length()) + "." + ucl[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("middle crust") > 0) {
				String mc = lineTxt0;
				mc = mc.replaceAll(" ", "");
				String mcl[] = mc.split("\\.");
				mc1 = mcl[0] + "." + mcl[1].substring(0, 2);
				mc2 = mcl[1].substring(4, mcl[1].length()) + "." + mcl[2].substring(0, 2);
				mc3 = mcl[2].substring(4, mcl[2].length()) + "." + mcl[3].substring(0, 2);
				mc4 = mcl[3].substring(4, mcl[3].length()) + "." + mcl[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("lower crust") > 0) {
				String lc = lineTxt0;
				lc = lc.replaceAll(" ", "");
				String lcl[] = lc.split("\\.");
				lc1 = lcl[0] + "." + lcl[1].substring(0, 2);
				lc2 = lcl[1].substring(4, lcl[1].length()) + "." + lcl[2].substring(0, 2);
				lc3 = lcl[2].substring(4, lcl[2].length()) + "." + lcl[3].substring(0, 2);
				lc4 = lcl[3].substring(4, lcl[3].length()) + "." + lcl[4].substring(0, 2);
			}
		}

		//4、创建 wus.mod
		log.info("Create File wus.mod");
		logWriter.append(ud.getStrDate() + ": 创建wus.mod \n\r");
		mtinvFactory.create_wus(uc1, uc2, uc3, uc4, mc1, mc2, mc3, mc4, lc1, lc2, lc3, lc4);


		/*************************************************生成  GETPOINT.sh  并执行 结束*******************************************************************/


		/*************************************第7步，创建步骤三脚本文件（结束）***********************************************/
		//   xiugai  makeglib.csh
		log.info("Modify makeglib.csh");
		String cdcd = "cd " + MTINVPath;
		fu.writeTxtByStr(MTINVPath + "makeglib.csh", cdcd);

		/*************************************第8步，执行步骤三脚本文件***********************************************/

		// 1 makeglib.csh
		log.info("Run makeglib.csh");
		logWriter.append(ud.getStrDate() + ": 执行makeglib.csh \n\r");
		command = "csh " + MTINVPath + "makeglib.csh";
		runsh.runAndLog(command, logWriter);

		// 第一次修改run.csh
		log.info("Modify First run.csh");
		logWriter.append(ud.getStrDate() + ": 修改run.csh \n\r");
		mtinvFactory.changeRunCshFirstTime(rootPath);

		//生成shaixuan.sh
		log.info("Create File shaixuan.sh");
		logWriter.append(ud.getStrDate() + ": 创建shaixuan.sh \n\r");
		mtinvFactory.makeShaiXuanSh(rootPath);

		//执行shaixuan.sh
		log.info("Run shaixuan.sh");
		logWriter.append(ud.getStrDate() + ": 执行shaixuan.sh \n\r");
		command = "csh " + MTINVPath + "shaixuan.csh";
		runsh.runAndLog(command, logWriter);

		// 第二次修改run.csh
		log.info("Modify Second run.csh");
		logWriter.append(ud.getStrDate() + ": 修改run.csh \n\r");
		mtinvFactory.changeRunCshSecondTime(rootPath);

		// 3 run.csh
		log.info("Run run.csh");
		logWriter.append(ud.getStrDate() + ": 执行run.csh \n\r");
		command = "csh " + MTINVPath + "run.csh";
		runsh.runAndLog(command, logWriter);

		/*************************************第8步，执行步骤三脚本文件（结束）***********************************************/

		log.info("Run tar.sh");
		logWriter.append(ud.getStrDate() + ": 执行tar.sh \n\r");
		command = "sh " + MTINVPath + "tar.sh";
		runsh.runAndLog(command, logWriter);

		String endTimeTemp = ud.getStrDate();
		resultPath = MTINVPath + "result/";

		ReadTXT rt = new ReadTXT();
		String para = rt.getTxtPara(resultPath);
		para = para + ";" + resultPath;
		String[] paraArray = para.split(";");
		String result_m = paraArray[6];
		String result_s1 = paraArray[7];
		String result_d1 = paraArray[8];
		String result_r1 = paraArray[9];
		String result_s2 = paraArray[10];
		String result_d2 = paraArray[11];
		String result_r2 = paraArray[12];

		//create 111.cmt
		log.info("Create File 111.cmt");
		mtinvFactory.create_111(MTINVPath, lon, lat, depth, result_s1, result_d1, result_r1, result_m);

		//-----------------------------------------------画图----------------------------------------------------
		//获取画图参数
		double lat_d = Double.parseDouble(lat);
		double lon_d = Double.parseDouble(lon);
		double lonmax = lon_d + 0.5;
		double lonmin = lon_d - 0.5;
		double latmax = lat_d + 0.5;
		double latmin = lat_d - 0.5;
		String ddd = "EARE=\"" + lonmin + "/" + lonmax + "/" + latmin + "/" + latmax + "\"";

		//创建图脚本 draw-map.bat and draw-meca.bat
		log.info("Create File draw-map.bat 和 draw-meca.bat");
		logWriter.append(ud.getStrDate() + ": 创建 draw-map.bat 和 draw-meca.bat \n\r");
		mtinvFactory.create_draw(MTINVPath, ddd);

		//run draw-map.bat
		log.info("Run draw-map.bat");
		logWriter.append(ud.getStrDate() + ": 执行 draw-map.bat \n\r");
		command = "sh " + MTINVPath + "draw-map.bat";
		runsh.runAndLog(command, logWriter);

		//run draw-map.bat
		log.info("Run draw-meca.bat");
		logWriter.append(ud.getStrDate() + ": 执行 draw-meca.bat \n\r");
		command = "sh " + MTINVPath + "draw-meca.bat";
		runsh.runAndLog(command, logWriter);

		//-----------------------------------------------画图结束----------------------------------------------------

		//-----------------------------------------------生成word--------------------------------------------

		log.info("Create Word");
		logWriter.append(ud.getStrDate() + ": 创建word文档 \n\r");
		mtinvFactory.create_word(MTINVPath, o_time, lat_d, lon_d, depth, magnitude, localName, result_s1, result_d1, result_r1, result_s2, result_d2, result_r2, result_m);

		//-----------------------------------------------生成word结束--------------------------------------------
		log.info("Calculation Done！");
		logWriter.append(endTimeTemp + ": 计算完成 \n\r");
		logWriter.flush();

		return para;
	}

	/*自动计算结果: 手动添加地震计算*/
	public Map<String, String> doMath_auto(String lon, String lat, String depth, String o_time, String magnitude, String distance_min, String distance_max,
										   String filter_min, String filter_max, String institution, String location, String sn) throws ParseException, IOException{
		//开始计算时间
		String startTimeTemp = ud.getStrDate();
		String command = "";

		String RAWDATAPath = "";
		String SACPath = "";
		String MTINVPath = "";
		String resultPath = "";

		String saveurl_old = "";
		String saveurl_new = "";

		//国际时间
		String UTCTime = ud.getHours(o_time, -8);
		String fileTime = UTCTime.substring(0, 4) + UTCTime.substring(5, 7) + UTCTime.substring(8, 10) + UTCTime.substring(11, 13) + UTCTime.substring(14, 16) + UTCTime.substring(17, 19);
		String upTimeTemp = ud.getStrDate();
		String upTime = upTimeTemp.substring(0,4)+upTimeTemp.substring(5,7)+upTimeTemp.substring(8,10)+upTimeTemp.substring(11,13)+upTimeTemp.substring(14,16)+upTimeTemp.substring(17,19);
		String rootPath = "/autoMTInv/autoCompute/" + fileTime + "_" + upTime + "/";

		String seedName = UTCTime.replaceAll("-", "");
		seedName = seedName.replaceAll(":", "");
		seedName = seedName.replaceAll(" ", "");
		seedName = seedName + ".seed";

		new File(rootPath).mkdir();
		RAWDATAPath = rootPath + "RAWDATA/";
		new File(RAWDATAPath).mkdir();
		SACPath = rootPath + "SAC/";
		new File(SACPath).mkdir();
		MTINVPath = rootPath + "MTINV/";
		new File(MTINVPath).mkdir();


		//创建日志文件
		File logFile = new File(rootPath + "logfile.txt");
		logFile.createNewFile();
		//logFile写入
		BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), StandardCharsets.UTF_8)); // 指定编码格式，以免读取时中文字符异常

		//把seed文件复制到RAWDATA文件夹里
		saveurl_old = sn;
		saveurl_new = RAWDATAPath + seedName;
		fu.fileChannelCopy(new File(saveurl_old), new File(saveurl_new));

		//儒略日
		String OTCTime = "";
		int yy = Integer.parseInt(UTCTime.substring(0,4));
		int mm = Integer.parseInt(UTCTime.substring(5,7));
		int dd = Integer.parseInt(UTCTime.substring(8,10));
		OTCTime = "" + ud.getDate(yy, mm, dd);

		/**
		 *	步骤一、进行seed波形的解压及头文件的改写
		 *
		 *	步骤二、挑选符合震中距的台站
		 *
		 *	步骤三、进行格林函数脚本的准备、计算
		 */
		/*************************************第3步，创建步骤一的脚本文件***********************************************/

		//①	addinfounpackmtinv.sh
		log.info("Create File addinfounpackmtinv.sh");
		logWriter.append(ud.getStrDate() + ": 创建addinfounpackmtinv.sh \n\r");
		mtinvFactory.create_addinfounpackmtinv_manual(RAWDATAPath, lon, lat, depth, seedName);

		//②	cho.sm
		log.info("Create File cho.sm");
		logWriter.append(ud.getStrDate() + ": 创建cho.sm \n\r");
		mtinvFactory.create_cho(RAWDATAPath, UTCTime, OTCTime, lon, lat, depth);

		//③	cutdata.cmd
		log.info("Create File cutdata.cmd");
		logWriter.append(ud.getStrDate() + ": 创建cutdata.cmd \n\r");
		mtinvFactory.create_cutdata(RAWDATAPath);

		//④	syn.sh
		log.info("Create File syn.sh");
		logWriter.append(ud.getStrDate() + ": 创建syn.sh \n\r");
		mtinvFactory.create_syn(RAWDATAPath);

		//⑤	unpack.csh
		log.info("Create File unpack.csh");
		logWriter.append(ud.getStrDate() + ": 创建unpack.csh \n\r");
		mtinvFactory.create_unpack_manual(RAWDATAPath);
		/*************************************第3步，创建步骤一的脚本文件(结束)***********************************************/


		/*************************************第4步，执行步骤一的脚本文件***********************************************/

		// 1 addinfounpackmtinv.sh
		log.info("Run addinfounpackmtinv.sh");
		logWriter.append(ud.getStrDate() + ": 执行addinfounpackmtinv.sh \n\r");
		command = "bash " + RAWDATAPath + "addinfounpackmtinv.sh";
		runsh.runAndLog(command, logWriter);


		// 2 cutdata.cmd
		log.info("Run cutdata.cmd");
		logWriter.append(ud.getStrDate() + ": 执行cutdata.cmd \n\r");
		command = "bash " + RAWDATAPath + "cutdata.cmd";
		runsh.runAndLog(command, logWriter);


		// 3 syn.sh
		log.info("Run syn.sh");
		logWriter.append(ud.getStrDate() + ": 执行syn.sh \n\r");
		command = "bash " + RAWDATAPath + "syn.sh";
		runsh.runAndLog(command, logWriter);

		/*************************************第4步，执行步骤一的脚本文件（结束）***********************************************/


		/*************************************第5步，创建步骤二脚本文件（结束）***********************************************/

		//①	IDODIST.sh
		log.info("Create File IDODIST.sh");
		logWriter.append(ud.getStrDate() + ": 创建IDODIST.sh \n\r");
		mtinvFactory.create_IDODISH(SACPath, RAWDATAPath, distance_min, distance_max);

		//②	NETST.sh
		log.info("Create File NETST.sh");
		logWriter.append(ud.getStrDate() + ": 创建NETST.sh \n\r");
		mtinvFactory.create_NETST(SACPath);

		/*************************************第5步，创建步骤二脚本文件（结束）*****************************************/


		/*************************************第6步，执行步骤二脚本文件***********************************************/

		// 1 IDODIST.sh
		log.info("Run IDODIST.sh");
		logWriter.append(ud.getStrDate() + ": 执行IDODIST.sh \n\r");
		command = "bash " + SACPath + "IDODIST.sh";
		runsh.runAndLog(command, logWriter);


		// 2 NETST.Sh
		log.info("Run NETST.Sh");
		logWriter.append(ud.getStrDate() + ": 执行NETST.Sh \n\r");
		command = "bash " + SACPath + "NETST.sh";
		runsh.runAndLog(command, logWriter);

		/*************************************第6步，执行步骤二脚本文件（结束）*****************************************/


		/*************************************第7步，创建步骤三脚本文件***********************************************/


		//makeglib.csh
		log.info("Create File makeglib.csh");
		logWriter.append(ud.getStrDate() + ": 创建makeglib.csh \n\r");
		mtinvFactory.create_makeglib_auto(MTINVPath, RAWDATAPath, SACPath, UTCTime, lat, lon, filter_min, filter_max);

		//tar.sh
		log.info("Create File tar.sh");
		logWriter.append(ud.getStrDate() + ": 创建tar.sh \n\r");
		mtinvFactory.create_tar(MTINVPath, fileTime);

		/*************************************第7步，创建步骤三脚本文件（结束）***********************************************/

		//   xiugai  makeglib.csh
		log.info("Modify makeglib.csh");
		String cdcd = "cd " + MTINVPath;
		fu.writeTxtByStr(MTINVPath + "makeglib.csh", cdcd);

		/*************************************第8步，执行步骤三脚本文件***********************************************/

		// 1 makeglib.csh
		log.info("Run makeglib.csh");
		logWriter.append(ud.getStrDate() + ": 执行makeglib.csh \n\r");
		command = "csh " + MTINVPath + "makeglib.csh";
		runsh.runAndLog(command, logWriter);

		// 第一次修改run.csh
		log.info("Modify First run.csh");
		logWriter.append(ud.getStrDate() + ": 修改run.csh \n\r");
		mtinvFactory.changeRunCshFirstTime(rootPath);

		//生成shaixuan.csh
		log.info("Create File shaixuan.sh");
		logWriter.append(ud.getStrDate() + ": 创建shaixuan.sh \n\r");
		mtinvFactory.makeShaiXuanSh(rootPath);

		//执行shaixuan.csh
		log.info("Run shaixuan.sh");
		logWriter.append(ud.getStrDate() + ": 执行shaixuan.sh \n\r");
		command = "csh " + MTINVPath + "shaixuan.csh";
		runsh.runAndLog(command, logWriter);

		// 第二次修改run.csh
		log.info("Modify Second run.csh");
		logWriter.append(ud.getStrDate() + ": 修改run.csh \n\r");
		mtinvFactory.changeRunCshSecondTime(rootPath);

		// 3 run.csh
		log.info("Run run.csh");
		logWriter.append(ud.getStrDate() + ": 执行run.csh \n\r");
		command = "csh " + MTINVPath + "run.csh";
		runsh.runAndLog(command, logWriter);

		/*************************************第8步，执行步骤三脚本文件（结束）***********************************************/

		log.info("Run tar.sh");
		logWriter.append(ud.getStrDate() + ": 执行tar.sh \n\r");
		command = "sh " + MTINVPath + "tar.sh";
		runsh.runAndLog(command, logWriter);

		resultPath = MTINVPath + "result/";
		ReadTXT rt = new ReadTXT();
		String para = rt.getTxtPara(resultPath);
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
		String result_time = result_riqi + " " + result_sj;
		String cataid = institution + seedName.substring(0, seedName.length() - 4) + "AC";

		//计算结束时间
		String endTimeTemp = ud.getStrDate();

		//-----------------------------------------------画图----------------------------------------------------

		//create 111.cmt
		log.info("Create File 111.cmt");
		logWriter.append(ud.getStrDate() + ": 创建 111.cmt \n\r");
		mtinvFactory.create_111(MTINVPath, lon, lat, depth, result_s1, result_d1, result_r1, result_m);

		//获取画图参数
		double lat_d = Double.parseDouble(lat);
		double lon_d = Double.parseDouble(lon);
		double lonmax = lon_d + 0.5;
		double lonmin = lon_d - 0.5;
		double latmax = lat_d + 0.5;
		double latmin = lat_d - 0.5;
		String ddd = "EARE=\"" + lonmin + "/" + lonmax + "/" + latmin + "/" + latmax + "\"";

		//创建图脚本 draw-map.bat and draw-meca.bat
		log.info("Create File draw-map.bat 和 draw-meca.bat");
		logWriter.append(ud.getStrDate() + ": 创建 draw-map.bat 和 draw-meca.bat \n\r");
		mtinvFactory.create_draw(MTINVPath, ddd);

		//run draw-map.bat
		log.info("Run draw-map.bat");
		logWriter.append(ud.getStrDate() + ": 执行 draw-map.bat \n\r");
		command = "sh " + MTINVPath + "draw-map.bat";
		runsh.runAndLog(command, logWriter);

		//run draw-map.bat
		log.info("Run draw-meca.bat");
		logWriter.append(ud.getStrDate() + ": 执行 draw-meca.bat \n\r");
		command = "sh " + MTINVPath + "draw-meca.bat";
		runsh.runAndLog(command, logWriter);

		//-----------------------------------------------画图结束-------------------------------------------

		//-----------------------------------------------生成word--------------------------------------------

		log.info("Create Word");
		logWriter.append(ud.getStrDate() + ": 创建word文档 \n\r");
		mtinvFactory.create_word(MTINVPath, o_time, lat_d, lon_d, depth, magnitude, location,result_s1, result_d1, result_r1, result_s2, result_d2, result_r2, result_m);

		//----------------------------------------------生成word结束-----------------------------------------

		logWriter.append(endTimeTemp + ": 计算完成 \n\r");
		logWriter.flush();

		Map<String, String> map = new LinkedHashMap<>();
		map.put("cataid", cataid);
		map.put("o_time", o_time);
		map.put("lat", lat);
		map.put("lon", lon);
		map.put("depth", depth);
		map.put("m", magnitude);
		map.put("location", location);
		map.put("mind", distance_min);
		map.put("maxd", distance_max);
		map.put("minl", filter_min);
		map.put("maxl", filter_max);
		map.put("startTimeTemp", startTimeTemp);
		map.put("endTimeTemp", endTimeTemp);
		map.put("resultPath", resultPath);
		map.put("resultFile", result_file);
		map.put("resultTime", result_time);
		map.put("resultLon", result_lon);
		map.put("resultLat", result_lat);
		map.put("resultDepth", result_depth);
		map.put("result_m", result_m);
		map.put("result_s1", result_s1);
		map.put("result_d1", result_d1);
		map.put("result_r1", result_r1);
		map.put("result_s2", result_s2);
		map.put("result_d2", result_d2);
		map.put("result_r2", result_r2);
		map.put("result_pvr", result_pvr);
		map.put("rootPath", rootPath);


		String resultList = "";
		String showPath = resultPath;
		File resultFile = new File(resultPath);
		String[] rf = resultFile.list();
		Arrays.sort(rf);
		for (int i = 0; i < rf.length; i++) {
			if (rf[i].substring(rf[i].length() - 3, rf[i].length()).equals("jpg")) {
				resultList = resultList + showPath + rf[i] + ",";
			}
		}
		if (resultList.length() > 0) {
			resultList = resultList.substring(0, resultList.length() - 1);
		}
		map.put("resultList", resultList);
		log.info("Calculation Done");

		return map;
	}

	/*手动计算*/
	public Map<String, String> doMath_manual(HttpServletRequest request, String lon, String lat, String depth, String time, String magnitude,
								  String distance_min, String distance_max, String filter_min, String filter_max, String seedName) throws Exception {
		request.getSession().setAttribute("step", "0");

		String computePath = "/autoMTInv/compute/";
		String rootPath = "";
		String RAWDATAPath = "";
		String SACPath = "";
		String MTINVPath = "";
		String resultPath = "";
		String tarPath = "";
		String command = "";

		String saveurl_old = "";
		String saveurl_new = "";

		//国际时间
		String UTCTime = "";
		UTCTime = ud.getHours(time, -8);

		//创建文件夹
		String fileTime = UTCTime.substring(0, 4) + UTCTime.substring(5, 7) + UTCTime.substring(8, 10) + UTCTime.substring(11, 13) + UTCTime.substring(14, 16) + UTCTime.substring(17, 19);
		String upTimeTemp = ud.getStrDate();
		String upTime = upTimeTemp.substring(0, 4) + upTimeTemp.substring(5, 7) + upTimeTemp.substring(8, 10) + upTimeTemp.substring(11, 13) + upTimeTemp.substring(14, 16) + upTimeTemp.substring(17, 19);

		rootPath = computePath + fileTime + "_" + upTime + "/";
		new File(rootPath).mkdir();
		RAWDATAPath = rootPath + "RAWDATA/";
		new File(RAWDATAPath).mkdir();
		SACPath = rootPath + "SAC/";
		new File(SACPath).mkdir();
		MTINVPath = rootPath + "MTINV/";
		new File(MTINVPath).mkdir();

		//把seed文件复制到RAWDATA文件夹里
		saveurl_old = computePath + seedName;
		saveurl_new = RAWDATAPath + seedName;
		fu.fileChannelCopy(new File(saveurl_old), new File(saveurl_new));

		//儒略日
		String OTCTime = "";
		int yy = Integer.parseInt(UTCTime.substring(0,4));
		int mm = Integer.parseInt(UTCTime.substring(5,7));
		int dd = Integer.parseInt(UTCTime.substring(8,10));
		OTCTime = "" + ud.getDate(yy, mm, dd);

		/*************************************第2步，获取参数（结束）***********************************************/

		/**
		 *	步骤一、进行seed波形的解压及头文件的改写
		 *
		 *	步骤二、挑选符合震中距的台站
		 *
		 *	步骤三、进行格林函数脚本的准备、计算
		 *
		 */
		/*************************************第3步，创建步骤一的脚本文件***********************************************/

		//①	addinfounpackmtinv.sh
		log.info("Create File addinfounpackmtinv.sh");
		mtinvFactory.create_addinfounpackmtinv_manual(RAWDATAPath, lon, lat, depth, seedName);

		//②	cho.sm
		log.info("Create File cho.sm");
		mtinvFactory.create_cho(RAWDATAPath, UTCTime, OTCTime, lon, lat, depth);

		//③	cutdata.cmd
		log.info("Create File cutdata.cmd");
		mtinvFactory.create_cutdata(RAWDATAPath);

		//④	syn.sh
		log.info("Create File syn.sh");
		mtinvFactory.create_syn(RAWDATAPath);

		//⑤	unpack.csh
		log.info("Create File unpack.csh");
		mtinvFactory.create_unpack_manual(RAWDATAPath);

		//*************************************第3步，创建步骤一的脚本文件(结束)***********************************************/


		//*************************************第4步，执行步骤一的脚本文件***********************************************/

		// 1 addinfounpackmtinv.sh
		log.info("Run addinfounpackmtinv.sh");
		command = "bash " + RAWDATAPath + "addinfounpackmtinv.sh";
		runsh.rsh(command);


		// 2 cutdata.cmd
		log.info("Run cutdata.cmd");
		command = "bash " + RAWDATAPath + "cutdata.cmd";
		runsh.rsh(command);


		// 3 syn.sh
		log.info("Run syn.sh");
		command = "bash " + RAWDATAPath + "syn.sh";
		runsh.rsh(command);

		//*************************************第4步，执行步骤一的脚本文件（结束）***********************************************/


		request.getSession().setAttribute("step", "2");


		//*************************************第5步，创建步骤二脚本文件***********************************************/

		//①	IDODIST.sh
		log.info("Create File IDODIST.sh");
		mtinvFactory.create_IDODISH(SACPath, RAWDATAPath, distance_min, distance_max);

		//②	NETST.sh
		log.info("Create File NETST.sh");
		mtinvFactory.create_NETST(SACPath);

		//*************************************第5步，创建步骤二脚本文件（结束）***********************************************/


		//*************************************第6步，执行步骤二脚本文件***********************************************/

		// 1 IDODIST.sh
		log.info("Run IDODIST.sh");
		command = "bash " + SACPath + "IDODIST.sh";
		runsh.rsh(command);

		// 2 NETST.Sh
		log.info("Run NETST.sh");
		command = "bash " + SACPath + "NETST.sh";
		runsh.rsh(command);

		//*************************************第6步，执行步骤二脚本文件（结束）***********************************************/

		request.getSession().setAttribute("step", "3");

		//*************************************第7步，创建步骤三脚本文件***********************************************/

		//makeglib.csh
		log.info("Create File makeglib.csh");
		mtinvFactory.create_makeglib_manual(MTINVPath, RAWDATAPath, SACPath, UTCTime, lat, lon, filter_min, filter_max);

		//tar.sh
		log.info("Create File tar.sh");
		mtinvFactory.create_tar(MTINVPath, fileTime);

		/***************************************************************************************************************

		 2017-05-05
		 取速度模型
		 生成  GETPOINT.sh  并执行

		 ***************************************************************************************************************/
		//1、创建 GETPOINT.sh
		log.info("Create File GETPOINT.sh");
		mtinvFactory.create_GETPOINT(lon, lat);

		//2、执行 GETPOINT.sh
		log.info("Run GETPOINT.sh");
		command = "bash /autoMTInv/modeldb/crust2.0/GETPOINT.sh";
		runsh.rsh(command);

		//3、读取outcr
		String se1 = "";
		String se2 = "";
		String se3 = "";
		String se4 = "";
		String he1 = "";
		String he2 = "";
		String he3 = "";
		String he4 = "";
		String uc1 = "";
		String uc2 = "";
		String uc3 = "";
		String uc4 = "";
		String mc1 = "";
		String mc2 = "";
		String mc3 = "";
		String mc4 = "";
		String lc1 = "";
		String lc2 = "";
		String lc3 = "";
		String lc4 = "";
		File outcr = new File("/autoMTInv/modeldb/crust2.0/outcr");
		InputStreamReader read0 = new InputStreamReader(new FileInputStream(outcr));
		BufferedReader bufferedReader0 = new BufferedReader(read0);
		String lineTxt0 = null;
		while ((lineTxt0 = bufferedReader0.readLine()) != null) {
			if (lineTxt0.indexOf("soft sed") > 0) {
				String se = lineTxt0;
				se = se.replaceAll(" ", "");
				String[] sel = se.split("\\.");
				se1 = sel[0] + "." + sel[1].substring(0, 2);
				se2 = sel[1].substring(4, sel[1].length()) + "." + sel[2].substring(0, 2);
				se3 = sel[2].substring(4, sel[2].length()) + "." + sel[3].substring(0, 2);
				se4 = sel[3].substring(4, sel[3].length()) + "." + sel[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("hard sed") > 0) {
				String he = lineTxt0;
				he = he.replaceAll(" ", "");
				String[] hel = he.split("\\.");
				he1 = hel[0] + "." + hel[1].substring(0, 2);
				he2 = hel[1].substring(4, hel[1].length()) + "." + hel[2].substring(0, 2);
				he3 = hel[2].substring(4, hel[2].length()) + "." + hel[3].substring(0, 2);
				he4 = hel[3].substring(4, hel[3].length()) + "." + hel[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("upper crust") > 0) {
				String uc = lineTxt0;
				uc = uc.replaceAll(" ", "");
				String[] ucl = uc.split("\\.");
				uc1 = ucl[0] + "." + ucl[1].substring(0, 2);
				uc2 = ucl[1].substring(4, ucl[1].length()) + "." + ucl[2].substring(0, 2);
				uc3 = ucl[2].substring(4, ucl[2].length()) + "." + ucl[3].substring(0, 2);
				uc4 = ucl[3].substring(4, ucl[3].length()) + "." + ucl[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("middle crust") > 0) {
				String mc = lineTxt0;
				mc = mc.replaceAll(" ", "");
				String[] mcl = mc.split("\\.");
				mc1 = mcl[0] + "." + mcl[1].substring(0, 2);
				mc2 = mcl[1].substring(4, mcl[1].length()) + "." + mcl[2].substring(0, 2);
				mc3 = mcl[2].substring(4, mcl[2].length()) + "." + mcl[3].substring(0, 2);
				mc4 = mcl[3].substring(4, mcl[3].length()) + "." + mcl[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("lower crust") > 0) {
				String lc = lineTxt0;
				lc = lc.replaceAll(" ", "");
				String[] lcl = lc.split("\\.");
				lc1 = lcl[0] + "." + lcl[1].substring(0, 2);
				lc2 = lcl[1].substring(4, lcl[1].length()) + "." + lcl[2].substring(0, 2);
				lc3 = lcl[2].substring(4, lcl[2].length()) + "." + lcl[3].substring(0, 2);
				lc4 = lcl[3].substring(4, lcl[3].length()) + "." + lcl[4].substring(0, 2);
			}
		}

		//4、创建 wus.mod
		log.info("Create File wus.mod");
		mtinvFactory.create_wus(uc1, uc2, uc3, uc4, mc1, mc2, mc3, mc4, lc1, lc2, lc3, lc4);

		/**************************************生成  GETPOINT.sh  并执行 结束******************************************************/


		/*************************************第7步，创建步骤三脚本文件（结束）***********************************************/
		//   xiugai  makeglib.csh
		log.info("Modify makeglib.csh");
		String cdcd = "cd " + MTINVPath;
		fu.writeTxtByStr(MTINVPath + "makeglib.csh", cdcd);

		/*************************************第8步，执行步骤三脚本文件***********************************************/

		// 1 makeglib.csh
		log.info("Run makeglib.csh");
		command = "csh " + MTINVPath + "makeglib.csh";
		runsh.rsh(command);

		request.getSession().setAttribute("step", "4");

		// 第一次修改run.csh
		log.info("Modify First run.csh");
		mtinvFactory.changeRunCshFirstTime(rootPath);


		//生成shaixuan.sh
		log.info("Create File shaixuan.sh");
		mtinvFactory.makeShaiXuanSh(rootPath);


		//执行shaixuan.sh
		log.info("Run shaixuan.csh");
		command = "csh " + MTINVPath + "shaixuan.csh";
		runsh.rsh(command);


		// 第二次修改run.csh
		log.info("Modify Second run.csh");
		mtinvFactory.changeRunCshSecondTime(rootPath);


		// 3 run.csh
		log.info("Run run.csh");
		command = "csh " + MTINVPath + "run.csh";
		runsh.rsh(command);

		/*************************************第8步，执行步骤三脚本文件（结束）***********************************************/

		log.info("Run tar.sh");
		command = "bash " + MTINVPath + "tar.sh";
		runsh.rsh(command);

		/*************************************第9步，执行步骤三脚本文件***********************************************/

		//创建gmtmap1.csh

		//gmtmap1.csh
		log.info("Create File gmtmap1.csh");
		mtinvFactory.create_gmtmap1(MTINVPath);


		//运行gmtmap1.csh
		log.info("Run gmtmap1.csh");
		command = "csh " + MTINVPath + "gmtmap1.csh";
		runsh.rsh(command);

		String endTimeTemp = ud.getStrDate();

		//-----------------------------------------------读取txt文件----------------------------------------------------
		request.getSession().setAttribute("step", "5");
		resultPath = MTINVPath + "result/";

		ReadTXT rt = new ReadTXT();
		String para = rt.getTxtPara(resultPath);
		String[] paraArray = para.split(";");
		String result_file = paraArray[0];
		String result_date = paraArray[1];
		String result_time = paraArray[2];
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
		String result_datetime = result_date + " " + result_time;

		// 筛选最优解
		int email_index = result_file.indexOf("email");
		String[] ls = result_file.substring(email_index).split("_");
		String result_label = ls[1] + "_" + ls[2];
		log.info("Create File tar_best.sh");
		mtinvFactory.create_tar_best(MTINVPath, fileTime,result_label);
		log.info("Run tar_best.sh");
		command = "bash " + MTINVPath + "tar_best.sh";
		runsh.rsh(command);

		String resultList = "";
		String showPath = resultPath;
		File resultFile = new File(resultPath);
		String[] rf = resultFile.list();
		Arrays.sort(rf);
//		for (String s : rf) {
//			if (s.substring(s.length() - 3).equals("jpg")) {
//				resultList = resultList + showPath + s + ",";
//			}
//		}
		for (String s : rf) {
			if (s.substring(s.length() - 3).equals("jpg")) {
				if (s.contains(result_label) || s.contains("gmtmap") || s.contains("plotmech") || s.contains("plotz") || s.contains("results")) {
					resultList = resultList + showPath + s + ",";
				}
			}

		}
		if (resultList.length() > 0) {
			resultList = resultList.substring(0, resultList.length() - 1);
		}

		//-----------------------------------------------读取txt文件结束----------------------------------------------------

		log.info("Create File 111.cmt");
		mtinvFactory.create_111(MTINVPath, lon, lat, depth, result_s1, result_d1, result_r1, result_m);

		//-----------------------------------------------画图----------------------------------------------------

		//获取画图参数
		double lat_d = Double.parseDouble(lat);
		double lon_d = Double.parseDouble(lon);
		double lonmax = lon_d + 0.5;
		double lonmin = lon_d - 0.5;
		double latmax = lat_d + 0.5;
		double latmin = lat_d - 0.5;
		String ddd = "EARE=\"" + lonmin + "/" + lonmax + "/" + latmin + "/" + latmax + "\"";


		//两张图脚本 draw-map.bat draw-meca.bat
		log.info("Create File draw-map.bat 和 draw-meca.bat");
		mtinvFactory.create_draw(MTINVPath, ddd);

		//run draw-map.bat
		log.info("Run draw-map.bat");
		command = "bash " + MTINVPath + "draw-map.bat";
		runsh.rsh(command);

		//run draw-map.bat
		log.info("Run draw-map.bat");
		command = "bash " + MTINVPath + "draw-meca.bat";
		runsh.rsh(command);

		//-----------------------------------------------画图结束----------------------------------------------------

		//-----------------------------------------------生成word---------------------------------------------------

		log.info("Create Word");
		mtinvFactory.create_word(MTINVPath, time, lat_d, lon_d, depth, magnitude, "地名",
				result_s1, result_d1, result_r1, result_s2, result_d2, result_r2, result_m);

		//-----------------------------------------------生成word结束------------------------------------------------
		tarPath = MTINVPath + fileTime + "_best.tar";

		//将参数存储在map中返回
		Map<String, String> map = new LinkedHashMap<>();
		map.put("time", time);
		map.put("lat", lat);
		map.put("lon", lon);
		map.put("depth", depth);
		map.put("magnitude", magnitude);
		map.put("distance_min", distance_min);
		map.put("distance_max", distance_max);
		map.put("filter_min", filter_min);
		map.put("filter_max", filter_max);
		map.put("upTimeTemp", upTimeTemp);
		map.put("endTimeTemp", endTimeTemp);
		map.put("resultPath", resultPath);
		map.put("result_file", result_file);
		map.put("result_time", result_datetime);
		map.put("result_lon", result_lon);
		map.put("result_lat", result_lat);
		map.put("result_depth", result_depth);
		map.put("result_m", result_m);
		map.put("result_s1", result_s1);
		map.put("result_r1", result_r1);
		map.put("result_d1", result_d1);
		map.put("result_s2", result_s2);
		map.put("result_r2", result_r2);
		map.put("result_d2", result_d2);
		map.put("result_pvr", result_pvr);
		map.put("tarPath", tarPath);
		map.put("resultList", resultList);
		map.put("rootPath", rootPath);

		log.info("Calculation Done!");
		return map;
	}

	/*批量计算*/
	public Map<String, String> doMath_manual_batch(String lon, String lat, String depth, String time, String magnitude,
											 String distance_min, String distance_max, String filter_min, String filter_max, String seedName) throws Exception {
		String computePath = "/autoMTInv/compute/";
		String rootPath = "";
		String RAWDATAPath = "";
		String SACPath = "";
		String MTINVPath = "";
		String resultPath = "";
		String tarPath = "";
		String command = "";

		String saveurl_old = "";
		String saveurl_new = "";

		//国际时间
		String UTCTime = "";
		UTCTime = ud.getHours(time, -8);

		//创建文件夹
		String fileTime = UTCTime.substring(0, 4) + UTCTime.substring(5, 7) + UTCTime.substring(8, 10) + UTCTime.substring(11, 13) + UTCTime.substring(14, 16) + UTCTime.substring(17, 19);
		String upTimeTemp = ud.getStrDate();
		String upTime = upTimeTemp.substring(0, 4) + upTimeTemp.substring(5, 7) + upTimeTemp.substring(8, 10) + upTimeTemp.substring(11, 13) + upTimeTemp.substring(14, 16) + upTimeTemp.substring(17, 19);

		rootPath = computePath + fileTime + "_" + upTime + "/";
		new File(rootPath).mkdir();
		RAWDATAPath = rootPath + "RAWDATA/";
		new File(RAWDATAPath).mkdir();
		SACPath = rootPath + "SAC/";
		new File(SACPath).mkdir();
		MTINVPath = rootPath + "MTINV/";
		new File(MTINVPath).mkdir();

		//把seed文件复制到RAWDATA文件夹里
		saveurl_old = computePath + seedName;
		saveurl_new = RAWDATAPath + seedName;
		fu.fileChannelCopy(new File(saveurl_old), new File(saveurl_new));

		//儒略日
		String OTCTime = "";
		int yy = Integer.parseInt(UTCTime.substring(0,4));
		int mm = Integer.parseInt(UTCTime.substring(5,7));
		int dd = Integer.parseInt(UTCTime.substring(8,10));
		OTCTime = "" + ud.getDate(yy, mm, dd);

		/*************************************第2步，获取参数（结束）***********************************************/

		/**
		 *	步骤一、进行seed波形的解压及头文件的改写
		 *
		 *	步骤二、挑选符合震中距的台站
		 *
		 *	步骤三、进行格林函数脚本的准备、计算
		 *
		 */
		/*************************************第3步，创建步骤一的脚本文件***********************************************/

		//①	addinfounpackmtinv.sh
		log.info("Create File addinfounpackmtinv.sh");
		mtinvFactory.create_addinfounpackmtinv_manual(RAWDATAPath, lon, lat, depth, seedName);

		//②	cho.sm
		log.info("Create File cho.sm");
		mtinvFactory.create_cho(RAWDATAPath, UTCTime, OTCTime, lon, lat, depth);

		//③	cutdata.cmd
		log.info("Create File cutdata.cmd");
		mtinvFactory.create_cutdata(RAWDATAPath);

		//④	syn.sh
		log.info("Create File syn.sh");
		mtinvFactory.create_syn(RAWDATAPath);

		//⑤	unpack.csh
		log.info("Create File unpack.csh");
		mtinvFactory.create_unpack_manual(RAWDATAPath);

		//*************************************第3步，创建步骤一的脚本文件(结束)***********************************************/


		//*************************************第4步，执行步骤一的脚本文件***********************************************/

		// 1 addinfounpackmtinv.sh
		log.info("Run addinfounpackmtinv.sh");
		command = "bash " + RAWDATAPath + "addinfounpackmtinv.sh";
		runsh.rsh(command);


		// 2 cutdata.cmd
		log.info("Run cutdata.cmd");
		command = "bash " + RAWDATAPath + "cutdata.cmd";
		runsh.rsh(command);


		// 3 syn.sh
		log.info("Run syn.sh");
		command = "bash " + RAWDATAPath + "syn.sh";
		runsh.rsh(command);

		//*************************************第4步，执行步骤一的脚本文件（结束）***********************************************/


		//*************************************第5步，创建步骤二脚本文件***********************************************/

		//①	IDODIST.sh
		log.info("Create File IDODIST.sh");
		mtinvFactory.create_IDODISH(SACPath, RAWDATAPath, distance_min, distance_max);

		//②	NETST.sh
		log.info("Create File NETST.sh");
		mtinvFactory.create_NETST(SACPath);

		//*************************************第5步，创建步骤二脚本文件（结束）***********************************************/


		//*************************************第6步，执行步骤二脚本文件***********************************************/

		// 1 IDODIST.sh
		log.info("Run IDODIST.sh");
		command = "bash " + SACPath + "IDODIST.sh";
		runsh.rsh(command);

		// 2 NETST.Sh
		log.info("Run NETST.sh");
		command = "bash " + SACPath + "NETST.sh";
		runsh.rsh(command);

		//*************************************第6步，执行步骤二脚本文件（结束）***********************************************/


		//*************************************第7步，创建步骤三脚本文件***********************************************/

		//makeglib.csh
		log.info("Create File makeglib.csh");
		mtinvFactory.create_makeglib_manual(MTINVPath, RAWDATAPath, SACPath, UTCTime, lat, lon, filter_min, filter_max);

		//tar.sh
		log.info("Create File tar.sh");
		mtinvFactory.create_tar(MTINVPath, fileTime);

		/***************************************************************************************************************

		 2017-05-05
		 取速度模型
		 生成  GETPOINT.sh  并执行

		 ***************************************************************************************************************/
		//1、创建 GETPOINT.sh
		log.info("Create File GETPOINT.sh");
		mtinvFactory.create_GETPOINT(lon, lat);

		//2、执行 GETPOINT.sh
		log.info("Run GETPOINT.sh");
		command = "bash /autoMTInv/modeldb/crust2.0/GETPOINT.sh";
		runsh.rsh(command);

		//3、读取outcr
		String se1 = "";
		String se2 = "";
		String se3 = "";
		String se4 = "";
		String he1 = "";
		String he2 = "";
		String he3 = "";
		String he4 = "";
		String uc1 = "";
		String uc2 = "";
		String uc3 = "";
		String uc4 = "";
		String mc1 = "";
		String mc2 = "";
		String mc3 = "";
		String mc4 = "";
		String lc1 = "";
		String lc2 = "";
		String lc3 = "";
		String lc4 = "";
		File outcr = new File("/autoMTInv/modeldb/crust2.0/outcr");
		InputStreamReader read0 = new InputStreamReader(new FileInputStream(outcr));
		BufferedReader bufferedReader0 = new BufferedReader(read0);
		String lineTxt0 = null;
		while ((lineTxt0 = bufferedReader0.readLine()) != null) {
			if (lineTxt0.indexOf("soft sed") > 0) {
				String se = lineTxt0;
				se = se.replaceAll(" ", "");
				String[] sel = se.split("\\.");
				se1 = sel[0] + "." + sel[1].substring(0, 2);
				se2 = sel[1].substring(4, sel[1].length()) + "." + sel[2].substring(0, 2);
				se3 = sel[2].substring(4, sel[2].length()) + "." + sel[3].substring(0, 2);
				se4 = sel[3].substring(4, sel[3].length()) + "." + sel[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("hard sed") > 0) {
				String he = lineTxt0;
				he = he.replaceAll(" ", "");
				String[] hel = he.split("\\.");
				he1 = hel[0] + "." + hel[1].substring(0, 2);
				he2 = hel[1].substring(4, hel[1].length()) + "." + hel[2].substring(0, 2);
				he3 = hel[2].substring(4, hel[2].length()) + "." + hel[3].substring(0, 2);
				he4 = hel[3].substring(4, hel[3].length()) + "." + hel[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("upper crust") > 0) {
				String uc = lineTxt0;
				uc = uc.replaceAll(" ", "");
				String[] ucl = uc.split("\\.");
				uc1 = ucl[0] + "." + ucl[1].substring(0, 2);
				uc2 = ucl[1].substring(4, ucl[1].length()) + "." + ucl[2].substring(0, 2);
				uc3 = ucl[2].substring(4, ucl[2].length()) + "." + ucl[3].substring(0, 2);
				uc4 = ucl[3].substring(4, ucl[3].length()) + "." + ucl[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("middle crust") > 0) {
				String mc = lineTxt0;
				mc = mc.replaceAll(" ", "");
				String[] mcl = mc.split("\\.");
				mc1 = mcl[0] + "." + mcl[1].substring(0, 2);
				mc2 = mcl[1].substring(4, mcl[1].length()) + "." + mcl[2].substring(0, 2);
				mc3 = mcl[2].substring(4, mcl[2].length()) + "." + mcl[3].substring(0, 2);
				mc4 = mcl[3].substring(4, mcl[3].length()) + "." + mcl[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("lower crust") > 0) {
				String lc = lineTxt0;
				lc = lc.replaceAll(" ", "");
				String[] lcl = lc.split("\\.");
				lc1 = lcl[0] + "." + lcl[1].substring(0, 2);
				lc2 = lcl[1].substring(4, lcl[1].length()) + "." + lcl[2].substring(0, 2);
				lc3 = lcl[2].substring(4, lcl[2].length()) + "." + lcl[3].substring(0, 2);
				lc4 = lcl[3].substring(4, lcl[3].length()) + "." + lcl[4].substring(0, 2);
			}
		}

		//4、创建 wus.mod
		log.info("Create File wus.mod");
		mtinvFactory.create_wus(uc1, uc2, uc3, uc4, mc1, mc2, mc3, mc4, lc1, lc2, lc3, lc4);

		/**************************************生成  GETPOINT.sh  并执行 结束******************************************************/


		/*************************************第7步，创建步骤三脚本文件（结束）***********************************************/
		//   xiugai  makeglib.csh
		log.info("Modify makeglib.csh");
		String cdcd = "cd " + MTINVPath;
		fu.writeTxtByStr(MTINVPath + "makeglib.csh", cdcd);

		/*************************************第8步，执行步骤三脚本文件***********************************************/

		// 1 makeglib.csh
		log.info("Run makeglib.csh");
		command = "csh " + MTINVPath + "makeglib.csh";
		runsh.rsh(command);

		// 第一次修改run.csh
		log.info("Modify First run.csh");
		mtinvFactory.changeRunCshFirstTime(rootPath);


		//生成shaixuan.sh
		log.info("Create File shaixuan.sh");
		mtinvFactory.makeShaiXuanSh(rootPath);


		//执行shaixuan.sh
		log.info("Run shaixuan.csh");
		command = "csh " + MTINVPath + "shaixuan.csh";
		runsh.rsh(command);


		// 第二次修改run.csh
		log.info("Modify Second run.csh");
		mtinvFactory.changeRunCshSecondTime(rootPath);


		// 3 run.csh
		log.info("Run run.csh");
		command = "csh " + MTINVPath + "run.csh";
		runsh.rsh(command);

		/*************************************第8步，执行步骤三脚本文件（结束）***********************************************/

		log.info("Run tar.sh");
		command = "bash " + MTINVPath + "tar.sh";
		runsh.rsh(command);

		/*************************************第9步，执行步骤三脚本文件***********************************************/

		//创建gmtmap1.csh

		//gmtmap1.csh
		log.info("Create File gmtmap1.csh");
		mtinvFactory.create_gmtmap1(MTINVPath);


		//运行gmtmap1.csh
		log.info("Run gmtmap1.csh");
		command = "csh " + MTINVPath + "gmtmap1.csh";
		runsh.rsh(command);

		String endTimeTemp = ud.getStrDate();

		//-----------------------------------------------读取txt文件----------------------------------------------------
		resultPath = MTINVPath + "result/";

		ReadTXT rt = new ReadTXT();
		String para = rt.getTxtPara(resultPath);
		String[] paraArray = para.split(";");
		String result_file = paraArray[0];
		String result_date = paraArray[1];
		String result_time = paraArray[2];
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
		String result_datetime = result_date + " " + result_time;

		// 筛选最优解
		int email_index = result_file.indexOf("email");
		String[] ls = result_file.substring(email_index).split("_");
		String result_label = ls[1] + "_" + ls[2];
		log.info("Create File tar_best.sh");
		mtinvFactory.create_tar_best(MTINVPath, fileTime,result_label);
		log.info("Run tar_best.sh");
		command = "bash " + MTINVPath + "tar_best.sh";
		runsh.rsh(command);

		String resultList = "";
		String showPath = resultPath;
		File resultFile = new File(resultPath);
		String[] rf = resultFile.list();
		Arrays.sort(rf);
//		for (String s : rf) {
//			if (s.substring(s.length() - 3).equals("jpg")) {
//				resultList = resultList + showPath + s + ",";
//			}
//		}
		for (String s : rf) {
			if (s.substring(s.length() - 3).equals("jpg")) {
				if (s.contains(result_label) || s.contains("gmtmap") || s.contains("plotmech") || s.contains("plotz") || s.contains("results")) {
					resultList = resultList + showPath + s + ",";
				}
			}

		}
		if (resultList.length() > 0) {
			resultList = resultList.substring(0, resultList.length() - 1);
		}

		//-----------------------------------------------读取txt文件结束----------------------------------------------------

		log.info("Create File 111.cmt");
		mtinvFactory.create_111(MTINVPath, lon, lat, depth, result_s1, result_d1, result_r1, result_m);

		//-----------------------------------------------画图----------------------------------------------------

		//获取画图参数
		double lat_d = Double.parseDouble(lat);
		double lon_d = Double.parseDouble(lon);
		double lonmax = lon_d + 0.5;
		double lonmin = lon_d - 0.5;
		double latmax = lat_d + 0.5;
		double latmin = lat_d - 0.5;
		String ddd = "EARE=\"" + lonmin + "/" + lonmax + "/" + latmin + "/" + latmax + "\"";


		//两张图脚本 draw-map.bat draw-meca.bat
		log.info("Create File draw-map.bat 和 draw-meca.bat");
		mtinvFactory.create_draw(MTINVPath, ddd);

		//run draw-map.bat
		log.info("Run draw-map.bat");
		command = "bash " + MTINVPath + "draw-map.bat";
		runsh.rsh(command);

		//run draw-map.bat
		log.info("Run draw-map.bat");
		command = "bash " + MTINVPath + "draw-meca.bat";
		runsh.rsh(command);

		//-----------------------------------------------画图结束----------------------------------------------------

		//-----------------------------------------------生成word---------------------------------------------------

		log.info("Create Word");
		mtinvFactory.create_word(MTINVPath, time, lat_d, lon_d, depth, magnitude, "地名",
				result_s1, result_d1, result_r1, result_s2, result_d2, result_r2, result_m);

		//-----------------------------------------------生成word结束------------------------------------------------
		tarPath = MTINVPath + fileTime + "_best.tar";

		//将参数存储在map中返回
		Map<String, String> map = new LinkedHashMap<>();
		map.put("time", time);
		map.put("lat", lat);
		map.put("lon", lon);
		map.put("depth", depth);
		map.put("magnitude", magnitude);
		map.put("distance_min", distance_min);
		map.put("distance_max", distance_max);
		map.put("filter_min", filter_min);
		map.put("filter_max", filter_max);
		map.put("upTimeTemp", upTimeTemp);
		map.put("endTimeTemp", endTimeTemp);
		map.put("resultPath", resultPath);
		map.put("result_file", result_file);
		map.put("result_time", result_datetime);
		map.put("result_lon", result_lon);
		map.put("result_lat", result_lat);
		map.put("result_depth", result_depth);
		map.put("result_m", result_m);
		map.put("result_s1", result_s1);
		map.put("result_r1", result_r1);
		map.put("result_d1", result_d1);
		map.put("result_s2", result_s2);
		map.put("result_r2", result_r2);
		map.put("result_d2", result_d2);
		map.put("result_pvr", result_pvr);
		map.put("tarPath", tarPath);
		map.put("resultList", resultList);
		map.put("rootPath", rootPath);

		log.info("Calculation Done!");
		return map;
	}


	/*多套滤波参数手动计算*/
	public Map<String, String> doMath_manual_batch(String lon, String lat, String depth, String time, String magnitude, String distance_min, String distance_max, String[] filters, String seedName) throws Exception {
		String computePath = "/autoMTInv/compute/";
		String rootPath = "";
		String RAWDATAPath = "";
		String SACPath = "";
		String MTINVPath = "";
		String resultPath = "";
		String tarPath = "";
		String command = "";
		String saveurl_old = "";
		String saveurl_new = "";

		//国际时间
		String UTCTime = "";
		UTCTime = ud.getHours(time, -8);

		//创建文件夹
		String fileTime = UTCTime.substring(0, 4) + UTCTime.substring(5, 7) + UTCTime.substring(8, 10) + UTCTime.substring(11, 13) + UTCTime.substring(14, 16) + UTCTime.substring(17, 19);
		String upTimeTemp = ud.getStrDate();
		String upTime = upTimeTemp.substring(0, 4) + upTimeTemp.substring(5, 7) + upTimeTemp.substring(8, 10) + upTimeTemp.substring(11, 13) + upTimeTemp.substring(14, 16) + upTimeTemp.substring(17, 19);

		rootPath = computePath + fileTime + "_" + upTime + "/";
		new File(rootPath).mkdir();
		RAWDATAPath = rootPath + "RAWDATA/";
		new File(RAWDATAPath).mkdir();
		SACPath = rootPath + "SAC/";
		new File(SACPath).mkdir();
		MTINVPath = rootPath + "MTINV/";
		new File(MTINVPath).mkdir();

		//把seed文件复制到RAWDATA文件夹里
		saveurl_old = computePath + seedName;
		saveurl_new = RAWDATAPath + seedName;
		fu.fileChannelCopy(new File(saveurl_old), new File(saveurl_new));

		//儒略日
		String OTCTime = "";
		int yy = Integer.parseInt(UTCTime.substring(0,4));
		int mm = Integer.parseInt(UTCTime.substring(5,7));
		int dd = Integer.parseInt(UTCTime.substring(8,10));
		OTCTime = "" + ud.getDate(yy, mm, dd);

		/*************************************第2步，获取参数（结束）***********************************************/

		/**
		 *	步骤一、进行seed波形的解压及头文件的改写
		 *
		 *	步骤二、挑选符合震中距的台站
		 *
		 *	步骤三、进行格林函数脚本的准备、计算
		 *
		 */
		/*************************************第3步，创建步骤一的脚本文件***********************************************/

		//①	addinfounpackmtinv.sh
		log.info("Create File addinfounpackmtinv.sh");
		mtinvFactory.create_addinfounpackmtinv_manual(RAWDATAPath, lon, lat, depth, seedName);

		//②	cho.sm
		log.info("Create File cho.sm");
		mtinvFactory.create_cho(RAWDATAPath, UTCTime, OTCTime, lon, lat, depth);

		//③	cutdata.cmd
		log.info("Create File cutdata.cmd");
		mtinvFactory.create_cutdata(RAWDATAPath);

		//④	syn.sh
		log.info("Create File syn.sh");
		mtinvFactory.create_syn(RAWDATAPath);

		//⑤	unpack.csh
		log.info("Create File unpack.csh");
		mtinvFactory.create_unpack_manual(RAWDATAPath);

		//*************************************第3步，创建步骤一的脚本文件(结束)***********************************************/


		//*************************************第4步，执行步骤一的脚本文件***********************************************/

		// 1 addinfounpackmtinv.sh
		log.info("Run addinfounpackmtinv.sh");
		command = "bash " + RAWDATAPath + "addinfounpackmtinv.sh";
		runsh.rsh(command);


		// 2 cutdata.cmd
		log.info("Run cutdata.cmd");
		command = "bash " + RAWDATAPath + "cutdata.cmd";
		runsh.rsh(command);


		// 3 syn.sh
		log.info("Run syn.sh");
		command = "bash " + RAWDATAPath + "syn.sh";
		runsh.rsh(command);

		//*************************************第4步，执行步骤一的脚本文件（结束）***********************************************/


		//*************************************第5步，创建步骤二脚本文件***********************************************/

		//①	IDODIST.sh
		log.info("Create File IDODIST.sh");
		mtinvFactory.create_IDODISH(SACPath, RAWDATAPath, distance_min, distance_max);

		//②	NETST.sh
		log.info("Create File NETST.sh");
		mtinvFactory.create_NETST(SACPath);

		//*************************************第5步，创建步骤二脚本文件（结束）***********************************************/


		//*************************************第6步，执行步骤二脚本文件***********************************************/

		// 1 IDODIST.sh
		log.info("Run IDODIST.sh");
		command = "bash " + SACPath + "IDODIST.sh";
		runsh.rsh(command);

		// 2 NETST.Sh
		log.info("Run NETST.sh");
		command = "bash " + SACPath + "NETST.sh";
		runsh.rsh(command);

		//*************************************第6步，执行步骤二脚本文件（结束）***********************************************/

		//*************************************第7步，创建步骤三脚本文件***********************************************/

		//makeglib.csh
		log.info("Create File create_makeglib");
		mtinvFactory.create_makeglib_manual_batch(MTINVPath, RAWDATAPath, SACPath, UTCTime, lat, lon);

		/***************************************************************************************************************

		 2017-05-05
		 取速度模型
		 生成  GETPOINT.sh  并执行

		 ***************************************************************************************************************/
		//1、创建 GETPOINT.sh
		log.info("Create File GETPOINT.sh");
		mtinvFactory.create_GETPOINT(lon, lat);

		//2、执行 GETPOINT.sh
		log.info("Run GETPOINT.sh");
		command = "bash /autoMTInv/modeldb/crust2.0/GETPOINT.sh";
		runsh.rsh(command);

		//3、读取outcr
		String se1 = "";
		String se2 = "";
		String se3 = "";
		String se4 = "";
		String he1 = "";
		String he2 = "";
		String he3 = "";
		String he4 = "";
		String uc1 = "";
		String uc2 = "";
		String uc3 = "";
		String uc4 = "";
		String mc1 = "";
		String mc2 = "";
		String mc3 = "";
		String mc4 = "";
		String lc1 = "";
		String lc2 = "";
		String lc3 = "";
		String lc4 = "";
		File outcr = new File("/autoMTInv/modeldb/crust2.0/outcr");
		InputStreamReader read0 = new InputStreamReader(new FileInputStream(outcr));
		BufferedReader bufferedReader0 = new BufferedReader(read0);
		String lineTxt0 = null;
		while ((lineTxt0 = bufferedReader0.readLine()) != null) {
			if (lineTxt0.indexOf("soft sed") > 0) {
				String se = lineTxt0;
				se = se.replaceAll(" ", "");
				String sel[] = se.split("\\.");
				se1 = sel[0] + "." + sel[1].substring(0, 2);
				se2 = sel[1].substring(4, sel[1].length()) + "." + sel[2].substring(0, 2);
				se3 = sel[2].substring(4, sel[2].length()) + "." + sel[3].substring(0, 2);
				se4 = sel[3].substring(4, sel[3].length()) + "." + sel[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("hard sed") > 0) {
				String he = lineTxt0;
				he = he.replaceAll(" ", "");
				String hel[] = he.split("\\.");
				he1 = hel[0] + "." + hel[1].substring(0, 2);
				he2 = hel[1].substring(4, hel[1].length()) + "." + hel[2].substring(0, 2);
				he3 = hel[2].substring(4, hel[2].length()) + "." + hel[3].substring(0, 2);
				he4 = hel[3].substring(4, hel[3].length()) + "." + hel[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("upper crust") > 0) {
				String uc = lineTxt0;
				uc = uc.replaceAll(" ", "");
				String ucl[] = uc.split("\\.");
				uc1 = ucl[0] + "." + ucl[1].substring(0, 2);
				uc2 = ucl[1].substring(4, ucl[1].length()) + "." + ucl[2].substring(0, 2);
				uc3 = ucl[2].substring(4, ucl[2].length()) + "." + ucl[3].substring(0, 2);
				uc4 = ucl[3].substring(4, ucl[3].length()) + "." + ucl[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("middle crust") > 0) {
				String mc = lineTxt0;
				mc = mc.replaceAll(" ", "");
				String mcl[] = mc.split("\\.");
				mc1 = mcl[0] + "." + mcl[1].substring(0, 2);
				mc2 = mcl[1].substring(4, mcl[1].length()) + "." + mcl[2].substring(0, 2);
				mc3 = mcl[2].substring(4, mcl[2].length()) + "." + mcl[3].substring(0, 2);
				mc4 = mcl[3].substring(4, mcl[3].length()) + "." + mcl[4].substring(0, 2);
			}
			if (lineTxt0.indexOf("lower crust") > 0) {
				String lc = lineTxt0;
				lc = lc.replaceAll(" ", "");
				String lcl[] = lc.split("\\.");
				lc1 = lcl[0] + "." + lcl[1].substring(0, 2);
				lc2 = lcl[1].substring(4, lcl[1].length()) + "." + lcl[2].substring(0, 2);
				lc3 = lcl[2].substring(4, lcl[2].length()) + "." + lcl[3].substring(0, 2);
				lc4 = lcl[3].substring(4, lcl[3].length()) + "." + lcl[4].substring(0, 2);
			}
		}

		//4、创建 wus.mod
		log.info("Create File wus.mod");
		mtinvFactory.create_wus(uc1, uc2, uc3, uc4, mc1, mc2, mc3, mc4, lc1, lc2, lc3, lc4);

		/**************************************生成  GETPOINT.sh  并执行 结束******************************************************/


		/*************************************第7步，创建步骤三脚本文件（结束）***********************************************/

		/*************************************第8步，执行步骤三脚本文件***********************************************/

		// 1 makeglib.csh
		log.info("Run create_makeglib.csh");
		command = "csh " + MTINVPath + "create_makeglib.csh";
		for (String filter: filters) {
			command += " " + filter;
		}
		runsh.rsh(command);

		int iNum = 1;
		while (iNum <= filters.length / 2) {
			String lf = String.valueOf(filters[iNum * 2 - 2]);
			String hf = String.valueOf(filters[iNum * 2 - 1]);
			String dirName = lf + "-" + hf;
			log.info("Calculate：" + dirName);

			// 第一次修改run.csh
			log.info("Modify First run.csh");
			mtinvFactory.changeRunCshFirstTimeBatch(rootPath, dirName);

			//生成shaixuan.sh
			log.info("Create File shaixuan.sh");
			mtinvFactory.makeShaiXuanShBatch(rootPath, dirName);

			//执行shaixuan.sh
			log.info("Run shaixuan.sh");
			command = "csh " + rootPath + "MTINV/" + dirName + "/shaixuan.csh";
			runsh.rsh(command);

			// 第二次修改run.csh
			log.info("Modify Second run.csh");
			mtinvFactory.changeRunCshSecondTimeBatch(rootPath, dirName);

			// 执行run.csh
			log.info("Run run.csh");
			command = "csh " + rootPath + "MTINV/" + dirName + "/run.csh";
			runsh.rsh(command);

			iNum++;
		}

		//创建 maxpvr.sh
		log.info("Create File maxpvr.sh");
		mtinvFactory.create_maxpvr(MTINVPath);

		// 执行maxpvr.sh
		log.info("Run maxpvr.sh");
		command = "bash " + MTINVPath + "maxpvr.sh";
		runsh.rsh(command);

		// 获取BEST文件夹路径
		String BestPath = "";
		String[] BestFilter = null;
		File dirFile = new File(MTINVPath);
		File[] files = dirFile.listFiles();
		if (files != null) {
			for (File fileChildDir : files) {
				if (fileChildDir.isDirectory() && fileChildDir.getName().contains("BEST")) {
					BestPath = rootPath + "MTINV/" + fileChildDir.getName() + "/";
					BestFilter = fileChildDir.getName().split("-");
				}
			}
		}

		// 读取best_result.txt
		InputStreamReader read = new InputStreamReader(new FileInputStream(MTINVPath + "/best_result.txt"));
		BufferedReader bufferedReader = new BufferedReader(read);
		String bestResultInfo = bufferedReader.readLine();
		read.close();

		// 在BEST文件夹中创建tar.sh
		log.info("Create File tar.sh");
		mtinvFactory.create_tar(BestPath, fileTime);

		// 执行tar.sh
		log.info("Run tar.sh");
		command = "bash " + BestPath + "tar.sh";
		runsh.rsh(command);

		/*************************************第8步，执行步骤三脚本文件（结束）***********************************************/


		/*************************************第9步，执行步骤三脚本文件***********************************************/

		//创建gmtmap1.csh
		log.info("Create File gmtmap1.csh");
		mtinvFactory.create_gmtmap1(BestPath);


		//运行gmtmap1.csh
		log.info("Run mtmap1.csh");
		command = "csh " + BestPath + "gmtmap1.csh";
		runsh.rsh(command);

		String endTimeTemp = ud.getStrDate();
        //-----------------------------------------------读取txt文件----------------------------------------------------
		resultPath = BestPath + "result/";

		ReadTXT rt = new ReadTXT();
		String para = rt.getTxtPara(resultPath);
		String[] paraArray = para.split(";");
		String result_file = paraArray[0];
		String result_date = paraArray[1];
		String result_time = paraArray[2];
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
		String result_datetime = result_date + " " + result_time;

		// 筛选最优解
		int email_index = result_file.indexOf("email");
		String[] ls = result_file.substring(email_index).split("_");
		String result_label = ls[1] + "_" + ls[2];
		log.info("Create File tar_best.sh");
		mtinvFactory.create_tar_best(BestPath, fileTime,result_label);
		log.info("Run tar_best.sh");
		command = "bash " + BestPath + "tar_best.sh";
		runsh.rsh(command);

		String resultList = "";
		String showPath = resultPath;
		File resultFile = new File(resultPath);
		String[] rf = resultFile.list();
		Arrays.sort(rf);
//		for (String s : rf) {
//			if (s.substring(s.length() - 3, s.length()).equals("jpg")) {
//				resultList = resultList + showPath + s + ",";
//			}
//		}
		for (String s : rf) {
			if (s.substring(s.length() - 3).equals("jpg")) {
				if (s.contains(result_label) || s.contains("gmtmap") || s.contains("plotmech") || s.contains("plotz") || s.contains("results")) {
					resultList = resultList + showPath + s + ",";
				}
			}
		}
		if (resultList.length() > 0) {
			resultList = resultList.substring(0, resultList.length() - 1);
		}

		//-----------------------------------------------读取txt文件结束----------------------------------------------------

		//创建111.cmt
		log.info("Create File 111.cmt");
		mtinvFactory.create_111(BestPath, lon, lat, depth, result_s1, result_d1, result_r1, result_m);

		//-----------------------------------------------画图----------------------------------------------------

		//获取画图参数
		double lat_d = Double.parseDouble(lat);
		double lon_d = Double.parseDouble(lon);
		double lonmax = lon_d + 0.5;
		double lonmin = lon_d - 0.5;
		double latmax = lat_d + 0.5;
		double latmin = lat_d - 0.5;
		String ddd = "EARE=\"" + lonmin + "/" + lonmax + "/" + latmin + "/" + latmax + "\"";

		//两张图脚本 draw-map.bat draw-meca.bat
		log.info("Create File draw-map.bat and draw-meca.bat");
		mtinvFactory.create_draw(BestPath, ddd);

		//run draw-map.bat
		log.info("Run draw-map.bat");
		command = "bash " + BestPath + "draw-map.bat";
		runsh.rsh(command);

		//run draw-map.bat
		log.info("Run draw-map.bat");
		command = "bash " + BestPath + "draw-meca.bat";
		runsh.rsh(command);

		//-----------------------------------------------画图结束----------------------------------------------------

		//-----------------------------------------------生成word---------------------------------------------------

		log.info("Create Word");
		mtinvFactory.create_word(BestPath, time, lat_d, lon_d, depth, magnitude, "地名", result_s1, result_d1, result_r1, result_s2, result_d2, result_r2, result_m);

		//-----------------------------------------------生成word结束------------------------------------------------
		tarPath = BestPath + fileTime + "_best.tar";

		//将参数存储在map中返回
		Map<String, String> map = new LinkedHashMap<>();
		map.put("time", time);
		map.put("lat", lat);
		map.put("lon", lon);
		map.put("depth", depth);
		map.put("magnitude", magnitude);
		map.put("distance_min", distance_min);
		map.put("distance_max", distance_max);
		map.put("filter_min", BestFilter[0]);
		map.put("filter_max", BestFilter[1].substring(0, 4));
		map.put("upTimeTemp", upTimeTemp);
		map.put("endTimeTemp", endTimeTemp);
		map.put("resultPath", resultPath);
		map.put("result_file", result_file);
		map.put("result_time", result_datetime);
		map.put("result_lon", result_lon);
		map.put("result_lat", result_lat);
		map.put("result_depth", result_depth);
		map.put("result_m", result_m);
		map.put("result_s1", result_s1);
		map.put("result_r1", result_r1);
		map.put("result_d1", result_d1);
		map.put("result_s2", result_s2);
		map.put("result_r2", result_r2);
		map.put("result_d2", result_d2);
		map.put("result_pvr", result_pvr);
		map.put("tarPath", tarPath);
		map.put("resultList", resultList);
		map.put("rootPath", rootPath);
		map.put("bestResultInfo", bestResultInfo);

		log.info("Calculation Done!");
		return map;
	}
}