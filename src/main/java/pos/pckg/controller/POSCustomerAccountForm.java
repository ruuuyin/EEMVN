package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.DataBridgeDirectory;
import pos.pckg.misc.InputRestrictor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class POSCustomerAccountForm extends POSCustomerAccount {

    @FXML
    private StackPane rootPane;

    @FXML
    private TextField tfFirstName;

    @FXML
    private TextField tfMiddleInitial;

    @FXML
    private TextField tfLastName;

    @FXML
    private JFXRadioButton rbMale;

    @FXML
    private JFXRadioButton rbFemale;

    @FXML
    private ToggleGroup sex;

    @FXML
    private TextField tfMobileNumber;

    @FXML
    private TextField tfEmailAddress;

    @FXML
    private TextField tfAddress;

    @FXML
    private JFXButton btnCancel;

    @FXML
    private JFXButton btnCreate;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InputRestrictor.limitInput(tfMobileNumber,11);
        InputRestrictor.numbersInput(tfMobileNumber);
        InputRestrictor.limitInput(tfMiddleInitial,3);
        BackgroundProcesses.createCacheDir(DataBridgeDirectory.DOCUMENT+"etc\\cache-new-account.file");
    }

    @FXML
    void sexOptionOnAction(ActionEvent event) {

    }

    @FXML
    void btnCreateOnAction(ActionEvent event) throws IOException {

        if (hasEmptyField()){
            POSMessage.showMessage(rootPane,"Please fill all the required fields","Invalid Value", POSMessage.MessageType.ERROR);
        }else if (!mobileIsValid()){
            POSMessage.showMessage(rootPane,"The mobile number you've entered is invalid","Invalid Value", POSMessage.MessageType.ERROR);
        }else{
            String newAcc = "";
            BufferedWriter writer = new BufferedWriter(new FileWriter(BackgroundProcesses.getFile(DataBridgeDirectory.DOCUMENT+"etc\\cache-new-account.file")));
            newAcc += tfFirstName.getText();
            newAcc += "\n"+(tfMiddleInitial.getText().equals("")?"N/A":tfMiddleInitial.getText());
            newAcc += "\n"+tfLastName.getText();
            newAcc += "\n" + tfAddress.getText();
            newAcc += "\n" + (tfEmailAddress.getText().equals("") ? "N/A" : tfEmailAddress.getText());
            newAcc += "\n" + tfMobileNumber.getText();
            newAcc += "\n"+(rbMale.isSelected() ? "Male" : "Female");

            writer.write(newAcc);
            writer.close();
            sceneManipulator.closeDialog();
            sceneManipulator.openDialog((StackPane) sceneManipulator.getDialogController(),"POSCardInformation");
        }

    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        sceneManipulator.closeDialog();
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
}

