package com.ibm.sc.skittles.zipper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.JTextArea;

public class FileZipper {

	/*
	 * private String commonFolder; private String jobFolder; private String
	 * tempFolder;
	 * 
	 * public FileZipper(String commonFolder, String jobFolder, String tempFolder) {
	 * super(); this.commonFolder = commonFolder; this.jobFolder = jobFolder;
	 * this.tempFolder = tempFolder; }
	 */

	public String toTemporaryFolder(String srcDir, String tempFolder,JTextArea console) throws Exception {
		String workingFloder = null;
		String zipFileName = System.currentTimeMillis() + ".zip";
		try {
			zip(srcDir, tempFolder, zipFileName);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		console.append("zip folder "+ srcDir +" to dir:"+ tempFolder +" zipped file name :"+ zipFileName +" \n");
		try {
			unzip(tempFolder + File.separator + zipFileName, tempFolder, false);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		console.append("unziped folder "+ tempFolder + File.separator + zipFileName +" to dir:"+ tempFolder +" \n");
		workingFloder = tempFolder + File.separator + srcDir.substring(srcDir.lastIndexOf(File.separator) + 1);
		return workingFloder;
	}

	/**
	 * 递归压缩文件夹
	 * 
	 * @param srcRootDir
	 *            压缩文件夹根目录的子路径
	 * @param file
	 *            当前递归压缩的文件或目录对象
	 * @param zos
	 *            压缩文件存储对象
	 * @throws Exception
	 */// source sourceFile resfile
	private static void zip(String srcRootDir, File file, ZipOutputStream zos) throws Exception {
		if (file == null) {
			throw new RuntimeException("File Object is not exists");
		}

		// 如果是文件，则直接压缩该文件
		if (file.isFile()) {
			int count, bufferLen = 1024;
			byte data[] = new byte[bufferLen];

			// 获取文件相对于压缩文件夹根目录的子路径
			String subPath = file.getAbsolutePath();
			int index = subPath.indexOf(srcRootDir);
			if (index != -1) {
				subPath = subPath.substring(srcRootDir.length() + File.separator.length());
			}
			ZipEntry entry = new ZipEntry(subPath);
			try (FileInputStream fis = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(fis);) {
				zos.putNextEntry(entry);
				while ((count = bis.read(data, 0, bufferLen)) != -1) {
					zos.write(data, 0, count);
				}
				zos.closeEntry();
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		// 如果是目录，则压缩整个目录
		else {
			// 压缩目录中的文件或子目录
			File[] childFileList = file.listFiles();
			// 获取文件相对于压缩文件夹根目录的子路径
			if (childFileList.length == 0) {
				String subPath = file.getAbsolutePath();
				int index = subPath.indexOf(srcRootDir);
				if (index != -1) {
					subPath = subPath.substring(srcRootDir.length() + File.separator.length());
				}
				ZipEntry entry = new ZipEntry(subPath + File.separator);
				try {
					zos.putNextEntry(entry);
					zos.closeEntry();
				} catch (IOException e) {
					throw new RuntimeException("Error occurs when zipping files /n" + e.getMessage());
				}
			} else {
				for (int n = 0; n < childFileList.length; n++) {
					childFileList[n].getAbsolutePath().indexOf(file.getAbsolutePath());
					zip(srcRootDir, childFileList[n], zos);
				}
			}
		}
	}

	/**
	 * 对文件或文件目录进行压缩
	 * 
	 * @param srcPath
	 *            要压缩的源文件路径。如果压缩一个文件，则为该文件的全路径；如果压缩一个目录，则为该目录的顶层目录路径
	 * @param zipPath
	 *            压缩文件保存的路径。注意：zipPath不能是srcPath路径下的子文件夹
	 * @param zipFileName
	 *            压缩文件名
	 * @throws Exception
	 */
	public void zip(String srcPath, String zipPath, String zipFileName) throws Exception {
		File srcFile = new File(srcPath);
		// 判断压缩文件保存的路径是否为源文件路径的子文件夹，如果是，则抛出异常（防止无限递归压缩的发生）
		if (srcFile.isDirectory() && zipPath.indexOf(srcPath) != -1) {
			throw new RuntimeException("zipPath must not be the child directory of srcPath.");
		}

		// 判断压缩文件保存的路径是否存在，如果不存在，则创建目录
		File zipDir = new File(zipPath);
		if (!zipDir.exists() || !zipDir.isDirectory()) {
			zipDir.mkdirs();
		}

		// 创建压缩文件保存的文件对象
		String zipFilePath = zipPath + File.separator + zipFileName;
		File zipFile = new File(zipFilePath);
		if (zipFile.exists()) {
			// 检测文件是否允许删除，如果不允许删除，将会抛出SecurityException
			// SecurityManager securityManager = new SecurityManager();
			// securityManager.checkDelete(zipFilePath);
			// zipFile.delete();
			throw new RuntimeException("zip file already exists");
		}
		try (CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(zipFile), new CRC32());
				ZipOutputStream zos = new ZipOutputStream(cos);) {
			String srcRootDir = srcPath.substring(0, srcPath.lastIndexOf(File.separator));
			zip(srcRootDir, srcFile, zos);// source sourceFile resfile
			zos.flush();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * 解压缩zip包
	 * 
	 * @param zipFilePath
	 *            zip文件的全路径
	 * @param unzipFilePath
	 *            解压后的文件保存的路径
	 * @param includeZipFileName
	 *            解压后的文件保存的路径是否包含压缩文件的文件名。true-包含；false-不包含
	 */
	public void unzip(String zipFilePath, String unzipFilePath, boolean includeZipFileName) throws Exception {
		File zipFile = new File(zipFilePath);
		// 如果解压后的文件保存路径包含压缩文件的文件名，则追加该文件名到解压路径
		if (includeZipFileName) {
			String fileName = zipFile.getName();
			if (null != fileName && fileName.equals(fileName)) {
				fileName = fileName.substring(0, fileName.lastIndexOf("."));
			}
			unzipFilePath = unzipFilePath + File.separator + fileName;
		}
		// 创建解压缩文件保存的路径
		File unzipFileDir = new File(unzipFilePath);
		if (!unzipFileDir.exists() || !unzipFileDir.isDirectory()) {
			unzipFileDir.mkdirs();
		}

		// 开始解压
		ZipEntry entry = null;
		String entryFilePath = null, entryDirPath = null;
		File entryFile = null, entryDir = null;
		int index = 0, count = 0, bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;

		try (ZipFile zip = new ZipFile(zipFile)) {
			Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
			while (entries.hasMoreElements()) {
				entry = entries.nextElement();
				// 构建压缩包中一个文件解压后保存的文件全路径
				entryFilePath = unzipFilePath + File.separator + entry.getName();
				// 构建解压后保存的文件夹路径
				index = entryFilePath.lastIndexOf(File.separator);
				if (index != -1) {
					entryDirPath = entryFilePath.substring(0, index);
				} else {
					entryDirPath = "";
				}
				entryDir = new File(entryDirPath);
				// 如果文件夹路径不存在，则创建文件夹
				if (!entryDir.exists() || !entryDir.isDirectory()) {
					entryDir.mkdirs();
				}
				if (!entryFilePath.endsWith(File.separator)) {
					// 创建解压文件
					entryFile = new File(entryFilePath);
					/*
					 * if (entryFile.exists()) { // 检测文件是否允许删除，如果不允许删除，将会抛出SecurityException
					 * SecurityManager securityManager = new SecurityManager();
					 * securityManager.checkDelete(entryFilePath); // 删除已存在的目标文件 entryFile.delete();
					 * }
					 */

					// 写入文件
					bos = new BufferedOutputStream(new FileOutputStream(entryFile));
					bis = new BufferedInputStream(zip.getInputStream(entry));
					while ((count = bis.read(buffer, 0, bufferSize)) != -1) {
						bos.write(buffer, 0, count);
					}
					bos.flush();
					bos.close();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			if (null != bos) {
				try {
					bos.close();
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage());
				}
			}
			if (null != bis) {
				try {
					bis.close();
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage());
				}
			}
		}
	}

	/*
	 * public static void main(String[] args) { String dir =
	 * "C:\\Users\\IBM_ADMIN\\Desktop\\yangt2"; String zipPath =
	 * "C:\\Users\\IBM_ADMIN\\Desktop\\yangt1"; String zipFileName = "test.zip"; try
	 * { zip(dir, zipPath, zipFileName); } catch (Exception e) {
	 * e.printStackTrace(); }
	 * 
	 * String zipFilePath = "C:\\Users\\IBM_ADMIN\\Desktop\\yangt1\\test.zip";
	 * String unzipFilePath = "C:\\Users\\IBM_ADMIN\\Desktop\\upzip"; try {
	 * unzip(zipFilePath, unzipFilePath, false); } catch (Exception e) {
	 * e.printStackTrace(); } }
	 */
}
