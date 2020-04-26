package pos.pckg.controller;

import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import pos.pckg.MiscInstances;
import pos.pckg.data.entity.ProductOrder;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.SceneManipulator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSSecondaryMain implements Initializable {

    @FXML
    private StackPane rootPane;

    @FXML
    private JFXTreeTableView<ProductOrder> ttvOrderList;

    @FXML
    private TreeTableColumn<ProductOrder, String> chProductID;

    @FXML
    private TreeTableColumn<ProductOrder, String> chProduct;

    @FXML
    private TreeTableColumn<ProductOrder, Double> chUnitPrice;

    @FXML
    private TreeTableColumn<ProductOrder, Integer> chQuantity;

    @FXML
    private TreeTableColumn<ProductOrder, Double> chTotal;

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

    protected static ObservableList<ProductOrder> productList = FXCollections.observableArrayList();
    protected static double discount = 0.0,total = 0,subTotal = 0;
    protected static int items = 0,type = 0;
    protected MiscInstances misc;
    protected static Timeline mainThread,statusThread;
    protected static POSDialog dialog;// static dialog to make it accessible
    // to the Dialog that is currently open
    // and easy to access the close method of the Dialog

    protected static final SceneManipulator sceneManipulator = new SceneManipulator();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        BackgroundProcesses.createCacheDir("etc\\cache-secondary-table.file");
        loadTable();
        checkoutStatusRefresher();
        statusChecker();
    }

    @FXML
    void ttvOrderOnKeyReleased(KeyEvent event) {

    }

    @FXML
    void ttvOrderOnMouseClicked(MouseEvent event) {

    }

    private void checkoutStatusRefresher(){//for refreshing the checkout Status
        mainThread = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            queryAllData();
            checkoutStatusCalculate();
            lblDiscount.setText(String.valueOf(discount));
        }),new KeyFrame(Duration.millis(100)));
        mainThread.setCycleCount(Animation.INDEFINITE);
        mainThread.play();
    }



    private void checkoutStatusCalculate(){
        type = 0;
        subTotal = 0.0;
        items = 0;
        total = 0;
        productList.forEach(e->{
            subTotal = subTotal+(e.getQuantity()*e.getUnitPrice());
            items+=e.getQuantity();
            type++;
        });
        lblTypeCount.setText(type+"");
        lblSubtotal.setText(subTotal+"");
        lblNumberItem.setText(items+"");
        total =discount!=0
                ? subTotal-((subTotal*discount)/100)
                : subTotal;
        lblTotal.setText(total+"");
    }

    private void loadTable(){
        chProductID.setCellValueFactory(new TreeItemPropertyValueFactory<ProductOrder,String>("productID"));
        chProduct.setCellValueFactory(new TreeItemPropertyValueFactory<ProductOrder,String>("product"));
        chUnitPrice.setCellValueFactory(new TreeItemPropertyValueFactory<ProductOrder,Double>("unitPrice"));
        chQuantity.setCellValueFactory(new TreeItemPropertyValueFactory<ProductOrder,Integer>("quantity"));
        chTotal.setCellValueFactory(new TreeItemPropertyValueFactory<ProductOrder,Double>("total"));
        TreeItem<ProductOrder> dataItem = new RecursiveTreeItem<ProductOrder>(productList, RecursiveTreeObject::getChildren);
        ttvOrderList.setRoot(dataItem);
        ttvOrderList.setShowRoot(false);
    }
    private Scanner scan;
    private void queryAllData(){
        productList.clear();
        try {
            scan = new Scanner(new FileInputStream("etc\\cache-secondary-table.file"));
            while(scan.hasNextLine()){
                String p[] = scan.nextLine().split(":");
                ProductOrder product = new ProductOrder(p[0],p[1]
                        ,Double.parseDouble(p[2])
                        ,Integer.parseInt(p[3])
                        ,Double.parseDouble(p[4]));
                productList.add(product);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
    
    private void statusChecker(){
        statusThread = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            try {
                Scanner scan = new Scanner(new FileInputStream("etc\\cache-secondary-status.file"));
                short status = Short.parseShort(scan.nextLine());
                switch (status){
                    case 0:
                        if (hasDialog()){
                            sceneManipulator.closeDialog();
                            mainThread.play();
                        }
                        break;
                    case 1:
                        if (!hasDialog())
                            sceneManipulator.openDialog(rootPane,"POSSecondaryCheckout");
                        break;
                    case 2:
                        if (!hasDialog())
                            sceneManipulator.openDialog(rootPane,"POSSecondaryOffline");
                        break;
                }

            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }

        }),new KeyFrame(Duration.millis(100)));
        statusThread.setCycleCount(Animation.INDEFINITE);
        statusThread.play();

    }

    private boolean hasDialog(){
        for (Node node:rootPane.getChildren())
            if (node.getId()!=null && node.getId().equals("apDialogBase")) return true;
        return false;
    }

}

//STATUS
//      0       -       On Scanning - Available - Dialog are close
//      1       -       On Checkout Procedure - Scanning card
//      2       -       Busy - Status