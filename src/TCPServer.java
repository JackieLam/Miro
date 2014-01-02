import java.net.*;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;

public class TCPServer
{
	static CopyOnWriteArrayList<User> onlineUserList = new CopyOnWriteArrayList<User>();

	static TimerTask timerTask = new TimerTask() {
		public void run()
		{
			for (User user:onlineUserList) {
				user.lifetime = user.lifetime - 1;
			}
		}
	};

	public static void main(String argv[]) throws Exception {
		
	}
}