package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.StackPane;
import pos.pckg.controller.message.POSMessage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSPasswordManagement extends POSDashboard implements Initializable {

    @FXML
    private StackPane rootPane;

    @FXML
    private PasswordField pfOldPassword;

    @FXML
    private PasswordField pfNew;

    @FXML
    private PasswordField pfConfirm;

    @FXML
    private JFXButton btnCancel;

    @FXML
    private JFXButton btnSave;

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        misc.sceneManipulator.closeDialog();
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        JFXButton msgCloser = new JFXButton("Close");
        msgCloser.setOnAction(e -> {
            POSMessage.closeMessage();
        });
        if (hasEmptyField()){
            POSMessage.showConfirmationMessage(rootPane, "Please fill all the required fields",
                    "Invalid Value", POSMessage.MessageType.ERROR, msgCloser);
        }else if (invalidOldPin()){
            POSMessage.showConfirmationMessage(rootPane, "Invalid old Password", "Invalid Password",
                    POSMessage.MessageType.ERROR, msgCloser);
        }else if (passwordDidNotMatch()) {
            POSMessage.showConfirmationMessage(rootPane, "Password does not match", "Invalid Password",
                    POSMessage.MessageType.ERROR, msgCloser);
        }else if (passwordLengthIsInvalid()){
            POSMessage.showConfirmationMessage(rootPane, "Invalid Password Length", "Invalid Password",
                    POSMessage.MessageType.ERROR, msgCloser);
        }else{

            JFXButton btnNo = new JFXButton("No");
            btnNo.setOnAction(e->POSMessage.closeMessage());

            JFXButton btnYes = new JFXButton("Yes");
            btnYes.setOnAction(e->{

                POSMessage.closeMessage();

                String sql = "Update User set password = md5('"+pfNew.getText()+"') where userID = '"+userID+"'";

                misc.dbHandler.startConnection();
                misc.dbHandler.execUpdate(sql);
                misc.dbHandler.closeConnection();

                JFXButton btnOk = new JFXButton("Ok");
                btnOk.setOnAction(ev -> {
                    POSMessage.closeMessage();
                    misc.sceneManipulator.closeDialog();
                });

                POSMessage.showConfirmationMessage(rootPane,
                        "Password has been updated",
                        "Update Success",
                        POSMessage.MessageType.INFORM,btnOk);

            });

            POSMessage.showConfirmationMessage(rootPane,"Do you really want to change the your\npassword?"
                    ,"Please Confirm Update", POSMessage.MessageType.CONFIRM,btnNo,btnYes);

        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Scanner scan = null;
        try {
            scan = new Scanner(new FileInputStream("etc\\cache-user.file"));
            userID = scan.nextLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String sql = "Select password from user where userID = '"+userID+"'";
        misc.dbHandler.startConnection();
        ResultSet res =  misc.dbHandler.execQuery(sql);

        try {
            res.next();
            oldPassword = res.getString("password");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        misc.dbHandler.closeConnection();
    }

    private boolean hasEmptyField() {
        return pfConfirm.getText().equals("") ||
                pfNew.getText().equals("");
    }



    private boolean passwordDidNotMatch() {
        return !pfNew.getText().equals(pfConfirm.getText());
    }

    private boolean passwordLengthIsInvalid(){
        return pfNew.getText().length()<5 || pfConfirm.lengthProperty().get()<5;
    }
    private String userID = "";
    private String oldPassword = "";

    private boolean invalidOldPin(){
        String sql = "Select md5('"+pfOldPassword.getText()+"') as pass from user";
        misc.dbHandler.startConnection();
        ResultSet res =  misc.dbHandler.execQuery(sql);
        String pass = "";
        try {
            res.next();
            pass = res.getString("pass");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        misc.dbHandler.closeConnection();

        return !pass.equals(oldPassword);
    }
}
