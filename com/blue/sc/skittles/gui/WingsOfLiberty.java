package com.ibm.sc.skittles.gui;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.PasswordView;

public class WingsOfLiberty {
	private JFrame mainFrame = new JFrame("Lazy Boy v.3000");
	private Icon okIcon = new ImageIcon("ico/ok.png");
	private JButton ok = new JButton("Confirm", okIcon);
	private Box parameterFields = Box.createVerticalBox();
	private Box labelAndText = Box.createHorizontalBox();

	private JTextField host = new JTextField(40);
	private JTextField port = new JTextField(40);
	private JTextField userName = new JTextField(40);
	private JTextField password = new JTextField(40);
	private JTextField jenkins = new JTextField(40);
	private JTextField commonFolder = new JTextField(40);
	private JTextField jobFolder = new JTextField(40);
	private JTextField tempFolder = new JTextField(40);
	JTextArea console = new JTextArea(8, 20);
	private JScrollPane taJsp = new JScrollPane(console);
	private JDialog alert = new JDialog(mainFrame, "alert", true);

	public void init() {
		labelAndText.add(new JLabel("host:"));
		labelAndText.add(host);
		parameterFields.add(labelAndText);

		labelAndText = Box.createHorizontalBox();
		labelAndText.add(new JLabel("port:"));
		labelAndText.add(port);
		parameterFields.add(labelAndText);

		labelAndText = Box.createHorizontalBox();
		labelAndText.add(new JLabel("userName:"));
		labelAndText.add(userName);
		parameterFields.add(labelAndText);

		labelAndText = Box.createHorizontalBox();
		labelAndText.add(new JLabel("password:"));
		labelAndText.add(password);
		parameterFields.add(labelAndText);

		labelAndText = Box.createHorizontalBox();
		labelAndText.add(new JLabel("JenkinsFolder:"));
		labelAndText.add(jenkins);
		parameterFields.add(labelAndText);

		labelAndText = Box.createHorizontalBox();
		labelAndText.add(new JLabel("commonFolder:"));
		labelAndText.add(commonFolder);
		parameterFields.add(labelAndText);

		labelAndText = Box.createHorizontalBox();
		labelAndText.add(new JLabel("jobFolder:"));
		labelAndText.add(jobFolder);
		parameterFields.add(labelAndText);

		labelAndText = Box.createHorizontalBox();
		labelAndText.add(new JLabel("tempFolder:"));
		labelAndText.add(tempFolder);
		parameterFields.add(labelAndText);

		labelAndText = Box.createHorizontalBox();
		labelAndText.add(new JLabel("Console:"));
		labelAndText.add(taJsp);
		parameterFields.add(labelAndText);
		mainFrame.add(parameterFields, BorderLayout.NORTH);

		JPanel bottom = new JPanel();
		bottom.add(ok);
		mainFrame.add(bottom, BorderLayout.SOUTH);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.pack();
		addListener(ok);
		mainFrame.setVisible(true);
		mainFrame.setLocationRelativeTo(null);
	}

	private void addListener(JButton item) {
		ButtonListener bl = new ButtonListener(host, port, 
				userName, password,
				jenkins, commonFolder, 
				jobFolder, tempFolder,alert,console);
		item.addActionListener(bl);
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			System.exit(0);
		}
		new WingsOfLiberty().init();
	}
}
