package multiChat;

import java.net.*;
import java.util.Arrays;
import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;

public class ChattingPane extends JPanel {
	private MulticastSocket socket = null;
	private MulticastSocket socketf = null;
	private InetAddress multiAddr = null;
	private static final int DATA_LEN = 4096;
	RecvThread r = new RecvThread();
	ExecutorService exec = Executors.newSingleThreadExecutor();
	Timer timer = null;
	
	private MultiChat parent = null;
	private JTextArea msgArea = new JTextArea(19, 20);
	private JScrollPane message = new JScrollPane(msgArea);
	private JTextArea sendArea = new JTextArea(5, 20);
	private JPanel bottomPane = new JPanel();
	private JButton exit = new JButton("退出");
	private JButton send = new JButton("发送");
	private JButton sendFile = new JButton("文件");
	private JLabel msgLabel = new JLabel("消息窗口");
	private JPanel msgPane = new JPanel(new GridLayout());
	private JPanel sendPane = new JPanel(new GridLayout());
	private JLabel sendLabel = new JLabel("发送窗口");
	private JFileChooser fileChooser = new JFileChooser();
	private Font font = new Font("宋体", Font.PLAIN, 15);
	
	public ChattingPane(MulticastSocket s, MulticastSocket sf, InetAddress addr, MultiChat p) {
		this.socket = s;
		this.socketf = sf;
		this.multiAddr = addr;
		this.parent = p;
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				parent.exit();
			}			
		});
		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				sendMsg();
			}
		});
		sendFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int i = fileChooser.showOpenDialog(parent);
				if (i == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					sendfile(selectedFile);
				}
			}		
		});
		bottomPane.setLayout(new FlowLayout());
		bottomPane.add(exit);
		bottomPane.add(send);
		bottomPane.add(sendFile);
		msgArea.setEditable(false);
		msgArea.setLineWrap(true);
		msgArea.setFont(font);
		sendArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		sendArea.setLineWrap(true);
		sendArea.setFont(font);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		msgPane.add(msgLabel);
		msgPane.setPreferredSize(new Dimension(800, 31));
		sendPane.add(sendLabel);
		sendPane.setPreferredSize(new Dimension(800, 31));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		this.add(msgPane);
		this.add(message);
		this.add(sendPane);
		this.add(sendArea);
		this.add(Box.createVerticalStrut(5));
		this.add(bottomPane);
		sendArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send");
		sendArea.getActionMap().put("send", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				sendMsg();
			}	
		});
		r.start();
		timer = new Timer(5000, event->sendOnline());
		timer.start();
	}
	
	void reqFocus() {
		sendArea.requestFocus();
	}
	
	private void sendMsg() {
		if (sendArea.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "不能发送空白信息", "警告", JOptionPane.WARNING_MESSAGE);
		}
		else {
			String msg = parent.getName() + ":\n" + sendArea.getText() + "\n";
			byte[] m = msg.getBytes();
			DatagramPacket out = new DatagramPacket(m, m.length, multiAddr, MultiChat.MULTICAST_PORT);
			try {
				socket.send(out);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			sendArea.setText("");
		}
	}
	
	private void sendfile(File file) {
		String filename = file.getName();
		String filelen = "" + file.length();
		byte[] sendBuf = new String("File").getBytes();
		DatagramPacket out = new DatagramPacket(sendBuf, sendBuf.length, multiAddr, MultiChat.MULTICAST_PORT);
		try {
			socket.send(out); 
			sendBuf = filename.getBytes();
			out.setData(sendBuf);
			socket.send(out); //发送文件名
			sendBuf = filelen.getBytes();
			out.setData(sendBuf);
			socket.send(out); //发送文件大小
			sendBuf = parent.getName().getBytes();
			out.setData(sendBuf);
			socket.send(out); //发送用户昵称
		} catch (IOException e) {
			e.printStackTrace();
		}
		try(FileInputStream in = new FileInputStream(file)){
			sendBuf = new byte[DATA_LEN];
			int readBytes = 0;
			while ((readBytes = in.read(sendBuf)) != -1) {
				out = new DatagramPacket(sendBuf, readBytes, multiAddr, MultiChat.MULTICAST_PORT + 1);
				socketf.send(out);
				Thread.sleep(1);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendOnline() {
		String s = "online|" + parent.getName();
		byte[] sendBuf = s.getBytes();
		DatagramPacket out = new DatagramPacket(sendBuf, sendBuf.length, multiAddr, MultiChat.MULTICAST_PORT);
		try {
			socket.send(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class RecvThread extends Thread {
		public RecvThread() {}
		public void run() {
			byte[] buf = new byte[DATA_LEN];
			DatagramPacket recv = new DatagramPacket(buf, buf.length);
			try {
				while (true) {
					socket.receive(recv);
					String s = new String(recv.getData(),0, recv.getLength());
					if (s.equals("File")) {
						socket.receive(recv);
						String file = new String(recv.getData(), 0, recv.getLength());
						socket.receive(recv);
						String len = new String(recv.getData(), 0, recv.getLength());
						int fileLen = Integer.parseInt(len);
						socket.receive(recv);
						String user = new String(recv.getData(), 0, recv.getLength());
						System.out.println(file);
						exec.execute(new RecvFile(file, fileLen, user));
					} else if (s.length() > 7 && s.substring(0, 6).equals("online") && !s.endsWith("\n")) {
						String name = s.substring(7, s.length());
						SocketAddress addr = recv.getSocketAddress();
						User u = new User(name, addr);
						parent.addUser(u);
					} else if (s.length() > 4 && s.substring(0, 4).equals("exit") && !s.endsWith("\n")) {
						String name = s.substring(4, s.length());
						SocketAddress socketAddress = recv.getSocketAddress();
						parent.delUser(new User(name, socketAddress));
					} else if (s.length() > 3 && s.substring(0, 3).equals("del") && !s.endsWith("\n")) {
						String[] msg = s.split("\\|");
						String localip = InetAddress.getLocalHost().getHostAddress();
						if (msg[1].equals(parent.getName()) && msg[2].equals(localip)) {
							JOptionPane.showMessageDialog(null, "您被移出了群聊", "提示", JOptionPane.INFORMATION_MESSAGE);
							exit();
						}
					} else {
						Arrays.fill(buf, (byte)0);
						msgArea.append(s);
						msgArea.selectAll();
						if (msgArea.getSelectedText() != null) {
							msgArea.setCaretPosition(msgArea.getSelectedText().length());
							msgArea.requestFocus();
						}
						sendArea.requestFocus();
					}					
				}
			} catch(IOException e) {
				if (!socket.isClosed())
					e.printStackTrace();
			} 
		}
		
		private void exit() {
			try {
				String s = "exit" + parent.getName();
				byte[] buf = s.getBytes();
				DatagramPacket out = new DatagramPacket(buf, buf.length, multiAddr, MultiChat.MULTICAST_PORT);
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
			exec.shutdown();
			timer.stop();
			System.gc();
			System.exit(0);
		}
	}
	
	class RecvFile implements Runnable	 {
		private String path;
		private int len;
		private String sender;
		public RecvFile(String f, int l, String user) {
			path = ".\\" + f;
			System.out.println(path);
			sender = user;
			len = l;
			msgArea.append(sender + "发送了文件：" + f + "\n");
			msgArea.selectAll();
			if (msgArea.getSelectedText() != null) {
				msgArea.setCaretPosition(msgArea.getSelectedText().length());
				msgArea.requestFocus();
			}
			sendArea.requestFocus();
		}
		public void run() {
			byte[] buf = new byte[DATA_LEN];
			DatagramPacket recv = new DatagramPacket(buf, buf.length);
			int writeBytes = 0;
			int numBytes = 0;
			try(BufferedOutputStream out = 
					new BufferedOutputStream(
						new FileOutputStream(path))) {
				socketf.receive(recv);
				writeBytes = recv.getLength();
				numBytes += writeBytes;
				while (numBytes < len) {
					out.write(buf, 0, writeBytes);
					socketf.receive(recv);
					writeBytes = recv.getLength();
					numBytes += writeBytes;
				}
				out.write(buf, 0, writeBytes);
				out.flush();
				System.out.println("发送完成");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
