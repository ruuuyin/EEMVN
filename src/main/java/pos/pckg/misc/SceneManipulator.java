package pos.pckg.misc;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import pos.pckg.controller.POSDialog;

import java.io.IOException;

public class SceneManipulator {
    private Scene scene;
    private Stage stage;
    private Parent root;
    private POSDialog dialog;

    /**
     * A Scene Manupulator method that changes the Scene inside the Stage
     * @param rootPane - it is the root node of a the FXML.
     * @param fxmlName - the FXML file name.
     * @param title - to change the Title of the stage
     * */
    public void changeScene(Pane rootPane,String fxmlName,String title){
        root =  getFXML(fxmlName);
        stage = getStage(rootPane);
        stage.setTitle(BackgroundProcesses.getStoreName()+title);

        boolean isOnFull = stage.isFullScreen();
        boolean isMaximized = stage.isMaximized();
        double y,x,w,h,ww,hh;
        y=stage.getY();
        x=stage.getX();
        w=stage.getWidth();
        h=stage.getHeight();
        hh = stage.getScene().getHeight();
        ww = stage.getScene().getWidth();
        scene = new Scene(root,ww,hh);
        stage.setScene(scene);

        //stage.centerOnScreen();
        stage.setMaximized(isMaximized);
        stage.setFullScreenExitHint("");
        stage.setFullScreen(isOnFull);//to full screen when switching a UI
    }


    /**
     * A SceneManipulator method that changes the Window or the Stage
     * @param rootPane - it is the root node of a the FXML.
     * @param fxmlName - the FXML file name.
     * @param title - to change the Title of the stage
     */
    public void changeStage(Pane rootPane,String fxmlName,String title){
        root =  getFXML(fxmlName);
        stage = getStage(rootPane);
        stage.close();
        scene = new Scene(root);
        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setFullScreen(true);
        stage.show();
    }

    /**
     * A SceneManipulator method that open new Stage without closing the last Stage
     * @param fxmlName - the FXML file name.
     * @param title - to change the Title of the stage
     */
    public void openStage(String fxmlName,String title){
        root = getFXML(fxmlName);
        scene = new Scene(root);
        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    /**
     * A SceneManipulator method for opening FXML inside the Pane
     * @param parent - the anchor where the FXML file to be placed
     * @param fxmlName - the FXML file name.
     */
    public void attachNode(AnchorPane parent,String fxmlName){
        root = getFXML(fxmlName);
        AnchorPane.setBottomAnchor(root,0.0);
        AnchorPane.setLeftAnchor(root,0.0);
        AnchorPane.setRightAnchor(root,0.0);
        AnchorPane.setTopAnchor(root,0.0);
        if (parent.getChildren().size()==1)
            parent.getChildren().clear();
        parent.getChildren().add(root);
    }

    /**
     * To cloase a specific Stage bt passing the rootPane
     * @param rootPane
     */
    public void closeStage(Pane rootPane){
        stage = getStage(rootPane);
        stage.close();
    }

    /**
     * Getting the FXML file and saving it inside a Node
     * @param fxmlName
     * @return it returns the Parent where the fxml files is placed
     */
    private Parent getFXML(String fxmlName){
        try {
            return FXMLLoader.load(getClass().getResource("/"+ DirectoryHandler.FXML+fxmlName+".fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * To get the Stage where the rootPane is placed
     * @param rootPane
     * @return
     */
    private Stage getStage(Pane rootPane){
        return (Stage) rootPane
                .getScene()
                .getWindow();
    }

    public void openDialog(StackPane rootPane, String fxml){
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("/"+ DirectoryHandler.FXML+fxml+".fxml"));
            dialog = new POSDialog(rootPane, (Pane) parent,false);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeDialog(){
        if (dialog!=null)
            dialog.close();
    }

    public void changeOpenDialog(StackPane rootPane, String fxml){
        closeDialog();
        openDialog(rootPane,fxml);
    }

    public Object getDialogController(){
        return dialog.getParentRoot();
    }
}
