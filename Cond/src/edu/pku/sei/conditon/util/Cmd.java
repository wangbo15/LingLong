package edu.pku.sei.conditon.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Cmd {
	public static void runCmd(String cmd, File dir) {
		try {
			final Process process = Runtime.getRuntime().exec(cmd, null, dir);
			/*
			new Thread() {
				public void run() {
					
					InputStream errorInStream = new BufferedInputStream(process.getErrorStream());
					int num = 0;
					byte[] bs = new byte[1024];
					try {
						while ((num = errorInStream.read(bs)) != -1) {
							String str = new String(bs, 0, num, "UTF-8");
							System.err.println(str);
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							errorInStream.close();
						} catch (IOException e) {
						}
					}
					
				}
			}.start();

			new Thread() {
				public void run() {
					InputStream processInStream = new BufferedInputStream(process.getInputStream());
					int num = 0;
					byte[] bs = new byte[1024];
					try {
						while ((num = processInStream.read(bs)) != -1) {
							String str = new String(bs, 0, num, "UTF-8");
							System.out.println(str);
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							processInStream.close();
						} catch (IOException e) {
						}
					}
					
				}
			}.start();*/

			process.waitFor();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
