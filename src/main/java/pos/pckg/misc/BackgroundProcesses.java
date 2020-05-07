package pos.pckg.misc;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class BackgroundProcesses {
    public static final String DATE_FORMAT = "MM-dd-YYYY";
    public static final int MIN_BARCODE_LENGTH = 8;
    public static final int MAX_BARCODE_LENGTH = 13;
    public static void realTimeClock(Label lblDate){
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime currentTime = LocalDateTime.now();
            lblDate.setText(currentTime.format(DateTimeFormatter.ofPattern("hh:mm a EEE, MMM dd, yyyy")));
        }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    public static void createCacheDir(String file){
        File f = new File(file);
        if (f.exists()) {
            f.delete();
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File getFile(String file) {
        return new File(file);
    }

    public static void changeSecondaryFormStageStatus(short status){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(BackgroundProcesses.getFile(DataBridgeDirectory.DOCUMENT+"etc\\cache-secondary-status.file")));
            writer.write(String.valueOf(status));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Node getRoot(Node control) {
        Node node = control;
        while (true) {
            node = node.getParent();
            if (node.getId() != null && node.getId().equals("rootPane")) break;
        }
        return node;
    }

    public static void populateComboFromFile(String fileName, ComboBox cb) {
        try {
            cb.getItems().clear();
            Scanner scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\loader\\" + fileName + ".file"));
            do {
                cb.getItems().add(scan.nextLine());
            } while (scan.hasNextLine());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getStoreName(){
        Scanner scan = null;
        try {
            scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\initial.file"));
            if (scan.hasNextLine()){
                return  scan.nextLine();
            }else{
                return "";
            }

        } catch (FileNotFoundException e) {
            return "";
        }
    }


}
