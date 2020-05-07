package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import pos.Main;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.data.entity.Item;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.DataBridgeDirectory;
import pos.pckg.misc.InputRestrictor;

import java.io.*;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSReturn extends POSCashier implements Initializable {

    @FXML
    private StackPane rootPane;

    @FXML
    private VBox vbSearchProduct;

    @FXML
    private TextField tfProductCode;

    @FXML
    private Label lblName;

    @FXML
    private Label lblPrice;

    @FXML
    private TextField tfQuantity;
    @FXML
    private TextField tfTransaction;

    @FXML
    private Label lblTotal;

    @FXML
    private TextArea taReasons;

    @FXML
    private Label lblCardID;

    @FXML
    private Label lblCardOwner;

    @FXML
    private HBox lblScanCardNotifier;

    private int itemId;
    private BufferedWriter writer;
    private String phone = "";
    private Timeline cardIdScannerThread;
    private String cardID = null, customerID = null;
    private Scanner scan;
    private boolean cardDetected = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        BackgroundProcesses.changeSecondaryFormStageStatus((short) 2);
        InputRestrictor.numbersInput(tfQuantity);
        InputRestrictor.limitInput(tfQuantity, 11);
        scanCard();
    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        BackgroundProcesses.changeSecondaryFormStageStatus((short) 0);
        try {
            Main.rfid.cancelOperation();
        } catch (Exception e) {
            e.printStackTrace();
        }
        gsmSignalThread.play();
        rfidStatus.play();
        sceneManipulator.closeDialog();
    }

    @FXML
    void btnProceedOnAction(ActionEvent event) throws SQLException {
        if (lblName.getText().equals("N/A")) {
            createStandardMessage(rootPane, "Invalid Item", "Please specify the Item", POSMessage.MessageType.ERROR);
        } else if (taReasons.getText().equals("")) {
            createStandardMessage(rootPane, "Invalid Value", "Please specify the Reason(s)", POSMessage.MessageType.ERROR);
        } else {
            //updateCustomer();
            updateStocks(tfProductCode.getText(),Integer.parseInt(tfQuantity.getText()));
            int id = insertReturnItem();
            insertTransaction(id);

            JFXButton btnOk = new JFXButton("Ok");
            btnOk.setOnAction(evt -> {
                try {
                    gsmSignalThread.play();
                    rfidStatus.play();
                    closeDialogs();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                POSMessage.closeMessage();
            });

            POSMessage.showConfirmationMessage((StackPane) BackgroundProcesses.getRoot(rootPane),
                    "Transaction Complete",
                    "Message",
                    POSMessage.MessageType.INFORM, btnOk);
        }
    }

    @FXML
    void checkBtn(ActionEvent event) {
        if (tfProductCode.getText().length() >= 12) {
            try {
                scanOrderedItem();
            } catch (Exception e) {
                e.getMessage();
            }
        } else {
            lblName.setText("N/A");
            lblPrice.setText("0.0");
            lblTotal.setText("0.0");
        }
    }

    @FXML
    void tfQuantityOnReleased(KeyEvent event) {
        if (!tfQuantity.getText().equals("") && Integer.parseInt(tfQuantity.getText()) > 0) {
            double total = Integer.parseInt(tfQuantity.getText()) * Double.parseDouble(lblPrice.getText());
            lblTotal.setText(String.valueOf(total));
        } else {
            tfQuantity.setText("1");
        }
    }

    private void scanCard() {
        try {
            Main.rfid.scan();
            cardIdScannerThread = new Timeline(new KeyFrame(Duration.ZERO, e -> {
                Scanner scan = null;
                try {
                    scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\rfid-cache.file"));
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
                if (scan.hasNextLine()) {
                    String scanned[] = scan.nextLine().split("=");
                    if (scanned[0].equals("scan")) {
                        cardID = scanned[1];
                        queryCard();
                        Main.rfid.clearCache();
                        cardIdScannerThread.stop();
                    }
                }
            }),
                    new KeyFrame(Duration.seconds(1))
            );
            cardIdScannerThread.setCycleCount(Animation.INDEFINITE);
            cardIdScannerThread.play();
        } catch (NullPointerException e) {
            JFXButton button = new JFXButton("Ok");
            button.setOnAction(s -> {
                POSMessage.closeMessage();
            });
            POSMessage.showConfirmationMessage(rootPane, "Please connect the RFID Scanner to complete Task",
                    "Cannot Detect Scanner",
                    POSMessage.MessageType.ERROR, button);
        }
    }

    private void queryCard() {
        String sql = "Select * from card where cardID='" + cardID + "' and isActive = 1";
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);

        try {
            String data = "";
            if (result.next()) {
                data += result.getString("cardId") + "\n"
                        + result.getDouble("credits") + "\n"
                        + result.getInt("customerID");

                customerID = result.getInt("customerID") + "";
                writer = new BufferedWriter(new FileWriter(DataBridgeDirectory.DOCUMENT+"etc\\cache-reload-card.file"));
                writer.write(data);
                writer.close();
                misc.dbHandler.closeConnection();

                sql = "Select * from customer where customerID = " + customerID + "";
                misc.dbHandler.startConnection();
                result = misc.dbHandler.execQuery(sql);
                result.next();
                data = "";
                phone = result.getString("phoneNumber");
                data += result.getInt("customerID") + "\n" +
                        result.getString("firstName") + "\n" +
                        result.getString("middleInitial") + "\n" +
                        result.getString("lastName") + "\n" +
                        phone + "\n";
                writer = new BufferedWriter(new FileWriter(DataBridgeDirectory.DOCUMENT+"etc\\cache-reload-customer.file"));
                writer.write(data);
                writer.close();
                misc.dbHandler.closeConnection();
                populateData();
                taReasons.setDisable(false);
                vbSearchProduct.setDisable(false);
                lblScanCardNotifier.setVisible(false);
            } else {
                JFXButton button = new JFXButton("Ok");
                button.setOnAction(s -> {
                    POSMessage.closeMessage();
                    scanCard();
                });
                POSMessage.showConfirmationMessage(rootPane, "Card doesn't exist",
                        "Invalid Card",
                        POSMessage.MessageType.ERROR, button);
                misc.dbHandler.closeConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateData() throws FileNotFoundException {
        scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-reload-card.file"));
        lblCardID.setText(scan.nextLine());

        scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-reload-customer.file"));
        scan.nextLine();
        lblCardOwner.setText(scan.nextLine() + " " + scan.nextLine() + ". " + (scan.nextLine().charAt(0)) + ".");
        cardDetected = true;
    }

    private void createStandardMessage(Node node, String title, String message, POSMessage.MessageType type) {
        JFXButton btnOk = new JFXButton("Close");
        btnOk.setOnAction(evt -> {
            POSMessage.closeMessage();
        });

        POSMessage.showConfirmationMessage((StackPane) BackgroundProcesses.getRoot(node), message, title, type, btnOk);
    }

    private String sql;


    private void updateCustomer() {
        //update card set card.credits =  ((Select card.credits from card where card.customerID=4)+500) where card.cardID='4
        sql = "update card set card.credits =  ((Select card.credits from card where card.cardID='" + cardID + "')+" + item.getItemPrice() + ") where card.cardID='" + cardID + "'";
        misc.dbHandler.startConnection();
        misc.dbHandler.execUpdate(sql);
        misc.dbHandler.closeConnection();
    }

    private int insertReturnItem() throws SQLException {
        sql = "Insert into returnItem(itemID,reason,Quantity,totalAmount) values(" + item.getItemID() + ",'" + taReasons.getText() + "'," + tfQuantity.getText() + "," + lblTotal.getText() + ")";

        misc.dbHandler.startConnection();
        misc.dbHandler.execUpdate(sql);
        misc.dbHandler.closeConnection();

        sql = "Select max(returnID) as lastID from returnItem";
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);
        result.next();
        int id = result.getInt("lastID");
        misc.dbHandler.closeConnection();

        return id;
    }


    private String date, time;
    private int transactionNumber;

    private final void insertTransaction(int id) throws SQLException {
        LocalDateTime currentTime = LocalDateTime.now();
        Date d = new Date();
        SimpleDateFormat date = new SimpleDateFormat(BackgroundProcesses.DATE_FORMAT);
        sql = "Insert into transaction(type,userID,customerID,typeID,date,time) " +
                "values('Item Return','" + POSCashier.userID + "'," + customerID + "," + id + ",'" + date.format(d) + "','" + currentTime.format(DateTimeFormatter.ofPattern("hh:mm a")) + "')";

        misc.dbHandler.startConnection();
        misc.dbHandler.execUpdate(sql);
        misc.dbHandler.closeConnection();
        this.date = date.format(d);
        this.time = currentTime.format(DateTimeFormatter.ofPattern("hh:mm a"));

        sql = "Select max(transactionID) as lastID from transaction";
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);
        result.next();
        transactionNumber = result.getInt("lastID");
        misc.dbHandler.closeConnection();
    }


    private final void closeDialogs() throws IOException {
        BackgroundProcesses.changeSecondaryFormStageStatus((short) 0);
        sceneManipulator.closeDialog();
    }

    private Item item = null;
    private int quantity = 0, orderID = 0, secretID = 0;
    private double toBeReturn = 0d;

    private final void scanOrderedItem() throws SQLException {
        item = null;
        quantity = 0;
        secretID = 0;
        sql = "Select typeID from transaction where transactionID = " + tfTransaction.getText();
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);
        if (result.next()) {
            orderID = result.getInt("typeID");
            misc.dbHandler.startConnection();
        } else {
            createStandardMessage(rootPane, "Invalid Value", "You've entered an invalid\ntranscation number", POSMessage.MessageType.ERROR);
            misc.dbHandler.closeConnection();
            return;
        }

        sql = "Select itemID,quantity,subtotal from orderitem where orderID =" + orderID + "";
        misc.dbHandler.startConnection();
        result = misc.dbHandler.execQuery(sql);
        while (result.next()) {
            quantity = result.getInt("quantity");
            secretID = result.getInt("itemID");
            allItem.forEach(e -> {
                if (e.getItemID() == secretID)
                    if (e.getItemCode().equals(tfProductCode.getText()))
                        item = e;
            });
        }

        if (item != null) {
            lblName.setText(item.getItemName());
            tfQuantity.setText(quantity + "");
            lblPrice.setText(item.getItemPrice() + "");
            double total = Integer.parseInt(tfQuantity.getText()) * Double.parseDouble(lblPrice.getText());
            lblTotal.setText(String.valueOf(total));
        } else {
            createStandardMessage(rootPane, "Invalid Item", "There's no such item from\ncustomer's logs", POSMessage.MessageType.ERROR);
        }

    }


    private final void updateStocks( String itemCode, int count) throws SQLException {

        sql = "Select stock from item where itemCode = " + itemCode + "";
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);
        result.next();
        int newStock = (result.getInt("stock") - count);
        misc.dbHandler.closeConnection();


        sql = "Update item set stock = " + newStock + " where " +
                "itemCode = '" + itemCode + "'";
        misc.dbHandler.startConnection();
        misc.dbHandler.execUpdate(sql);
    }
}