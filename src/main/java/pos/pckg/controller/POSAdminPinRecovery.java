package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.misc.DataBridgeDirectory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSAdminPinRecovery extends POSAdminUser implements Initializable {

    @FXML
    private StackPane rootPane;

    @FXML
    private TextField tfUid;

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

                String sql = "Update User set password = md5('"+pfNew.getText()+"') where userID = '"+tfUid.getText()+"'";

                misc.dbHandler.startConnection();
                misc.dbHandler.execUpdate(sql);
                misc.dbHandler.closeConnection();


                JFXButton btnOk = new JFXButton("Ok");
                btnOk.setOnAction(ev -> {
                    POSMessage.closeMessage();
                    queryAllItems();
                    misc.sceneManipulator.closeDialog();
                });

                POSMessage.showConfirmationMessage(rootPane,
                        "Password has been updated",
                        "Update Success",
                        POSMessage.MessageType.INFORM,btnOk);

            });

            POSMessage.showConfirmationMessage(rootPane,"Do you really want to change the user's\npassword?"
                    ,"Please Confirm Update", POSMessage.MessageType.CONFIRM,btnNo,btnYes);

        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Scanner scan = null;
        try {
            scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-admin-selected-user.file"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        tfUid.setText(scan.nextLine());
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
}
