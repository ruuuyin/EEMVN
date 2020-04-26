package pos.pckg.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.misc.BackgroundProcesses;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSSimpleMessage extends POSMessage implements Initializable {

    @FXML
    private Label lblTitle;

    @FXML
    private ImageView ivIcon;

    @FXML
    private Label lblMessage;

    @FXML
    void okBtnOnAction(ActionEvent event) {
        POSMessage.sceneManipulator.closeDialog();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Scanner scan = new Scanner(new FileInputStream(BackgroundProcesses.getFile("etc\\cache-message.file")));
            lblTitle.setText(scan.nextLine());
            lblMessage.setText(scan.nextLine());
            ivIcon.setImage(new Image(scan.nextLine()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
