package pos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.DirectoryHandler;
import pos.pckg.rfid.RFIDReaderInterface;


public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        if (!BackgroundProcesses.getStoreName().equals("")){
            BackgroundProcesses.changeSecondaryFormStageStatus((short)0);
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/POSSecondaryMain.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle(BackgroundProcesses.getStoreName()+" | Customer View");
            stage.setMinHeight(679);
            stage.setMinWidth(1137);
            Image icon = new Image(getClass().getResource("/img/pos-icon.png").openStream());
            stage.getIcons().add(icon);
            //stage.setMaximized(true);
            if ( Screen.getScreens().size()>1){
                Rectangle2D bounds = Screen.getScreens().get(1).getVisualBounds();
                stage.setX(bounds.getMinX() + 100);
                stage.setY(bounds.getMinY() + 100);
            }
            stage.setOnCloseRequest(e->{
                System.exit(0);
            });
            stage.setFullScreen(true);
            stage.show();


            stage = new Stage();
            root =  FXMLLoader.load(getClass().getResource("/fxml/POSLogin.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle(BackgroundProcesses.getStoreName()+" | Login");
            stage.setMinHeight(679);
            stage.setMinWidth(1137);
            stage.setMaximized(true);
            stage.getIcons().add(icon);
            //stage.setFullScreen(true);
            stage.setOnCloseRequest(e->{
                System.exit(0);
            });
            stage.show();

        }else{
            Parent root = FXMLLoader.load(getClass().getResource("/"+ DirectoryHandler.FXML+ "POSInitialSetup.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("POS | Initial Setup");
            stage.setResizable(false);
            Image icon = new Image(DirectoryHandler.IMG+ "pos-icon.png");
            stage.getIcons().add(icon);
            stage.show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // 2 parameters, one for each line. This doesn't have a character length limiter, so make sure to only pass Strings
    // that are 39 characters long (it can handle up to 40, but I wouldn't recommend using that max
    public static RFIDReaderInterface rfid = new RFIDReaderInterface("Welcome to " + BackgroundProcesses.getStoreName(),"");
}