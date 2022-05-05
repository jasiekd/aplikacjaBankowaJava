package com.example.aplikacjabankowajava;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class transactionPropertiesController {

    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private Label transactionNumberText;
    @FXML
    private Label titleText;
    @FXML
    private Label amountText;
    @FXML
    private  Label dateText;
    @FXML
    private Label senderName;
    @FXML
    private Label receiverName;
    @FXML
    private  Label senderAcc;
    @FXML
    private  Label receiverAcc;
    @FXML
    private Label balanceLabel;
    @FXML
    private Label typeLabel;


    public void init(transaction transaction){
        transactionNumberText.setText("Transakcja nr. "+transaction.getNumber());
        titleText.setText("Tytuł transakcji:\n"+transaction.getTitle());
        dateText.setText("Data operacji:\n"+transaction.getDate());
        senderName.setText("Nadawca:\n"+transaction.getFirstAccName());
        receiverName.setText("Odbiorca:\n"+transaction.getSecondAccName());
        balanceLabel.setText("Saldo po operacji:\n"+String.format("%.02f",transaction.getNewBalance()) + transaction.getCurrency());
        if (transaction.isTransactionType()) {
            amountText.setText("Kwota operacji:\n"+ "-" + String.format("%.02f", transaction.getBalance())+ transaction.getCurrency());
            typeLabel.setText("Typ transakcji:\nWychodząca");
        } else  {
            amountText.setText("Kwota operacji:\n"+ "+" + String.format("%.02f", transaction.getBalance())+ transaction.getCurrency());
            typeLabel.setText("Typ transakcji:\nPrzychodząca");
        }
        if(transaction.getFirstAcc()==null)
        {
            senderAcc.setText("");
        }
        else
        {
            senderAcc.setText("Nr konta nadawcy\n"+transaction.getFirstAcc().toString());
        }

        receiverAcc.setText("Nr konta odbiorcy\n"+transaction.getSecondAcc().toString());

    }
    @FXML
    protected void goBack(ActionEvent event) throws IOException, ClassNotFoundException {
        FXMLLoader loader;
        if(serialization.deserializeString("exit.txt").equals("user")){
            loader = new FXMLLoader(getClass().getResource("panelUser.fxml"));
            root=loader.load();
            userController userController = loader.getController();
            ArrayList<user> tempList = serialization.deserializeUserList("data.txt");
            Long loginT = Long.valueOf(serialization.deserializeString("login.txt"));
            int i;
            for(i=0;i<tempList.size();i++){
                if(loginT.equals(tempList.get(i).getLogin())) {
                   break;
                }
            }
            userController.initUser(tempList.get(i),i);
            userController.initList(tempList.get(i));
        }else{
            loader = new FXMLLoader(getClass().getResource("transactionList.fxml"));
            root=loader.load();
            transactionController transactionController = loader.getController();
            transactionController.initList();
        }

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
