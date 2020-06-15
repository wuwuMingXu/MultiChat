package multiChat;

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

public class MultiChat extends JFrame{
	static final String MULTICAST_IP = "233.0.0.1";
	static final int MULTICAST_PORT = 23333;
	
	private MulticastSocket socket = null;
	private MulticastSocket socketf = null;
	private InetAddress multiAddr = null;
	private JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
	private ChattingPane chatPanel = null;
	private UserPane userPanel = null;
	private String name;
	
	public MultiChat(String n) {
		name = n;
		setSize(1000,600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("多播聊天工具");
		this.getContentPane().add(mainPanel);
		try {
			socket = new MulticastSocket(MULTICAST_PORT);
			multiAddr = InetAddress.getByName(MULTICAST_IP);
			socket.joinGroup(multiAddr);
			socket.setLoopbackMode(false);
			socketf = new MulticastSocket(MultiChat.MULTICAST_PORT + 1);
			socketf.joinGroup(multiAddr);
			socketf.setLoopbackMode(false);
		} catch(Exception e) {
			e.printStackTrace();
		}
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		
		chatPanel = new ChattingPane(socket, socketf, multiAddr, this);
		userPanel = new UserPane(name, chatPanel);
		mainPanel.setDividerSize(2);
		mainPanel.setOneTouchExpandable(false);
		mainPanel.setEnabled(false);
		mainPanel.setLeftComponent(chatPanel);
		mainPanel.setRightComponent(userPanel);
		this.setResizable(false);
		this.setVisible(true);
		mainPanel.setDividerLocation(0.8);
		this.setLocationRelativeTo(null);
		chatPanel.reqFocus();
	}
	
	public String getName() {
		return name;
	}
	
	public void addUser(User u) {
		userPanel.addUser(u);
	}
	
	public void delUser(User u) {
		userPanel.delUser(u);
	}
	
	public void exit() {
		try {
			String s = "exit" + name;
			byte[] buf = s.getBytes();
			DatagramPacket out = new DatagramPacket(buf, buf.length, multiAddr, MULTICAST_PORT);
			socket.send(out);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (socket != null)
				{
					socket.leaveGroup(multiAddr);
					socket.close();
				}
				if (socketf != null)
				{
					socketf.leaveGroup(multiAddr);
					socketf.close();
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		chatPanel.exec.shutdown();
		chatPanel.timer.stop();
		try {
			chatPanel.r.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.gc();
		System.exit(0);
	}
	
	public static void main(String[] args) {
		try {  
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }catch (Exception e) {  
           e.printStackTrace();  
        }  
		new LoginFrame();
	}
}
