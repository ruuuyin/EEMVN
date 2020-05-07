package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import pos.Main;
import pos.pckg.MiscInstances;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.data.AES;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.DataBridgeDirectory;
import pos.pckg.misc.InputRestrictor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSCardInformation extends POSCustomerAccount implements Initializable {

    @FXML
    private StackPane rootPane;

    @FXML
    private TextField tfCardID;

    @FXML
    private TextField tfInitialBalance;

    @FXML
    private PasswordField pfPIN;

    @FXML
    private PasswordField pfConfirm;

    @FXML
    private JFXButton btnCancel;

    @FXML
    private JFXButton btnDone;

    private MiscInstances misc = new MiscInstances();
    private Timeline cardIdScannerThread;
    private Timeline pinThread = null;
    private double minimum = 0;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InputRestrictor.numbersInput(tfInitialBalance);
        initCardScan();
        try {
            Scanner scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-others.file"));
            minimum = Double.parseDouble(scan.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        tfInitialBalance.setPromptText("Required (Min. "+minimum+")");
    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        try{
            Main.rfid.cancelOperation();
        }catch (Exception e){
            e.printStackTrace();
        }

        sceneManipulator.closeDialog();
    }

    @FXML
    void btnCreateOnAction(ActionEvent event) {
        JFXButton msgCloser = new JFXButton("Close");
        msgCloser.setOnAction(e -> {
            POSMessage.closeMessage();
        });
        if (hasEmptyField()) {
            POSMessage.showMessage(rootPane, "Please fill all the required fields", "Invalid Value", POSMessage.MessageType.ERROR);
        } else if (Integer.parseInt(tfInitialBalance.getText()) < minimum) {
            POSMessage.showConfirmationMessage(rootPane, "Please Insert at least\n" + minimum + " initial Balance"
                    , "Invalid Value", POSMessage.MessageType.ERROR, msgCloser);
        } else {
            JFXButton btnNo = new JFXButton("No");// Confirmation button - "No"
            btnNo.setOnAction(e -> POSMessage.closeMessage());// After pressing the No button, it simply close the message

            JFXButton btnYes = new JFXButton("Yes");// Confirmation button - "Yes"
            btnYes.setOnAction(e -> {
                try {
                    String sql = "Select cardID from card where cardID = '" + tfCardID.getText() + "'";
                    misc.dbHandler.startConnection();
                    if (misc.dbHandler.execQuery(sql).next()) {
                        JFXButton btnOk = new JFXButton("Rescan");
                        btnOk.setOnAction(rescan->{
                            tfCardID.setText("");
                            pfPIN.setText("");
                            initCardScan();
                            POSMessage.closeMessage();
                        });
                        POSMessage.closeMessage();
                        POSMessage.showConfirmationMessage(rootPane,"The card is already exist",
                                "Insert failed", POSMessage.MessageType.ERROR,btnOk);//TODO Need to test
                    }else {
                        POSMessage.closeMessage();
                        doInsertion();
                    }
                    misc.dbHandler.closeConnection();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // Confirmation Message
            POSMessage.showConfirmationMessage(rootPane, "Do your really want to create\nnew account?"
                    , "Please Confirm", POSMessage.MessageType.INFORM, btnNo, btnYes);

        }
    }



    private void scanForPIN(){
        Main.rfid.PINCreate();
         pinThread = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            try {
                Scanner scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\rfid-cache.file"));
                while (scan.hasNextLine()){
                    String []scanned = scan.nextLine().split("=");
                    if (scanned[0].equals("PINCreate")){
                        pfPIN.setText(scanned[1]);
                        Main.rfid.clearCache();
                        pinThread.stop();
                        break;
                    }
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }

        }),
                new KeyFrame(Duration.seconds(1))
        );
        pinThread.setCycleCount(Animation.INDEFINITE);
        pinThread.play();
    }

    private boolean hasEmptyField(){
        return tfCardID.getText().equals("") || pfPIN.getText().equals("") ||
                tfInitialBalance.getText().equals("");
    }

    private final void doInsertion() throws FileNotFoundException, SQLException {
        Scanner scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-new-account.file"));
        String firstName,middleInitial,lastName,address,email,mobile,sex;
        firstName = scan.nextLine();
        middleInitial = scan.nextLine();
        lastName = scan.nextLine();
        address = scan.nextLine();
        email = scan.nextLine();
        mobile = scan.nextLine();
        sex = scan.nextLine();
        String sql = "Insert into customer(firstname,middleInitial,lastName,address,emailAddress,phoneNumber,sex) values ("
                + "'"+firstName+"',"
                + "'"+middleInitial+"',"
                + "'"+lastName+"',"
                + "'"+address+"',"
                + "'"+email+"',"
                + "'"+mobile+"',"
                + "'"+sex+"')";

        misc.dbHandler.startConnection();
        misc.dbHandler.execUpdate(sql);
        misc.dbHandler.closeConnection();


        sql = "Select max(customerID) as cusID from customer";

        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);

        int id = 0;
        if (result.next()){
            id = result.getInt("cusID");
        }
        misc.dbHandler.closeConnection();

        Date d = new Date();
        SimpleDateFormat date = new SimpleDateFormat(BackgroundProcesses.DATE_FORMAT);
        String cardID, PIN, activation, expiry;
        cardID = tfCardID.getText();
        PIN = pfPIN.getText();
        activation  = date.format(d);
        date = new SimpleDateFormat("MM-dd-");
        expiry = date.format(d)+((d.getYear()+1900)+1);

        sql = "Insert into card(cardID,PIN,activationDate,expiryDate,credits,customerID) values("
                + "'" +cardID+ "',"
                + "'" + AES.encrypt(PIN,S_KEY) + "',"
                + "'" +activation+ "',"
                + "'" +expiry+ "',"
                + Double.parseDouble(tfInitialBalance.getText())+","
                + id+ ")";

        misc.dbHandler.startConnection();
        misc.dbHandler.execUpdate(sql);
        misc.dbHandler.closeConnection();

        date = new SimpleDateFormat(BackgroundProcesses.DATE_FORMAT);
        sql = "INSERT INTO systemlogs(type, eventAction, date, userID, referencedID)" +
                " VALUES ( 'Customer Management'" +
                ", 'Add'" +
                ", '" + date.format(d) + "'" +
                ", '" + POSCustomerAccount.userID + "'" +
                ", " + id + ");";

        misc.dbHandler.startConnection();
        misc.dbHandler.execUpdate(sql);
        misc.dbHandler.closeConnection();

        JFXButton close = new JFXButton("Close");
        close.setOnAction(e->{
            POSCustomerAccount.queryAllItems();
            POSMessage.closeMessage();
            sceneManipulator.closeDialog();
        });
        POSMessage.showConfirmationMessage(rootPane,"New account has been added","Registration Successful"
                , POSMessage.MessageType.INFORM,close);
    }

    private void initCardScan(){
        try {
            Main.rfid.scan();
            cardIdScannerThread = new Timeline(new KeyFrame(Duration.ZERO, e -> {
                try {
                    Scanner scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\rfid-cache.file"));
                    while (scan.hasNextLine()){
                        String scanned[] = scan.nextLine().split("=");
                        if (scanned[0].equals("scan")){
                            tfCardID.setText(scanned[1]);
                            Main.rfid.clearCache();
                            scanForPIN();
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

}
