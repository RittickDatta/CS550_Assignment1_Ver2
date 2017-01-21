import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import javax.xml.bind.SchemaOutputResolver;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.util.ArrayList;

/**
 * Created by rittick on 1/17/17.
 */
public class Peer {
    private static int PEER_SERVER_PORT;
    private static String PEER_CLIENT_FILE_LOCATION;
    private static String FILE_LOCATION = "/Myfiles/";

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
            FILE_LOCATION = PEER_CLIENT_FILE_LOCATION.substring(0, 5) + FILE_LOCATION;
            System.out.println(FILE_LOCATION);
            System.out.println("Peer Client is Running...");
            new PeerClient(serverAddress, 3000, PEER_CLIENT_FILE_LOCATION, PEER_SERVER_PORT).start();

            //-----------------------------------------------------

            while (true) {
                Socket newConnection = peerServer.accept();
                new PeerServer(clientID, newConnection, FILE_LOCATION).start();
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
        Socket socketForPeerServer = null;
        BufferedReader userInput = null;
        BufferedReader socketInput = null;
        PrintWriter writer = null;
        String messageFromServer;
        String messageToServer = "1";
        String serverResponse;
        String peerFileLocation;
        int peerServerPort;


        public PeerClient(InetAddress serverAddress, int serverPort, String peerFileLocation, int peerServerPort) {
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
                while (messageToServer.compareTo("3") != 0) {
                    messageToServer = selectOption();
                    writer.println(messageToServer);
                    writer.flush();
                    serverResponse = socketInput.readLine();
                    System.out.println("Server's message:" + serverResponse);

                    if (serverResponse == null) {
                        break;
                    }

                    switch (serverResponse) {
                        case "SEND FILE DATA":
                            System.out.println("Preparing File Data.");

                            String fileData = getFileData(peerFileLocation);


                            //messageToServer = fileData;  call method for file data
                            writer.println(fileData);
                            writer.flush();


                            System.out.println("File Data Sent.");

                            serverResponse = socketInput.readLine();
                            System.out.println(serverResponse);
                            break;

                        case "NAME OF FILE TO SEARCH":
                            System.out.println("Enter Name of File to Search:");
                            messageToServer = userInput.readLine();

                            String fileToDownload = messageToServer;

                            writer.println(messageToServer);
                            writer.flush();
                            messageFromServer = socketInput.readLine();
                            System.out.println(messageFromServer);
                            String portAndLocation = messageFromServer.substring(messageFromServer.length() - 27);
                            String Location = portAndLocation.substring(portAndLocation.length()-23);
                            System.out.println(Location);
                            System.out.println("Location: "+Location.substring(9));
                            Integer portNumber = Integer.parseInt(portAndLocation.substring(0,4));
                            System.out.println("Port: "+portNumber);
                            //Integer portNumber = Integer.parseInt(messageFromServer.substring(messageFromServer.length() - 4));
                            //System.out.println(portNumber);

                            //-------DOWNLOAD FILE?-------------

                            PrintWriter writerDownload = null;

                            System.out.println("Do you want to download file? (y/n) File will be saved in 'Downloads' directory.");
                            String download = userInput.readLine();
                            if (download.compareToIgnoreCase("y") == 0) {
                                System.out.println("Contacting Peer To Download File...");
                                socketForPeerServer = new Socket(serverAddress, portNumber);
                                writerDownload = new PrintWriter(socketForPeerServer.getOutputStream());
                                writerDownload.println(fileToDownload);
                                writerDownload.flush();
                            }
                            break;
                    }


                    //messageToServer = userInput.readLine();
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

        public static String getFileData(String path) {
            String fileData = "";
            File file = new File(path);
            String[] list = file.list();
            for (int i = 0; i < list.length; i++)
                fileData += "Path: " + path + "#Filename: " + list[i] + "#Size:" + list[i].length() + "!";
            return fileData;
        }

        public static String selectOption() throws IOException {
            String option;
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.println("Select an Operation (number, e.g. 1 or 2)");
                System.out.println("1. Register Files");
                System.out.println("2. Search File");
                System.out.println("3. Quit");
                option = userInput.readLine();
                return option;
            }

        }


    }

    private static class PeerServer extends Thread {
        private ArrayList<String> fileNamesRegister = new ArrayList<String>();

        private int clientId;
        private Socket connection;
        private String fileLocation;
        private BufferedReader inputStream;
        private PrintWriter writerServer;

        public PeerServer(int clientId, Socket connection, String fileLocation) {
            this.clientId = clientId;
            this.connection = connection;
            this.fileLocation = fileLocation;
        }

        public void run() {
            try {
                inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                writerServer = new PrintWriter(connection.getOutputStream());

                String messageFromPeerClient = inputStream.readLine();
                System.out.println(messageFromPeerClient);
                fileLocation = fileLocation+messageFromPeerClient;
                System.out.println("Full file address :" + fileLocation);

                System.out.println(" PEER SERVER, CLIENT ID: " + clientId);
            }catch (IOException e){

            }
            finally {

            }
        }

        public String obtain(String fileName) {
            String flag = null;

            return flag;
        }
    }
}

