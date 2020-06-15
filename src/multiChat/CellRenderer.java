package multiChat;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.*;

public class CellRenderer extends JPanel implements ListCellRenderer<User> {
	private String name;
	private Color background;
	private Color foreground;
	
	@Override
	public Component getListCellRendererComponent(JList<? extends User> list, User value, int index, boolean isSelected,
			boolean cellHasFocus) {
		// TODO Auto-generated method stub
		name = value.getName();
		background = isSelected ? list.getSelectionBackground() : list.getBackground();
		foreground = isSelected ? list.getSelectionForeground() : list.getForeground();
		return this;
	}
	
	public void paintComponent(Graphics g) {
		g.setColor(background);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(foreground);
		g.setFont(new Font("宋体", Font.PLAIN, 15));
		g.drawString(name, 5, 20);

	}
	
	public Dimension getPreferredSize() {
		return new Dimension(30, 30);
	}

}
