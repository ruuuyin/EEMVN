package pos.pckg.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import pos.pckg.misc.DataBridgeDirectory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSTransactionAddBalance extends POSTransactionLogs implements Initializable {

    @FXML
    private Label lblTransactionNo;

    @FXML
    private TextField tfType;

    @FXML
    private TextField tfUser;

    @FXML
    private TextField tfCustomer;

    @FXML
    private TextField tfDate;

    @FXML
    private TextField tfTime;

    @FXML
    private TextField tfOrder;

    @FXML
    private TextField tfAmount;

    @FXML
    private TextField tfCard;

    @FXML
    void btnCloseOnAction(ActionEvent event) {
        sceneManipulator.closeDialog();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Scanner scan = null;
        try {
            scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-tl-view.file"));
            lblTransactionNo.setText(scan.nextLine());
            tfType.setText(scan.nextLine());
            tfUser.setText(scan.nextLine());
            tfCustomer.setText(scan.nextLine());
            tfDate.setText(scan.nextLine());
            tfTime.setText(scan.nextLine());
            tfOrder.setText(scan.nextLine());
            tfAmount.setText(scan.nextLine());
            tfCard.setText(scan.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
