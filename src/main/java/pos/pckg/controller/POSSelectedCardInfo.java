package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import pos.Main;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.data.AES;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.DataBridgeDirectory;
import pos.pckg.misc.InputRestrictor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSSelectedCardInfo extends POSCustomerAccount {

    @FXML
    private TextField tfCardID;

    @FXML
    private TextField tfBalance;

    @FXML
    private TextField tfStatus;

    @FXML
    private TextField tfActivationDate;

    @FXML
    private TextField tfExpirationDate;

    @FXML
    private JFXButton btnChangePin;

    @FXML
    private VBox vbPINContainer;

    @FXML
    private PasswordField pfOld;

    @FXML
    private PasswordField pfNew;

    @FXML
    private JFXButton btnCancel;

    @FXML
    private JFXButton btnSave;

    @FXML
    private JFXButton btnClose;

    private String pin,customerID;
    private Timeline oldPINThread,newPINThread;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InputRestrictor.numbersInput(pfOld);
        InputRestrictor.numbersInput(pfNew);
        InputRestrictor.limitInput(pfNew,6);
        InputRestrictor.limitInput(pfOld,6);
        try {
            Scanner scan = new Scanner(new FileInputStream(BackgroundProcesses.getFile(DataBridgeDirectory.DOCUMENT+"etc\\cache-card-info.file")));
            tfCardID.setText(scan.nextLine());
            tfBalance.setText(scan.nextLine());
            tfStatus.setText(scan.nextLine().equals("1")?"Active":"Inactive");
            tfActivationDate.setText(scan.nextLine());
            tfExpirationDate.setText(scan.nextLine());
            pin = scan.nextLine();
            customerID = scan.nextLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnChangePinOnAction(ActionEvent event) throws FileNotFoundException {
        vbPINContainer.setDisable(false);
        btnChangePin.setDisable(true);
        try{
            scanOldPIN();
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

    @FXML
    void btnCloseOnAction(ActionEvent event) {
        sceneManipulator.closeDialog();
    }

    @FXML
    void pinFunctionsOnAction(ActionEvent actionEvent) {
        if (actionEvent.getSource().equals(this.btnSave))doUpdate();
        pfNew.setText("");
        pfOld.setText("");
        btnChangePin.setDisable(false);
        vbPINContainer.setDisable(true);
    }

    private Scanner scan;
    private String forChallenge;
    private void scanOldPIN() throws FileNotFoundException {
        scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-card-info.file"));
        for (int i  = 1; i<=5;i++) System.out.println(scan.nextLine());
        forChallenge= AES.decrypt(scan.nextLine(), POSCustomerAccount.S_KEY);//TODO Under observation
        Main.rfid.PINChallenge(forChallenge);

        oldPINThread = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            try {
                scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\rfid-cache.file"));
                while (scan.hasNextLine()){
                    String scanned[] = scan.nextLine().split("=");
                    if (scanned[0].equals("PINChallenge")){
                        if (scanned[1].equals("1")){
                            pfOld.setText(forChallenge);
                            Main.rfid.clearCache();
                            scanNewPIN();
                            oldPINThread.stop();
                            break;
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                oldPINThread.stop();
            }

        }),
                new KeyFrame(Duration.seconds(1))
        );
        oldPINThread.setCycleCount(Animation.INDEFINITE);
        oldPINThread.play();
    }

    private void scanNewPIN(){
        Main.rfid.PINCreate();
        newPINThread = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            try {
                Scanner scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\rfid-cache.file"));
                while (scan.hasNextLine()){
                    String []scanned = scan.nextLine().split("=");
                    if (scanned[0].equals("PINCreate")){
                        pfNew.setText(scanned[1]);
                        Main.rfid.clearCache();
                        newPINThread.stop();
                        break;
                    }
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }),
                new KeyFrame(Duration.seconds(1))
        );
        newPINThread.setCycleCount(Animation.INDEFINITE);
        newPINThread.play();
    }

    private void doUpdate(){
        String sql = "Update card set PIN='"+ AES.encrypt(pfNew.getText(), POSCustomerAccount.S_KEY)+"' where cardID = '"+tfCardID.getText()+"';";
        misc.dbHandler.startConnection();
        misc.dbHandler.execUpdate(sql);
        misc.dbHandler.closeConnection();

        Date d = new Date();
        SimpleDateFormat date = new SimpleDateFormat(BackgroundProcesses.DATE_FORMAT);
        sql = "INSERT INTO systemlogs(type, eventAction, date, userID, referencedID)" +
                " VALUES ( 'Customer Management'" +
                ", 'Change PIN'" +
                ", '" + date.format(d) + "'" +
                ", '" + POSCustomerAccount.userID + "'" +
                ", '" + customerID + "');";

        misc.dbHandler.startConnection();
        misc.dbHandler.execUpdate(sql);
        misc.dbHandler.closeConnection();

        JFXButton close = new JFXButton("Close");
        close.setOnAction(e->{
            POSCustomerAccount.queryAllItems();
            POSMessage.closeMessage();
        });
        POSMessage.showConfirmationMessage(rootPane,"Card PIN is now updated","Update Success"
                , POSMessage.MessageType.INFORM,close);
    }
}
