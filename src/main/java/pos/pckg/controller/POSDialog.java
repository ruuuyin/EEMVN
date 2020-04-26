package pos.pckg.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import pos.pckg.misc.DirectoryHandler;

import java.io.IOException;

public class POSDialog {

    private Pane pane;
    private AnchorPane root;
    private FXMLLoader loader;
    private StackPane parentRoot;
    private boolean backgroundClosable;

    public POSDialog(StackPane parent,Pane pane, boolean backgroundClosable) {
        invokeParent();
        this.parentRoot = parent;
        this.pane = pane;
        this.backgroundClosable = backgroundClosable;
        loader.<POSDialogContainer>getController().setBackgroundClosable(backgroundClosable);
    }

    private void invokeParent(){
        try {
            loader = new FXMLLoader(getClass().getResource("/"+ DirectoryHandler.FXML+ "POSDialogContainer.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public POSDialog(StackPane parent,Pane pane) {
        invokeParent();
        this.parentRoot = parent;
        this.pane = pane;
    }

    public void show(){
        DropShadow shadow = new DropShadow(20,0,0,new Color(0,0,0,.1));
        pane.setEffect(shadow);
        loader.<POSDialogContainer>getController().setDialogPane(pane);
        loader.<POSDialogContainer>getController().setRoot(parentRoot);
        parentRoot.getChildren().add(root);
        root.requestFocus();
    }

    public void close(){
        loader.<POSDialogContainer>getController().close();
    }

    public Object getDialogController(){
        return loader.getController();
    }

    public StackPane getParentRoot(){
        return loader.<POSDialogContainer>getController().getRoot();
    }
}
