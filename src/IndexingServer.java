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
    private static ConcurrentHashMap<Integer, String> clientIdToFilename = new ConcurrentHashMap<Integer, String>();
    private static ConcurrentHashMap<Integer, String> clientIdToFileLocation = new ConcurrentHashMap<Integer, String>();
    private static ConcurrentHashMap<String, Integer> fileNameToFileSize = new ConcurrentHashMap<String, Integer>();
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

                //outputStream.println("Please select an Operation. 1. Register Local Files 2. Search for a File");
                //outputStream.flush();

                while (true) {
                    messageFromClient = Integer.parseInt(inputStream.readLine());
                    //System.out.println("Message from client: " + messageFromClient);
                    switch (messageFromClient) {
                        case 1:
                            System.out.println("FILE REGISTRATION REQUEST");
                            outputStream.println("SEND FILE DATA");
                            outputStream.flush();
                            String requestData = inputStream.readLine();

                            boolean flag = registry(clientID, requestData);

                           // while (requestData.compareToIgnoreCase("QUIT") != 0) {
                            if(flag) {
                                outputStream.println("FILES REGISTERED");
                                outputStream.flush();
                            }
                                //requestData = inputStream.readLine();
                            //}
                            break;

                        case 2:
                            System.out.println("FILE SEARCH REQUEST");
                            outputStream.println("NAME OF FILE TO SEARCH");
                            outputStream.flush();

                            String requestDataSearch = inputStream.readLine();
                            while(requestDataSearch.compareToIgnoreCase("cancel")!=0) {
                                System.out.println(requestDataSearch);

                                //SEARCH OPERATION HERE
                                Integer nodeId = search(requestDataSearch);
                                outputStream.println("Client: "+nodeId+" has the file.");
                                outputStream.flush();
                                break;
                            }
                            break;

                        case 3:
                            System.out.println("QUIT REQUEST");
                            if(connection != null){
                                connection.close();
                            }
                            if(inputStream != null){
                                inputStream.close();
                            }
                            if(outputStream != null){
                                outputStream.close();
                            }
                            break;

                        default:
                            System.out.println("INVALID OPTION");


                    }
                }
            }catch (IOException e){

            }
        }



        public boolean registry(int clientID, String fileData) {
            //Enter data into CHM
            String filename = null;
            boolean[] flag = {false, false, false};
            //ArrayList<String> files = new ArrayList<String>();


            String[] allRecords = fileData.split("!");
            for (String oneRecord : allRecords) {
                String[] singleRecord = oneRecord.split("#");
                for (String field : singleRecord) {

                    if (field.contains("Filename:")) {
                        filename = field.substring(10);
                        System.out.println("clientID: "+clientID+" Filename: "+filename);
                        clientIdToFilename.put(clientID, filename);
                        //System.out.println("FILE NAME ADDED");
                        flag[0] = true;
                    }
                    if (field.contains("Path:")) {
                        String path = field.substring(6);
                        clientIdToFileLocation.put(clientID, path);
                        //System.out.println("PATH DATA ADDED");
                        flag[1] = true;
                    }
                    if (field.contains("Size:")) {
                        int size = Integer.parseInt(field.substring(5));
                        fileNameToFileSize.put(filename, size);
                        //System.out.println("SIZE ADDED");
                        flag[2] = true;

                    }
                }
            }

            for(Integer key:clientIdToFilename.keySet()){
                System.out.println("ID: "+key+"Filename: "+clientIdToFilename.get(key));
            }

            boolean finalFlag = false;
            if (flag[0] == true && flag[1] == true && flag[2] == true) {
                finalFlag = true;
            }
            return finalFlag;
        }

        public Integer search(String fileName) {
            Integer nodeID = null;
            for(Integer key : clientIdToFilename.keySet()){
                String currentFile = clientIdToFilename.get(key);
                System.out.println(currentFile);
                if(currentFile.equals(fileName)){
                    nodeID = key;
                    System.out.println(nodeID);
                    return  nodeID;
                }
            }
            return nodeID;
        }

    }

}

