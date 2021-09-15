package com.neu.mtinv.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RunSH {
	public void rsh(String command){
		try{
			Process process = Runtime.getRuntime().exec(command);
			printStreamInfo(process.getInputStream());
			printStreamInfo(process.getErrorStream());
			boolean i = process.waitFor(120, TimeUnit.SECONDS);

			if (i) {
				log.info(command + " complete");
			} else {
				log.warn(command + " stop with error");
			}
			process.destroy();
		}catch(Exception e){
			log.error("error occurs: ", e);
		}
	}

	private void printStreamInfo(InputStream inputStream) {
		new Thread(() -> {
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			try {
				String line;
				while ((line = br.readLine()) != null) {

				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void runAndLog(String command, BufferedWriter ss) throws IOException {
		try{
			Process process = Runtime.getRuntime().exec(command);
			logStreamInfo(process.getInputStream(), ss);
			logStreamInfo(process.getErrorStream(), ss);
			int i = process.waitFor();

			if (i == 0) {
				log.info(command + "complete");
				ss.append(command + ": complete \n\r");
			} else {
				log.warn(command + "stop with error");
				ss.append(command + ": stop with error \n\r");
			}
			process.destroy();
			ss.append("\r\n\r\n\r\n\r\n\r\n");
		}catch(Exception e){
			log.error("error occurs: ", e);
			ss.append("error occurs: " + e);
			ss.close();
		}
	}

	private void logStreamInfo(InputStream inputStream, BufferedWriter ss) {
		new Thread(() -> {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
				String line;
				while ((line = br.readLine()) != null) {
					ss.append(line + "\r\n");
				}
				br.close();
			} catch (IOException e) {
				log.error("error occurs: ", e);
			}
		}).start();
	}
}
