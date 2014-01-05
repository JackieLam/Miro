import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.Calendar;

public class ProcessThread implements Runnable{
	Socket minetSocket;
	final static String VERSION = "CS1.1";
	CopyOnWriteArrayList<User> onlineUserList;
	boolean isSocketClosed;
	
	public ProcessThread(Socket socket, CopyOnWriteArrayList<User> onlineList){
		minetSocket = socket;
		//还没考虑onlineList的多线程操作
		onlineUserList = onlineList;
		isSocketClosed = false;
	}

	@Override
	public void run(){
		try{
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(minetSocket.getInputStream()));
			PrintWriter printWriter = new PrintWriter(minetSocket.getOutputStream(), true);
			
			while(!isSocketClosed){
				String command;
				if((command = inFromClient.readLine()) != null){
					String []info = command.split(" ");
					if(info != null){
						//handshake before exchanging any valid requests and response
						if(info[0].equals("MINET")){
							handshake(printWriter, info);
						}
						else if(info[0].equals(VERSION)){
							//haven't considered the incomplete command
							if(info[1].equals("LOGIN")){
								login(inFromClient, printWriter, info);
							}
							else if(info[1].equals("GETLIST")){
								getList(inFromClient, printWriter, info);
							}
							else if(info[1].equals("MESSAGE")){
								String userName = info[2];
								sendMessage(inFromClient, printWriter, userName);
							}
							else if(info[1].equals("LEAVE")){
								String userName = info[2];
								leave(inFromClient, printWriter, userName);
							}
							else if(info[1].equals("BEAT")){
								beat(info[2]);
								String str;
								String date = null;
								int length = 0;
								while(!(str = inFromClient.readLine()).equals("")){
									//only content-Length information is useful for us now
									String []head = str.split(" ");
									if(head[0].equals("Content-Length")){
										length = Integer.valueOf(head[1]);
									}
									//the date info
									else if(head[0].equals("Date")){
										date = head[1];
									}
								}
								//read the data if it exists
								char []message = new char[length];
								inFromClient.read(message, 0, length);
							}
						}
					}
					
				}
			}
			inFromClient.close();
			printWriter.close();
			close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void close(){
		try {
			minetSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void handshake(PrintWriter pw, String info[]){
		try  
        {
			if(info[1] != null){
				InetAddress ia = InetAddress.getLocalHost();   
				String hostname = ia.getHostName();
				String response = "MIRO " + hostname;
				pw.println(response);
			}
			else{
				System.out.println("incomplete command");
			}
        }  catch(UnknownHostException e)  
        {  
        	e.printStackTrace();  
        }  
	}
	
	private void sendOnlineList(PrintWriter pw){
		String request =  VERSION + " LIST\r\n";
		String data = "";
		if(!onlineUserList.isEmpty()){
			for(int i = 0; i < onlineUserList.size(); i++){
				User user = onlineUserList.get(i);
				if(user.lifetime > 0){
					Socket socket = user.getSocket();
					data += user.getName() + " " + socket.getInetAddress().getHostAddress() + " " + user.getP2pPort() + "\r\n";
				}	
			}
			data += "\r\n";
		}
		System.out.println("online users are : ");
		System.out.println(data);
		String head = getDate() + getContentLength(data) + "\r\n";
		String response = request + head + data;
		pw.print(response);
		pw.flush();
	}
	
	private void login(BufferedReader bf, PrintWriter pw, String info[]) throws IOException{
		if(info.length != 3){
			//error detected!!!!
		}
		//reads the head line
		String str;
		int length = 0;
		int P2pPort  = -1;
		while(!(str = bf.readLine()).equals("")){
			//only content-Length information is useful for us now
			String []head = str.split(" ");
			if(head[0].equals("Content-Length")){
				length = Integer.valueOf(head[1]);
			}
			if(head[0].equals("Port")){
				P2pPort = Integer.valueOf(head[1]);
			}
		}
		//read the data if it exists
		char []d = new char[1024];  //maximum size is 1024 bytes
		bf.read(d, 0, length);
		
		//read length bytes 
		String request = null;
		String head = null;
		String data = null;
		
		if(info[2] == null){
			System.out.println("incomplete command");
			//throw exception
		}
		String userName = info[2];
		boolean isNameExist = false;
		if(!onlineUserList.isEmpty()){
			for(int i = 0; i < onlineUserList.size(); i ++){
				User u = onlineUserList.get(i);
				if(u.getName().equals(userName)){
					if(u.lifetime > 0){
						isNameExist = true;
						request = VERSION + " STATUS 0\r\n";
						if(u.getSocket().getInetAddress().getHostAddress().equals(minetSocket.getInetAddress().getHostAddress())){
							data = "Already logged in";
						}
						else{
							data = "Name Already used";
						}
						head = getDate() + getContentLength(data) + "\r\n";
						String response = request + head + data;
						pw.print(response);
						pw.flush();
						break;
					}
				}
			}
		}
		//login success!
		if(!isNameExist){
			User user = new User(minetSocket, userName, P2pPort);
			onlineUserList.add(user);
			//send status message
			request = VERSION + " STATUS 1\r\n";
			head = getDate() + getContentLength("") + "\r\n";
			String response = request + head;
			pw.print(response);
			pw.flush();
			sendOnlineList(pw);
			update(1, userName, minetSocket, P2pPort);
		}
	}
	
	private void getList(BufferedReader bf, PrintWriter pw, String info[]) throws IOException{
		String str;
		int length = 0;
		while(!(str = bf.readLine()).equals("")){
			//only content-Length information is useful for us now
			String []head = str.split(" ");
			if(head[0].equals("Content-Length")){
				length = Integer.valueOf(head[1]);
			}
		}
		//read the data if it exists
		char []d = new char[1024];  //maximum size is 1024 bytes
		bf.read(d, 0, length);
		
		sendOnlineList(pw);
	}
	
	private void sendMessage(BufferedReader bf, PrintWriter pw, String userName) throws NumberFormatException, IOException{
		//read the message data
		String str;
		int length = 0;
		while(!(str = bf.readLine()).equals("")){
			//only content-Length information is useful for us now
			String []head = str.split(" ");
			if(head[0].equals("Content-Length")){
				length = Integer.valueOf(head[1]);
			}
		}
		//read the data if it exists
		char []message = new char[length];
		bf.read(message, 0, length);
		//get data
		String data = String.valueOf(message);
		
		String request = VERSION + " CSMESSAGE " + userName + "\r\n";
		String head = getDate() + getContentLength(data) + "\r\n";
		
		String response = request + head + data;
		System.out.println("The message going to send is :");
		System.out.println(data);

		if(!onlineUserList.isEmpty()){
			for(int i = 0; i < onlineUserList.size(); i++){
				User user = onlineUserList.get(i);
				if(user.lifetime > 0){
					try {
						if(!userName.equals(user.getName())){
							PrintWriter printWriter = new PrintWriter(user.getSocket().getOutputStream(), true);
							printWriter.print(response);
							printWriter.flush();
							printWriter = null;
						}
						else{
							pw.print(response);
							pw.flush();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	//status: 0-someone off-line, 1-someone online
	private void update(int status, String userName, Socket socket, int P2pPort){
		String hint = "";
		if(status == 0)
			hint = "some one off line";
		else if(status == 1)
			hint = "some one on line";
		System.out.println(hint);
		System.out.println();
		String data = "";
		String request = VERSION + " " + "UPDATE " + status + " " + userName + " " + socket.getInetAddress().getHostAddress() + " " + P2pPort + "\r\n";
		String head = getDate() + getContentLength(data) + "\r\n";
		String response = request + head + data;
		if(!onlineUserList.isEmpty()){
			for(int i = 0; i < onlineUserList.size(); i++){
				User user = onlineUserList.get(i);
				if(!user.getName().equals(userName)){
					if(user.lifetime > 0){
						try {
							PrintWriter printWriter = new PrintWriter(user.getSocket().getOutputStream(), true);
							printWriter.print(response);
							printWriter.flush();
							printWriter = null;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	private void leave(BufferedReader bf, PrintWriter pw, String userName) throws NumberFormatException, IOException{
		String str;
		String date = null;
		int length = 0;
		while(!(str = bf.readLine()).equals("")){
			//only content-Length information is useful for us now
			String []head = str.split(" ");
			if(head[0].equals("Content-Length")){
				length = Integer.valueOf(head[1]);
			}
			//the date info
			else if(head[0].equals("Date")){
				date = head[1];
			}
		}
		//read the data if it exists
		char []message = new char[length];
		bf.read(message, 0, length);
		
		for(int i = 0; i < onlineUserList.size(); i++){
			User user = onlineUserList.get(i);
			if(user.getName().equals(userName)){
				onlineUserList.remove(i);
				isSocketClosed = true;
				update(0, userName, minetSocket, user.getP2pPort());
				break;
			}
		}
	}
	
	private void beat(String name) throws NumberFormatException, IOException{
		for(User user:onlineUserList)
		{
			if(user.name.equals(name))
				user.lifetime = 10;
		}
	}
	
	private String getMonthInString(int month){
		String mon;
		switch(month){
		case 0:
			mon = "Jan";
			break;
		case 1:
			mon = "Feb";
			break;
		case 2:
			mon = "Mar";
			break;
		case 3:
			mon = "Apr";
			break;
		case 4:
			mon = "May";
			break;
		case 5:
			mon = "Jun";
			break;
		case 6:
			mon = "Jul";
			break;
		case 7:
			mon = "Aug";
			break;
		case 8:
			mon = "Sept";
			break;
		case 9:
			mon = "Oct";
			break;
		case 10:
			mon = "Nov";
			break;
		case 11:
			mon = "Dec";
			break;
		default:
			mon = "Jan";
			break;
		}
		return mon;
	}

	private String getDate(){
		String headDate;
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"), Locale.CHINESE);
	    int year = calendar.get(Calendar.YEAR);
	    int month=calendar.get(Calendar.MONTH); 
	    int day=calendar.get(Calendar.DATE);
	    int minute=calendar.get(Calendar.MINUTE);
	    int hour=calendar.get(Calendar.HOUR);
	    int second=calendar.get(Calendar.SECOND);
	    int daysOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
	    String mon;
	    switch(daysOfWeek){
		case 1:
			mon = "Sun";
			break;
	    case 2:
	    	mon = "Mon";
	    	break;
	    case 3:
	    	mon = "Tues";
	    	break;
	    case 4:
	    	mon = "Wed";
	    	break;
	    case 5:
	    	mon = "Thur";
	    	break;
	    case 6:
	    	mon = "Fri";
	    	break;
		case 7:
			mon = "Sat";
			break;
	    default:
	    	mon = "Sun";
	    	break;
	    }
	    //unfinished, minute 9 should be 09
	    String h;
	    if(hour < 10)
	    	h = "0" + hour;
	    else
	    	h = String.valueOf(hour);
	    String min;
	    if(minute < 10)
	    	min = "0" + minute;
	    else
	    	min = String.valueOf(minute);
	    String date = mon + ", " + day + " " + getMonthInString(month) + " " + year + " " + h + ":" + min + ":" + second + " GMT";
	    headDate = "Date " + date + "\r\n";
		return headDate;
	}
	
	private String getContentLength(String data){
		byte[] buff = data.getBytes();
		int l = buff.length;
		String contentLength = "Content-Length " + l + "\r\n";
		return contentLength;
	}
}

