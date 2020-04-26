package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSAbout extends POSDashboard  implements Initializable {
    private String productV="1.0.0",bName,bAddress,bEmail,bPhone,cUser,copy;
    private String value;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Scanner scan = new Scanner(new FileInputStream("etc\\initial.file"));
            bName = scan.nextLine();
            bAddress =scan.nextLine();
            bPhone = scan.nextLine();
            bEmail = scan.nextLine();
            scan = new Scanner(new FileInputStream("etc\\cache-user.file"));
            cUser = scan.nextLine()+", "+scan.nextLine()+" "+scan.nextLine()+". "+scan.nextLine();
            copy = "Copyright (c) 2019-2020, All Rights Reserved.";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        value  = "Software Version:\n"+productV+
                "\n\nBusiness Information:\n"+bName+
                "\n"+bAddress+
                "\n"+bEmail+
                "\n"+bPhone+
                "\n\nCurrent User:\n"+cUser+
                "\n\n"+copy;
        taAbout.setText(value);
    }

    @FXML
    private StackPane rootPane;

    @FXML
    private TextArea taAbout;

    @FXML
    private JFXButton btnClose;

    @FXML
    void btnCloseOnAction(ActionEvent event) {
        misc.sceneManipulator.closeDialog();
    }
}
