package pos.pckg.controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

public class POSDialogContainer{
    private Pane pane = null;
    private boolean backgroundClosable = true;
    private StackPane rootPane;

    @FXML
    private HBox hbDialogContainer;

    @FXML
    private AnchorPane apDialogBase;

    @FXML
    private VBox vbDialogContainer;

    public void close(){
        Pane parent = (Pane) apDialogBase.getParent();
        parent.getChildren().remove(apDialogBase);
    }

    public void apDialogBaseClickClose(MouseEvent mouseEvent) {
        if (!mouseEvent.getSource().equals(vbDialogContainer)&&isBackgroundClosable())close();
    }

    public void setBackgroundClosable(boolean closable){
        this.backgroundClosable = closable;
    }

    public void setDialogPane(Pane dialog){
        this.pane = dialog;
        hbDialogContainer.setPrefHeight(pane.getHeight());
        hbDialogContainer.setMaxHeight(pane.getMaxHeight());
        hbDialogContainer.setMinHeight(pane.getMinHeight());
        vbDialogContainer.setPrefWidth(pane.getWidth());
        vbDialogContainer.setMaxWidth(pane.getMaxWidth());
        vbDialogContainer.setMinWidth(pane.getMinWidth());
        vbDialogContainer.getChildren().add(pane);
    }

    public Pane getPane() {
        return pane;
    }

    public boolean isBackgroundClosable() {
        return backgroundClosable;
    }

    public void setRoot(StackPane pane){
        this.rootPane = pane;
    }
    public StackPane getRoot(){
        return rootPane;
    }
}
