import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
            System.out.println("New Connection Accepted. Client ID: " + clientID);
            clientID = clientID + 1;
        }
    }

    private static class ServerThread extends Thread {
        private int clientID;
        private Socket connection;
        private int messageFromClient;
        BufferedReader inputStream;
        PrintWriter outputStream;

        public ServerThread(int clientID, Socket connection) {
            this.clientID = clientID;
            this.connection = connection;
        }

        public void run() {

            //System.out.println(" INDEXING SERVER, CLIENT ID :" + clientID);
            try {
                inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                outputStream = new PrintWriter(connection.getOutputStream());

                outputStream.println("Please select an Operation. 1. Register Local Files 2. Search for a File");
                outputStream.flush();

                messageFromClient = Integer.parseInt(inputStream.readLine());
                System.out.println("Message from client: " + messageFromClient);
                switch (messageFromClient) {
                    case 1:
                        System.out.println("FILE REGISTRATION REQUEST");
                        outputStream.println("SEND FILE DATA");
                        outputStream.flush();
                        String requestData = inputStream.readLine();
                        while (requestData.compareToIgnoreCase("QUIT") != 0) {
                            outputStream.println("FILE DATA RECEIVED");
                            outputStream.flush();
                            requestData = inputStream.readLine();
                        }
                        break;

                    case 2:
                        System.out.println("FILE SEARCH REQUEST");
                        outputStream.println("NAME OF FILE TO SEARCH");
                        outputStream.flush();
                        String requestDataSearch = inputStream.readLine();
                        while (requestDataSearch.compareToIgnoreCase("QUIT") != 0) {
                            outputStream.println("FILE SEARCH DATA RECEIVED");
                            outputStream.flush();
                            requestDataSearch = inputStream.readLine();
                        }
                        break;

                    default:
                        System.out.println("INVALID OPTION");

                }
            } catch (IOException e1) {
            } catch (Exception e2) {
            }

        }

        public String registry(ArrayList<String> fileNameList, String fileLocation) {
            String flag = null;

            return flag;
        }

        public String search(String fileName) {
            String flag = null;

            return flag;
        }

    }

}

