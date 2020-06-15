package multiChat;

import java.awt.*;
import javax.swing.*;

public class UserPane extends JPanel {
	private JPanel namePane = new JPanel();
	private JLabel numLabel = new JLabel("共有0人");
	private String name;
	private DefaultListModel<User> model = new DefaultListModel<User>();
	private JList<User> member = new JList<User>(model);
	private JScrollPane memberPane = new JScrollPane(member);
	private ChattingPane chatPanel = null;
	private int number = 0;
	
	public UserPane(String n, ChattingPane p) {
		name = n;
		chatPanel = p;
		namePane.add(new JLabel(name));
		member.setCellRenderer(new CellRenderer());
		numLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
		this.setLayout(new BorderLayout());
		this.add(BorderLayout.NORTH, namePane);
		this.add(BorderLayout.CENTER, memberPane);
		this.add(BorderLayout.SOUTH, numLabel);
		p.sendOnline();
	}
	
	public void addUser(User u) {
		if (!model.contains(u)) {
			model.addElement(u);
			++number;
			numLabel.setText("共有"+ number + "人");
		}		
	}
	
	public void delUser(User u) {
		model.removeElement(u);
		--number;
		numLabel.setText("共有"+ number + "人");
	}

}
