package pos.pckg.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import pos.pckg.misc.DataBridgeDirectory;
import pos.pckg.misc.DirectoryHandler;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSSecondaryCheckout extends POSSecondaryMain {

    @FXML
    private Label lblTypeCount;

    @FXML
    private Label lblNumberItem;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblDiscount;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblStatus;

    @FXML
    private ImageView ivPrompt;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainThread.stop();
        checkoutStatusCalculate();
        cardDetect();
    }
    private void checkoutStatusCalculate(){
        type = 0;
        subTotal = 0.0;
        items = 0;
        total = 0;
        POSSecondaryMain.productList.forEach(e->{
            subTotal = subTotal+(e.getQuantity()*e.getUnitPrice());
            items+=e.getQuantity();
            type++;
        });
        lblTypeCount.setText(type+"");
        lblSubtotal.setText(subTotal+"");
        lblNumberItem.setText(items+"");
        lblDiscount.setText(discount+"");
        total =discount!=0
                ? subTotal-((subTotal*discount)/100)
                : subTotal;
        lblTotal.setText(total+"");
    }
    
    private Timeline cardThread;
    private Scanner scan;
    private BufferedWriter writer;
    private void cardDetect(){
            cardThread = new Timeline(new KeyFrame(Duration.ZERO, e -> {
                try {
                    scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-secondary-check-card.file"));
                    if (scan.hasNextLine()){
                        if (scan.nextLine().equals("1")){

                            ivPrompt.setImage(new Image(DirectoryHandler.IMG+ "pos-spinner.gif"));
                            lblStatus.setText("Processing transaction...");
                                //writer = new BufferedWriter(new FileWriter(DataBridgeDirectory.DOCUMENT+"etc\\cache-secondary-check-card.file"));
                                //writer.write("0");
                                //writer.close();

                            cardThread.stop();
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }),
                    new KeyFrame(Duration.seconds(1))
            );
            cardThread.setCycleCount(Animation.INDEFINITE);
            cardThread.play();

    }


}