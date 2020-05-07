package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import pos.Main;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.data.AES;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.DataBridgeDirectory;
import pos.pckg.misc.DirectoryHandler;

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

public class POSCheckout extends POSCashier {

    @FXML
    private StackPane rootPane;

    @FXML
    private Label lblCardID;

    @FXML
    private Label lblOwner;

    @FXML
    private Label lblBalance;

    @FXML
    private Label lblCheckout;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblRemaining;

    @FXML
    private ImageView ivPrompt;

    private boolean cardDetected = false, validBalance=false;
    private Scanner scan;
    private String forChallenge;
    private BufferedWriter writer;
    private Timeline cardIdScannerThread, checkPINThread;
    private String cardID=null,customerID=null;
    private static String sql="";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            gsmSignalThread.stop();
            rfidStatus.stop();
            cacheClear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BackgroundProcesses.changeSecondaryFormStageStatus((short) 1);
        scanCard();
    }

    @FXML
    void btnCancelOnAction(ActionEvent event) throws IOException {
        cacheClear();
        try{
            Main.rfid.cancelOperation();
            writer = new BufferedWriter(new FileWriter(DataBridgeDirectory.DOCUMENT+"etc\\cache-secondary-check-card.file"));
            writer.write("0");
            writer.close();
            gsmSignalThread.play();
            rfidStatus.play();
        }catch (Exception e){

        }

        BackgroundProcesses.changeSecondaryFormStageStatus((short) 0);
        sceneManipulator.closeDialog();
    }

    @FXML
    void btnProceedOnAction(ActionEvent event) throws SQLException {
        if (!cardDetected){
            createStandardMessage((Node) event.getSource(),
                    "Invalid Procedure",
                    "Please scan the customer's card first",POSMessage.MessageType.ERROR);
        }else if (!this.validBalance){
            createStandardMessage((Node) event.getSource(),
                    "Invalid Procedure",
                    "Insufficient Balance",POSMessage.MessageType.ERROR);
        }else{

            int orderID = insertToOrder();
            insertAllOrders(orderID);
            insertTransaction(orderID);
            updateCardBalance();
            String message = "Transaction : "+transactionNumber+"\n"
                    +"Cost : "+String.valueOf(transactionCost)+"\n"
                    //+"Date : "+this.date+"\n"
                    //+"Time : "+this.time+"\n"
                    +"Balance : "+remainingBalance;

            String intPhone = "63".concat(phone.substring(1,phone.length())).toString();
            try {

                System.out.println("Message\n============================================\n"+message);
                System.out.println("PhoneNo\n============================================\n"+intPhone);
                Main.rfid.sendSMS(intPhone,message);

            }catch (Exception e){
                e.printStackTrace();
            }
            JFXButton btnOk = new JFXButton("Ok");
            btnOk.setOnAction(evt -> {
                try {
                    closeDialogs();
                    queryAllItem();
                    clearAllData();
                    gsmSignalThread.play();
                    rfidStatus.play();
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

    private void scanCard(){
        try {
            Main.rfid.scan();
            cardIdScannerThread = new Timeline(new KeyFrame(Duration.ZERO, e -> {
                try {
                    Scanner scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\rfid-cache.file"));
                    while (scan.hasNextLine()){
                        String scanned[] = scan.nextLine().split("=");
                        if (scanned[0].equals("scan")){
                            cardID = scanned[1];
                            queryCard();
                            Main.rfid.clearCache();
                            cardIdScannerThread.stop();
                            break;
                        }

                    }
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            }),
                    new KeyFrame(Duration.seconds(1))
            );
            cardIdScannerThread.setCycleCount(Animation.INDEFINITE);
            cardIdScannerThread.play();
        }catch (NullPointerException e){
            JFXButton button = new JFXButton("Ok");
            button.setOnAction(s->{
                POSMessage.closeMessage();
            });
            POSMessage.showConfirmationMessage(rootPane,"Please connect the RFID Scanner to complete Task",
                    "Cannot Detect Scanner",
                    POSMessage.MessageType.ERROR,button);
        }
    }

    private void checkPIN() throws FileNotFoundException {

        scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-checkout-card.file"));
        for (int i  = 1; i<=6;i++) System.out.println(scan.nextLine());
        forChallenge= AES.decrypt(scan.nextLine(), POSCashier.S_KEY);//TODO Under observation
        Main.rfid.PINChallenge(forChallenge);

        checkPINThread = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            try {
                scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\rfid-cache.file"));
                while (scan.hasNextLine()){
                    String scanned[] = scan.nextLine().split("=");
                    if (scanned[0].equals("PINChallenge")){
                        if (scanned[1].equals("1")){
                            populateData();
                            Main.rfid.clearCache();
                            lblStatus.setText("You may now Proceed");
                            ivPrompt.setImage(new Image(DirectoryHandler.IMG+ "pos-done.png"));
                            checkPINThread.stop();
                            break;
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                checkPINThread.stop();
            }

        }),
                new KeyFrame(Duration.seconds(1))
        );
        checkPINThread.setCycleCount(Animation.INDEFINITE);
        checkPINThread.play();

    }

    private String phone="";
    private void queryCard(){
        String sql = "Select * from card where cardID='"+cardID+"' and isActive = 1";
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);

        String data="";
        try {
            if (result.next()){
                data+=result.getString("cardId")+"\n"
                    + result.getDouble("credits")+"\n"
                    + result.getInt("isActive")+"\n"
                    + result.getString("activationDate")+"\n"
                    + result.getString("expiryDate")+"\n"
                    + result.getInt("customerID")+"\n"
                    + result.getString("PIN");

                customerID = result.getInt("customerID")+"";
                writer = new BufferedWriter(new FileWriter(DataBridgeDirectory.DOCUMENT+"etc\\cache-checkout-card.file"));
                writer.write(data);
                writer.close();
                misc.dbHandler.closeConnection();

                sql = "Select * from customer where customerID = "+customerID+"";
                misc.dbHandler.startConnection();
                result = misc.dbHandler.execQuery(sql);
                result.next();
                data="";
                phone = result.getString("phoneNumber");
                data += result.getInt("customerID")+"\n"+
                        result.getString("firstName")+"\n"+
                        result.getString("middleInitial")+"\n"+
                        result.getString("lastName")+"\n"+
                        result.getString("Sex")+"\n"+
                        result.getString("address")+"\n"+
                        phone+"\n"+
                        result.getString("emailAddress");
                writer = new BufferedWriter(new FileWriter(DataBridgeDirectory.DOCUMENT+"etc\\cache-checkout-customer.file"));
                writer.write(data);
                writer.close();
                misc.dbHandler.closeConnection();

                checkPIN();
            }else{
                JFXButton button = new JFXButton("Ok");
                button.setOnAction(s->{
                    POSMessage.closeMessage();
                    scanCard();
                });
                POSMessage.showConfirmationMessage(rootPane,"Card doesn't exist or maybe\nit is deactivated",
                        "Invalid Card",
                        POSMessage.MessageType.ERROR,button);
                misc.dbHandler.closeConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void populateData() throws IOException {

        scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-checkout-card.file"));
        lblCardID.setText(scan.nextLine());
        lblBalance.setText(scan.nextLine());
        scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-checkout-customer.file"));
        scan.nextLine();
        lblOwner.setText(scan.nextLine()+" "+scan.nextLine()+". "+scan.nextLine());
        scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-checkout-total.file"));
        if (scan.hasNextLine())
            lblCheckout.setText(scan.nextLine());

        double total = Double.parseDouble(lblBalance.getText())
                - Double.parseDouble(lblCheckout.getText());
        lblRemaining.setStyle(total>0.0?"-fx-text-fill:#147696":"-fx-text-fill:#ff6475");
        lblRemaining.setText(total+"");

        writer = new BufferedWriter(new FileWriter(DataBridgeDirectory.DOCUMENT+"etc\\cache-secondary-check-card.file"));
        writer.write("1");
        writer.close();

        validBalance = (total>0);
        cardDetected = true;
    }//TODO Edit the color of the Remaining balance if it turns negative

    private void cacheClear() throws IOException {
        writer = new BufferedWriter(new FileWriter(DataBridgeDirectory.DOCUMENT+"etc\\cache-secondary-check-card.file"));
        writer.write("0");
        writer.close();
    }

    private void createStandardMessage(Node node , String title,String message,POSMessage.MessageType type){
        JFXButton btnOk = new JFXButton("Close");
        btnOk.setOnAction(evt -> {
            POSMessage.closeMessage();
        });

        POSMessage.showConfirmationMessage((StackPane) BackgroundProcesses.getRoot(node),message,title,type, btnOk);
    }

    private int transactionNumber;
    private double transactionCost,remainingBalance;
    private String date,time;


    private final int insertToOrder() throws SQLException {
        sql = "Insert into orders(itemCount,typeCount,subTotal,discount,total)" +
                "value ("+ POSCashier.items+","+ POSCashier.type+","+ POSCashier.subTotal+","+ POSCashier.discount+","+ POSCashier.total+")";
        misc.dbHandler.startConnection();
        misc.dbHandler.execUpdate(sql);
        misc.dbHandler.closeConnection();
        transactionCost = POSCashier.total;

        sql = "Select max(orderID) as lastID from Orders";
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);
        result.next();
        int id = result.getInt("lastID");
        misc.dbHandler.closeConnection();

        return id;
    }

    private final void insertAllOrders(int orderID){

        productList.forEach(e->{
            String item[] = e.getProductID().split("-");
            sql = "Insert into orderitem(itemID,quantity,subtotal,orderID) " +
                   "values("+item[1]+","+e.getQuantity()+","+e.getTotal()+","+orderID+")";
            misc.dbHandler.startConnection();
            misc.dbHandler.execUpdate(sql);
            misc.dbHandler.closeConnection();
            try {
                updateStocks(item[1],item[0],e.getQuantity());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

    }

    private final void updateStocks(String itemID,String itemCode,int count) throws SQLException {

        sql = "Select stock from item where itemID = "+itemID+"";
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);
        result.next();
        int newStock = (result.getInt("stock")-count);
        misc.dbHandler.closeConnection();


        sql = "Update item set stock = " + newStock + " where " +
                "itemID = " + itemID + " and " +
                "itemCode = '" + itemCode+ "'";
        misc.dbHandler.startConnection();
        misc.dbHandler.execUpdate(sql);
        misc.dbHandler.closeConnection();
    }

    private final void insertTransaction(int id) throws SQLException {
        LocalDateTime currentTime = LocalDateTime.now();
        Date d = new Date();
        SimpleDateFormat date = new SimpleDateFormat(BackgroundProcesses.DATE_FORMAT);
        sql = "Insert into transaction(type,userID,customerID,typeID,date,time) " +
                "values('Retail','"+ POSCashier.userID+"',"+customerID+","+id+",'"+date.format(d)+"','"+currentTime.format(DateTimeFormatter.ofPattern("hh:mm a"))+"')";

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

    private final void updateCardBalance(){
        sql = "Update card set credits = "+lblRemaining.getText()+" where customerID = "+customerID+" and cardID = '"+cardID+"'";
        misc.dbHandler.startConnection();
        misc.dbHandler.execUpdate(sql);
        misc.dbHandler.closeConnection();

        remainingBalance = Double.parseDouble(lblRemaining.getText());
    }

    private final void closeDialogs() throws IOException {
        BackgroundProcesses.changeSecondaryFormStageStatus((short) 0);
        sceneManipulator.closeDialog();
    }

    private final void clearAllData(){
        POSCashier.productList.clear();
        POSCashier.discount = 0;
    }
}
