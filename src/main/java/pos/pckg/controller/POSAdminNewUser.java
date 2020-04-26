package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.misc.InputRestrictor;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class POSAdminNewUser extends POSAdminUser implements Initializable {

    @FXML
    private StackPane rootPane;

    @FXML
    private TextField tfUid;

    @FXML
    private TextField tfFirstName;

    @FXML
    private TextField tfMiddleInitial;

    @FXML
    private TextField tfLastName;

    @FXML
    private JFXCheckBox cbCashier;

    @FXML
    private JFXCheckBox chInventory;

    @FXML
    private JFXCheckBox chCustomer;

    @FXML
    private JFXCheckBox chTransactions;

    @FXML
    private JFXCheckBox chSystemLogs;

    @FXML
    private JFXCheckBox chAdmin;

    @FXML
    private PasswordField pfNew;

    @FXML
    private PasswordField pfConfirm;

    @FXML
    private JFXButton btnCancel;

    @FXML
    private JFXButton btnCreate;

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        misc.sceneManipulator.closeDialog();
    }

    @FXML
    void btnCreateOnAction(ActionEvent event) {
        JFXButton msgCloser = new JFXButton("Close");
        msgCloser.setOnAction(e -> {
            POSMessage.closeMessage();
        });
        if (hasEmptyField()) {
            POSMessage.showConfirmationMessage(rootPane, "Please fill all the required fields",
                    "Invalid Value", POSMessage.MessageType.ERROR, msgCloser);
        } else if (!hasSelectedAccess()) {
            POSMessage.showConfirmationMessage(rootPane, "Please select at least one accessible\nfeature",
                    "No Selected Access", POSMessage.MessageType.ERROR, msgCloser);
        } else if (passwordDidNotMatch()) {
            POSMessage.showConfirmationMessage(rootPane, "Password does not match", "Invalid Password",
                    POSMessage.MessageType.ERROR, msgCloser);
        } else if (pfNew.getText().length() < 5) {
            POSMessage.showConfirmationMessage(rootPane, "Please check the character length of\nthe password",
                    "Invalid Password", POSMessage.MessageType.ERROR, msgCloser);
        } else {
            JFXCheckBox cBoxes[] = {cbCashier, chInventory, chCustomer, chTransactions, chSystemLogs, chAdmin};
            String userId, fname, mi, lname, password, access = "";
            userId = tfUid.getText();
            fname = tfFirstName.getText();
            mi = tfMiddleInitial.getText();
            lname = tfLastName.getText();
            password = pfConfirm.getText();
            for (JFXCheckBox cBox : cBoxes) if (cBox.isSelected()) access += (cBox.getId() + ",");
            access = access.substring(0, access.length() - 1);
            String sql = "Insert into User(UserId,firstName,middleInitial,lastName,password,access,accountType)" +
                    " values('" + userId + "','" + fname + "','" + mi + "','" + lname + "',md5('" + password + "'),'" + access + "',0)";
            misc.dbHandler.startConnection();
            misc.dbHandler.execUpdate(sql);
            misc.dbHandler.closeConnection();
            JFXButton close = new JFXButton("Close");
            close.setOnAction(e -> {
                POSAdminUser.queryAllItems();
                POSMessage.closeMessage();
                misc.sceneManipulator.closeDialog();
            });
            POSMessage.showConfirmationMessage(rootPane, "New account has been added", "Registration Successful"
                    , POSMessage.MessageType.INFORM, close);
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InputRestrictor.limitInput(tfMiddleInitial, 1);
        try {
            String sql = "select userID from user ORDER BY userID DESC LIMIT 1; ";
            misc.dbHandler.startConnection();
            ResultSet result = misc.dbHandler.execQuery(sql);
            result.next();
            tfUid.setText((Integer.parseInt(result.getString("userID")) + 1) + "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        misc.dbHandler.closeConnection();

    }


    private boolean hasEmptyField() {
        return tfFirstName.getText().equals("") ||
                tfLastName.getText().equals("") ||
                pfConfirm.getText().equals("") ||
                pfNew.getText().equals("");
    }

    private boolean hasSelectedAccess() {
        JFXCheckBox cBoxes[] = {cbCashier, chInventory, chCustomer, chTransactions, chSystemLogs, chAdmin};
        for (JFXCheckBox checkBox : cBoxes) if (checkBox.isSelected()) return true;
        return false;
    }

    private boolean passwordDidNotMatch() {
        return !pfNew.getText().equals(pfConfirm.getText());
    }
}
