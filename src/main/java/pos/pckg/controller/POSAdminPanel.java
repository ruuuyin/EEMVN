package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import pos.pckg.MiscInstances;

import java.net.URL;
import java.util.ResourceBundle;

public class POSAdminPanel implements Initializable {
    private final String ACTIVE_STYLE = "btn-high";
    private final String INACTIVE_STYLE = "controls-container";
    private static MiscInstances misc = new MiscInstances();
    @FXML
    private StackPane rootPane;

    @FXML
    private AnchorPane apContainer;

    @FXML
    private JFXButton btnHome;

    @FXML
    private JFXButton btnUserSettings;

    @FXML
    private JFXButton btnSystemSettings;

    @FXML
    void btnHomeOnAction(ActionEvent event) {
        misc.sceneManipulator.changeScene(rootPane, "POSDashboard", " |  Dashboard");
    }

    @FXML
    void navOnAction(ActionEvent event) {
        JFXButton btn = (JFXButton) event.getSource();
        if (btn.equals(btnUserSettings)){
            misc.sceneManipulator.attachNode(apContainer,"POSAdminUser");
            switchStyle(1);
        }else{
            misc.sceneManipulator.attachNode(apContainer,"POSAdminSetting");
            switchStyle(2);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        switchStyle(1);
        misc.sceneManipulator.attachNode(apContainer,"POSAdminUser");
    }

    private void switchStyle(int btnNo){
        switch (btnNo){
            case 1:
                changeStyle(btnUserSettings,btnSystemSettings);
                break;
            case 2:
                changeStyle(btnSystemSettings,btnUserSettings);
                break;
        }
    }

    private void changeStyle(Button active,Button inactive){
        active.getStyleClass().remove(INACTIVE_STYLE);
        inactive.getStyleClass().remove(ACTIVE_STYLE);
        active.getStyleClass().add(ACTIVE_STYLE);
        inactive.getStyleClass().add(INACTIVE_STYLE);
        btnSystemSettings.setStyle("-fx-border-radius: 0 5 5 0");
        btnUserSettings.setStyle("-fx-border-radius: 5 0 0 5");

        active.setOnAction(null);
        inactive.setOnAction((e)->navOnAction(e));
    }
}
