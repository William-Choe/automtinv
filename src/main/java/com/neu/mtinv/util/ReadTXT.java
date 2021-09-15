package com.neu.mtinv.util;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReadTXT {
	public String getTxtPara(String resultPath) throws IOException{
		String result = "";
		double maxValue = 0;
	   	String outFileName = "";
		
		//step 1
		File file = new File(resultPath);
		String[] filelist = file.list();
		for (int i = 0; i < filelist.length; i++) {
			String fileName = filelist[i];
			String[] fileNameArray = fileName.split("\\.");
			String txtName = "";
			double txtValue = 0;

			if (fileNameArray[fileNameArray.length - 1].equals("txt")) {
				txtName = fileName;
				File txtFile = new File(resultPath + fileName);
				InputStreamReader read = new InputStreamReader(new FileInputStream(txtFile));
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;

				while ((lineTxt = bufferedReader.readLine()) != null) {
					if (lineTxt != null && lineTxt.length() > 26) {
						if (lineTxt.indexOf("Percent Variance Reduction") > 0) {
							String[] lineTxtArray = lineTxt.split(" ");
							String value_s = lineTxtArray[lineTxtArray.length - 2];
							txtValue = Double.parseDouble(value_s);

							if (txtValue > maxValue) {
								outFileName = txtName;
								maxValue = txtValue;
							}
						}
					}
				}
			}
		}

		//step 2
		InputStreamReader read = new InputStreamReader(new FileInputStream(resultPath + outFileName), "utf-8");// 考虑到编码格式

		BufferedReader bufferedReader = new BufferedReader(read);
		String lineTxt = null;
		bufferedReader.readLine();
		bufferedReader.readLine();
		lineTxt = bufferedReader.readLine();
		String[] aa = lineTxt.split("\\s+");
		String riqi = aa[0];
		String time = aa[2];
		String lat = aa[3];
		String lon = aa[4];

		lineTxt = bufferedReader.readLine();
		lineTxt = lineTxt.substring(1, lineTxt.length());
		String[] bb = lineTxt.split("\\s+");
		String depth = bb[2];

		lineTxt = bufferedReader.readLine();
		lineTxt = lineTxt.substring(1, lineTxt.length());
		String[] cc = lineTxt.split("\\s+");
		String m = cc[2];

		bufferedReader.readLine();
		bufferedReader.readLine();
		bufferedReader.readLine();
		bufferedReader.readLine();
		bufferedReader.readLine();
		bufferedReader.readLine();
		bufferedReader.readLine();
		bufferedReader.readLine();
		bufferedReader.readLine();
		bufferedReader.readLine();

		lineTxt = bufferedReader.readLine();
		String dd[] = lineTxt.split("\\s+");
		String s1 = dd[4];
		String d1 = dd[5];
		String r1 = dd[6];

		lineTxt = bufferedReader.readLine();
		String ee[] = lineTxt.split("\\s+");
		String s2 = ee[4];
		String d2 = ee[5];
		String r2 = ee[6];

		String resultFileName = resultPath + outFileName;

		String pvr = maxValue + "%";

		result = resultFileName + ";" + riqi + ";" + time + ";" + lat + ";" + lon
				+ ";" + depth + ";" + m + ";" + s1 + ";" + d1 + ";"
				+ r1 + ";" + s2 + ";" + d2 + ";" + r2 + ";" + pvr;
		
		return result;
	}
	
	
	
	public List<String> readtxt(String fileName) throws IOException{
		List<String> result = new ArrayList<String>();

		// step 3
		InputStreamReader read = new InputStreamReader(new FileInputStream(fileName), "utf-8");// 考虑到编码格式

		BufferedReader bufferedReader = new BufferedReader(read);
		String lineTxt = null;
		while ((lineTxt = bufferedReader.readLine()) != null) {
			System.out.println(lineTxt);
			result.add(lineTxt);
		}
		read.close();

		return result;
	}
	
	
	
	public List<String> getResultImg(String resultFile){
		List<String> result = new ArrayList<String>();
		String ra[] = resultFile.split("/");
		String resultFileName = ra[ra.length-1];
		String resultPath = resultFile.substring(0, resultFile.length() - (resultFileName.length() + 1));
		String imgName_part = resultFileName.substring(5, resultFileName.length() - 4);
		
		File file = new File(resultPath);
		String[] filelist = file.list();

		for (int i = 0; i < filelist.length; i++) {
			String fileName = filelist[i];
			if (fileName.indexOf(imgName_part) > 0 & fileName.indexOf("jpg") > 0) {
				result.add(resultPath + "/" + fileName);
			}
		}
		return result;
	}
}
