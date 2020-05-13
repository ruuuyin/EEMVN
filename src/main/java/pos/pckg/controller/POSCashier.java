package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import pos.Main;
import pos.pckg.MiscInstances;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.data.entity.Item;
import pos.pckg.data.entity.ProductOrder;
import pos.pckg.misc.*;

import java.io.*;
import java.net.URL;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSCashier implements Initializable {

    /*************************************************/
    /*********** UI COMPONENT VARIABLES **************/
    /*************************************************/
    @FXML
    private StackPane rootPane;

    @FXML
    private JFXTreeTableView<ProductOrder> ttvOrderList;

    @FXML
    private TreeTableColumn<ProductOrder, String> chProduct;

    @FXML
    private TreeTableColumn<ProductOrder, String> chProductID;

    @FXML
    private TreeTableColumn<ProductOrder, Double> chUnitPrice;

    @FXML
    private TreeTableColumn<ProductOrder, Integer> chQuantity;

    @FXML
    private TreeTableColumn<ProductOrder, Double> chTotal;

    @FXML
    private JFXButton btnHome;

    @FXML
    private Label lblDate;

    @FXML
    private ImageView ivAdmin;

    @FXML
    private ImageView ivGsmSignal;

    @FXML
    private ImageView ivRfidSignal;

    @FXML
    private Label lblProductName;

    @FXML
    private Label lblBarcodeNumber;

    @FXML
    private Label lblUnitPrice;

    @FXML
    private JFXButton btnSubtract;

    @FXML
    private TextField tfQuantity;

    @FXML
    private JFXButton btnAdd;

    @FXML
    private JFXButton btnRemove;

    @FXML
    private JFXButton btnReturn;

    @FXML
    private JFXButton btnDiscount;

    @FXML
    private JFXButton btnAddCredits;

    @FXML
    private JFXButton btnRemoveAll;

    @FXML
    private JFXButton btnPriceInquiry;

    @FXML
    private JFXButton btnScanItem;

    @FXML
    private Label lblNumberItem;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblTypeCount;

    @FXML
    private Label lblDiscount;

    @FXML
    private Label lblTotal;

    @FXML
    private JFXButton btnCheckout;



    /*************************************************/
    /****************** VARIABLES ********************/
    /*************************************************/

    protected final static String S_KEY = "POS_CUSTOMER";
    protected static ObservableList<ProductOrder> productList = FXCollections.observableArrayList();
    protected static ArrayList <Item>allItem = new ArrayList<Item>();
    protected static double discount = 0.0;
    protected static double total = 0;
    protected static int items = 0,type = 0;
    protected static double subTotal = 0;
    private ProductOrder selectedProduct = null;
    protected static MiscInstances misc = new MiscInstances();
    protected static double overAllTotal=0;
    protected static POSDialog dialog;// static dialog to make it accessible
                            // to the Dialog that is currently open
                            // and easy to access the close method of the Dialog
    public static String userID = "";

    protected static final SceneManipulator sceneManipulator = new SceneManipulator();


    /*************************************************/
    /*************** EVENT HANDLERS ******************/
    /*************************************************/
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        BackgroundProcesses.changeSecondaryFormStageStatus((short)0);
        BackgroundProcesses.createCacheDir(DataBridgeDirectory.DOCUMENT+"etc\\cache-secondary-table.file");
        //pckg.misc = new pckg.MiscInstances();
        queryAllItem();
        InputRestrictor.numbersInput(this.tfQuantity);
        InputRestrictor.limitInput(this.tfQuantity,3);
        BackgroundProcesses.realTimeClock(lblDate);
        loadTable();
        try {
            Scanner scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-user.file"));
            userID = scan.nextLine();
            scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-user.file"));
            scan.nextLine();
            scan.nextLine();
            scan.nextLine();
            scan.nextLine();
            ivAdmin.setImage(scan.nextLine().equals("1")
                    ? new Image(DirectoryHandler.IMG+ "pos-admin.png")
                    : new Image(DirectoryHandler.IMG+ "pos-admin-disable.png") );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        checkoutStatusRefresher();
        checkGsmSignal();
        checkRFIDStatus();
        adminToolTip();
    }


    @FXML
    protected void btnHomeOnAction(ActionEvent event) {
        gsmSignalThread.stop();
        rfidStatus.stop();
        sceneManipulator.changeScene(rootPane,"POSDashboard", " | Dashboard");
    }

    @FXML
    protected void btnCheckoutOnAction(ActionEvent event) throws IOException {
        gsmSignalThread.stop();
        rfidStatus.stop();
        writer = new BufferedWriter(new FileWriter(DataBridgeDirectory.DOCUMENT+"etc\\cache-checkout-total.file"));
        writer.write(lblTotal.getText());
        writer.close();

        if (event.getSource().equals(this.btnCheckout))
            sceneManipulator.openDialog(rootPane,"POSCheckout");
    }

    @FXML
    protected void btnFunctionalitiesOnAction(ActionEvent event) {

        JFXButton selectedButton = (JFXButton) event.getSource();
        if (selectedButton.equals(this.btnScanItem)){
            sceneManipulator.openDialog(rootPane,"POSScanItem");
        }else if (selectedButton.equals(this.btnDiscount)){
            discount = Double.parseDouble(lblDiscount.getText());
            sceneManipulator.openDialog(rootPane,"POSDiscount");
        }else if (selectedButton.equals(this.btnPriceInquiry)){
            sceneManipulator.openDialog(rootPane,"POSPriceInquiry");
        }else if (selectedButton.equals(this.btnAddCredits)){
            gsmSignalThread.stop();
            rfidStatus.stop();
            sceneManipulator.openDialog(rootPane,"POSAddBalance");
        }else if (selectedButton.equals(this.btnReturn)){
            gsmSignalThread.stop();
            rfidStatus.stop();
            sceneManipulator.openDialog(rootPane,"POSReturn");
        }else if (selectedButton.equals(this.btnRemoveAll)){
            JFXButton btnNo = new JFXButton("No");// Confirmation button - "No"
            btnNo.setOnAction(ev -> POSMessage.closeMessage());// After pressing the No button, it simply close the messgae

            JFXButton btnYes = new JFXButton("Yes");// Confirmation button - "Yes"
            btnYes.setOnAction(ev -> {

                productList.clear();
                POSMessage.closeMessage();
                JFXButton btnOk = new JFXButton("Ok");
                btnOk.setOnAction(evt -> {
                    POSMessage.closeMessage();
                });

                POSMessage.showConfirmationMessage(rootPane,
                        "Item list has been cleared",
                        "Process Complete",
                        POSMessage.MessageType.INFORM, btnOk);


            });

            // Confirmation Message
            POSMessage.showConfirmationMessage(rootPane, "Do you really want to void \nthe transaction?"
                    , "Please confirm", POSMessage.MessageType.ERROR, btnNo, btnYes);
        }

    }

    @FXML
    protected void btnQuantityChangerOnAction(ActionEvent event) {
        if (tfQuantity.getText().isEmpty())
            return;
        else if (tfQuantity.getText().equals("1") && event.getSource().equals(btnSubtract))
            return;

        int x = Integer.parseInt(tfQuantity.getText());
        if (event.getSource().equals(btnAdd))
            x=x+1;
        else if (event.getSource().equals(btnSubtract))
            x=x-1;
        tfQuantity.setText(String.valueOf(x));

        int newQuantity = Integer.parseInt(tfQuantity.getText());
        changeQuantityOnTable(newQuantity);

    }

    @FXML
    protected void btnRemove(ActionEvent event) {
        String id[] =lblBarcodeNumber.getText().split(": ");
        if (id.length>1){
            productList.forEach(e->{
                if (e.getProductID().equals(id[1]))
                    selectedProduct = e;
            });
            productList.remove(selectedProduct);
            lblProductName.setText("Product: ");
            lblBarcodeNumber.setText("Product ID: ");
            lblUnitPrice.setText("Unit Price: ");
            tfQuantity.setText("");
            checkoutStatusRefresher();
        }
    }

    @FXML
    protected void ttvOrderOnKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN)
            populateProductInformation();
    }

    @FXML
    protected void ttvOrderOnMouseClicked(MouseEvent event) {
        populateProductInformation();
    }




    /*************************************************/
    /*********** FUNCTIONS AND PROCEDURES ************/
    /*************************************************/


    private void populateProductInformation(){
        TreeItem treeItem = ttvOrderList.getSelectionModel().getSelectedItem();
        if (treeItem!=null){
            ProductOrder prod = (ProductOrder) treeItem.getValue();
            lblProductName.setText("Product: "+prod.getProduct());
            lblBarcodeNumber.setText("Product ID: "+prod.getProductID());
            lblUnitPrice.setText("Unit Price: "+prod.getUnitPrice());
            tfQuantity.setText(prod.getQuantity()+"");
        }
    }



    private void checkoutStatusRefresher(){//for refreshing the checkout Status
        Timeline itemCountRefresher = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            if (lblTotal.getText().equals("0.0")){
                btnCheckout.setDisable(true);
                btnRemoveAll.setDisable(true);
            }else{
                btnCheckout.setDisable(false);
                btnRemoveAll.setDisable(false);
            }
            checkoutStatusCalculate();
            lblDiscount.setText(String.valueOf(discount));
            writeToCache(DataBridgeDirectory.DOCUMENT+"etc\\cache-secondary-table.file");
        }),new KeyFrame(Duration.millis(100)));
        itemCountRefresher.setCycleCount(Animation.INDEFINITE);
        itemCountRefresher.play();
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
        overAllTotal = total;
        lblTotal.setText(total+"");

    }

    private void changeQuantityOnTable(int newQuantity){
        String id[] =lblBarcodeNumber.getText().split(": ");
        if (id.length<1)return;
        productList.forEach((e)->{
            if (e.getProductID().equals(id[1])){
                e.setQuantity(newQuantity);
                e.setTotal(e.getQuantity()*e.getUnitPrice());
            }


        });
    }

    private void loadTable(){
        chProductID.setCellValueFactory(new TreeItemPropertyValueFactory<ProductOrder,String>("productID"));
        chProduct.setCellValueFactory(new TreeItemPropertyValueFactory<ProductOrder,String>("product"));
        chUnitPrice.setCellValueFactory(new TreeItemPropertyValueFactory<ProductOrder,Double>("unitPrice"));
        chQuantity.setCellValueFactory(new TreeItemPropertyValueFactory<ProductOrder,Integer>("quantity"));
        chTotal.setCellValueFactory(new TreeItemPropertyValueFactory<ProductOrder,Double>("total"));
        TreeItem <ProductOrder>dataItem = new RecursiveTreeItem<ProductOrder>(productList, RecursiveTreeObject::getChildren);
        ttvOrderList.setRoot(dataItem);
        ttvOrderList.setShowRoot(false);
    }

    protected static void queryAllItem(){
        allItem.clear();
        String sql = "Select * from Item";
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);
        try{
            while(result.next()){
                Item item = new Item(result.getInt("itemID")
                        ,result.getString("itemCode")
                        ,result.getString("itemName")
                        ,result.getDouble("itemPrice")
                        ,result.getInt("stock"),new JFXButton(),new HBox());
                allItem.add(item);
            }
            misc.dbHandler.closeConnection();
        }catch (Exception e){
            e.printStackTrace();
            misc.dbHandler.closeConnection();
        }

    }


    /*************************************************/
    /******** STATIC FUNCTIONS AND PROCEDURES ********/
    /*************************************************/
    protected static void addItemToList(ProductOrder productOrder){
        productList.add(productOrder);

    }

    private static BufferedWriter writer;
    private static void writeToCache(String file) {
        String cacheData="";
        for (ProductOrder p:productList) {
            cacheData+=p.getProductID()+":"+p.getProduct()+":"+p.getUnitPrice()+":"+p.getQuantity()+":"+p.getTotal()+"\n";
        }
        try {
            writer = new BufferedWriter(new FileWriter(BackgroundProcesses.getFile(file)));
            writer.write(cacheData);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    protected static Timeline gsmSignalThread,rfidStatus;
    private void checkGsmSignal(){
        gsmSignalThread = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            // Check to see if the device is in not in SMS mode
            // Any passed commands to the device while it is in the middle of an SMS operation will interfere with it,
            // potentially causing the device's GSM module to get stuck
                try {
                    Main.rfid.getSignalQuality();
                    Scanner scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc/status/rfid-gsm-signal.file"));
                    if (scan.hasNextLine()){
                        String value[] = scan.nextLine().split("=");
                        System.out.println("Source:Main.rfid.getSignalQuality();\n\t["+value[0]+"="+value[1]+"]");//TODO to check the returned status
                        if (value[0].equals("signalQuality")){
                            int val = Integer.parseInt(value[1]);
                            String url = "";
                            if (val>=1 && val<=10)
                                url = DirectoryHandler.IMG+ "pos-connection-low.png";
                            else if (val>=11 && val<=20)
                                url = DirectoryHandler.IMG+ "pos-connection-medium.png";
                            else if (val>=21 && val<=30)
                                url = DirectoryHandler.IMG+ "pos-connection-high.png";
                            ivGsmSignal.setImage(new Image(url));
                            gsmSignalToolTip();
                            Main.rfid.clearStatusCache();
                        }else{
                            String url = DirectoryHandler.IMG+ "pos-connection-dc.png";

                            ivGsmSignal.setImage(new Image(url));
                            gsmSignalToolTip();
                        }
                    }

                } catch (Exception ex) {
                    //TODO Stacktrace : status : OFF
                    //ex.printStackTrace();
                    String url = DirectoryHandler.IMG+ "pos-connection-dc.png";

                    ivGsmSignal.setImage(new Image(url));
                    gsmSignalToolTip();
                }
        }),
                new KeyFrame(Duration.seconds(3))
        );
        gsmSignalThread.setCycleCount(Animation.INDEFINITE);
        gsmSignalThread.play();
    }

    private void checkRFIDStatus(){
        rfidStatus = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            // Check to see if the device is in not in SMS mode
            // Any passed commands to the device while it is in the middle of an SMS operation will interfere with it,
            // potentially causing the device's GSM module to get stuck
                try {
                    Main.rfid.queryDevice();
                    Scanner scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc/status/rfid-device-signal.file"));
                    if (scan.hasNextLine()){
                        String value[] = scan.nextLine().split("=");
                        if (value[0].equals("deviceConnected")){
                            int val = Integer.parseInt(value[1]);
                            System.out.println("Source:Main.rfid.queryDevice();\n\t[deviceConnected="+val+"]");//TODO to check the returned status
                            String url = "";
                            if (val==0)
                                url = DirectoryHandler.IMG+"pos-rfid-signal-dc.png";
                            else if (val>0)//TODO Observe the status of rfid device. two status return added
                                url = DirectoryHandler.IMG+"pos-rfid-signal.png";

                            ivRfidSignal.setImage(new Image(url));
                            Main.rfid.clearStatusCache();
                        }
                    }
                } catch (Exception ex) {
                    //ex.printStackTrace();
                    String url = DirectoryHandler.IMG+"pos-rfid-signal-dc.png";

                    ivRfidSignal.setImage(new Image(url));
                }
                rfidToolTip();
        }),
                new KeyFrame(Duration.seconds(5))
        );
        rfidStatus.setCycleCount(Animation.INDEFINITE);
        rfidStatus.play();
    }
    private void adminToolTip(){
        File file = new File(ivAdmin.getImage().getUrl());
        String value = file.getName().equals("pos-admin-disable.png") ? "Non-Administrator" : "Administrator";
        Tooltip.install(ivAdmin,new Tooltip(value));

    }

    private void gsmSignalToolTip(){
        File file = new File(ivGsmSignal.getImage().getUrl());
        String value = file.getName();
        if (value.equals("pos-connection-low.png")){
            value = "GSM Signal : Weak";
        }else if (value.equals("pos-connection-medium.png"))
            value = "GSM Signal : Moderate";
        else if (value.equals("pos-connection-high.png"))
            value = "GSM Signal : Strong";
        else if (value.equals("pos-connection-dc.png"))
            value = "GSM Disconnected / No Signal";
        Tooltip.install(ivGsmSignal,new Tooltip(value));
    }

    private void rfidToolTip(){
        File file = new File(ivRfidSignal.getImage().getUrl());
        String value = file.getName();
        if (value.equals("pos-rfid-signal.png")){
            value="RFID Device | Connected";
        }else if (value.equals("pos-rfid-signal-dc.png")){
            value="RFID Device | Disconnected";
        }
        Tooltip.install(ivRfidSignal,new Tooltip(value));
    }
}
