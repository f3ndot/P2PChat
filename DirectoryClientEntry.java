import java.net.InetAddress;
import java.net.UnknownHostException;


public class DirectoryClientEntry {

	public String username;
	public String hostname;
	public InetAddress hostIP;
	public int protocolPort;
	public int rating;
	public String usernameChatroom; // chatroom name is another user's username
	
	public DirectoryClientEntry(String username, String hostIPStr, int protocolPort, int rating, String usernameChatroom) {
		
		InetAddress hostIP = null;
		try {
			hostIP = InetAddress.getByName(hostIPStr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		this.username = username;
		this.hostname = hostIP.getHostAddress();
		this.hostIP = hostIP;
		this.protocolPort = protocolPort;
		this.rating = rating;
		this.usernameChatroom = usernameChatroom;
	}
	
	public String toString() {
		String objectString = "User: "+username+"@"+hostname+" ("+hostIP+":"+protocolPort+")"+", Rating: "+rating+", In chatroom:"+usernameChatroom;
		return objectString;
	}
	
}
