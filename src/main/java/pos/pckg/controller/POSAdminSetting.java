package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import pos.pckg.MiscInstances;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.misc.DataBridgeDirectory;
import pos.pckg.misc.InputRestrictor;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSAdminSetting extends POSAdminPanel implements Initializable {
    private static MiscInstances misc = new MiscInstances();
    @FXML
    private StackPane rootPane;

    @FXML
    private VBox contBusiness;

    @FXML
    private TextField tfBName;

    @FXML
    private TextField tfBAddress;

    @FXML
    private TextField tfBEmail;

    @FXML
    private TextField tfBPhone;

    @FXML
    private JFXButton btnBCancel;

    @FXML
    private JFXButton btnBEdit;

    @FXML
    private VBox contAdmin;

    @FXML
    private TextField tfAUserId;

    @FXML
    private TextField tfAFirstName;

    @FXML
    private TextField tfAMiddleInitial;

    @FXML
    private TextField tfLastName;

    @FXML
    private JFXButton btnACancel;

    @FXML
    private JFXButton btnAEdit;

    @FXML
    private VBox contOthers;

    @FXML
    private TextField tfBalance;

    @FXML
    private JFXButton btnOCancel;

    @FXML
    private JFXButton btnOEdit;
    private String isAdmin, access;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Scanner scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\initial.file"));
            tfBName.setText(scan.nextLine());
            tfBAddress.setText(scan.nextLine());
            tfBPhone.setText(scan.nextLine());
            tfBEmail.setText(scan.nextLine());
            scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-user.file"));
            tfAUserId.setText(scan.nextLine());
            tfAFirstName.setText(scan.nextLine());
            tfAMiddleInitial.setText(scan.nextLine());
            tfLastName.setText(scan.nextLine());
            isAdmin = scan.nextLine();
            access = scan.nextLine();
            scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-others.file"));
            tfBalance.setText(scan.nextLine());
            InputRestrictor.limitInput(tfAMiddleInitial, 1);
            InputRestrictor.limitInput(tfBPhone, 11);
            InputRestrictor.numbersInput(tfBPhone);
            InputRestrictor.numbersInput(tfBalance);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnMainOnAction(ActionEvent event) {
        JFXButton btn = (JFXButton) event.getSource();
        String btnValue = btn.getText();

        if (btn.equals(btnBEdit) && btnValue.equals("Edit")) {
            btnBEdit.setText("Save");
            btnBCancel.setDisable(false);
            contBusiness.setDisable(false);
        } else if (btn.equals(btnAEdit) && btnValue.equals("Edit")) {
                btnAEdit.setText("Save");
                contAdmin.setDisable(false);
                btnACancel.setDisable(false);
        } else if (btn.equals(btnOEdit) && btnValue.equals("Edit")) {
            btnOEdit.setText("Save");
            contOthers.setDisable(false);
            btnOCancel.setDisable(false);
        } else if (btn.equals(btnBEdit) && btnValue.equals("Save")) {
            EditBusinessInformation();
        } else if (btn.equals(btnAEdit) && btnValue.equals("Save")) {
            EditAdmin();
        } else if (btn.equals(btnOEdit) && btnValue.equals("Save")) {
            EditMinimumBalance();
        } else if (btn.equals(btnBCancel)) {
            btnBEdit.setText("Edit");
            btnBCancel.setDisable(true);
            contBusiness.setDisable(true);
        } else if (btn.equals(btnACancel)) {
            btnAEdit.setText("Edit");
            btnACancel.setDisable(true);
            contAdmin.setDisable(true);
        } else if (btn.equals(btnOCancel)) {
            btnOEdit.setText("Edit");
            btnOCancel.setDisable(true);
            contOthers.setDisable(true);
        }


    }

    private void EditBusinessInformation() {
        if (BhasEmptyField()) {
            POSMessage.showMessage(rootPane, "Please fill all the important fields", "Invalid Value", POSMessage.MessageType.ERROR);
        } else if (!mobileIsValid()) {
            POSMessage.showMessage(rootPane, "The mobile number you've entered is invalid", "Invalid Value", POSMessage.MessageType.ERROR);
        } else {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(DataBridgeDirectory.DOCUMENT+"etc\\initial.file"));
                String business = "";
                business += tfBName.getText() + "\n" +
                        tfBAddress.getText() + "\n" +
                        tfBPhone.getText() + "\n" +
                        (tfBEmail.getText().equals("") ? "N/A" : tfBEmail.getText());
                writer.write(business);
                writer.close();
                btnBEdit.setText("Edit");
                contBusiness.setDisable(true);
                btnBCancel.setDisable(true);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private boolean BhasEmptyField() {
        return tfBName.getText().equals("") ||
                tfBAddress.getText().equals("") ||
                tfBPhone.getText().equals("");
    }

    private boolean mobileIsValid() {
        return (tfBPhone.getText().startsWith("0") && tfBPhone.getText().length() == 11) ||
                (tfBPhone.getText().startsWith("9") && tfBPhone.getText().length() == 10);

    }

    private void EditAdmin() {
        if (AhasEmptyField()) {
            POSMessage.showMessage(rootPane, "Please fill all the important fields", "Invalid Value", POSMessage.MessageType.ERROR);
        } else {
            String sql = "Update user set firstName = " +
                    "'" + tfAFirstName.getText() + "'" +
                    ",middleInitial = " +
                    "'" + (tfAMiddleInitial.getText().equals("") ? "N/A" : tfAMiddleInitial.getText()) + "'" +
                    ",lastName = '" + tfLastName.getText() + "' where userID = '" + tfAUserId.getText() + "'";
            misc.dbHandler.startConnection();
            misc.dbHandler.execUpdate(sql);
            misc.dbHandler.closeConnection();
            BufferedWriter writer = null;
            String cacheData = "";
            cacheData += tfAUserId.getText() + "\n";
            cacheData += tfAFirstName.getText() + "\n";
            cacheData += (tfAMiddleInitial.getText().equals("") ? "N/A" : tfAMiddleInitial.getText()) + "\n";
            cacheData += tfLastName.getText() + "\n";
            cacheData += this.isAdmin + "\n";
            cacheData += this.access;

            try {
                writer = new BufferedWriter(new FileWriter(DataBridgeDirectory.DOCUMENT+"etc\\cache-user.file"));
                writer.write(cacheData);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            btnAEdit.setText("Edit");
            contAdmin.setDisable(true);
            btnACancel.setDisable(true);
        }
    }

    private boolean AhasEmptyField() {
        return tfAFirstName.getText().equals("") ||
                tfLastName.getText().equals("");
    }

    private void EditMinimumBalance() {
        if (OhasEmptyFields()) {
            POSMessage.showMessage(rootPane, "Please fill all the important fields", "Invalid Value", POSMessage.MessageType.ERROR);
        } else {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(DataBridgeDirectory.DOCUMENT+"etc\\cache-others.file"));
                String min = tfBalance.getText();
                writer.write(min);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            btnOEdit.setText("Edit");
            contOthers.setDisable(true);
            btnOCancel.setDisable(true);
        }
    }

    private boolean OhasEmptyFields() {
        return tfBalance.getText().equals("");
    }
}
