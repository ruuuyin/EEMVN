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
import pos.pckg.MiscInstances;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.data.entity.Item;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.InputRestrictor;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class POSNewItem extends POSInventory {

    @FXML
    private StackPane rootPane;

    @FXML
    private TextField tfItemCode;

    @FXML
    private TextField tfItemName;

    @FXML
    private TextField tfPrice;

    @FXML
    private TextField tfInititalStock;

    @FXML
    private Label lblTotalValue;

    @FXML
    private JFXButton btnClose;

    @FXML
    private JFXButton btnAdd;

    private MiscInstances misc ;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InputRestrictor.limitInput(tfItemCode,13);
        InputRestrictor.numbersInput(tfItemCode);
        InputRestrictor.numbersInput(tfPrice);
        InputRestrictor.withouDecimal(tfInititalStock);

        tfItemCode.requestFocus();
        misc = new MiscInstances();
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            if (!tfPrice.getText().equals("") && !tfInititalStock.getText().equals("")){
                    double price = Double.parseDouble(tfPrice.getText());
                    int initStock = Integer.parseInt(tfInititalStock.getText());
                    lblTotalValue.setText((price*initStock)+"");
            }
        }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();


    }

    @FXML
    void btnAddOnAction(ActionEvent event) {
        if (codeExist(this.tfItemCode.getText())) {
            POSMessage.showMessage(rootPane, "Item Code Already Exist", "Item Code Error", POSMessage.MessageType.ERROR);
        }else if (hasEmptyField()){
            POSMessage.showMessage(rootPane,"Please fill all the Fields","Invalid Value", POSMessage.MessageType.ERROR);
        }else if (tfItemCode.getText().length()<12){
            POSMessage.showMessage(rootPane, "You've entered an Invalid Code", "Invalid Code", POSMessage.MessageType.ERROR);
        }else {
            String sql = "Insert into item(itemCode,itemName,itemPrice,stock)" +
                    " values ('" + tfItemCode.getText() + "'" +
                    ",'" + tfItemName.getText() + "'" +
                    "," + Double.parseDouble(tfPrice.getText()) + "" +
                    "," + Integer.parseInt(tfInititalStock.getText()) + ")";

            misc.dbHandler.startConnection();
            misc.dbHandler.execUpdate(sql);
            misc.dbHandler.closeConnection();

            Date d = new Date();
            SimpleDateFormat date = new SimpleDateFormat(BackgroundProcesses.DATE_FORMAT);
            sql = "INSERT INTO systemlogs(type, eventAction, date, userID, referencedID)" +
                    " VALUES ( 'Stock Management'" +
                    ", 'Add'" +
                    ", '" + date.format(d) + "'" +
                    ", '" + POSInventory.userID + "'" +
                    ", '" + tfItemCode.getText() + "');";

            misc.dbHandler.startConnection();
            misc.dbHandler.execUpdate(sql);
            misc.dbHandler.closeConnection();

            POSMessage.showMessage(rootPane, "New Item has been Added"
                    , "Item Added"
                    , POSMessage.MessageType.INFORM);


            queryAllItems();

            tfItemCode.setText("");
            tfItemName.setText("");
            tfInititalStock.setText("");
            tfPrice.setText("");
            lblTotalValue.setText("0");
            tfItemCode.requestFocus();
        }
    }

    @FXML
    void btnCloseOnAction(ActionEvent event) {
        sceneManipulator.closeDialog();
    }

    private boolean codeExist(String itemCode){
        boolean itemExist = false;
        for (Item item:itemList) {
            if (item.getItemCode().equals(itemCode)){
                itemExist = true;
                break;
            }
        }
        return itemExist;
    }

    private boolean hasEmptyField(){
        return tfItemName.getText().equals("") ||
                tfItemCode.getText().equals("") ||
                tfPrice.getText().equals("") ||
                tfInititalStock.getText().equals("");
    }

}
