package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.InputRestrictor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSCustomerEdit extends POSCustomerAccount {


    @FXML
    private StackPane rootPane;

    @FXML
    private Label lblCustomerID;

    @FXML
    private TextField tfFirstName;

    @FXML
    private TextField tfMiddleInitial;

    @FXML
    private TextField tfLastName;

    @FXML
    private JFXRadioButton rbMale;

    @FXML
    private ToggleGroup sex;

    @FXML
    private JFXRadioButton rbFemale;

    @FXML
    private TextField tfMobileNumber;

    @FXML
    private TextField tfEmailAddress;

    @FXML
    private TextField tfAddress;

    @FXML
    private JFXButton btnCancel;

    @FXML
    private JFXButton btnSave;

    private int customerID=0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        InputRestrictor.limitInput(tfMobileNumber,11);
        InputRestrictor.numbersInput(tfMobileNumber);
        InputRestrictor.limitInput(tfMiddleInitial,3);
        try {
            Scanner scan = new Scanner(new FileInputStream(BackgroundProcesses.getFile("etc\\cache-selected-customer.file")));
            customerID = Integer.parseInt(scan.nextLine());
            lblCustomerID.setText("ID : "+customerID);
            tfFirstName.setText(scan.nextLine());
            tfMiddleInitial.setText(scan.nextLine());
            tfLastName.setText(scan.nextLine());
            if(scan.nextLine().equals("Male"))
                rbMale.setSelected(true);
            else
                rbFemale.setSelected(false);

            tfAddress.setText(scan.nextLine());
            tfMobileNumber.setText(scan.nextLine());
            tfEmailAddress.setText(scan.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        sceneManipulator.closeDialog();
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        if (nothingHasChanged()){
            sceneManipulator.closeDialog();
        }else if (hasEmptyField()){
            POSMessage.showMessage(rootPane,"Please fill all the required fields","Invalid Value", POSMessage.MessageType.ERROR);
        }else if (!emailIsValid()){
            POSMessage.showMessage(rootPane,"The email you've entered is invalid","Invalid Value", POSMessage.MessageType.ERROR);
        }else if (!mobileIsValid()){
            POSMessage.showMessage(rootPane,"The mobile number you've entered is invalid","Invalid Value", POSMessage.MessageType.ERROR);
        }else{

            //When the function is pressed, a confirmation message will appear

            JFXButton btnNo = new JFXButton("No");// Confirmation button - "No"
            btnNo.setOnAction(ev -> POSMessage.closeMessage());// After pressing the No button, it simply close the messgae

            JFXButton btnYes = new JFXButton("Yes");// Confirmation button - "Yes"
            btnYes.setOnAction(ev -> {

                String sql = "Update Customer Set firstName = '"+tfFirstName.getText()+"',"+
                        "middleInitial = '"+tfMiddleInitial.getText()+"',"+
                        "lastName = '"+tfLastName.getText()+"',"+
                        "sex = '"+(rbMale.isSelected()?"Male":"Female")+"',"+
                        "address = '"+tfAddress.getText()+"',"+
                        "phonenumber = '"+tfMobileNumber.getText()+"',"+
                        "emailAddress = '"+tfEmailAddress.getText()+"'"+
                        "Where customerID = "+this.customerID;
                misc.dbHandler.startConnection();
                misc.dbHandler.execUpdate(sql);
                misc.dbHandler.closeConnection();

                Date d = new Date();
                SimpleDateFormat date = new SimpleDateFormat(BackgroundProcesses.DATE_FORMAT);
                sql = "INSERT INTO systemlogs(type, eventAction, date, userID, referencedID)" +
                        " VALUES ( 'Customer Management'" +
                        ", 'Edit'" +
                        ", '" + date.format(d) + "'" +
                        ", '" + POSCustomerAccount.userID + "'" +
                        ", " + this.customerID + ");";

                misc.dbHandler.startConnection();
                misc.dbHandler.execUpdate(sql);
                misc.dbHandler.closeConnection();

                POSMessage.closeMessage();

                JFXButton btnOk = new JFXButton("Ok");
                btnOk.setOnAction(e->{
                    POSMessage.closeMessage();
                    queryAllItems();
                    sceneManipulator.closeDialog();
                });

                POSMessage.showConfirmationMessage(super.rootPane,
                        "Customer "+customerID+" is now updated",
                        "Update Success",
                        POSMessage.MessageType.INFORM,btnOk);
            });

            // Confirmation Message
            POSMessage.showConfirmationMessage(rootPane,"Do you really want to update \ncustomer "+customerID+"?"
                    ,"Please Confirm Update", POSMessage.MessageType.CONFIRM,btnNo,btnYes);
        }

    }

    @FXML
    void sexOptionOnAction(ActionEvent event) {

    }


    private boolean hasEmptyField(){
        return tfFirstName.getText().equals("") ||
                tfLastName.getText().equals("") ||
                tfMobileNumber.getText().equals("") ||
                tfAddress.getText().equals("") ||
                !(rbFemale.isSelected() || rbMale.isSelected());
    }

    private boolean mobileIsValid(){
        return (tfMobileNumber.getText().startsWith("0") && tfMobileNumber.getText().length() == 11) ||
                (tfMobileNumber.getText().startsWith("9") && tfMobileNumber.getText().length() ==10);

    }

    private boolean emailIsValid(){
        return tfEmailAddress.getText().contains("@")
                && tfEmailAddress.getText().contains(".");
    }

    private boolean nothingHasChanged(){
        boolean nothingHasChanged = false ;
        try {
            Scanner scan = new Scanner(new FileInputStream(BackgroundProcesses.getFile("etc\\cache-selected-customer.file")));
            scan.nextLine();
            nothingHasChanged=  (tfFirstName.getText().equals(scan.nextLine()) &&
            tfMiddleInitial.getText().equals(scan.nextLine()) &&
            tfLastName.getText().equals(scan.nextLine()) &&
            (rbMale.isSelected()?"Male":"Female").equals(scan.nextLine()) &&
            tfAddress.getText().equals(scan.nextLine()) &&
            tfMobileNumber.getText().equals(scan.nextLine()) &&
            tfEmailAddress.getText().equals(scan.nextLine()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return nothingHasChanged;
    }

}
