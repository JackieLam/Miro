import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.TimerTask;
import java.util.Timer;

public class TCPServer {

	static CopyOnWriteArrayList<User> onlineUserList = new CopyOnWriteArrayList<User>();

	static TimerTask tt=new TimerTask() {
		public void run() {
			for(User user:onlineUserList) {
				user.lifetime -= 1;
				
			}
		}
	};
	public static void main(String argv[]) throws Exception{
		
		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		ServerSocket miroSocket = null;
		Socket minetSocket = null;
		int serverPort = 6789;
		Timer clock = new Timer();
		clock.scheduleAtFixedRate(tt, 1000, 1000);

		try{
			miroSocket = new ServerSocket(serverPort);
			System.out.println("Server on!");
			while(true){
				minetSocket = miroSocket.accept();
				
				System.out.println("Print user list");
				for(User user:onlineUserList)
				{
					System.out.println(user);
					if(user.lifetime==0)
						onlineUserList.remove(user);
				}
				
				System.out.println("New thread created!");
				ProcessThread pt = new ProcessThread(minetSocket, onlineUserList);
                Thread thread = new Thread(pt);   
				threadPool.execute(thread);
			}
		} catch (Exception exception){
			exception.printStackTrace();
		} finally {
			try{
				miroSocket.close();
				threadPool.shutdown();
			} catch (Exception exception){}
		}
	}
}
