import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by rittick on 1/17/17.
 */
public class Peer {
    private static int PEER_SERVER_PORT;
    private static String PEER_CLIENT_FILE_LOCATION;
    private static String File_Locations = "Files/";

    public Peer() {

    }

    public static void main(String[] args) throws IOException {



        // -----------------PEER SERVER SECTION----------------


        try {

            System.out.println("Peer Server Preparing to Start...");
            System.out.println("Enter Port Number for Peer Server:");
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            PEER_SERVER_PORT = Integer.parseInt(userInput.readLine());

            int clientID = 1;
            ServerSocket peerServer = new ServerSocket(PEER_SERVER_PORT);
            System.out.println("Peer Server is Listening...");

            // -----------------PEER CLIENT SECTION---------------

            InetAddress serverAddress = InetAddress.getLocalHost();
            System.out.println("Enter File Location for Peer:");
            PEER_CLIENT_FILE_LOCATION = userInput.readLine();
            System.out.println("Peer Client is Running...");
            new PeerClient(serverAddress, 3000, PEER_CLIENT_FILE_LOCATION).start();

            //-----------------------------------------------------

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
        String messageFromServer;
        String messageToServer;
        String serverResponse;
        String peerFileLocation;


        public PeerClient(InetAddress serverAddress, int serverPort, String peerFileLocation) {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
            this.peerFileLocation = peerFileLocation;
            //System.out.println(this.peerFileLocation);
        }

        public void run() {
            try {
                socket = new Socket(serverAddress, serverPort);
                userInput = new BufferedReader(new InputStreamReader(System.in));
                socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());

                messageFromServer = socketInput.readLine();
                System.out.println(messageFromServer);
                messageToServer = userInput.readLine();
                while  (messageToServer.compareTo("QUIT")!= 0){
                    writer.println(messageToServer);
                    writer.flush();
                    serverResponse = socketInput.readLine();
                    System.out.println("Server's message:"+serverResponse);
                    messageToServer = userInput.readLine();
                }

            } catch(IOException e){

            }
            finally {
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(socketInput != null){
                    try {
                        socketInput.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(writer != null){
                    writer.close();
                }
            }

        }

        public ArrayList<String> getFileNames(String fileLocation){
            String FILE_LOCATION = fileLocation;
            ArrayList<String> fileNames = null;

            return fileNames;
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

