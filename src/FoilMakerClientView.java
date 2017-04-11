import javax.swing.*;
import javax.swing.plaf.metal.MetalIconFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by sunil on 6/28/16.
 */
public class FoilMakerClientView extends JFrame implements ActionListener {

    private FoilMakerClient clientController = null;
    private boolean isLeader = false;
    private String currentCardFront = null, currentCardBack = null;

    // UI fields
    private static final int FRAME_WIDTH = 300, FRAME_HEIGHT = 500;
    private static final int STATUS_MSG_PANEL_HEIGHT = 50;
    private static final int MAIN_PANEL_WIDTH = FRAME_WIDTH - 10, MAIN_PANEL_HEIGHT = FRAME_HEIGHT - STATUS_MSG_PANEL_HEIGHT - 10;
    private static final int PANEL_WIDGET_WIDTH = MAIN_PANEL_WIDTH - 20, PANEL_WIDGET_HEIGHT = 40;
    private static final int TEXT_WIDTH = 28;
    private static final int SUGGESTION_LENGTH = 20;
    private JLabel statusMessageLabel = null;
    private JLabel topMessageLabel = null;
    private JPanel mainPanel = null;
    private static  enum ViewNames {LOGINVIEW, CHOOSEGAMEVIEW, NEWGAMEKEYVIEW, ENTERGAMEKEYVIEW, WAITINGFORLEADERVIEW,
                   ENTERSUGGESTIONVIEW, WAITINGFOROTHERSVIEW, PICKOPTIONVIEW, ROUNDRESULTVIEW }

    // UI: login Panel
    private JPanel loginViewPanel = null;
    private JTextField loginPanelUserName = null;
    private JPasswordField loginPanelPassword = null;
    private JButton loginPanelLoginButton = null, loginPanelCreateNewUserButton = null;

    // UI: choose game panel
    private JPanel chooseGameViewPanel = null;
    private JButton chooseGamePanelNewGameButton = null, chooseGamePanelJoinGameButton = null;

    // UI: new Game Key panel
    private JPanel newGameKeyPanel = null;
    private JTextField newGameKeyPanelKey = null;
    private JButton newGameKeyPanelStartGameButton = null;
    DefaultListModel participantListModel = null;
    private JList newGameKeyPanelParticipantListPanel = null;

    // UI: enter Game Key Panel
    private JPanel enterGameKeyPanel = null;
    private JButton enterGameKeyPanelJoinButton = null;
    private JTextField enterGameKeyPanelKey = null;

    // UI: Waiting for leader Panel
    private JPanel waitingForLeaderPanel = null;

    // UI: Waiting for others Panel
    private JPanel waitingForOthersPanel = null;

    // UI: game Participant Panel
    private JPanel enterSuggestionPanel = null;
    private JTextArea enterSuggestionPanelCardFrontTextArea = null;
    private JTextField enterSuggestionPanelSuggestionArea = null;
    private JButton enterSuggestionPanelSendButton = null;

    // UI: game Options Panel
    private JPanel pickOptionPanel = null;
    private JPanel pickOptionPanelOptionsPanel = null;
    private JRadioButton[] pickOptionPanelRadioButton = null;
    private ButtonGroup pickOptionPanelButtons = null;
    private JButton pickOptionPanelSendButton = null;

    // UI: round results panel
    private JPanel roundResultPanel = null;
    private JButton roundResultPanelContinueButton = null;
    private JTextArea roundResultTextArea = null;
    private DefaultListModel resultModel = null;
    private JList resultListPanel = null;

    public FoilMakerClientView(FoilMakerClient controller){
        //Setup all the necessary views.
        setupFrame();
        createLoginView();
        createChooseGameView();
        createNewGameKeyView();
        createEnterGameKeyView();
        createWaitingForLeaderView();
        createWaitingForOthersView();
        createParticipantView();
        createRoundOptionsView();
        createRoundResultsView();

        showLoginView();
        this.clientController = controller;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", null,
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (response == JOptionPane.YES_OPTION) {
                    try {
                        controller.logout();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    System.exit(0);
                }
            }
        });
    }

    private void setupFrame(){
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        mainPanel = new JPanel(new CardLayout());
        mainPanel.setSize(new Dimension(MAIN_PANEL_WIDTH, MAIN_PANEL_HEIGHT));

        statusMessageLabel = new JLabel("Welcome!");
        topMessageLabel = new JLabel("FoilMaker!");
        topMessageLabel.setHorizontalAlignment(JLabel.CENTER);
        contentPane.add(statusMessageLabel, BorderLayout.SOUTH);
        contentPane.add(topMessageLabel, BorderLayout.NORTH);
        contentPane.add(mainPanel, BorderLayout.CENTER);

        this.setTitle("FoilMaker");
        this.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        this.repaint();
    }

    private void createLoginView(){
        JPanel tempPanel1, tempPanel2;
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1.0;

        loginViewPanel = new JPanel(new GridBagLayout());
        loginViewPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        loginViewPanel.setSize(new Dimension(MAIN_PANEL_WIDTH, MAIN_PANEL_HEIGHT));

        loginPanelUserName = new JTextField("Enter user name", 20);
        loginPanelPassword = new JPasswordField("Enter password", 20);
        loginPanelLoginButton = new JButton("Login");
        loginPanelLoginButton.addActionListener(this);
        loginPanelCreateNewUserButton = new JButton("Register");
        loginPanelCreateNewUserButton.addActionListener(this);

        tempPanel1 = new JPanel(new GridLayout(2,2));
        tempPanel1.add(new JLabel("Username"));
        tempPanel1.add(loginPanelUserName);
        tempPanel1.add(new JLabel("Password"));
        tempPanel1.add(loginPanelPassword);

        tempPanel2 = new JPanel(new GridLayout(0, 2));
        tempPanel2.add(loginPanelLoginButton);
        tempPanel2.add(loginPanelCreateNewUserButton);


        constraints.weighty = 0.25;
        constraints.gridx = 0;
        constraints.gridy = 0;

        loginViewPanel.add(new JPanel(), constraints);
        constraints.gridy = 1;
        constraints.weighty = 0.5;
        loginViewPanel.add(tempPanel1, constraints);
        constraints.gridy = 2;
        loginViewPanel.add(tempPanel2, constraints);
        constraints.gridy = 3;
        constraints.weighty = 0.25;
        loginViewPanel.add(new JPanel(), constraints);

        loginViewPanel.setBorder(BorderFactory.createEtchedBorder());

        mainPanel.add(loginViewPanel, ViewNames.LOGINVIEW.toString());
    }

    private void createChooseGameView(){
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1.0;

        chooseGameViewPanel = new JPanel(new GridBagLayout());
        chooseGameViewPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        chooseGameViewPanel.setSize(new Dimension(MAIN_PANEL_WIDTH, MAIN_PANEL_HEIGHT));


        chooseGamePanelNewGameButton = new JButton("Start New Game");
        chooseGamePanelNewGameButton.addActionListener(this);

        chooseGamePanelJoinGameButton = new JButton("Join a Game");
        chooseGamePanelJoinGameButton.addActionListener(this);

        constraints.weighty = 0.25;
        constraints.gridx = 0;
        constraints.gridy = 0;

        chooseGameViewPanel.add(new JPanel());
        constraints.gridx = 1;
        constraints.weighty = 0.5;
        chooseGameViewPanel.add(chooseGamePanelNewGameButton);
        constraints.gridx = 2;
        chooseGameViewPanel.add(chooseGamePanelJoinGameButton);
        constraints.gridx = 3;
        constraints.weighty = 0.25;
        chooseGameViewPanel.add(new JPanel());

        chooseGameViewPanel.setBorder(BorderFactory.createEtchedBorder());

        mainPanel.add(chooseGameViewPanel, ViewNames.CHOOSEGAMEVIEW.toString());
    }

    private void createNewGameKeyView(){
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(5, 5, 5, 5);

        newGameKeyPanelKey = new JTextField("", FoilMakerNetworkProtocol.GAME_KEY_LENGTH);
        newGameKeyPanelKey.setEditable(false);
        JLabel label = new JLabel("Others should use this key to join your game");

        //newGameKeyPanelParticipantListPanel = new JPanel(new GridLayout(5,1));
        participantListModel = new DefaultListModel();
        newGameKeyPanelParticipantListPanel = new JList(participantListModel);
        newGameKeyPanelParticipantListPanel.setBackground(Color.getHSBColor(1.86f, 1.2f, 0.8f));
        newGameKeyPanelParticipantListPanel.setCellRenderer(new FoilMakerIconListRenderer());

        newGameKeyPanelStartGameButton = new JButton("Start Game");
        newGameKeyPanelStartGameButton.addActionListener(this);
        newGameKeyPanelStartGameButton.setEnabled(false);

        newGameKeyPanel = new JPanel(new GridBagLayout());
        newGameKeyPanel.setSize(MAIN_PANEL_WIDTH, MAIN_PANEL_HEIGHT);

        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = 0;

        newGameKeyPanel.add(label, constraints);
        constraints.gridy = 1;
        newGameKeyPanel.add(newGameKeyPanelKey, constraints);
        //constraints.weighty = 1;
        constraints.gridy  = 2;
        //constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPane = new JScrollPane(newGameKeyPanelParticipantListPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Participants"));
        scrollPane.setMinimumSize(new Dimension(285, 200));
        newGameKeyPanel.add(scrollPane, constraints);
        //constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridy = 3;
        newGameKeyPanel.add(newGameKeyPanelStartGameButton, constraints);

        newGameKeyPanel.setBorder(BorderFactory.createEtchedBorder());

        mainPanel.add(newGameKeyPanel, ViewNames.NEWGAMEKEYVIEW.toString());
    }

    private void createEnterGameKeyView(){
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(5, 5, 5, 5);

        enterGameKeyPanelKey = new JTextField("",  FoilMakerNetworkProtocol.GAME_KEY_LENGTH);
        JLabel label = new JLabel("Enter the game key to join a game");
        enterGameKeyPanelJoinButton = new JButton("Join Game");
        enterGameKeyPanelJoinButton.addActionListener(this);


        enterGameKeyPanel  = new JPanel(new GridBagLayout());
        enterGameKeyPanel.setSize(MAIN_PANEL_WIDTH, MAIN_PANEL_HEIGHT);
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = 0;

        enterGameKeyPanel.add(label, constraints);
        constraints.gridy = 1;
        enterGameKeyPanel.add(enterGameKeyPanelKey, constraints);
        constraints.gridy = 2;
        enterGameKeyPanel.add(enterGameKeyPanelJoinButton, constraints);

        enterGameKeyPanel.setBorder(BorderFactory.createEtchedBorder());

        mainPanel.add(enterGameKeyPanel, ViewNames.ENTERGAMEKEYVIEW.toString());
    }

    private void createWaitingForLeaderView(){
        waitingForLeaderPanel = new JPanel(new GridLayout(0,1));
        JLabel label = new JLabel("Waiting for leader ...");
        label.setHorizontalAlignment(JLabel.CENTER);
        waitingForLeaderPanel.add(label);

        waitingForLeaderPanel.setBorder(BorderFactory.createEtchedBorder());

        mainPanel.add(waitingForLeaderPanel, ViewNames.WAITINGFORLEADERVIEW.toString());
    }

    private void createWaitingForOthersView(){
        waitingForOthersPanel = new JPanel(new GridLayout(0,1));
        JLabel label = new JLabel("Waiting for other players ...");
        label.setHorizontalAlignment(JLabel.CENTER);
        waitingForOthersPanel.add(label);

        waitingForOthersPanel.setBorder(BorderFactory.createEtchedBorder());

        mainPanel.add(waitingForOthersPanel, ViewNames.WAITINGFOROTHERSVIEW.toString());
    }

    private void createParticipantView(){
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 0.8;

        enterSuggestionPanelCardFrontTextArea = new JTextArea();
        JScrollPane scrollPaneFront = new JScrollPane(enterSuggestionPanelCardFrontTextArea);
        enterSuggestionPanelCardFrontTextArea.setEditable(false);
        enterSuggestionPanelCardFrontTextArea.setBackground(Color.getHSBColor(1.46f, 1.2f, 0.8f));

        enterSuggestionPanelSuggestionArea = new JTextField(SUGGESTION_LENGTH);

        enterSuggestionPanelSendButton = new JButton("Submit Suggestion");
        enterSuggestionPanelSendButton.addActionListener(this);

        enterSuggestionPanel = new JPanel(new GridBagLayout());
        enterSuggestionPanel.setSize(MAIN_PANEL_WIDTH, MAIN_PANEL_HEIGHT);
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        enterSuggestionPanel.add(new JLabel("What is the word for"), constraints);

        constraints.gridy = 1;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;

        enterSuggestionPanel.add(scrollPaneFront, constraints);

        JPanel tempPanel = new JPanel();
        tempPanel.add(enterSuggestionPanelSuggestionArea);
        tempPanel.setBorder(BorderFactory.createTitledBorder("Your Suggestion"));

        constraints.gridy = 2;
        constraints.weighty = 0.5;

        enterSuggestionPanel.add(tempPanel, constraints);

        constraints.gridy = 3;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        enterSuggestionPanel.add(enterSuggestionPanelSendButton, constraints);

        enterSuggestionPanel.setBorder(BorderFactory.createEtchedBorder());

        mainPanel.add(enterSuggestionPanel, ViewNames.ENTERSUGGESTIONVIEW.toString());
    }

    private void createRoundOptionsView(){
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        pickOptionPanelOptionsPanel = new JPanel(new GridLayout(0,1));
        pickOptionPanelSendButton = new JButton("Submit Option");
        pickOptionPanelSendButton.addActionListener(this);

        pickOptionPanel = new JPanel (new GridBagLayout());
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        pickOptionPanel.add(new JLabel("Pick your option below"), constraints);

        constraints.gridy = 1;
        constraints.weighty = 1;
        pickOptionPanel.add(pickOptionPanelOptionsPanel, constraints);

        constraints.gridy = 2;
        constraints.weighty = 0;
        pickOptionPanel.add(pickOptionPanelSendButton, constraints);

        pickOptionPanel.setBorder(BorderFactory.createEtchedBorder());

        mainPanel.add(pickOptionPanel, ViewNames.PICKOPTIONVIEW.toString());
    }

    private void updateRoundOptionsView(String[] options){
        int numOptions = options.length;

        if(numOptions < 1)  {
            System.err.println("Unexpected number of tokens for view.createRoundOptionsView");
            return;
        }

        pickOptionPanelButtons = new ButtonGroup();
        pickOptionPanelOptionsPanel.removeAll();
        pickOptionPanelRadioButton = new JRadioButton[numOptions];
        for(int i = 0; i < numOptions; i++) {
            pickOptionPanelRadioButton[i] = new JRadioButton(options[i]);
            pickOptionPanelButtons.add(pickOptionPanelRadioButton[i]);
            pickOptionPanelOptionsPanel.add(pickOptionPanelRadioButton[i]);

        }
        pickOptionPanelRadioButton[0].setSelected(true);
    }

    public void createRoundResultsView(){
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        //roundResultPanelResultsPanel = new JPanel(new FlowLayout());

        roundResultPanelContinueButton = new JButton("Next Round");
        roundResultPanelContinueButton.addActionListener(this);

        roundResultPanel = new JPanel(new GridBagLayout());
        roundResultPanel.setSize(MAIN_PANEL_WIDTH, MAIN_PANEL_HEIGHT);

        roundResultTextArea = new JTextArea();
        roundResultTextArea.setEditable(false);
        roundResultTextArea.setBackground(Color.getHSBColor(1.46f, 1.2f, 0.8f));
        roundResultTextArea.setText("");

        JScrollPane scrollPaneFront = new JScrollPane(roundResultTextArea);
        scrollPaneFront.setBorder(BorderFactory.createTitledBorder("Round Result"));
        scrollPaneFront.setMinimumSize(new Dimension(285, 100));

        resultModel = new DefaultListModel();
        resultListPanel = new JList(resultModel);
        resultListPanel.setBackground(Color.getHSBColor(1.86f, 1.2f, 0.8f));
        resultListPanel.setCellRenderer(new FoilMakerIconListRenderer());

        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = 0;

        constraints.gridy = 0;
        roundResultPanel.add(scrollPaneFront, constraints);

        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPane = new JScrollPane(resultListPanel);
        scrollPane.setMinimumSize(new Dimension(285, 200));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Overall Results"));

        roundResultPanel.add(scrollPane, constraints);

        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.NONE;

        roundResultPanel.add(roundResultPanelContinueButton, constraints);

        roundResultPanel.setBorder(BorderFactory.createEtchedBorder());

        mainPanel.add(roundResultPanel, ViewNames.ROUNDRESULTVIEW.toString());
    }

    public void updateRoundResultsView(String[] resultTokens){
        for (int i = 0; i < resultTokens.length; i++) {
            if (resultTokens[i].equalsIgnoreCase(topMessageLabel.getText())) {
                roundResultTextArea.setText(resultTokens[i + 1]);
                break;
            }
        }

        for (int i = 1; i < resultTokens.length; i++) {
            String result = "";

            if ((i - 1) % 5 == 0) { // Name
                result += resultTokens[i] + " => "; // Name
                result += "Score : " + resultTokens[i + 2] + " | ";
                result += "Fooled : " + resultTokens[i + 3] + " player(s) | ";
                result += "Fooled by : " + resultTokens[i + 4] + " player(s)";

                resultModel.addElement(result);
            }
        }
    }

    private JPanel createResultPanelColumn(String[] displayValues){
        JPanel panel = new JPanel(new GridLayout(0,1));
        panel.setSize(PANEL_WIDGET_WIDTH, PANEL_WIDGET_HEIGHT);
        if (displayValues == null || displayValues.length < 1)
            return panel;
        for(String s: displayValues){
            panel.add(new JLabel(s));
        }
        return panel;
    }

    private void showView(ViewNames view){
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, view.toString());
        this.validate();
        this.repaint();
        this.setVisible(true);
    }

    public void showLoginView(){ showView(ViewNames.LOGINVIEW); }

    public void showChooseGameView(){ showView(ViewNames.CHOOSEGAMEVIEW); }

    public void showLeaderGameView() {
        showView(ViewNames.NEWGAMEKEYVIEW);
        newGameKeyPanelKey.setText(clientController.getCurrentGameKey());
        setStatusMsg("Press <Start Game> when all users have joined");
    }

    public void showParticipantView(boolean isLeader) {
        this.isLeader = isLeader;
        showView(ViewNames.WAITINGFORLEADERVIEW);
    }

    public void addParticipant(String name) {
        participantListModel.addElement(name);
        newGameKeyPanelStartGameButton.setEnabled(true);
    }

    public void showCard(String[] cardDetails){
        showView(ViewNames.ENTERSUGGESTIONVIEW);
        if(cardDetails.length != 2){
            System.err.println("Unexpected number of serverTokens in view.showCard");
            return;
        }
        currentCardFront = cardDetails[0];
        currentCardBack = cardDetails[1];
        enterSuggestionPanelCardFrontTextArea.setText(trimText(currentCardFront));
        enterSuggestionPanelSuggestionArea.setText("");
        setStatusMsg("Enter your suggestion then click on <Send Response>");
    }

    public void showRoundOptionsView(String[] options){
        updateRoundOptionsView(options);
        showView(ViewNames.PICKOPTIONVIEW);
        setStatusMsg("Pick your option and click <Send> ");
    }

    public void showRoundResultView(String[] results){
        updateRoundResultsView(results);
        showView(ViewNames.ROUNDRESULTVIEW);
        setStatusMsg(" Click <Next Round> when ready ");
    }

    public void setStatusMsg(String msg){
        if(msg != null)
            statusMessageLabel.setText(msg);
    }

    public void setTopMsg(String msg) {
        topMessageLabel.setText(msg);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String userName, userPassword;
        Object target = actionEvent.getSource();

        if(target == loginPanelLoginButton) {
            char[] password;
            userName = loginPanelUserName.getText();
            password = loginPanelPassword.getPassword();
            if(isValidPassword(password)) {
                userPassword = new String(password);
                clientController.loginToServer(userName, userPassword);
            } else {
                this.setStatusMsg("Invalid  Password");
            }
            return;
        }

        if(target == loginPanelCreateNewUserButton){
            char[] password;
            userName = loginPanelUserName.getText();
            password = loginPanelPassword.getPassword();
            if(!isValidUserName(userName)){
                this.setStatusMsg("Invalid User Name");
                return;
            }
            if(isValidPassword(password)) {
                userPassword = new String(password);
                clientController.createNewUser(userName, userPassword);
            } else {
                this.setStatusMsg("Invalid  Password");
            }
            return;
        }

        if(target == chooseGamePanelNewGameButton) {
            clientController.startNewGame();
            return;
        }

        if(target == chooseGamePanelJoinGameButton) {
            showView(ViewNames.ENTERGAMEKEYVIEW);
            return;
        }

        if(target == enterGameKeyPanelJoinButton) {
            clientController.joinGame(enterGameKeyPanelKey.getText());
            return;
        }

        if(target == newGameKeyPanelStartGameButton){
            //send message to start game!
            clientController.allPlayersReady();
            return;
        }

        if(target == enterSuggestionPanelSendButton){
            String suggestion;
            suggestion = enterSuggestionPanelSuggestionArea.getText();
            if(suggestion.length() < 1) {
                setStatusMsg("Please enter your suggestion");
                return;
            }
            showView(ViewNames.WAITINGFOROTHERSVIEW);
            clientController.sendSuggestion(suggestion);
            return;
        }

        if(target == pickOptionPanelSendButton){
            for(JRadioButton b: pickOptionPanelRadioButton)
                if(b.isSelected()) {
                    clientController.sendOption(b.getText());
                    showView(ViewNames.WAITINGFOROTHERSVIEW);
                }
            return;
        }

        if(target == roundResultPanelContinueButton){
            clientController.notifyModelToContinue();
        }

    }

    public void disableContinueButton() {
        roundResultPanelContinueButton.setEnabled(false);
    }

    private boolean isValidPassword(char[] password){
        String passString = new String (password);
        if ((passString.indexOf(FoilMakerNetworkProtocol.SEPARATOR) >= 0) ||
                (passString.compareTo(passString.trim()) != 0)
                )
            return false;
        return true;
    }

    private boolean isValidUserName(String userName){
        if ((userName.indexOf(FoilMakerNetworkProtocol.SEPARATOR) >= 0) ||
                (userName.compareTo(userName.trim()) != 0)
                )
            return false;
        return true;
    }

    private String trimText(String text){
        int currentLineLength;
        String word;
        Scanner scanner = new Scanner(text);
        StringBuilder outputStr = new StringBuilder("");

        currentLineLength = 0;
        while(scanner.hasNext()){
            word = scanner.next();
            if( (currentLineLength + word.length()) > TEXT_WIDTH){
                outputStr.append("\n" + word + " ");
                currentLineLength = word.length() + 1;
            } else {
                outputStr.append(word + " ");
                currentLineLength += word.length() + 1;
            }
        }
        System.err.println(outputStr.toString());
        return outputStr.toString();
    }

    public class FoilMakerIconListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean
                cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setIcon(MetalIconFactory.getFileChooserDetailViewIcon());
            return label;
        }
    }
}
