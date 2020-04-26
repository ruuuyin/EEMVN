package pos.pckg.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSSystemLogsView extends POSSystemLogs implements Initializable {

    @FXML
    private Label lblLogID;

    @FXML
    private TextField tfType;

    @FXML
    private TextField tfAction;

    @FXML
    private TextField tfDate;

    @FXML
    private TextField tfUser;

    @FXML
    private TextField tfRefID;

    @FXML
    private TextField tfName;

    @FXML
    void btnCloseOnAction(ActionEvent event) {
        sceneManipulator.closeDialog();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Scanner scan = new Scanner(new FileInputStream("etc\\cache-sl-view.file"));
            lblLogID.setText(scan.nextLine());
            tfType.setText(scan.nextLine());
            tfAction.setText(scan.nextLine());
            tfDate.setText(scan.nextLine());
            tfUser.setText(scan.nextLine());
            tfRefID.setText(scan.nextLine());
            tfName.setText(scan.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
