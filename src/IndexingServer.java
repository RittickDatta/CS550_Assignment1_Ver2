
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

/**
 * -------------------THIS IS THE MAIN SERVER CLASS-------------------------
 * The other two indexing server classes: IndexingServer_Auto and IndexingServer_Auto_Concurrent make use of code in this file.
 * The other two indexing server classes are hardcoded for testing purposes.
 */
public class IndexingServer {

    /**
     * I use ConcurrentHashMap to avoid deadlock and inconsistency.
     * "bigRegister" is the primary data structure for storage. In "bigRegister", the key is "serialNumber", it is an integer assigned 1;
     * The value is a concatenated string storing: Client ID, Port, Location and File Name
     * I parse this string while searching for files.
     */
    private static final int SERVER_PORT = 3000;
    private static ConcurrentHashMap<Integer, ArrayList<String>> clientIdToFilename = new ConcurrentHashMap<Integer, ArrayList<String>>();
    private static ConcurrentHashMap<Integer, String> clientIdToFileLocation = new ConcurrentHashMap<Integer, String>();
    private static ConcurrentHashMap<String, Integer> fileNameToFileSize = new ConcurrentHashMap<String, Integer>();
    private static ConcurrentHashMap<Integer, Integer> clientIdToPeerServer = new ConcurrentHashMap<Integer, Integer>();
    private static ConcurrentHashMap<String, String> fileNameToLocation = new ConcurrentHashMap<String, String>();

    private static ConcurrentHashMap<Integer, String> bigRegister = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, ArrayList<Integer>> clientIdToSerialNumber = new ConcurrentHashMap<>();
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

    /**
     * Each time the server receives a request, it spawns a new thread of this class.
     */
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
                    try{
                        messageFromClient = Integer.parseInt(inputStream.readLine());
                    }catch (NumberFormatException e){
                    }
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
                                //System.out.println("SEARCH RESULT: "+ searchResult);
                                //outputStream.println("Client: "+nodeId+" has the file. Peer server is running at port:"+ getPort(clientID) +"Location:"+ getLocation(requestDataSearch));
                                if(searchResult!= null) {
                                    outputStream.println(searchResult);
                                    outputStream.flush();
                                }else
                                {
                                    outputStream.println("FILE NOT FOUND. CLIENT HAS UNREGISTERED.");
                                    outputStream.flush();
                                }
                                break;
                            }
                            break;

                        case 3:
                            System.out.println("UNREGISTER FILES REQUEST");
                            Boolean unregistered = unregister(clientID);

                            if(unregistered){
                                System.out.println("Files Successfully Unregistered.");
                                for(Integer i: bigRegister.keySet()){
                                    System.out.println("Serial Number: "+ i + "Value: "+bigRegister.get(i));
                                }
                            }

                            if(connection != null){
                                connection.close();
                            }
                            if(inputStream != null){
                                inputStream.close();
                            }
                            if(outputStream != null){
                                outputStream.println("Your files are unregistered. Closing Connection.");
                                outputStream.flush();
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

        /**
         * This method unregisters files of a particular client.
         * @param clientID
         * @return a boolean value indicating if files were successfully unregistered
         */
        private Boolean unregister(int clientID) {
            boolean unregistered = false;

            for(Integer key: clientIdToSerialNumber.keySet()){
                if(key.equals(clientID)){
                    //System.out.println(key);
                    //System.out.println(clientID);
                    ArrayList<Integer> serialNumbersToRemove = clientIdToSerialNumber.get(clientID);
                    for(int i=0; i<serialNumbersToRemove.size(); i++){
                        bigRegister.remove(serialNumbersToRemove.get(i));
                    }
                    unregistered = true;
                }
            }
            return unregistered;
        }

       /* private String getLocation(String fileName) {
            for(String name: fileNameToLocation.keySet()){
                if(name.equals(fileName)){
                    return fileNameToLocation.get(name);
                }
            }
            return "";
        }*/

        /**
         * This method maps the client ID to the server port number. This information is used in Search operation
         * to build search result string.
         * @param clientID
         * @param clientServersPort
         */
        private void saveClientsServerPort(int clientID, int clientServersPort) {
            clientIdToPeerServer.put(clientID, clientServersPort);
            System.out.println("Peer's Server Port Saved.");
        }

        /**
         * This method makes use of the mapping of client ID to server port and returns the port number.
         * @param clientID
         * @return port number
         */
        private int getPort(int clientID){
            for(Integer key: clientIdToPeerServer.keySet()){
                if( key == clientID){
                    Integer peerServerPort = clientIdToPeerServer.get(key);
                    return peerServerPort;
                }
            }
            return 0;
        }

        /**
         * This method handles registration request of a client.
         * @param clientID
         * @param fileData
         * @return a boolean value indicating if the files were successfully registered
         */
        public boolean registry(int clientID, String fileData) {
            //Enter data into CHM
            boolean finalFlag = false;
            ArrayList<String> files = new ArrayList<String>();
            int port = getPort(clientID);
            ArrayList<Integer> serialNumbersList = new ArrayList<>();

            checkRegister(clientID);

            String[] allRecords = new String[0];
            try {
                allRecords = fileData.split("!");
            } catch (NullPointerException e) {}
            for (String oneRecord : allRecords) {
                String[] singleRecord = oneRecord.split("#");
                files.add(singleRecord[0]);
                fileNameToLocation.put(singleRecord[0],singleRecord[1]);

                // Client ID, Port, Location and Filename
                String bigString = clientID+"#"+port+"#"+singleRecord[1]+"#"+singleRecord[0];



                bigRegister.put(serialNumber, bigString);
                serialNumbersList.add(serialNumber);
                serialNumber += 1;
                finalFlag = true;
            }

            clientIdToFilename.put(clientID, files);

            clientIdToSerialNumber.put(clientID, serialNumbersList);

            for(Integer key:bigRegister.keySet()){
                System.out.println("Serial Number : "+key+" Value: "+bigRegister.get(key));
            }

            for(Integer key:clientIdToSerialNumber.keySet()){
                System.out.println("Client ID : "+key+" List of Serial Numbers: "+clientIdToSerialNumber.get(key));
            }

            return finalFlag;
        }

        /**
         * This method is used to check "bigRegister" when a client who has already registered tries to re-register.
         * @param clientID
         */
        private void checkRegister(int clientID) {

            for(Integer key: bigRegister.keySet()){
                String record = bigRegister.get(key);
                String[] fields = new String[0];
                try {
                    fields = record.split("#");
                } catch (NullPointerException e) {
                }
                if(Integer.parseInt(fields[1])==clientID){
                    ArrayList<Integer> serialNumbers = clientIdToSerialNumber.get(clientID);
                    for(int i=0; i<serialNumbers.size(); i++){
                        bigRegister.remove(serialNumbers.get(i));
                    }
                }
            }

        }

        /**
         * This method searches for files in "bigRegister"
         * @param fileName
         * @return
         */
        public String search(String fileName) {
            /*for(Integer key : clientIdToFilename.keySet()){
                ArrayList<String> files = clientIdToFilename.get(key);
                if(files.contains(fileName)){
                    nodeID = key;
                    return nodeID;
                }
            }*/

            String bigString = null+"#";
            for(Integer key: bigRegister.keySet()){
                String currentRecord = bigRegister.get(key);
                String[] recordPieces = currentRecord.split("#");
                if(fileName.equals(recordPieces[recordPieces.length-1])){
                    bigString += currentRecord + "!"+null+"#";
                }
            }

            return bigString;
        }

    }

}
