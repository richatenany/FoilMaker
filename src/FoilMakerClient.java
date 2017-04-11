import java.io.IOException;
import javax.swing.SwingWorker;

/**
 * Created by sunil on 6/28/16.
 * The controller class for the Client side.
 */
public class FoilMakerClient {
    private FoilMakerClientModel model;
    private FoilMakerClientView view;

    //TODO need to fix how errors are communicated to the controller

    public FoilMakerClient (String args[]){
        model = new FoilMakerClientModel(this, args);
        view = new FoilMakerClientView (this);
        try{
            model.connectToServer();
        } catch (Exception e){
            System.err.println("Unable to to establish to server:" + e.getMessage());
            e.printStackTrace();
            view.setStatusMsg("Unable to Connect to Server");
        }
    }

    public void loginToServer(String name, String password){
        try {
            if(model.loginToServer(name, password)){
                view.showChooseGameView();
                model.setStatus(FoilMakerClientModel.STATUS_T.CHOOSEGAME);
                view.setTopMsg(name);
            }
        } catch(FoilMakerException e){
            view.setStatusMsg("Login Failure: "+ e.getMessage());
        }
    }

    public void logout() throws IOException {
        model.logout();
        model.closeConnectionToServer();
    }

    public void createNewUser(String name, String password){

        if(model.createNewUser(name, password))
            view.setStatusMsg("New user created");
        else
            view.setStatusMsg("User creation failure ");
    }

    public void startNewGame(){
        SwingWorker worker = new SwingWorker<Boolean, Object>() {
            @Override
            public Boolean doInBackground() {
                return model.startNewGame();
            }

            @Override
            public void done() {
                try {
                    if (get()) {
                        view.showLeaderGameView();
                        model.setStatus(FoilMakerClientModel.STATUS_T.GAMELEADERWAITING);
                        view.setStatusMsg("Game started: You are the leader");

                        //model.playGame();
                    } else {
                        view.setStatusMsg("Failure: ");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();

        while (!worker.isDone());

        System.out.println("After leader is done");

        model.playGame();
    }

    public void joinGame(String gameKey){
        if(model.joinGame(gameKey)) {
            view.showParticipantView(false);
            model.setStatus(FoilMakerClientModel.STATUS_T.PARTICIPANT);
            view.setStatusMsg("Joined game: waiting for leader");
            model.playGame();
        } else
            view.setStatusMsg("Failure: ");
    }

    public void allPlayersReady() {
        model.allPlayersReady();
        view.showParticipantView(true);
    }

    public void showCard(String[] serverMessageTokens){
        view.showCard(serverMessageTokens);
        view.setStatusMsg("Enter your suggestion");
    }

    public void sendSuggestion(String suggestion){
        model.sendSuggestionToServer(suggestion);
    }
    public void showRoundOptions(String[] serverMessageTokens){
        view.showRoundOptionsView(serverMessageTokens);
        view.setStatusMsg("Pick your choice");
    }
    public void sendOption(String optionString){
        model.sendOptionToServer(optionString);
    }
    public void showRoundResult(String[] serverMessageTokens){
        view.showRoundResultView(serverMessageTokens);
    }

    public void notifyModelToContinue() {
        model.participateInGame();
    }

    public void addNewPlayer(String name){
        view.addParticipant(name);
        view.setStatusMsg("Press <Start game> to start game");
    }

    public void showGameOver() {
        view.disableContinueButton();
        view.setStatusMsg("Game over!");
    }

    public String getCurrentGameKey(){
        return model.getCurrentGameKey();
    }

    public static void main(String args[]){
        FoilMakerClient client = new FoilMakerClient(args);
        client.view.showLoginView();
    }

    public void setStatusMsg(String msg){
        view.setStatusMsg(msg);
    }
}
