package pos.pckg.controller.message;

import com.jfoenix.controls.JFXButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pos.Main;
import pos.pckg.controller.POSDialog;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.DataBridgeDirectory;
import pos.pckg.misc.DirectoryHandler;
import pos.pckg.misc.SceneManipulator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class POSMessage  {
    protected static SceneManipulator sceneManipulator = new SceneManipulator();

    public enum MessageType{
        ERROR,
        INFORM,
        CONFIRM
    }

    private static void writeToCache(String message,String title,String directory) {
        String cacheData = title+"\n"+message+"\n"+directory;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(BackgroundProcesses.getFile(DataBridgeDirectory.DOCUMENT+"etc\\cache-message.file")));
            writer.write(cacheData);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getIconDirectory(MessageType messageType){
        String directory = "";
        switch (messageType){
            case ERROR:
                directory = DirectoryHandler.IMG+ "pos-message-error.png";
                break;
            case INFORM:
                directory = DirectoryHandler.IMG+ "pos-message-inform.png";
                break;
            case CONFIRM:
                directory = DirectoryHandler.IMG+ "pos-message-confirm.png";
                break;
        }
        return directory;
    }

    public static void showMessage(StackPane parent,String message,String title,MessageType messageType){
        BackgroundProcesses.createCacheDir(DataBridgeDirectory.DOCUMENT+"etc\\cache-message.file");
        writeToCache(message,title,getIconDirectory(messageType));
        sceneManipulator.openDialog(parent,"POSSimpleMessage");

    }


    private static POSDialog dialog = null;
    public static void showConfirmationMessage(StackPane parent,String message,String title,MessageType messageType, JFXButton ... button){

         dialog = new POSDialog(parent
                , (Pane) createmMessageBox(message
                    ,title
                    ,getIconDirectory(messageType)
                    ,button)
                ,false);
         dialog.show();
    }


    private static Parent createmMessageBox(String message, String title, String directory, JFXButton ... button){
        StackPane rootPane = new StackPane();
        rootPane.setPrefSize(409,185);
        rootPane.setMinSize(409,185);
        rootPane.getStylesheets().add(Main.class.getResource("/style/style.css").getFile());
        rootPane.setStyle("-fx-background-color: white");
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPrefSize(409,185);

        VBox vBox = new VBox();
        vBox.setPrefSize(409,185);

        HBox titleContainer = new HBox();
        titleContainer.setPrefSize(409,46);
        titleContainer.setStyle("-fx-border-width: 0 0 1 0; -fx-border-color: #c5c5c5; -fx-border-style: solid;");

        HBox messageContainer = new HBox();
        messageContainer.setPrefSize(409,95);

        HBox controlsContainer = new HBox();
        controlsContainer.setPrefSize(409,54);
        controlsContainer.setAlignment(Pos.CENTER_RIGHT);

        Label lblTitle = new Label(title);
        lblTitle.setPrefWidth(409);
        lblTitle.setPrefHeight(59);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD,16));
        lblTitle.getStyleClass().add("lbl-name");
        lblTitle.setAlignment(Pos.CENTER_LEFT);
        titleContainer.getChildren().add(lblTitle);
        HBox.setMargin(lblTitle, new Insets(10.0,10.0,10.0,10.0));

        Label lblMessage = new Label(message);
        lblMessage.setFont(Font.font("Segoe UI", FontWeight.BOLD,16));
        lblMessage.setPrefSize(311,65);
        lblMessage.setAlignment(Pos.TOP_LEFT);

        ImageView icon = new ImageView(new Image(directory));
        icon.setFitWidth(65);
        icon.setFitHeight(65);
        icon.setPreserveRatio(true);
        icon.setSmooth(true);

        messageContainer.getChildren().addAll(icon,lblMessage);
        HBox.setMargin(icon, new Insets(10.0,10.0,10.0,10.0));
        HBox.setMargin(lblMessage, new Insets(10.0,10.0,10.0,10.0));

        controlsContainer.getChildren().addAll(button);
        for (JFXButton btn:button) {
            HBox.setMargin(btn,new Insets(10.0,10.0,10.0,10.0));
            btn.getStyleClass().add("controls-container");
            btn.setPrefSize(116,34);
        }
        vBox.getChildren().addAll(titleContainer,messageContainer,controlsContainer);
        anchorPane.getChildren().add(vBox);
        rootPane.getChildren().add(anchorPane);

        return rootPane;
    }

    public static void closeMessage(){
        dialog.close();
    }

}
