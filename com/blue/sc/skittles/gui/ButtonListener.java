package com.ibm.sc.skittles.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.ibm.sc.skittles.contentEditor.FileIteration;
import com.ibm.sc.skittles.sshLinker.FTPLinker;
import com.ibm.sc.skittles.zipper.FileZipper;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

public class ButtonListener implements ActionListener {
	private String ftpWrokingDir = "/home/btit/www/sales";
	private JTextField host;
	private JTextField port;
	private JTextField userName;
	private JTextField password;
	private JTextField jenkins;
	private JTextField commonFolder;
	private JTextField jobFolder;
	private JTextField tempFolder;
	private JDialog alert;
	private JTextArea console;

	public ButtonListener(JTextField host, JTextField port, JTextField userName, JTextField password,
			JTextField jenkins, JTextField commonFolder, JTextField jobFolder, JTextField tempFolder, JDialog alert,
			JTextArea console) {
		super();
		this.host = host;
		this.port = port;
		this.userName = userName;
		this.password = password;
		this.jenkins = jenkins;
		this.commonFolder = commonFolder;
		this.jobFolder = jobFolder;
		this.tempFolder = tempFolder;
		this.alert = alert;
		this.console = console;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String enptyFields = fieldsChecker();
		if (null != enptyFields) {
			JLabel content = new JLabel(enptyFields + " is empty!");
			this.alert.setBounds(20, 30, 50, 100);
			this.alert.add(content);
			this.alert.setLocationRelativeTo(null);
			this.alert.setVisible(true);
			this.alert.remove(content);
		} else {
			boolean res = true;
			try {
				if ("" != this.getContent(commonFolder)) {
					this.console.append("----------Common Folder----------\n");
					res = this.filesProcessor(this.getContent(commonFolder));
				}else {
					this.console.append("----------No Common Folder----------\n");
				}
				if ("" != this.getContent(jobFolder) && res) {
					this.console.append("----------Job Folder----------\n");
					res = this.filesProcessor(this.getContent(jobFolder));
				}
				if(res == true) {
					this.console.append("----------Done----------\n");
				}
			} catch (Exception ep) {
				this.console.append("suspend");
			}
		}
	}

	private boolean filesProcessor(String targetDir) throws Exception {
		FTPLinker ftp = new FTPLinker(getContent(this.host), getContent(this.port), getContent(this.userName),
				getContent(this.password), getContent(this.jenkins));
		Session session = null;
		try {
			session = ftp.connect();
		} catch (Exception e) {
			this.console.append(e.getMessage()+"\n");
			return false;
		}
		if (null != session) {
			String workingFolder = null;
			FileZipper fileZipper = new FileZipper();
			try {
				workingFolder = fileZipper.toTemporaryFolder(targetDir, getContent(this.tempFolder),this.console);
			} catch (Exception e) {
				this.console.append(e.getMessage()+"\n");
				return false;
			}
			this.console.append("working Folder:" + workingFolder +"\n");
			FileIteration fileIteration = new FileIteration(workingFolder, ftpWrokingDir + "/" + getContent(jenkins),
					getContent(host),this.console);
			try {
				fileIteration.fileIterator();
			} catch (Exception e) {
				this.console.append(e.getMessage()+"\n");
				return false;
			}
			String zipFileName = System.currentTimeMillis() + ".zip";
			try {
				fileZipper.zip(workingFolder, getContent(tempFolder), zipFileName);
			} catch (Exception e) {
				this.console.append(e.getMessage()+"\n");
				return false;
			}
			this.console.append("zip working folder "+ workingFolder +" to zipped file "+ zipFileName +"\n");
			String zipFile2Ftp = getContent(tempFolder) + File.separator + zipFileName;
			this.console.append("zipFile that will be transfered to Ftp: "+zipFile2Ftp+"\n");

			ChannelSftp channelSftp = null;
			ChannelExec channelExec = null;
			try {
				channelSftp = (ChannelSftp) session.openChannel("sftp");
				channelSftp.connect();
				String ftpJobDir = ftpWrokingDir + "/" + getContent(jenkins);
				String ftpZipFile = ftpJobDir + "/" + zipFileName;

				ftp.upload(ftpJobDir, zipFile2Ftp, channelSftp);
				this.console.append("ftpJobDir=" + ftpJobDir+"\n");
				this.console.append("ftpZipFile=" + ftpZipFile+"\n");
				String unzipCommand = "unzip -o " + ftpZipFile + " -d " + ftpJobDir;
				this.console.append("unzipCommand=" + unzipCommand+"\n");
				
				channelExec = (ChannelExec) session.openChannel("exec");
				channelExec.setCommand(unzipCommand);
				channelExec.setInputStream(null);
				channelExec.setErrStream(System.err);
				channelExec.connect();
				String info = ftp.exeCommends(unzipCommand, channelExec);
				this.console.append("unzip execution info: "+info+"\n");
				channelExec.disconnect();
				channelSftp.disconnect();
				session.disconnect();
			} catch (Exception e1) {
				channelExec.disconnect();
				channelSftp.disconnect();
				session.disconnect();
				throw new RuntimeException(e1.getMessage());
			} finally {
				if (null != channelExec) {
					channelExec.disconnect();
				}
				if (null != channelSftp) {
					channelSftp.disconnect();
				}
				session.disconnect();
				ftp = null;
				fileZipper = null;
				fileIteration = null;
			}
		}
		return true;
	}

	private String getContent(JTextField field) {
		return field.getText().trim();
	}

	private String fieldsChecker() {
		if ("".equals(getContent(host))) {
			return "host";
		} else if ("".equals(getContent(port))) {
			return "port";
		} else if ("".equals(getContent(userName))) {
			return "username";
		} else if ("".equals(getContent(password))) {
			return "password";
		} else if ("".equals(getContent(jenkins))) {
			return "Jenkins";
		} else if ("".equals(getContent(jobFolder))) {
			return "jobFolder";
		} else if ("".equals(getContent(tempFolder))) {
			return "tempFolder";
		} else {
			return null;
		}
	}
}