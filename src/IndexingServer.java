import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by rittick on 1/17/17.
 */
public class IndexingServer {

    private static final int SERVER_PORT = 3000;
    private static ConcurrentHashMap<String, ArrayList<String>> serverRegister = new ConcurrentHashMap<String, ArrayList<String>>();
    private static ConcurrentHashMap<String, String> peerFileLocations = new ConcurrentHashMap<String, String>();
    private static int activeConnections = 0;


    public static void main(String[] args) throws IOException {

        int clientID = 1;
        ServerSocket server = new ServerSocket(SERVER_PORT);
        System.out.println("Server is Listening...");

        while (true) {
            Socket newConnection = server.accept();
            new ServerThread(clientID, newConnection).start();
            clientID = clientID + 1;
            System.out.println("New Connection Accepted. Client ID: "+clientID);
        }
    }

    private static class ServerThread extends Thread {
        private int clientID;
        private Socket connection;

        public ServerThread(int clientID, Socket connection) {
            this.clientID = clientID;
            this.connection = connection;
        }

        public void run(){
            System.out.println(" INDEXING SERVER, CLIENT ID :"+clientID);
        }
    }

}

