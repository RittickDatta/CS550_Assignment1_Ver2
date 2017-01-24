import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by rittick on 1/24/17.
 */
public class Peer_AUTO {
    private static int PEER_SERVER_PORT;
    private static String PEER_CLIENT_FILE_LOCATION;
    private static String FILE_LOCATION = "/Myfiles/";
    private static String FILE_DOWNLOAD_LOCATION;
    private static HashMap<Integer, Integer> clientIdToPort = new HashMap<>();
    private static HashMap<Integer, String> portToLocation = new HashMap<>();
    private static String filesOfThisNode = null;
    private static Boolean registeredOnce = false;


    public Peer_AUTO() {

    }

    public static void main(String[] args) throws IOException {


        // -----------------PEER SERVER SECTION----------------


        try {

            System.out.println("Peer Server Preparing to Start...");
            System.out.println("Enter Port Number for Peer Server:");
            //BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            PEER_SERVER_PORT = 4000;//Integer.parseInt(userInput.readLine());

            int clientID = 1;
            ServerSocket peerServer = new ServerSocket(PEER_SERVER_PORT);
            System.out.println("Peer Server is Listening...");
            System.out.println();

            // -----------------PEER CLIENT SECTION---------------

            InetAddress serverAddress = InetAddress.getLocalHost();
            System.out.println("Enter Node Number for Peer: (e.g. Node1, Node2 etc.)");
            PEER_CLIENT_FILE_LOCATION = "Node1";//userInput.readLine();
            System.out.println();
            PEER_CLIENT_FILE_LOCATION += "/Myfiles/";
            // System.out.println("Enter Download Location for Peer:");
            FILE_DOWNLOAD_LOCATION = PEER_CLIENT_FILE_LOCATION.substring(0, 6) + "Downloads/";
            // System.out.println(FILE_DOWNLOAD_LOCATION);
            FILE_LOCATION = PEER_CLIENT_FILE_LOCATION.substring(0, 5) + FILE_LOCATION;
            // System.out.println(FILE_LOCATION);
            System.out.println("Peer Client is Running...");
            System.out.println();
            new Peer_AUTO.PeerClient_AUTO(serverAddress, 3000, PEER_CLIENT_FILE_LOCATION, PEER_SERVER_PORT).start();

            //-----------------------------------------------------

            while (true) {
                Socket newConnection = peerServer.accept();
                new Peer_AUTO.PeerServer_AUTO(clientID, newConnection).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Exception while initializing SERVER_PORT");
        }


    }

    private static class PeerClient_AUTO extends Thread {
        InetAddress serverAddress;
        int serverPort;
        Socket socket = null;
        Socket socketForPeerServer = null;
        BufferedReader userInput = null;
        BufferedReader socketInput = null;
        BufferedReader socketPeerServerInput = null;
        PrintWriter writer = null;
        String messageFromServer;
        String messageToServer = "1";
        String serverResponse;
        String peerFileLocation;
        int peerServerPort;
        InputStream input;
        ByteArrayOutputStream byteOutputStream;
        String fileData;

        public PeerClient_AUTO(InetAddress serverAddress, int serverPort, String peerFileLocation, int peerServerPort) {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
            this.peerFileLocation = peerFileLocation;
            this.peerServerPort = peerServerPort;
            //System.out.println(this.peerFileLocation);
        }

        public void run() {
            try {
                socket = new Socket(serverAddress, serverPort);
                userInput = new BufferedReader(new InputStreamReader(System.in));
                socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());

                //messageFromServer = socketInput.readLine();
                //System.out.println(messageFromServer);
                //messageToServer = userInput.readLine();
                //messageToServer = selectOption();
                writer.println(peerServerPort);
                writer.flush();
                //while (messageToServer.compareTo("q") != 0)
                for(int i=0; i<2; i++)
                {

                    if(registeredOnce){
                        String fileDataCheck = getFileData(peerFileLocation);
                        if(fileDataCheck.compareTo(fileData)==0){
                            System.out.println("No File is Modified. Update to Indexing Server is Not Required.");
                        }else{
                            /*messageToServer = "3";
                            writer.println(messageToServer);
                            writer.flush();
                            serverResponse = socketInput.readLine();*/

                            messageToServer = "1";
                            writer.println(messageToServer);
                            writer.flush();
                            serverResponse = socketInput.readLine();
                            fileData = getFileData(peerFileLocation);
                            writer.println(fileData);
                            writer.flush();
                            serverResponse = socketInput.readLine();
                            System.out.println(serverResponse);
                            System.out.println("Server's message:" + "FILES UPDATED AT INDEXING SERVER.");

                        }
                    }

                    messageToServer = "1";//selectOption();
                    writer.println(messageToServer);
                    writer.flush();
                    serverResponse = socketInput.readLine();


                    if (serverResponse == null) {
                        serverResponse = "Closing Connection...";
                        System.out.println("Server's message:" + serverResponse);
                        break;
                    }
                    System.out.println("Server's message:" + serverResponse);


                    switch (serverResponse) {
                        case "SEND FILE DATA":
                            registeredOnce = true;
                            System.out.println("Preparing File Data.");


                            fileData = getFileData(peerFileLocation);

                            filesOfThisNode = fileData;
                            //System.out.println("Files of this node: "+filesOfThisNode);

                            //messageToServer = fileData;  call method for file data
                            writer.println(fileData);
                            writer.flush();


                            System.out.println("File Data Sent.");

                            serverResponse = socketInput.readLine();
                            System.out.println(serverResponse);
                            break;

                        case "NAME OF FILE TO SEARCH":
                            messageToServer = "";
                            System.out.println("Enter Name of File to Search:");
                            messageToServer = userInput.readLine();

                            String FILE_DOWNLOAD_LOCATION_copy = FILE_DOWNLOAD_LOCATION;
                            FILE_DOWNLOAD_LOCATION_copy += messageToServer;

                            String fileToDownload = messageToServer;

                            writer.println(messageToServer);
                            writer.flush();
                            messageFromServer = socketInput.readLine();
                            //System.out.println(messageFromServer);

                            if(messageFromServer.compareTo("FILE NOT FOUND. CLIENT HAS UNREGISTERED.")==0) {
                                break;
                            }
                            handleServerResult(messageFromServer);

                            System.out.println("Enter Client Number to Download File:");
                            Integer clientNumber = Integer.parseInt(userInput.readLine());

                            System.out.println("Enter Port Number of Client to Download File:");
                            Integer portNumber = Integer.parseInt(userInput.readLine());

                            //System.out.println("Enter Location of Client to Download File:");
                            String Location = "Node"+clientNumber+"/Myfiles/";//userInput.readLine();



                            //-------DOWNLOAD FILE?-------------

                            PrintWriter writerDownload = null;

                            System.out.println("Do you want to download file? (y/n) File will be saved in 'Downloads' directory.");
                            String download = userInput.readLine();
                            if (download.compareToIgnoreCase("y") == 0) {

                                System.out.println("Contacting Peer To Download File...");
                                //Receive file below.
                                socketForPeerServer = new Socket(serverAddress, portNumber);
                                writerDownload = new PrintWriter(socketForPeerServer.getOutputStream());
                                writerDownload.println(Location + fileToDownload);
                                writerDownload.flush();

                                socketPeerServerInput = new BufferedReader(new InputStreamReader(socketForPeerServer.getInputStream()));

                                byte[] byteArray = new byte[1];
                                int bytesRead;
                                input = socketForPeerServer.getInputStream();
                                byteOutputStream = new ByteArrayOutputStream();
                                if (input != null) {

                                    BufferedOutputStream bufferedOStream = null;
                                    try {
                                        bufferedOStream = new BufferedOutputStream(new FileOutputStream(FILE_DOWNLOAD_LOCATION_copy));
                                        bytesRead = input.read(byteArray, 0, byteArray.length);

                                        do {
                                            byteOutputStream.write(byteArray, 0, byteArray.length);
                                            bytesRead = input.read(byteArray);
                                        } while (bytesRead != -1);

                                        bufferedOStream.write(byteOutputStream.toByteArray());
                                        bufferedOStream.flush();
                                        //bufferedOStream.close();
                                        //socketForPeerServer.close();
                                        //String peerServerInput = socketPeerServerInput.readLine();
                                        //System.out.println(peerServerInput);

                                    } catch (IOException e) {

                                    }
                                }

                            }
                            break;
                        case "FILE NOT FOUND. CLIENT HAS UNREGISTERED.":
                            System.out.println("File Not Found at Indexing Server. File not present or client has unregistered.");
                    }


                }
                System.out.println("Client closing connection.");

            } catch (IOException e) {

            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (socketInput != null) {
                    try {
                        socketInput.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (writer != null) {
                    writer.close();
                }
            }

        }




        private void handleServerResult(String messageFromServer) {

            ArrayList<String> processedRecords = new ArrayList<>();

            System.out.println("----------------------------------------------------------");
            System.out.println("The Following Clients have the File:");

            String[] fileLocations = messageFromServer.split("!");
            for(String oneRecord: fileLocations){
                //System.out.println(oneRecord);
                /*if(processedRecords.contains(oneRecord))continue;
                processedRecords.add(oneRecord);*/

                String[] fields = oneRecord.split("#");
                try {
                    System.out.println("Client ID: "+fields[1]/*.substring(4,5)*/+" Port: "+fields[2]+" Location: "+fields[3]+" File Name: "+fields[4]);

                }catch (ArrayIndexOutOfBoundsException e){}
                // clientIdToPort.put(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));
                // portToLocation.put(Integer.parseInt(fields[1]), fields[2]);
            }
        }

        public static String getFileData(String path) {
            String fileData = "";
            File file = new File(path);
            String[] list = file.list();
            for (int i = 0; i < list.length; i++)
                fileData += list[i] + "#" + path + "!";
            return fileData;
        }

        public static String selectOption() throws IOException {
            String option;
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.println("-----------------------------------------------------------------");
                System.out.println("Select an Operation (number, e.g. 1 or 2)");
                System.out.println("1. Register Files");
                System.out.println("2. Search File");
                System.out.println("3. Unregister Files");
                option = userInput.readLine();
                return option;
            }

        }


    }

    private static class PeerServer_AUTO extends Thread {
        private ArrayList<String> fileNamesRegister = new ArrayList<String>();

        private int clientId;
        private Socket connection;
        private BufferedReader inputStream;
        private PrintWriter writerServer;
        private BufferedOutputStream output;
        private BufferedInputStream fileInputStream;

        public PeerServer_AUTO(int clientId, Socket connection) {
            this.clientId = clientId;
            this.connection = connection;

        }

        public void run() {
            try {
                inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                writerServer = new PrintWriter(connection.getOutputStream());
                output = new BufferedOutputStream(connection.getOutputStream());
                String fullFileAddress = inputStream.readLine();

//                System.out.println(fullFileAddress);
                System.out.println("-------------SERVER LOG FOR CLIENT REQUEST------------------");
                System.out.println("Full file address :" + fullFileAddress);
                System.out.println(" PEER SERVER, CLIENT ID: " + clientId);

                //TODO
                //Sending File

                obtain(fileInputStream, writerServer, output, fullFileAddress);


                System.out.println("File Successfully Sent.");
                System.out.println("-------------END OF REQUEST LOG------------------------------");
                System.out.println("Select an Operation (number, e.g. 1 or 2)");
                System.out.println("1. Register Files");
                System.out.println("2. Search File");
                System.out.println("3. Unregister Files");
                System.out.println();


            } catch (IOException e) {

            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (writerServer != null) {
                    writerServer.close();
                }
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        public void obtain(BufferedInputStream InputStream, PrintWriter writerServer, BufferedOutputStream output, String fullFileAddress) {
            // boolean flag = false;

            try {

                if (output != null) {
                    //fullFileAddress = fullFileAddress.substring(9);
                    File file = new File(fullFileAddress);
                    System.out.println("File Address: "+ fullFileAddress);
                    byte[] byteArray = new byte[(int) file.length()];

                    FileInputStream fileInputStream = null;

                    try{
                        fileInputStream = new FileInputStream(file);
                    }catch (FileNotFoundException e){
                        System.out.println("File Not Found.");
                    }

                    InputStream = new BufferedInputStream(fileInputStream);

                    try {
                        InputStream.read(byteArray, 0, byteArray.length);
                        //System.out.println("Bytes Read : "+byteArray);
                        output.write(byteArray, 0, byteArray.length);
                        output.flush();
                    } catch (IOException e) {
                        System.out.println("Problem in Reading and Writing File.");
                    }


                    //output.close();
                    //connection.close();

                    //writerServer.println("FILE SUCCESSFULLY SENT.");
                    //writerServer.flush();

                }

            }finally {

            }
        }
    }
}
