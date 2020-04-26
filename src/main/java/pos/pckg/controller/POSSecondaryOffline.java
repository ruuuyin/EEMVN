package pos.pckg.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import pos.pckg.misc.BackgroundProcesses;

import java.net.URL;
import java.util.ResourceBundle;

public class POSSecondaryOffline implements Initializable {
    @FXML
    private Label lblOffline;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        int lblLength = BackgroundProcesses.getStoreName().length();
        Font font;
        if (lblLength>=19){
            font = new Font(30);
            lblOffline.setFont(font);
        }else if (lblLength>=28){
            font = new Font(25);
            lblOffline.setFont(font);
        }


        //TODO to be set
    }
}
