package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import pos.pckg.misc.InputRestrictor;

import java.net.URL;
import java.util.ResourceBundle;

public class POSDiscount extends POSCashier implements Initializable {

    @FXML
    private StackPane rootPane;

    @FXML
    private TextField tfDiscount;

    @FXML
    private JFXButton btnClose;

    @FXML
    private JFXButton btnAddDiscount;

    @FXML
    void btnAddDiscountOnAction(ActionEvent event) {
        if (!tfDiscount.getText().equals("")){
            POSCashier.discount = Double.parseDouble(tfDiscount.getText());
            POSSecondaryMain.discount = Double.parseDouble(tfDiscount.getText());
        }
        sceneManipulator.closeDialog();
    }

    @FXML
    void btnCloseOnAction(ActionEvent event) {
        sceneManipulator.closeDialog();//get the scenemanipulator from
                                                // the parent scene and call the closeDialog
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InputRestrictor.numbersInput(tfDiscount);
        tfDiscount.setText(POSCashier.discount+"");
    }
}
