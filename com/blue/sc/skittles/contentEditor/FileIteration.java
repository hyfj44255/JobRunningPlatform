package com.ibm.sc.skittles.contentEditor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import javax.swing.JTextArea;

public class FileIteration {
	private static String substitutionFile = "\\source\\sfa.variables";
	private String workingPath;
	private String ftpWorkingPath;
	private String host;
	private JTextArea console;

	public FileIteration(String workingPath, String ftpWorkingPath, String host,JTextArea console) {
		super();
		this.workingPath = workingPath;
		this.ftpWorkingPath = ftpWorkingPath;
		this.host = host;
		this.console = console;
	}

	public void fileIterator() throws Exception {
		try {
			Files.walkFileTree(Paths.get(this.workingPath), new SimpleFileVisitor<Path>() {
				// before visiting folder
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					//System.out.println("visiting " + dir + " -dir");
					return FileVisitResult.CONTINUE;
				}

				// visiting file
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.toString().contains(".ksh") || file.toString().contains(".php")
							|| file.toString().contains(".sql")) {
						//console.append("modified file: "+file.toString()+"\n");
						ArrayList<String> list;
						try {
							list = (ArrayList<String>) Files.readAllLines(file, Charset.forName("gbk"));
							ArrayList<String> afterRplace = sqlSchemasHandler(list);
							Files.write(file, afterRplace, Charset.forName("gbk"));
						} catch (IOException e) {
							throw new RuntimeException(e.getMessage());
						}
					} else if (file.toString().endsWith("sfa.variables")) {
						//console.append("modified file: "+file.toString()+"\n");
						try {
							commonJobPretreat();
						} catch (Exception e) {
							throw new RuntimeException(e.getMessage());
						}
					}
					return FileVisitResult.CONTINUE;
				}

				// after visiting failed
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException { // 写一些具体的业务逻辑
					return super.visitFileFailed(file, exc);
				}

				// after visiting folder
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException { // 写一些具体的业务逻辑
					return super.postVisitDirectory(dir, exc);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void commonJobPretreat() throws Exception {
		String srcFile = System.getProperty("user.dir") + substitutionFile;
		ArrayList<String> list = null;
		try {
			list = (ArrayList<String>) Files.readAllLines(Paths.get(srcFile), Charset.forName("gbk"));
			list = this.dirModifier(list);
			StringBuffer targetFile = new StringBuffer();
			targetFile.append(workingPath);
			targetFile.append(File.separator);
			targetFile.append("sfa.variables");
			Files.write(Paths.get(targetFile.toString()), list, Charset.forName("gbk"));
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	private ArrayList<String> dirModifier(ArrayList<String> parameter) {
		ArrayList<String> res = new ArrayList<String>();
		String content = null;
		String dev = host.substring(0, host.indexOf("."));
		String jenkins = ftpWorkingPath.substring(ftpWorkingPath.lastIndexOf("/") + 1);
		for (int i = 0, j = parameter.size(); i < j; i++) {
			content = parameter.get(i);
			res.add(content.replace("${dev}", dev).replace("${jenkins}", jenkins));
		}
		return res;
	}

	public ArrayList<String> sqlSchemasHandler(ArrayList<String> list) {
		ArrayList<String> afterReplace = new ArrayList<String>();
		for (int i = 0, j = list.size(); i < j; i++) {
			afterReplace.add(list.get(i).replaceAll("sme.|sctid.", "").replaceAll("/var/www/htdocs/sales/salesconnect",
					ftpWorkingPath));
		} 
		return afterReplace;
	}

	public static String sqlSchemasHandler2(String sql) {
		String[] splitedSql = sql.toUpperCase().split("UNION");
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < splitedSql.length; i++) {
			String sqlGroup = splitedSql[i];
			String[] subSqlGroup = sqlGroup.split("FROM|JOIN");

			for (int j = 0; j < subSqlGroup.length; j++) {
				String afterProcess = subSqlGroup[j];
				if (j > 0) {
					int firstBlank = subSqlGroup[j].trim().indexOf(" ");
					String firstWord = subSqlGroup[j].trim().substring(0, firstBlank);
					String theRest = subSqlGroup[j].trim().substring(firstBlank + 1);
					if (firstWord.contains(".")) {
						firstWord = firstWord.substring(firstWord.indexOf(".") + 1);
					}
					if (j == 1) {
						afterProcess = "FROM " + firstWord + " " + theRest;
					} else if (j > 1) {
						afterProcess = "JOIN " + firstWord + " " + theRest;
					}
				}
				buffer.append(afterProcess);
			}
			if (i != splitedSql.length - 1) {
				buffer.append("UNION");
			}
		}
		return buffer.toString();
	}

}
