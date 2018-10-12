package com.ibm.sc.skittles.sshLinker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class FTPLinker {

	private String host;
	private String port;
	private String userName;
	private String password;
	private String jenkinsNum;

	public FTPLinker(String host, String port, String userName, String password, String jenkinsNum) {
		this.host = host;
		this.port = port;
		this.userName = userName;
		this.password = password;
		this.jenkinsNum = jenkinsNum;
	}

	public void file2FTP(String srcfile) {
		int port = Integer.parseInt(this.port);
		ChannelSftp sftp = null;
		Session session = null;
		String destDir = "/home/btit/www/sales/" + jenkinsNum + "/batch";
		try {
			session = this.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();
			sftp = (ChannelSftp) channel;
			this.upload(destDir, srcfile, sftp);
			// this.download("C:\\Users\\IBM_ADMIN\\Desktop", destDir,
			// "C:\\Users\\IBM_ADMIN\\Desktop\\testing.rar", sftp);
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");// yang
			this.exeCommends("unzip", channelExec);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sftp != null)
				sftp.disconnect();
			if (session != null)
				session.disconnect();
		}
	}

	public String exeCommends(String cmd, ChannelExec channelExec) throws Exception {
//		channelExec.setCommand(cmd);
//		channelExec.setInputStream(null);
//		channelExec.setErrStream(System.err);
		StringBuffer sb = new StringBuffer();
		try (InputStream in = channelExec.getInputStream();
				InputStreamReader isr = new InputStreamReader(in, Charset.forName("gbk"));//UTF-8
				BufferedReader reader = new BufferedReader(isr);) {
			String buf = null;
			while ((buf = reader.readLine()) != null) {
				sb.append(buf);
			}
			reader.close();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return sb.toString();
	}

	/**
	 * 连接sftp服务器
	 * 
	 * @param host
	 *            远程主机ip地址
	 * @param port
	 *            sftp连接端口，null 时为默认端口
	 * @param user
	 *            用户名
	 * @param password
	 *            密码
	 * @return
	 * @throws JSchException
	 */
	public Session connect() throws Exception {
		Session session = null;
		try {
			JSch jsch = new JSch();
			if (port != null) {
				session = jsch.getSession(this.userName, this.host, Integer.parseInt(this.port));
			} else {
				session = jsch.getSession(this.userName, this.host);
			}
			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");// 设置第一次登陆的时候提示，可选值:(ask|yes|no)
			session.connect(30000);
		} catch (JSchException e) {
			throw new RuntimeException("Failed in connecting ftp");
		}
		return session;
	}

	/**
	 * sftp上传文件(夹)
	 * 
	 * @param directory
	 * @param uploadFile
	 * @param sftp
	 * @throws Exception
	 */
	public void upload(String directory, String uploadFile, ChannelSftp sftp) throws Exception {
		File file = new File(uploadFile);
		if (file.exists()) {
			// 因为ChannelSftp无法去判读远程linux主机的文件路径
			try {
				Vector content = sftp.ls(directory);
				if (content == null) {
					sftp.mkdir(directory);
				}
			} catch (SftpException e) {
				throw new RuntimeException(e.getMessage());
			}
			// 进入目标路径
			try {
				sftp.cd(directory);
			} catch (SftpException e) {
				throw new RuntimeException(e.getMessage());
			}
			if (file.isFile()) {
				try (InputStream ins = new FileInputStream(file)) {
					sftp.put(ins, new String(file.getName().getBytes(), "gbk"));// UTF-8
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}
			}
			// else {
			// File[] files = file.listFiles();
			// for (File file2 : files) {
			// String dir = file2.getAbsolutePath();
			// if (file2.isDirectory()) {
			// String str = dir.substring(dir.lastIndexOf(File.separator));
			// directory = "";// FileUtil.normalize(directory + str);
			// }
			// upload(directory, dir, sftp);
			// }
			// }
		} else {
			throw new RuntimeException("file is not exists");
		}
	}

	/**
	 * sftp下载文件（夹）
	 * 
	 * @param directory
	 *            下载文件上级目录
	 * @param srcFile
	 *            下载文件完全路径
	 * @param saveFile
	 *            保存文件路径
	 * @param sftp
	 *            ChannelSftp
	 * @throws UnsupportedEncodingException
	 */
	public static void download(String directory, String srcFile, String saveFile, ChannelSftp sftp)
			throws UnsupportedEncodingException {
		Vector conts = null;
		try {
			conts = sftp.ls(srcFile);
		} catch (SftpException e) {
			e.printStackTrace();
		}
		File file = new File(saveFile);
		if (!file.exists())
			file.mkdir();
		// 文件
		if (srcFile.indexOf(".") > -1) {
			try {
				sftp.get(srcFile, saveFile);
			} catch (SftpException e) {
				e.printStackTrace();
			}
		} else {
			// 文件夹(路径)
			for (Iterator iterator = conts.iterator(); iterator.hasNext();) {
				LsEntry obj = (LsEntry) iterator.next();
				String filename = new String(obj.getFilename().getBytes(), "UTF-8");
				if (!(filename.indexOf(".") > -1)) {
					directory = "";// FileUtil.normalize(directory + System.getProperty("file.separator") +
									// filename);
					srcFile = directory;
					saveFile = "";// FileUtil.normalize(saveFile + System.getProperty("file.separator") +
									// filename);
				} else {
					// 扫描到文件名为".."这样的直接跳过
					String[] arrs = filename.split("\\.");
					if ((arrs.length > 0) && (arrs[0].length() > 0)) {
						srcFile = "";// FileUtil.normalize(directory + System.getProperty("file.separator") +
										// filename);
					} else {
						continue;
					}
				}
				download(directory, srcFile, saveFile, sftp);
			}
		}
	}

	public boolean link2FTP() {
		JSch jsch = new JSch(); // 创建JSch对象
		int port = Integer.parseInt(this.port);// 端口号
		String cmd = "pwd";// 要运行的命令
		Session session;
		boolean connected = false;
		try {
			session = jsch.getSession(userName, host, port);
			session.setPassword(password); // 设置密码
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config); // 为Session对象设置properties
			int timeout = 20000;
			session.setTimeout(timeout); // 设置timeout时间
			session.connect(); // 通过Session建立链接
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");// yang
			channelExec.setCommand(cmd);
			channelExec.setInputStream(null);
			channelExec.setErrStream(System.err);
			channelExec.connect();
			InputStream in = channelExec.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
			String buf = null;
			StringBuffer sb = new StringBuffer();
			while ((buf = reader.readLine()) != null) {
				sb.append(buf);
				System.out.println(buf);// 打印控制台输出
			}
			reader.close();
			channelExec.disconnect();
			if (null != session) {
				session.disconnect();
			}
			if (sb.toString().length() > 0) {
				connected = true;
			}
		} catch (JSchException | IOException e) {
			e.printStackTrace();
		}
		return connected;
	}

	/*
	 * public static void main(String[] args) throws Exception { JSch jsch = new
	 * JSch(); // 创建JSch对象 String userName = " ";// 用户名 String password =
	 * " ";// 密码 String host = " ";// 服务器地址 int port
	 * = 22;// 端口号 String cmd = "ls";// 要运行的命令 Session session =
	 * jsch.getSession(userName, host, port); // 根据用户名，主机ip，端口获取一个Session对象
	 * session.setPassword(password); // 设置密码 Properties config = new Properties();
	 * config.put("StrictHostKeyChecking", "no"); session.setConfig(config); //
	 * 为Session对象设置properties int timeout = 20000; session.setTimeout(timeout); //
	 * 设置timeout时间 session.connect(); // 通过Session建立链接 ChannelExec channelExec =
	 * (ChannelExec) session.openChannel("exec"); channelExec.setCommand(cmd);
	 * channelExec.setInputStream(null); channelExec.setErrStream(System.err);
	 * channelExec.connect(); InputStream in = channelExec.getInputStream();
	 * BufferedReader reader = new BufferedReader(new InputStreamReader(in,
	 * Charset.forName("UTF-8"))); String buf = null; StringBuffer sb = new
	 * StringBuffer(); while ((buf = reader.readLine()) != null) { sb.append(buf);
	 * System.out.println(buf);// 打印控制台输出 } reader.close();
	 * channelExec.disconnect(); if (null != session) { session.disconnect(); } }
	 */
}
