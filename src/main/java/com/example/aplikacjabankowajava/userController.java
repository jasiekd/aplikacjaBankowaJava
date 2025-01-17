package com.example.aplikacjabankowajava;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class userController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private ListView listView;
    @FXML
    private Label balanceLabel;
    @FXML
    private Label numerKonta;
    @FXML
    private Label clipboardAlert;
    @FXML
    private Button creditB;
    @FXML
    private Button transactionB;
    @FXML
    private Label listText;
    @FXML
    private Button newCreditB;

    private Stage stage;
    private Scene scene;
    private Parent root;

    Dialog<String> dialog = new Dialog<>();

    public boolean isValid(String pass){
        boolean validation = true;
        if (pass.length() > 20 || pass.length() < 8)
        {
            System.out.println("Password must be less than 20 and more than 8 characters in length.");
            validation = false;
        }
        String upperCaseChars = "(.*[A-Z].*)";
        if (!pass.matches(upperCaseChars ))
        {
            System.out.println("Password must have atleast one uppercase character");
            validation = false;
        }
        String lowerCaseChars = "(.*[a-z].*)";
        if (!pass.matches(lowerCaseChars ))
        {
            System.out.println("Password must have atleast one lowercase character");
            validation = false;
        }
        String numbers = "(.*[0-9].*)";
        if (!pass.matches(numbers ))
        {
            System.out.println("Password must have atleast one number");
            validation = false;
        }
        String specialChars = "(.*[@,#,$,%].*$)";
        if (!pass.matches(specialChars ))
        {
            System.out.println("Password must have atleast one special character among @#$%");
            validation = false;
        }
        return validation;
    }

    @FXML
    public void initUser(user user, int position){

        dialog.setTitle("Zmiana hasła");
        dialog.setHeaderText("Wymagana zmiana hasła!");
        ButtonType loginButtonType = new ButtonType("Zatwierdź", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        password.textProperty().addListener((observable, oldValue, newValue) -> {
            if(isValid(newValue)){
                loginButton.setDisable(false);
            }else{
                loginButton.setDisable(true);
            }
        });
        dialog.setOnCloseRequest(null);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(password::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return password.getText();
            }
            return null;
        });

        if(!user.isActive()){
            Optional<String> result = dialog.showAndWait();

            result.ifPresent(passwordN ->{
                ArrayList<user> tempList;
                try {
                    tempList = serialization.deserializeUserList("data.txt");
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                tempList.get(position).setPassword(passwordN);
                tempList.get(position).setActive(true);
                try {
                    serialization.serializeUserList("data.txt",tempList);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        welcomeLabel.setText("Witaj " + user.getName());
        balanceLabel.setText(String.format("%.02f", user.getBalance()) + user.getCurrency());
        numerKonta.setText("Numer konta: " + user.getAccNumber());
        if(!user.getCreditList().isEmpty()){
            if((user.getCreditList().get(0).getStatus().equals("Oczekujący")) || (user.getCreditList().get(0).getStatus().equals("Zaakceptowany"))){
                newCreditB.setDisable(true);
            }
        }
    }

    @FXML
    public void initList(user user) throws IOException, ClassNotFoundException {
        ArrayList<transaction> transactions = user.getTransacionList();
        if(!transactions.isEmpty()){
            transactionB.setDisable(false);
            loadTransactions();
        }else if(!user.getCreditList().isEmpty()){
            loadCredits();
        }
        if(!user.getCreditList().isEmpty()){
            creditB.setDisable(false);
        }

    }

    @FXML
    protected  void loadCredits() throws IOException, ClassNotFoundException {
        listView.getItems().clear();
        listText.setText("Kredyty");
        String login = serialization.deserializeString("login.txt");
        ArrayList<user> userList = serialization.deserializeUserList("data.txt");
        int j;
        for(j = 0;j<userList.size()-1;j++)
        {
            if(userList.get(j).getLogin().equals(Long.valueOf(login)))
            {
                break;
            }
        }
        ArrayList<credit> credits = userList.get(j).getCreditList();
        for(int i=0;i<credits.size();i++)
        {
            listView.getItems().add(credits.get(i).getNumber() + "\t\t\tStatus: " + credits.get(i).getStatus());
        }
        listView.setOnMouseClicked(event -> {
            if(event.getClickCount()==2)
            {
                try {
                    switchToPropertiesCredit(credits.get(listView.getSelectionModel().getSelectedIndex()));
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @FXML
    protected  void loadTransactions() throws IOException, ClassNotFoundException {
        listView.getItems().clear();
        listText.setText("Historia transakcji");
        String login = serialization.deserializeString("login.txt");
        ArrayList<user> userList = serialization.deserializeUserList("data.txt");
        int j;
        for(j = 0;j<userList.size()-1;j++)
        {
            if(userList.get(j).getLogin().equals(Long.valueOf(login)))
            {
                break;
            }
        }
        ArrayList<transaction> transactions = userList.get(j).getTransacionList();
        for(int i=0;i<transactions.size();i++)
        {
            if(transactions.get(i).isTransactionType()){
                listView.getItems().add("-" + String.format("%.02f", transactions.get(i).getBalance()) + transactions.get(i).getCurrency() + "\t\t" + transactions.get(i).getTitle() +"\t\t" + transactions.get(i).getSecondAccName());
            }
            else{
                listView.getItems().add("+" + String.format("%.02f", transactions.get(i).getBalance()) + transactions.get(i).getCurrency() +"\t\t" + transactions.get(i).getTitle() +"\t\t" + transactions.get(i).getFirstAccName() );
            }

        }
        listView.setOnMouseClicked(event -> {
            if(event.getClickCount()==2)
            {
                try {
                    switchToPropertiesTransaction(transactions.get(listView.getSelectionModel().getSelectedIndex()));
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @FXML
    public void toClipboard(){
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        String temp = numerKonta.getText();
        content.putString(temp.substring(13));
        clipboard.setContent(content);

        clipboardAlert.setVisible(true);

        Timer timerClipboardPopup = new Timer();
        timerClipboardPopup.schedule(new TimerTask() {
            @FXML
            @Override
            public void run() {
                Platform.runLater(()->{
                    clipboardAlert.setVisible(false);
                    timerClipboardPopup.cancel();
                });
            }
        },5000);
    }

    public void switchToPropertiesTransaction(transaction transaction) throws IOException, ClassNotFoundException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("transactionProperties.fxml"));
        root=loader.load();
        transactionPropertiesController transactionPropertiesController = loader.getController();
        transactionPropertiesController.init(transaction);
        stage = (Stage)listView.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToPropertiesCredit(credit credit) throws IOException, ClassNotFoundException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("creditProperties.fxml"));
        root=loader.load();
        creditPropertiesController creditPropertiesController = loader.getController();
        creditPropertiesController.init(credit, false);
        stage = (Stage)listView.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToExchange() throws IOException, ClassNotFoundException {
        String login = serialization.deserializeString("login.txt");
        ArrayList<user> userList = serialization.deserializeUserList("data.txt");
        int j;
        for(j = 0;j<userList.size()-1;j++)
        {
            if(userList.get(j).getLogin().equals(Long.valueOf(login)))
            {
                break;
            }
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("exchangeList.fxml"));
        root=loader.load();
        exchangeController exchangeController = loader.getController();
        exchangeController.init(userList.get(j).getCurrency());
        stage = (Stage)listView.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void switchToTransfer() throws IOException, ClassNotFoundException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("newTransfer.fxml"));
        root=loader.load();
        transferController transferController = loader.getController();
        transferController.init();
        stage = (Stage)listView.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToCredit() throws IOException, ClassNotFoundException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("creditUser.fxml"));
        root=loader.load();
        creditController creditController = loader.getController();
        creditController.init();
        stage = (Stage)listView.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToCreditworthiness() throws IOException, ClassNotFoundException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("creditworthinessUser.fxml"));
        root=loader.load();
        creditworthinessController creditworthinessController = loader.getController();
        creditworthinessController.init();
        stage = (Stage)listView.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void logOut(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("panelLogowania.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
