package multiChat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;

import javax.swing.*;

public class LoginFrame extends JFrame{
	private JLabel label = new JLabel("请输入昵称：");
	private JTextField name = new JTextField(35);
	private JButton btn = new JButton("登录");
	private JButton exit = new JButton("退出");
	private JPanel up = new JPanel(new BorderLayout());
	private JPanel namePanel = new JPanel();
	private JPanel bottom = new JPanel();
	
	public LoginFrame() {
		this.setSize(350, 200);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("登录");
		this.setLocationRelativeTo(null);
		
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (name.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "请输入昵称！", "警告", JOptionPane.WARNING_MESSAGE);
				} 
				else {
					LoginFrame.this.setVisible(false);
					new MultiChat(name.getText());
				}
			}
		});
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
		});
		
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		label.setFont(new Font("宋体", 1, 20));
		label.setHorizontalAlignment(JLabel.CENTER);
		namePanel.setPreferredSize(new Dimension(350, 40));
		bottom.setPreferredSize(new Dimension(350, 60));
		up.add(label, BorderLayout.CENTER);
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.LINE_AXIS));
		namePanel.add(name);
		bottom.add(btn);
		bottom.add(Box.createHorizontalStrut(80));
		bottom.add(exit);
		this.add(up);
		this.add(namePanel);
		this.add(bottom);
		this.getRootPane().setDefaultButton(btn);
		this.setVisible(true);
	}
}
