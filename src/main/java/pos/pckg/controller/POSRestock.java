package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.InputRestrictor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSRestock extends POSInventory{

    @FXML
    private StackPane rootPane;

    @FXML
    private TextField tfItemCode;

    @FXML
    private TextField tfItemName;

    @FXML
    private TextField tfCurrentStock;

    @FXML
    private TextField tfAddStock;

    @FXML
    private Label lblEstimatedValue;

    @FXML
    private JFXButton btnCancel;

    @FXML
    private JFXButton btnSave;

    @FXML
    private JFXButton btnAdd,btnSubtract;

    protected double price = 0;
    private int itemID = 0;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InputRestrictor.numbersInput(tfAddStock);
        InputRestrictor.limitInput(tfAddStock, 3);
        try {
            Scanner scan = new Scanner(new FileInputStream(BackgroundProcesses.getFile("etc\\cache-selected-item.file")));
            itemID = Integer.parseInt(scan.nextLine());
            tfItemCode.setText(scan.nextLine());
            tfItemName.setText(scan.nextLine());
            price = Double.parseDouble(scan.nextLine());
            tfCurrentStock.setText(scan.nextLine());
            lblEstimatedValue.setText(scan.nextLine());
            tfAddStock.setText(tfCurrentStock.getText());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            int newVal = tfAddStock.getText().equals("") ? 0 : Integer.parseInt(tfAddStock.getText());
            lblEstimatedValue.setText((newVal*price)+"");
        }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

    }
    //TODO recalculating the Text everytime the user are entering new stock value
    //TODO update database when adding stocks

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        sceneManipulator.closeDialog();
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {

        JFXButton btnNo = new JFXButton("No");
        btnNo.setOnAction(e-> POSMessage.closeMessage());

        JFXButton btnYes = new JFXButton("Yes");
        btnYes.setOnAction(e->{

            POSMessage.closeMessage();

            String sql = "Update item set stock = " + tfAddStock.getText() + " where" +
                    "\nitemID = " + itemID + " and " +
                    "\nitemCode = '" + tfItemCode.getText() + "'";

            misc.dbHandler.startConnection();
            misc.dbHandler.execUpdate(sql);
            misc.dbHandler.closeConnection();

            Date d = new Date();
            SimpleDateFormat date = new SimpleDateFormat(BackgroundProcesses.DATE_FORMAT);
            sql = "INSERT INTO systemlogs(type, eventAction, date, userID, referencedID)" +
                    " VALUES ( 'Stock Management', 'Restock', '" + date.format(d) + "', '" + POSInventory.userID + "', '" + tfItemCode.getText() + "');";

            misc.dbHandler.startConnection();
            misc.dbHandler.execUpdate(sql);
            misc.dbHandler.closeConnection();

            JFXButton btnOk = new JFXButton("Ok");
            btnOk.setOnAction(ev -> {
                POSMessage.closeMessage();
                queryAllItems();
                sceneManipulator.closeDialog();
            });

            POSMessage.showConfirmationMessage(rootPane,
                    "Item " + itemID + " is now updated",
                    "Update Success",
                    POSMessage.MessageType.INFORM,btnOk);

        });

        POSMessage.showConfirmationMessage(rootPane,"Do you really want to change the \n" +
                        "stock value of  this item?"
                ,"Please Confirm Update", POSMessage.MessageType.CONFIRM,btnNo,btnYes);

    }

    @FXML
    private void changeStockButton(ActionEvent event) {
        if (tfAddStock.getText().isEmpty())
            return;
        else if (tfAddStock.getText().equals("1") && event.getSource().equals(btnSubtract))
            return;

        int x = Integer.parseInt(tfAddStock.getText());
        if (event.getSource().equals(btnAdd))
            x=x+1;
        else if (event.getSource().equals(btnSubtract))
            x=x-1;
        tfAddStock.setText(String.valueOf(x));

    }
}
