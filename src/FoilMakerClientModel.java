import java.net.*;
import java.io.*;
import java.util.HashMap;

/**
 * Created by sunil on 6/28/16.
 */

public class FoilMakerClientModel {

    public enum STATUS_T {LOGIN, CHOOSEGAME, GAMELEADERWAITING, GAMELEADERPLAYING, PARTICIPANT, GAMEOVER};

    private final int NUM_PLAYERS = 1;
    private final String DEFAULT_SERVER = "127.0.0.1";
    private final int DEFAULT_PORT = 8000;
    private FoilMakerClient controller = null;
    private STATUS_T status = STATUS_T.LOGIN;
    private String userName = null, userPassword = null;
    private String loginToken = null; //represents the token used by the server to track a user's session
    private String currentGameKey = null; //holds the key for this game used by others to join the game
    private HashMap<String, Integer> allParticipants = null;

        //Server connection data
    private Socket socket = null;
    private String serverName = null;
    private int serverPortNumber = 0;
    private Socket serverSocket = null;
    private BufferedReader inFromServer = null;
    private PrintWriter outToServer = null;
    private boolean connectedToServer = false;

    public FoilMakerClientModel(FoilMakerClient controller, String[] args){
        this.controller = controller;
        if(args.length == 2){
            this.serverName = args[0];
            serverPortNumber = Integer.parseInt(args[1]);
        } else if(args.length == 0) {
            this.serverName = DEFAULT_SERVER;
            serverPortNumber = DEFAULT_PORT;
        } else {
            System.err.println("Usage: java FoilMakerClient server port");
            System.exit(1);
        }
        connectedToServer = false;
    }

    public void connectToServer() throws IOException {
        serverSocket = new Socket(serverName, serverPortNumber);
        if(serverSocket != null) {
            inFromServer = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            outToServer = new PrintWriter(serverSocket.getOutputStream(), true);
            connectedToServer = true;
        } else {
            connectedToServer = false;
        }
    }

    public void closeConnectionToServer() throws IOException {
        if(serverSocket != null)
            serverSocket.close();
        if(inFromServer != null)
            inFromServer.close();
        if(outToServer != null)
            outToServer.close();
        connectedToServer = false;
    }

    public boolean loginToServer(String user, String password) throws FoilMakerException {
        String sendTokens[] = new String[2];
        String serverMessage;
        String serverMessageTokens[];
        FoilMakerNetworkProtocol.MSG_TYPE serverMsgType, clientRequestType;
        FoilMakerNetworkProtocol.MSG_DETAIL_T msgDetail;

        if(!connectedToServer) {
            throw new FoilMakerException(FoilMakerNetworkProtocol.MSG_DETAIL_T.NO_CONNECTION_TO_SERVER);
        }

        if( (user == null) || (user.length() < 1) || (password == null) || (password.length() < 1) ){
            return false;  //Should already have been checked before calling this method
        }
        sendTokens[0] = userName = user;
        sendTokens[1] = userPassword = password;

        sendToServer(FoilMakerNetworkProtocol.MSG_TYPE.LOGIN, sendTokens);
            //read response from server.
        serverMessage = readLineFromServer();
        if(serverMessage == null)
            return false;
        serverMessageTokens = parseServerMessage(serverMessage);
        if(serverMessageTokens == null)
            return false;
        serverMsgType = FoilMakerNetworkProtocol.MSG_TYPE.valueOf(serverMessageTokens[0]);
        clientRequestType = FoilMakerNetworkProtocol.MSG_TYPE.valueOf(serverMessageTokens[1]);
        msgDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.valueOf(serverMessageTokens[2]);

        if((serverMsgType == FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE) &&
                (clientRequestType == FoilMakerNetworkProtocol.MSG_TYPE.LOGIN) &&
                (msgDetail == FoilMakerNetworkProtocol.MSG_DETAIL_T.SUCCESS)){
            this.loginToken = serverMessageTokens[3];
            setStatus (STATUS_T.CHOOSEGAME);
            return true;
        } else
            return false;
    }

    public boolean createNewUser(String user, String password) {
        String sendTokens[] = new String[2];
        String serverMessage;
        String serverMessageTokens[];
        FoilMakerNetworkProtocol.MSG_TYPE serverMsgType, clientRequestType;
        FoilMakerNetworkProtocol.MSG_DETAIL_T msgDetail;

        if(!connectedToServer) {
            return false; //TODO: set status message!
        }

        if( (user == null) || (user.length() < 1) || (password == null) || (password.length() < 1) ){
            System.err.println("Error with arguments" + user + password);
            return false;
        }
        sendTokens[0] = userName = user;
        sendTokens[1] = userPassword = password;

        sendToServer(FoilMakerNetworkProtocol.MSG_TYPE.CREATENEWUSER, sendTokens);
        serverMessage = readLineFromServer();
        serverMessageTokens = parseServerMessage(serverMessage);
        if(serverMessageTokens == null)
            return false;

        serverMsgType = FoilMakerNetworkProtocol.MSG_TYPE.valueOf(serverMessageTokens[0]);
        clientRequestType = FoilMakerNetworkProtocol.MSG_TYPE.valueOf(serverMessageTokens[1]);
        msgDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.valueOf(serverMessageTokens[2]);

        if ( (serverMsgType == FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE) &&
                (clientRequestType == FoilMakerNetworkProtocol.MSG_TYPE.CREATENEWUSER) &&
                (msgDetail == FoilMakerNetworkProtocol.MSG_DETAIL_T.SUCCESS) ){
            return true;
        } else
            return false;
    }

    public boolean startNewGame(){
        String serverMessage;
        String serverMessageTokens[];
        FoilMakerNetworkProtocol.MSG_TYPE serverMsgType, clientRequestType;
        FoilMakerNetworkProtocol.MSG_DETAIL_T msgDetail;

        sendToServer(FoilMakerNetworkProtocol.MSG_TYPE.STARTNEWGAME, loginToken);
        serverMessage = readLineFromServer();
        serverMessageTokens = parseServerMessage(serverMessage);
        if(serverMessageTokens == null)
            return false;

        serverMsgType = FoilMakerNetworkProtocol.MSG_TYPE.valueOf(serverMessageTokens[0]);
        clientRequestType = FoilMakerNetworkProtocol.MSG_TYPE.valueOf(serverMessageTokens[1]);
        msgDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.valueOf(serverMessageTokens[2]);

        if ((serverMsgType == FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE) &&
                (clientRequestType == FoilMakerNetworkProtocol.MSG_TYPE.STARTNEWGAME) &&
                (msgDetail == FoilMakerNetworkProtocol.MSG_DETAIL_T.SUCCESS) ){
            setStatus (STATUS_T.GAMELEADERWAITING);
            this.currentGameKey = serverMessageTokens[3];
            this.allParticipants = new HashMap<String, Integer>();
            return true;
        } else
            return false;
    }

    public boolean joinGame(String gameKey){
        String sendTokens[];
        String serverMessage;
        String serverMessageTokens[];
        FoilMakerNetworkProtocol.MSG_TYPE serverMsgType, clientRequestType;
        FoilMakerNetworkProtocol.MSG_DETAIL_T msgDetail;

        sendTokens = new String[2];
        sendTokens[0] = loginToken;
        //TODO: check if it is okay to join a game (e.g., in game currently, as leader, etc.
        sendTokens[1] = gameKey;
        sendToServer(FoilMakerNetworkProtocol.MSG_TYPE.JOINGAME, sendTokens);
        serverMessage = readLineFromServer();
        serverMessageTokens = parseServerMessage(serverMessage);
        if(serverMessageTokens == null)
            return false;

        serverMsgType = FoilMakerNetworkProtocol.MSG_TYPE.valueOf(serverMessageTokens[0]);
        clientRequestType = FoilMakerNetworkProtocol.MSG_TYPE.valueOf(serverMessageTokens[1]);
        msgDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.valueOf(serverMessageTokens[2]);

        if ( (serverMsgType == FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE) &&
                (clientRequestType == FoilMakerNetworkProtocol.MSG_TYPE.JOINGAME) &&
                (msgDetail == FoilMakerNetworkProtocol.MSG_DETAIL_T.SUCCESS)) {
            setStatus (STATUS_T.PARTICIPANT);
            this.currentGameKey = serverMessageTokens[3];
            return true;
        } else
            return false;
    }

    private void sendToServer(FoilMakerNetworkProtocol.MSG_TYPE msg_type, String arg) {
        String[] args = new String[1];
        args[0] = arg;
        sendToServer(msg_type, args);
    }

    public void playGame(){
        //TODO: is it necessary to create a separate thread?
        //this.start();
        this.run();

    }

    //@Override
    public void run(){
        //Play a game -- then notify on currentGame;
        System.err.println("Starting game thread");

        if(isLeader()) {
            FoilMakerSwingWorker firstWorker = null;
            FoilMakerSwingWorker prevWorker = null;

            for (int i = 0; i < NUM_PLAYERS; i++) {
                FoilMakerSwingWorker worker = new FoilMakerSwingWorker<String, Object>() {
                    @Override
                    public String doInBackground() {
                        return waitForParticipant();
                    }

                    @Override
                    public void done() {
                        try {
                            String participantName = get();
                            controller.addNewPlayer(participantName);

                            if (nextWorker != null)
                                nextWorker.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                if (prevWorker != null)
                    prevWorker.setNextWorker(worker);

                if (i == 0)
                    firstWorker = worker;

                prevWorker = worker;
            }

            firstWorker.execute();
        }

        System.err.println("Done with waiting for participants");
        participateInGame();

        //reset status since game is over;
        setStatus(STATUS_T.CHOOSEGAME); //TODO: Check this
        //end game thread
    }

    private String waitForParticipant () {
        String serverMessage;
        String serverMessageTokens[];
        int score;
        String participantName;
        FoilMakerNetworkProtocol.MSG_TYPE serverMsgType;

        try{
            //serverSocket.setSoTimeout(1000);
            serverMessage = readLineFromServer();
            serverMessageTokens = parseServerMessage(serverMessage);
            serverMsgType = FoilMakerNetworkProtocol.MSG_TYPE.valueOf(serverMessageTokens[0]);
            if(serverMsgType != FoilMakerNetworkProtocol.MSG_TYPE.NEWPARTICIPANT){
                System.err.println("Unexpected message  from server: " + serverMessage); //TODO
		System.exit(1);
            }
            participantName = serverMessageTokens[1];
            score = Integer.parseInt(serverMessageTokens[2]);

            allParticipants.put(participantName, score);
            serverSocket.setSoTimeout(0);

            return participantName;
        } catch(SocketException e){
            System.err.println("Unexpected error when setting socket timeout");
        }

        return null;
    }

    public void allPlayersReady(){
        System.err.println("Changing leader status to all PLAYING");
        setStatus(STATUS_T.GAMELEADERPLAYING);

        //send message to server that all are ready
        String[] sendTokens;
        sendTokens = new String[3];
        sendTokens[0] = loginToken;
        sendTokens[1] = currentGameKey;
        sendToServer(FoilMakerNetworkProtocol.MSG_TYPE.ALLPARTICIPANTSHAVEJOINED, sendTokens);
    }

    public void participateInGame() {
        FoilMakerSwingWorker worker3 = new FoilMakerSwingWorker<String, Object>() {
            @Override
            public String doInBackground() {
                return readLineFromServer();
            }

            @Override
            public void done() {
                try {
                    String serverMessageTokens[], controllerTokens[];
                    FoilMakerNetworkProtocol.MSG_TYPE serverMsgType;

                    String serverMessage = get(); // Return value of doInBackground

                    serverMessageTokens = parseServerMessage(serverMessage);
                    controller.showRoundResult(serverMessageTokens);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        FoilMakerSwingWorker worker2 = new FoilMakerSwingWorker<String, Object>() {
            @Override
            public String doInBackground() {
                // get server response with everyone's suggestions
                return readLineFromServer();
            }

            @Override
            public void done() {
                try {
                    String serverMessageTokens[], controllerTokens[];
                    FoilMakerNetworkProtocol.MSG_TYPE serverMsgType;
                    int numTokensToDrop;

                    String serverMessage = get(); // Return value of doInBackground

                    serverMessageTokens = parseServerMessage(serverMessage);
                    numTokensToDrop = 1;
                    controllerTokens = new String[serverMessageTokens.length - numTokensToDrop];
                    for(int i = 0; i < controllerTokens.length; i++)
                        controllerTokens[i] = serverMessageTokens[i+numTokensToDrop];
                    controller.showRoundOptions(controllerTokens);

                    worker3.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        FoilMakerSwingWorker worker1 = new FoilMakerSwingWorker<String, Object>() {
            @Override
            public String doInBackground() {
                System.err.println("Starting new round");
                return readLineFromServer();
            }

            @Override
            public void done() {
                try {
                    String serverMessageTokens[], controllerTokens[];
                    FoilMakerNetworkProtocol.MSG_TYPE serverMsgType;
                    int numTokensToDrop;

                    String serverMessage = get(); // Return value of doInBackground
                    serverMessageTokens = parseServerMessage(serverMessage);
                    serverMsgType = FoilMakerNetworkProtocol.MSG_TYPE.valueOf(serverMessageTokens[0]);

                    if (serverMsgType == FoilMakerNetworkProtocol.MSG_TYPE.GAMEOVER){
                        setStatus(STATUS_T.GAMEOVER);
                        controller.showGameOver();
                        return;
                    }

                    System.err.println("Received new Card");
                    numTokensToDrop = 1;
                    controllerTokens = new String[serverMessageTokens.length - numTokensToDrop];
                    for(int i = 0; i < controllerTokens.length; i++)
                        controllerTokens[i] = serverMessageTokens[i+numTokensToDrop];
                    controller.showCard(controllerTokens);

                    worker2.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        worker1.execute();
    }

    public void logout() {
        sendToServer(FoilMakerNetworkProtocol.MSG_TYPE.LOGOUT, "");
    }

    public void sendSuggestionToServer(String suggestion) {
        String[] sendTokens = new String[3];

        sendTokens[0] = loginToken;
        sendTokens[1] = currentGameKey;
        sendTokens[2] = suggestion;
        sendToServer(FoilMakerNetworkProtocol.MSG_TYPE.PLAYERSUGGESTION, sendTokens);
    }

    public void sendOptionToServer(String option){
        String[] sendTokens = new String[3];

        sendTokens[0] = loginToken;
        sendTokens[1] = currentGameKey;
        sendTokens[2] = option;
        sendToServer(FoilMakerNetworkProtocol.MSG_TYPE.PLAYERCHOICE, sendTokens);
    }

    public String getCurrentGameKey() { return this.currentGameKey; }

    public  void setStatus(STATUS_T status) {
        synchronized(status) {
            this.status = status;
        }
    }

    public boolean isLeader() {
        synchronized(status) {
            return ((status == STATUS_T.GAMELEADERWAITING || status == STATUS_T.GAMELEADERPLAYING));
        }
    }

    public boolean isParticipant() {
        synchronized(status) {
            return (status == STATUS_T.PARTICIPANT);
        }
    }

    private void sendToServer(FoilMakerNetworkProtocol.MSG_TYPE msg_type, String[] args){
        StringBuilder message;

        if(outToServer == null)
            return;
        message = new StringBuilder(""+ msg_type);
        for(String s: args){
            if(s!=null)
                message.append(FoilMakerNetworkProtocol.SEPARATOR + s);
        }
        synchronized (outToServer) {
            outToServer.println(message);
        }
        System.err.println("Sent to server:" + message);
    }

    private String readLineFromServer(){
        String serverMessage;
        if(!connectedToServer || inFromServer == null){
            controller.setStatusMsg("Not connected to server");
            return null;
        }
        try {
            synchronized (inFromServer) {
                serverMessage = inFromServer.readLine();
            }
            System.err.println("Read from server:" + serverMessage);
        } catch (SocketTimeoutException e) {
            return null;
        } catch(SocketException e){
            return null;
        } catch (IOException e){
            System.err.println("Error reading from server for user: " + userName + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return serverMessage;
    }

    private String[] parseServerMessage(String message){
        if(message == null)
            return null;
        return message.split(FoilMakerNetworkProtocol.SEPARATOR);

    }
}
