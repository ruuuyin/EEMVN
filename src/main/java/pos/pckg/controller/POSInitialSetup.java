package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import pos.pckg.MiscInstances;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.DirectoryHandler;
import pos.pckg.misc.InputRestrictor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class POSInitialSetup implements Initializable {

    @FXML
    private StackPane rootPane;

    @FXML
    private Label titleLabel;

    @FXML
    private Label titleSubtextLabel;

    @FXML
    private TextField tfStoreName;

    @FXML
    private TextField tfStoreAddress;

    @FXML
    private TextField tfStoreNumber;

    @FXML
    private TextField tfStoreEmail;

    @FXML
    private TextField tfAccountID;

    @FXML
    private PasswordField tfPassword;

    @FXML
    private PasswordField tfConfirmPassword;

    @FXML
    private TextField tfFirstName;

    @FXML
    private TextField tfMiddleInitial;

    @FXML
    private TextField tfLastName;

    @FXML
    private JFXButton btnSave;

    MiscInstances misc = new MiscInstances();

    @FXML
    void btnSaveOnAction(ActionEvent event) throws IOException {
        if (hasEmptyValue()){
                POSMessage.showMessage(rootPane,"Please fill all the required fields","Invalid Value", POSMessage.MessageType.ERROR);
        }else if (!emailIsValid()){
                POSMessage.showMessage(rootPane,"The email you've entered is invalid","Invalid Value", POSMessage.MessageType.ERROR);
        }else if (!mobileIsValid()) {
            POSMessage.showMessage(rootPane, "The mobile number you've entered is invalid", "Invalid Value", POSMessage.MessageType.ERROR);
        }else if (!tfConfirmPassword.getText().equals(tfPassword.getText())) {
                JFXButton btnOk = new JFXButton("Ok");
                btnOk.setOnAction(evt -> {
                    POSMessage.closeMessage();
                });

                POSMessage.showConfirmationMessage(rootPane,
                        "Password didn't Matched",
                        "Invalid Value",
                        POSMessage.MessageType.ERROR, btnOk);
        }else{
            BufferedWriter writer = new BufferedWriter(new FileWriter("etc\\initial.file"));
            String business = "";
            business += tfStoreName.getText() + "\n" +
                    tfStoreAddress.getText() + "\n" +
                    tfStoreNumber.getText() + "\n" +
                    tfStoreEmail.getText() + "\n";
            writer.write(business);
            writer.close();

            writer = new BufferedWriter(new FileWriter("etc\\cache-others.file"));
            String min = "500";

            writer.write(min);
            writer.close();

            String sql = "insert into user(userID,firstname,middleInitial,lastName,accountType,password,access) values ('" + tfAccountID.getText() + "','" + tfFirstName.getText() + "'" +
                    ",'" + (tfMiddleInitial.getText().equals("") ? "N/A" : tfMiddleInitial.getText()) + "'" +
                    ",'" + tfLastName.getText() + "',1,md5('" + tfConfirmPassword.getText() + "'),'cashier,inventory,customer,transaction,system,admin')";
            misc.dbHandler.startConnection();
            misc.dbHandler.execUpdate(sql);
            misc.dbHandler.closeConnection();
            createReportDirectory();
            openLogin();
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InputRestrictor.limitInput(tfStoreNumber,11);
        InputRestrictor.numbersInput(tfStoreNumber);
        InputRestrictor.limitInput(tfMiddleInitial,3);
        Date d = new Date();
        SimpleDateFormat format = new SimpleDateFormat("YY");
        tfAccountID.setText(format.format(d)+"001");
    }

    private void openLogin(){
        Stage stage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/"+ DirectoryHandler.FXML+ "POSSecondaryMain.fxml"));

            stage.setScene(new Scene(root));
            stage.setTitle(BackgroundProcesses.getStoreName()+" | Customer View");
            stage.setMinHeight(679);
            stage.setMinWidth(1137);
            if ( Screen.getScreens().size()>1){
                Rectangle2D bounds = Screen.getScreens().get(1).getVisualBounds();
                stage.setX(bounds.getMinX() + 100);
                stage.setY(bounds.getMinY() + 100);
            }
            stage.setOnCloseRequest(e->{
                System.exit(0);
            });
            stage.setFullScreen(true);
            stage.show();


            stage = new Stage();
            root =  FXMLLoader.load(getClass().getResource("/"+ DirectoryHandler.FXML+ "POSLogin.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle(BackgroundProcesses.getStoreName()+" | Login");
            stage.setMinHeight(679);
            stage.setMinWidth(1137);
            stage.setMaximized(true);
            //stage.setFullScreen(true);
            stage.setOnCloseRequest(e->{
                System.exit(0);
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean hasEmptyValue(){
        return tfStoreName.getText().equals("") ||
                tfStoreNumber.getText().equals("") ||
                tfStoreAddress.getText().equals("") ||
                tfStoreEmail.getText().equals("") ||
                tfPassword.getText().equals("") ||
                tfConfirmPassword.getText().equals("") ||
                tfFirstName.getText().equals("") ||
                tfLastName.getText().equals("");
    }

    private boolean mobileIsValid(){
        return (tfStoreNumber.getText().startsWith("0") && tfStoreNumber.getText().length() == 11) ||
                (tfStoreNumber.getText().startsWith("9") && tfStoreNumber.getText().length() ==10);

    }

    private boolean emailIsValid(){
        return tfStoreEmail.getText().contains("@")
                && tfStoreEmail.getText().contains(".");
    }

    private void createReportDirectory(){
        File file = new File("C:\\POS-Reports\\System Logs");
        if (!file.exists()) file.mkdirs();
        file = new File("C:\\POS-Reports\\Transaction Logs");
        if (!file.exists()) file.mkdirs();
    }
}
