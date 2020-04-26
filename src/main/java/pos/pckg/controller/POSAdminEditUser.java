package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import pos.pckg.controller.message.POSMessage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSAdminEditUser extends POSAdminUser implements Initializable {

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

        if (hasEmptyField()) {
            POSMessage.showConfirmationMessage(rootPane, "Please fill all the required fields",
                    "Invalid Value", POSMessage.MessageType.ERROR, msgCloser);
        } else if (!hasSelectedAccess()) {
            POSMessage.showConfirmationMessage(rootPane, "Please select at least one accessible\nfeature",
                    "No Selected Access", POSMessage.MessageType.ERROR, msgCloser);
        }else{
            JFXButton btnNo = new JFXButton("No");
            btnNo.setOnAction(e-> POSMessage.closeMessage());

            JFXButton btnYes = new JFXButton("Yes");
            btnYes.setOnAction(e->{

                POSMessage.closeMessage();
                JFXCheckBox cBoxes[] = {cbCashier, chInventory, chCustomer, chTransactions, chSystemLogs, chAdmin};
                String access="";
                for (JFXCheckBox cBox : cBoxes) if (cBox.isSelected()) access += (cBox.getId() + ",");
                //This is where the update will process
                String sql = "Update User set " +
                        "firstName = '" + tfFirstName.getText() + "', " +
                        "middleInitial = '" + tfMiddleInitial.getText() + "'," +
                        "lastName = '" + tfLastName.getText() + "'," +
                        "access = '" + access + "'" +
                        " where userID = " + tfUid.getText();//Basis for the update is the Id of the selected item

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
                        "User "+tfUid.getText()+" is now updated",
                        "Update Success",
                        POSMessage.MessageType.INFORM,btnOk);

            });

            POSMessage.showConfirmationMessage(rootPane,"Do you really want to update the user?"
                    ,"Please Confirm Update", POSMessage.MessageType.CONFIRM,btnNo,btnYes);

        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Scanner scan = new Scanner(new FileInputStream("etc\\cache-admin-selected-user.file"));
            tfUid.setText(scan.nextLine());
            tfFirstName.setText(scan.nextLine());
            tfMiddleInitial.setText(scan.nextLine());
            tfLastName.setText(scan.nextLine());

            String accessArray[] = scan.nextLine().split(" ");

            for (String access:accessArray) {
                switch (access.toLowerCase()){
                    case "cashier":
                        cbCashier.setSelected(true);
                        break;
                    case "inventory":
                        chInventory.setSelected(true);
                        break;
                    case "customer":
                        chCustomer.setSelected(true);
                        break;
                    case "transaction":
                        chTransactions.setSelected(true);
                        break;
                    case "system":
                        chSystemLogs.setSelected(true);
                        break;
                    case "admin":
                        chAdmin.setSelected(true);
                        break;
                    default:
                        continue;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private boolean hasEmptyField() {
        return tfFirstName.getText().equals("") ||
                tfLastName.getText().equals("");
    }

    private boolean hasSelectedAccess() {
        JFXCheckBox cBoxes[] = {cbCashier, chInventory, chCustomer, chTransactions, chSystemLogs, chAdmin};
        for (JFXCheckBox checkBox : cBoxes) if (checkBox.isSelected()) return true;
        return false;
    }
}
