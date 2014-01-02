import java.net.*;

public class User {
	Socket socket;
	int p2pPort;
	String name;
	int lifetime;
	
	public User(Socket s, String n, int p){
		socket = s;
		name = n;
		lifetime = 10;
		p2pPort = p;
	}

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public int getP2pPort() {
        return p2pPort;
    }

    public void setP2pPort(int p2pPort) {
        this.p2pPort = p2pPort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLifetime() {
        return lifetime;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

}
