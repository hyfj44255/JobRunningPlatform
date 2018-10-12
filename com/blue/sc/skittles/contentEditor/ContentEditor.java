package com.ibm.sc.skittles.contentEditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ContentEditor {

	public static void main(String[] args) throws Exception {
		File file = new File("C:\\Users\\IBM_ADMIN\\Desktop\\common\\commonDBValidation.ksh");
		InputStream is = new FileInputStream(file);
		InputStreamReader fileR = new InputStreamReader(is);
		BufferedReader bufferR = new BufferedReader(fileR);
		String line;
		StringBuffer stringBuffer = new StringBuffer();
		while ((line = bufferR.readLine()) != null) {
			stringBuffer.append(line).append("\n");
		}
		File resFile = new File("C:\\Users\\IBM_ADMIN\\Desktop\\commonDBValidation.ksh");
		FileWriter fileWriter = new FileWriter(resFile);
		String reString = stringBuffer.toString();
		fileWriter.write(reString);
		bufferR.close();
		fileWriter.flush();
		fileWriter.close();
	}
}
