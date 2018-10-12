package com.ibm.sc.skittles.gui;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Frame;
import java.awt.TextField;

import javax.swing.UIManager;

public class SendMailer
{
	private Frame f = new Frame("123");
	private TextField tf = new TextField(40);
	private Button send = new Button("456");
	public void init()
	{
//		send.addActionListener(new Draw(tf));
		f.add(tf);
		f.add(send , BorderLayout.SOUTH);
		f.pack();
		f.setVisible(true);
	}
	public static void main(String[] args) 
	{
		//new SendMailer().init();
		for(UIManager.LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) {
			System.out.println(info.getName()+"-->"+info);
		}
	}
}