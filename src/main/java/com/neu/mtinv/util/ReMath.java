package com.neu.mtinv.util;

import com.neu.mtinv.entity.Mtinfo;
import com.neu.mtinv.mapper.MtinfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.text.DecimalFormat;

@Component
@Slf4j
public class ReMath {
	@Resource
	private MtinfoMapper mtinfoMapper;

	@Resource
	private RunSH runSH;

	@Resource
	private MtinvFactory mtinvFactory;

	public String getStations(String rootPath) {
		String result = "";

		File file = new File(rootPath + "MTINV/run.csh");

		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file));
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			String ttt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				if (lineTxt != null && !lineTxt.equals("")) {
					if (lineTxt.substring(0, 5).equals("# sta")) {
						while ((ttt = bufferedReader.readLine()) != null) {
							if (ttt.substring(0, 3).equals("EOF")) {
								break;
							}
							String[] asd = ttt.split("\t");
							result = result + asd[0] + "_" + asd[1] + "/";
						}
						break;
					}
				}
			}
			read.close();
		} catch (Exception e) {
			log.error("error occurs: ", e);
		}

		if (result.length() > 0) {
			result = result.substring(0, result.length() - 1);
		}

		return result;
	}

	public void remath(String rootPath, String checkedStation, String uncheckedStation, String filter_min, String filter_max, String id) {
		log.info("修改backglib.sh");
		changeBackGlibSh(rootPath);

		log.info("修改run.csh");
		changeRunCsh(rootPath, uncheckedStation, filter_min, filter_max);

		log.info("修改clean.sh");
		changeCleanSh(rootPath);

		log.info("修改cpglib.sh");
		changeCpglibSh(rootPath, checkedStation);

		String command = "";

		log.info("执行backupglib.sh");
		command = "bash " + rootPath + "MTINV/backupglib.sh";
		runSH.rsh(command);

		log.info("执行clean.sh");
		command = "bash " + rootPath + "MTINV/clean.sh";
		runSH.rsh(command);

		log.info("执行cpglib.sh");
		command = "bash " + rootPath + "MTINV/cpglib.sh";
		runSH.rsh(command);

		log.info("执行run.csh");
		command = "csh " + rootPath + "MTINV/run.csh";
		runSH.rsh(command);

		log.info("执行tar.sh");
		command = "bash " + rootPath + "MTINV/tar.sh";
		runSH.rsh(command);

		log.info("修改gmtmap1.csh");
		changeGmtCsh(rootPath);

		log.info("执行gmtmap1.csh");
		command = "csh " + rootPath + "MTINV/gmtmap1.csh";
		runSH.rsh(command);

		String resultPath = rootPath + "MTINV/result/";
		ReadTXT rt = new ReadTXT();
		String para;
		try {
			para = rt.getTxtPara(resultPath);

			String[] paraArray = para.split(";");
			String result_file = paraArray[0];
			String result_m = paraArray[6];
			String result_s1 = paraArray[7];
			String result_d1 = paraArray[8];
			String result_r1 = paraArray[9];
			String result_s2 = paraArray[10];
			String result_d2 = paraArray[11];
			String result_r2 = paraArray[12];
			String result_pvr = paraArray[13];

			Mtinfo mtinfo = mtinfoMapper.getMtinfoById(id);
			String lon = mtinfo.getLon();
			String lat = mtinfo.getLat();
			String depth = mtinfo.getDepth();
			String o_time = mtinfo.getO_time();
			String localName = mtinfo.getLocation_cname();
			String m = mtinfo.getM();

			//create 111.cmt
			log.info("创建111.cmt");
			String MTINVPath = rootPath + "MTINV/";
			File cmt = new File(MTINVPath + "111.cmt");
			cmt.delete();
			cmt.createNewFile();
			String cmt111 = lon + " " + lat + " " + depth + " " + result_s1 + " " + result_d1 + " " + result_r1 + " " + result_m + " " + lon + " " + lat;
			RandomAccessFile mm_cmt;
			try {
				mm_cmt = new RandomAccessFile(cmt, "rw");
				mm_cmt.writeBytes(cmt111);
				mm_cmt.close();
			} catch (IOException e1) {
				log.error("error occurs: ", e1);
			}

			//-----------------------------------------------画图----------------------------------------------------
			//获取画图参数
			double lat_d = Double.parseDouble(lat);
			double lon_d = Double.parseDouble(lon);
			double lon_max = lon_d + 0.5;
			double lon_min = lon_d - 0.5;
			double lat_max = lat_d + 0.5;
			double lat_min = lat_d - 0.5;
			String ddd = "EARE=\"" + lon_min + "/" + lon_max + "/" + lat_min + "/" + lat_max + "\"";

			//两张图脚本 draw-map.bat draw-meca.bat
			log.info("创建draw-map.bat draw-meca.bat");
			mtinvFactory.create_draw(MTINVPath, ddd);

			//run draw-map.bat
			log.info("执行draw-map.bat");
			command = "bash " + MTINVPath + "draw-map.bat";
			runSH.rsh(command);

			//run draw-map.bat
			log.info("执行draw-map.bat");
			command = "bash " + MTINVPath + "draw-meca.bat";
			runSH.rsh(command);

			//生成word
			log.info("生成word");
			mtinvFactory.create_word(MTINVPath, o_time, lat_d, lon_d, depth, m, localName,result_s1, result_d1, result_r1, result_s2, result_d2, result_r2, result_m);

			//更新数据库
			log.info("更新数据库");
			mtinfoMapper.reMathSet(result_file, result_s1, result_d1, result_r1, result_s2, result_d2, result_r2, result_pvr, id);
		} catch (Exception e) {
			log.error("error occurs: ", e);
		}
	}
	
	public static void changeBackGlibSh(String rootPath){
		File cleanSh = new File(rootPath + "MTINV/backupglib.sh");
		try {
			cleanSh.createNewFile();

			String filein = "#!/bin/sh\n";
			filein += "cd " + rootPath + "MTINV\n";
			filein += "if [ ! -d backup ]\n";
			filein += "then\n";
			filein += "mkdir backup\n";
			filein += "fi\n";
			filein += "cp " + rootPath + "MTINV/*.glib  " + rootPath + "MTINV/backup\n";
			
			RandomAccessFile mm_add = null;
			mm_add = new RandomAccessFile(cleanSh, "rw");
			mm_add.writeBytes(filein);
			mm_add.close();
		} catch (IOException e) {
			log.error("error occurs: ", e);
		}
	}

	public static void changeRunCsh(String rootPath, String uncheckedStation, String filter_min, String filter_max) {
		try {
			File file = new File(rootPath + "MTINV/run.csh");
			InputStreamReader read = new InputStreamReader(new FileInputStream(file));
			BufferedReader bufferedReader = new BufferedReader(read);
			StringBuffer buf = new StringBuffer();
			String lineTxt = null;
			String ttt = null;
			boolean temp = true;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				buf = buf.append(lineTxt);
				buf = buf.append(System.getProperty("line.separator"));

				if (lineTxt != null && lineTxt.length() > 5) {
					if (lineTxt.substring(0, 5).equals("# sta")) {
						while ((ttt = bufferedReader.readLine()) != null) {
							if (ttt.substring(0, 3).equals("EOF")) {
								buf = buf.append(ttt);
								buf = buf.append(System.getProperty("line.separator"));
								break;
							}

							String newTT = "";

							//change lf hf
							DecimalFormat df = new DecimalFormat("0.000");
							double lf_d = Double.parseDouble(filter_min);
							String lf_s = df.format(lf_d);
							double hf_d = Double.parseDouble(filter_max);
							String hf_s = df.format(hf_d);
							String[] text = ttt.split("\t");
							String longT = text[2];
							newTT = text[0] + "\t" + text[1] + "\t" + longT.substring(0, 8) + lf_s + " " + hf_s + longT.substring(19, longT.length());

							if (newTT.substring(0, 1).equals("#")) {
								String[] asd = newTT.split("\t");
								String oldName = asd[0].substring(1, asd[0].length()) + "_" + asd[1];

								String[] oldSta = uncheckedStation.split("/");
								for (int i = 0; i < oldSta.length; i++) {
									if (oldName.equals(oldSta[i])) {
										buf = buf.append(newTT);
										buf = buf.append(System.getProperty("line.separator"));
										temp = false;
										break;
									} else {
										temp = true;
									}
								}

								if (temp) {
									buf = buf.append(newTT.substring(1, newTT.length()));
									buf = buf.append(System.getProperty("line.separator"));
								}
							} else {
								String[] asd = newTT.split("\t");
								String oldName = asd[0] + "_" + asd[1];

								String[] oldSta = uncheckedStation.split("/");
								for (int i = 0; i < oldSta.length; i++) {
									if (oldName.equals(oldSta[i])) {
										buf = buf.append("#" + newTT);
										buf = buf.append(System.getProperty("line.separator"));
										temp = false;
										break;
									} else {
										temp = true;
									}
								}

								if (temp) {
									buf = buf.append(newTT);
									buf = buf.append(System.getProperty("line.separator"));
								}
							}
						}
					}
				}
			}

			read.close();

			FileOutputStream fos = new FileOutputStream(file);
			PrintWriter pw = new PrintWriter(fos);
			pw.write(buf.toString().toCharArray());
			pw.flush();
			pw.close();
		} catch (Exception e) {
			log.error("error occurs: ", e);
		}
	}
	
	
	public static void changeCleanSh(String rootPath) {
		
		File cleanSh = new File(rootPath + "MTINV/clean.sh");
		try {
			cleanSh.createNewFile();

			String filein = "#!/bin/sh\n";
			filein += "### CLEAN UP ###\n";
			filein += "cd " + rootPath + "MTINV\n";
			filein += "rm -rf plotmech *.ps *.txt *.xy  *.jpg mtinv.out results.?.* *.pdf *.data *.out *.par *.in *.sql *.sql gmtmap.csh plotz.csh *.ginv *.tar result *.glib gmtmap1.csh\n";

			RandomAccessFile mm_add = null;

			mm_add = new RandomAccessFile(cleanSh, "rw");
			mm_add.writeBytes(filein);
			mm_add.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}


	public static void changeCpglibSh(String rootPath, String oldStr) {
		String[] oldSta = oldStr.split("/");

		String gilbString = "";
		for (int i = 0; i < oldSta.length; i++) {
			String[] staNames = oldSta[i].split("_");

			String staName = staNames[0];
			String snName = staNames[1];

			gilbString = gilbString + staName + "." + snName + ".*.glib ";

		}

		File cleanSh = new File(rootPath + "MTINV/cpglib.sh");
		try {
			cleanSh.createNewFile();

			String filein = "#!/bin/sh\n";
			filein += "### CPGLIB ###\n";
			filein += "cd " + rootPath + "MTINV/backup\n";
			filein += "cp " + rootPath + "MTINV/backup/ " + gilbString + " " + rootPath + "MTINV\n";

			RandomAccessFile mm_add;
			mm_add = new RandomAccessFile(cleanSh, "rw");
			mm_add.writeBytes(filein);
			mm_add.close();
		} catch (IOException e) {
			log.error("error occurs: ", e);
		}
	}
	
	// 创建gmtmap1.csh
	public static void changeGmtCsh(String rootPath) {
		try {
			File gmtFile = new File(rootPath + "MTINV/gmtmap1.csh");
			InputStreamReader read = new InputStreamReader(new FileInputStream(gmtFile));
			BufferedReader bufferedReader = new BufferedReader(read);
			StringBuffer buf = new StringBuffer();
			String lineTxt = null;
			int i = 0;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				i++;
				buf = buf.append(lineTxt);
				buf = buf.append(System.getProperty("line.separator"));
				if (i == 7) {
					String a = "cd " + rootPath + "MTINV/";
					buf = buf.append(a);
					buf = buf.append(System.getProperty("line.separator"));
				}
			}

			read.close();

			FileOutputStream fos = new FileOutputStream(gmtFile);
			PrintWriter pw = new PrintWriter(fos);
			pw.write(buf.toString().toCharArray());
			pw.flush();
			pw.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}