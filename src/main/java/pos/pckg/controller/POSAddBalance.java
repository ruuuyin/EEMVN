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
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import pos.Main;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.misc.BackgroundProcesses;
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

public class POSAddBalance extends POSCashier implements Initializable {

    @FXML
    private StackPane rootPane;

    @FXML
    private Label lblCardID;

    @FXML
    private Label lblCardOwner;

    @FXML
    private Label lblRemainingBalance;

    @FXML
    private Label lblPastReload;

    @FXML
    private ImageView ivRfid;

    @FXML
    private TextField tfAddBalance;

    @FXML
    private Label lblNewBalance;

    private BufferedWriter writer;
    private String phone="";
    private Timeline cardIdScannerThread;
    private String cardID=null,customerID=null;
    private Scanner scan;
    private boolean cardDetected = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InputRestrictor.numbersInput(tfAddBalance);
        InputRestrictor.limitInput(tfAddBalance,5);
        BackgroundProcesses.changeSecondaryFormStageStatus((short) 2);
        scanCard();
    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        try{
            Main.rfid.cancelOperation();
            gsmSignalThread.play();
            rfidStatus.play();
        }catch (Exception e){
            e.printStackTrace();
        }
        BackgroundProcesses.changeSecondaryFormStageStatus((short) 0);
        sceneManipulator.closeDialog();
    }

    @FXML
    void btnProceedOnAction(ActionEvent event) throws SQLException {
        if (!cardDetected){
            createStandardMessage((Node) event.getSource(),
                    "Invalid Procedure",
                    "Please scan the customer's card first", POSMessage.MessageType.ERROR);
        }else{
            updateCard();
            int id = recredit();
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
    void tfAddBalanceOnKeyReleased(KeyEvent event) {
        if (!tfAddBalance.equals("")){
            lblNewBalance.setText(String.valueOf(
                    Double.parseDouble(tfAddBalance.getText())
                            +Double.parseDouble(lblRemainingBalance.getText())
            ));
        }
    }

    private void scanCard(){
        try {
            Main.rfid.scan();
            cardIdScannerThread = new Timeline(new KeyFrame(Duration.ZERO, e -> {
                Scanner scan = null;
                try {
                    scan = new Scanner(new FileInputStream("etc\\pckg.rfid-cache.file"));
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
                if (scan.hasNextLine()){
                        String scanned[] = scan.nextLine().split("=");
                        if (scanned[0].equals("scan")){
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

    private void queryCard() {
        String sql = "Select * from card where cardID='"+cardID+"' and isActive = 1";
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);

        try {
            String data="";
            if (result.next()){
                data+=result.getString("cardId")+"\n"
                        + result.getDouble("credits")+"\n"
                        + result.getString("activationDate")+"\n"
                        + result.getString("expiryDate")+"\n"
                        + result.getInt("customerID");

                customerID = result.getInt("customerID")+"";
                writer = new BufferedWriter(new FileWriter("etc\\cache-reload-card.file"));
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
                        phone+"\n";
                writer = new BufferedWriter(new FileWriter("etc\\cache-reload-customer.file"));
                writer.write(data);
                writer.close();
                misc.dbHandler.closeConnection();
                populateData();
                tfAddBalance.setDisable(false);
                ivRfid.setOpacity(.2);
            }else{
                JFXButton button = new JFXButton("Ok");
                button.setOnAction(s->{
                    POSMessage.closeMessage();
                    scanCard();
                });
                POSMessage.showConfirmationMessage(rootPane,"Card doesn't exist",
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

    private void populateData() throws FileNotFoundException {
        scan = new Scanner(new FileInputStream("etc\\cache-reload-card.file"));
        lblCardID.setText(scan.nextLine());
        lblRemainingBalance.setText(scan.nextLine());
        lblPastReload.setText(scan.nextLine());

        scan = new Scanner(new FileInputStream("etc\\cache-reload-customer.file"));
        scan.nextLine();
        lblCardOwner.setText(scan.nextLine()+" "+scan.nextLine()+". "+(scan.nextLine().charAt(0))+".");
        lblNewBalance.setText(lblRemainingBalance.getText());
        cardDetected = true;
    }

    private void createStandardMessage(Node node , String title, String message, POSMessage.MessageType type){
        JFXButton btnOk = new JFXButton("Close");
        btnOk.setOnAction(evt -> {
            POSMessage.closeMessage();
        });

        POSMessage.showConfirmationMessage((StackPane) BackgroundProcesses.getRoot(node),message,title,type, btnOk);
    }

    private String sql;
    private void updateCard(){
        Date d = new Date();
        SimpleDateFormat date = new SimpleDateFormat(BackgroundProcesses.DATE_FORMAT);
        String activation, expiry;
        activation  = date.format(d);
        date = new SimpleDateFormat("MM-dd-");
        expiry = date.format(d)+((d.getYear()+1900)+1);
        sql = "Update card set " +
                "credits = "+lblNewBalance.getText()+", isActive = 1"+", activationDate = '"+activation+"', expiryDate = '" +expiry+"'" +
                " where cardId = '"+cardID+"'";

        misc.dbHandler.startConnection();
        misc.dbHandler.execUpdate(sql);
        misc.dbHandler.closeConnection();
    }

    private int recredit() throws SQLException {
        sql = "Insert into recredit(Amount,cardID) values("+tfAddBalance.getText()+",'"+cardID+"')";
        misc.dbHandler.startConnection();
        misc.dbHandler.execUpdate(sql);
        misc.dbHandler.closeConnection();

        sql = "Select max(recreditID) as lastID from recredit";
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);
        result.next();
        int id = result.getInt("lastID");
        misc.dbHandler.closeConnection();

        return id;
    }

    private String date,time;
    private int transactionNumber;
    private final void insertTransaction(int id) throws SQLException {
        LocalDateTime currentTime = LocalDateTime.now();
        Date d = new Date();
        SimpleDateFormat date = new SimpleDateFormat(BackgroundProcesses.DATE_FORMAT);
        sql = "Insert into transaction(type,userID,customerID,typeID,date,time) " +
                "values('Add Balance','"+ POSCashier.userID+"',"+customerID+","+id+",'"+date.format(d)+"','"+currentTime.format(DateTimeFormatter.ofPattern("hh:mm a"))+"')";

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
}
