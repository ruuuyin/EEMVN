package pos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import pos.pckg.data.CacheWriter;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.DataBridgeDirectory;
import pos.pckg.misc.DirectoryHandler;
import pos.pckg.rfid.RFIDReaderInterface;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        createLocalDataDirectory();


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

    private static void createLocalDataDirectory(){
        File file = new File("etc");
        if (!file.exists()){
            file.mkdir();
            new File("etc/loader").mkdir();
            new File("etc/status").mkdir();

            String localData[] = DataBridgeDirectory.getAllLocalDataBridge();
            for (int i = 0;i<localData.length;i++){
                BackgroundProcesses.createCacheDir(localData[i]);
                cacheWriter("",localData[i]);
                if (i==0)
                   cacheWriter("---\n" +
                           "Add\n" +
                           "Edit\n" +
                           "Delete\n" +
                           "Restock\n" +
                           "Change PIN",localData[i]);
                else if(i==1)
                    cacheWriter("---\n" +
                            "Customer Management\n" +
                            "Stock Management",localData[i]);
                else if(i==3)
                    cacheWriter("---\n" +
                            "Retail\n" +
                            "Add Balance\n" +
                            "Item Return",localData[i]);

                else if(i==5)
                    cacheWriter("gsmSignal=20",localData[i]);
                else if (i==6)
                    cacheWriter("gsmStatus=0",localData[i]);
                else if(i==25){
                    cacheWriter("jdbc:mysql://localhost:3306/ee-pos?useTimezone=true&serverTimezone=UTC\n" +
                            "root\n" +
                            "N/A",localData[i]);
                }


            }
        }
    }

    private static void cacheWriter(String data,String file){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(BackgroundProcesses.getFile(file)));
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}