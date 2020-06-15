package multiChat;

import java.net.SocketAddress;

public class User {
	private String name = null;
	private SocketAddress addr = null;
	
	public User() {}
	public User(String n, SocketAddress a) {
		setName(n);
		setAddr(a);
	}
	
	private void setName(String n) {
		name = n;
	}
	private void setAddr(SocketAddress a) {
		addr = a;
	}
	public String getName() {
		return name;
	}
	public SocketAddress getAddr() {
		return addr;
	}
	
	public int hashCode() {
		return addr.hashCode();
	}
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == User.class) {
			User u = (User)obj;
			if (addr != null)
				return addr.equals(u.getAddr()) && name.equals(u.getName());
		}
		return false;
	}
}
