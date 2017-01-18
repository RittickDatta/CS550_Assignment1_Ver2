import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by rittick on 1/17/17.
 */
public class Peer {
    private static int PEER_SERVER_PORT;
    private static String File_Locations = "Files/";

    public Peer() {

    }

    public static void main(String[] args) throws IOException {

        // -----------------PEER CLIENT SECTION---------------

        InetAddress serverAddress = InetAddress.getLocalHost();

        System.out.println("Peer Client is Running...");
        new PeerClient(serverAddress, 3000).start();


        // -----------------PEER SERVER SECTION----------------

        System.out.println("Peer Server is preparing to start...");
        System.out.println("Enter Port Number for Server: ");
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        try {
            PEER_SERVER_PORT = Integer.parseInt(userInput.readLine());

            int clientID = 1;
            ServerSocket peerServer = new ServerSocket(PEER_SERVER_PORT);
            System.out.println("Peer Server is Listening...");

            while (true) {
                Socket newConnection = peerServer.accept();
                new PeerServer(clientID, newConnection).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Exception while initializing SERVER_PORT");
        }


    }

    private static class PeerClient extends Thread {
        InetAddress serverAddress;
        int serverPort;
        Socket socket = null;
        BufferedReader userInput = null;
        BufferedReader socketInput = null;
        PrintWriter writer = null;
        String message;


        public PeerClient(InetAddress serverAddress, int serverPort) {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
        }

        public void run() {
            try {
                socket = new Socket(serverAddress, serverPort);
                //userInput = new BufferedReader(new InputStreamReader(System.in));
                socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());

                message =socketInput.readLine();
                while (message.compareTo("QUIT")!= 0) {
                    writer.println("Hello from client.");
                    writer.flush();

                    String serverResponse = socketInput.readLine();
                    System.out.println("Server response"+ serverResponse);
                }

            } catch (Exception e) {

            }

        }


    }

    private static class PeerServer extends Thread {
        private ArrayList<String> fileNamesRegister = new ArrayList<String>();

        private int clientId;
        private Socket connection;

        public PeerServer(int clientId, Socket connection) {
            this.clientId = clientId;
            this.connection = connection;
        }

        public void run() {


            System.out.println(" PEER SERVER, CLIENT ID: " + clientId);
        }

        public String obtain(String fileName){
            String flag = null;

            return flag;
        }
    }
}

