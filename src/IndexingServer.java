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
    private static ConcurrentHashMap<Integer, ArrayList<String>> clientIdToFilename = new ConcurrentHashMap<Integer, ArrayList<String>>();
    private static ConcurrentHashMap<Integer, String> clientIdToFileLocation = new ConcurrentHashMap<Integer, String>();
    private static ConcurrentHashMap<String, Integer> fileNameToFileSize = new ConcurrentHashMap<String, Integer>();
    private static ConcurrentHashMap<Integer, Integer> clientIdToPeerServer = new ConcurrentHashMap<Integer, Integer>();
    private static ConcurrentHashMap<String, String> fileNameToLocation = new ConcurrentHashMap<String, String>();

    private static ConcurrentHashMap<Integer, String> bigRegister = new ConcurrentHashMap<>();

    private static int activeConnections = 0;
    private static int serialNumber = 1;


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
        int clientServersPort;

        private int clientServerPort;

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

                clientServersPort = Integer.parseInt(inputStream.readLine());
                System.out.println("Client's Server Port : "+ clientServersPort);
                saveClientsServerPort(clientID, clientServersPort);

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
                                String searchResult = search(requestDataSearch);
                                //outputStream.println("Client: "+nodeId+" has the file. Peer server is running at port:"+ getPort(clientID) +"Location:"+ getLocation(requestDataSearch));
                                outputStream.println(searchResult);
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

        private String getLocation(String fileName) {
            for(String name: fileNameToLocation.keySet()){
                if(name.equals(fileName)){
                    return fileNameToLocation.get(name);
                }
            }
            return "";
        }

        private void saveClientsServerPort(int clientID, int clientServersPort) {
            clientIdToPeerServer.put(clientID, clientServersPort);
            System.out.println("Peer's Server Port Saved.");
        }

        private int getPort(int clientID){
            for(Integer key: clientIdToPeerServer.keySet()){
                if( key == clientID){
                    Integer peerServerPort = clientIdToPeerServer.get(key);
                    return peerServerPort;
                }
            }
            return 0;
        }

        public boolean registry(int clientID, String fileData) {
            //Enter data into CHM
            boolean finalFlag = false;
            ArrayList<String> files = new ArrayList<String>();
            int port = getPort(clientID);

            String[] allRecords = fileData.split("!");
            for (String oneRecord : allRecords) {
                String[] singleRecord = oneRecord.split("#");
                files.add(singleRecord[0]);
                fileNameToLocation.put(singleRecord[0],singleRecord[1]);

                // Client ID, Port, Location and Filename
                String bigString = clientID+"#"+port+"#"+singleRecord[1]+"#"+singleRecord[0];
                bigRegister.put(serialNumber, bigString);
                serialNumber += 1;
                finalFlag = true;
            }

            clientIdToFilename.put(clientID, files);


            for(Integer key:bigRegister.keySet()){
                System.out.println("Serial Number : "+key+" Value: "+bigRegister.get(key));
            }

            return finalFlag;
        }

        public String search(String fileName) {
            /*for(Integer key : clientIdToFilename.keySet()){
                ArrayList<String> files = clientIdToFilename.get(key);
                if(files.contains(fileName)){
                    nodeID = key;
                    return nodeID;
                }
            }*/

            String bigString = "";
            for(Integer key: bigRegister.keySet()){
                String currentRecord = bigRegister.get(key);
                String[] recordPieces = currentRecord.split("#");
                if(fileName.equals(recordPieces[recordPieces.length-1])){
                    bigString += currentRecord + "!";
                }
            }

            return bigString;
        }

    }

}
