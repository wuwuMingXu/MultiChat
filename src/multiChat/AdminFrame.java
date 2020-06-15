package multiChat;

import java.awt.*;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;

public class AdminFrame extends JFrame {
	static final String MULTICAST_IP = "233.0.0.1";
	static final int MULTICAST_PORT = 23333;
	private MulticastSocket socket = null;
	private InetAddress multiAddr = null;
	
	private DefaultListModel<User> model = new DefaultListModel<User>();
	private JList<User> list = new JList<User>(model);
	private JScrollPane listPanel = new JScrollPane(list);
	private JLabel label = new JLabel("在线用户：");
	private JLabel numLabel = new JLabel("共有0人");
	private int num = 0;
	
	public AdminFrame() {
		this.setSize(350, 500);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.setTitle("管理员");
		this.setLocationRelativeTo(null);
		try {
			socket = new MulticastSocket(MULTICAST_PORT);
			multiAddr = InetAddress.getByName(MULTICAST_IP);
			socket.joinGroup(multiAddr);
			socket.setLoopbackMode(false);
		} catch(Exception e) {
			e.printStackTrace();
		}
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					if (socket != null)
					{
						socket.leaveGroup(multiAddr);
						socket.close();
					}
				} catch(Exception e1)
				{
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					int opt = JOptionPane.showConfirmDialog(null, "是否移除该用户？", "移除用户", JOptionPane.YES_NO_OPTION);
					if (opt == JOptionPane.YES_OPTION) {
						User u = list.getSelectedValue();
						InetSocketAddress useraddr = (InetSocketAddress) u.getAddr();
						String addr = useraddr.getAddress().getHostAddress();
						String msg = "del|" + u.getName() + "|" + addr;
						byte[] buf = msg.getBytes();
						DatagramPacket out = new DatagramPacket(buf, buf.length, multiAddr, MULTICAST_PORT);
						try {
							socket.send(out);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		});
		list.setCellRenderer(new CellRenderer());
		label.setPreferredSize(new Dimension(350, 30));
		numLabel.setPreferredSize(new Dimension(350, 30));
		this.add(label, BorderLayout.NORTH);
		this.add(listPanel, BorderLayout.CENTER);
		this.add(numLabel, BorderLayout.SOUTH);
		this.setVisible(true);
		listen();
	}
	
	public void listen() {
		byte[] buf = new byte[1024];
		DatagramPacket in = new DatagramPacket(buf, buf.length);
		try {
			while (true) {
				socket.receive(in);
				String s = new String(in.getData(), 0, in.getLength());
				if (s.length() > 7 && s.substring(0, 6).equals("online") && !s.endsWith("\n")) {
					String name = s.substring(7, s.length());
					SocketAddress addr = in.getSocketAddress();
					User u = new User(name, addr);
					if (!model.contains(u)) {
						model.addElement(u);
						++num;
						numLabel.setText("共有" + num + "人");
					}
				} else if (s.length() > 4 && s.substring(0, 4).equals("exit") && !s.endsWith("\n")) {
					String name = s.substring(4, s.length());
					SocketAddress addr = in.getSocketAddress();
					User u = new User(name, addr);
					model.removeElement(u);
					--num;
					numLabel.setText("共有" + num + "人");
				}
			}
		} catch(IOException e) {
			if (!socket.isClosed()) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	public static void main(String[] args) {
		try {  
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }catch (Exception e) {  
           e.printStackTrace();  
        }  
		new AdminFrame();
	}
}
