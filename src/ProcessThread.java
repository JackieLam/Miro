import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;


public class ProcessThread implements Runnable{
    final static String VERSION = "CS1.0";
    Socket minetSocket;
    CopyOnWriteArrayList<User> onlineUserList;
    
    /**
     * mark whether this user is online
     */
    boolean online;
    
    public ProcessThread(Socket minetSocket,
            CopyOnWriteArrayList<User> onlineUserList) {
        this.minetSocket = minetSocket;
        this.onlineUserList = onlineUserList;
        online = true;
    }

    @Override
    public void run() {
        
    }
    public void closeSocket(){
        
    }
    
    private void handshake(PrintWriter printWriter, String[] info){
        
    }
    private void login(BufferedReader br, PrintWriter pw, String[] info) 
            throws IOException{
        
    }
    private void getList(BufferedReader br, PrintWriter pw)
            throws NumberFormatException, IOException{
        
    }
    private void leave(BufferedReader br, String userName)
            throws IOException{
        
    }
    private void sendMessage(BufferedReader br, PrintWriter pw,
            String userName) throws IOException{
        
    }
    private void beat(String name) 
            throws NumberFormatException, IOException{
        
    }
    
}
