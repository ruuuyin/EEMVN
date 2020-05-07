package pos.pckg;

import pos.pckg.data.DatabaseHandler;
import pos.pckg.misc.DataBridgeDirectory;
import pos.pckg.misc.SceneManipulator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class MiscInstances {
    public DatabaseHandler dbHandler;
    public SceneManipulator sceneManipulator;

    public MiscInstances() {
        sceneManipulator = new SceneManipulator();
        instantiateDBHandler();
    }

    private void instantiateDBHandler(){
        String url,uname,password;
        try {
            Scanner textScan = new Scanner(
                    new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\Connection.properties"));
            url = textScan.nextLine();
            uname = textScan.nextLine();
            password = textScan.nextLine();
            dbHandler= new DatabaseHandler(url,uname,password.equals("N/A")?"":password);// initializing the DatabaseHandler
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
