package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.robot.Robot;
import pos.pckg.data.entity.ProductOrder;
import pos.pckg.misc.InputRestrictor;

import java.net.URL;
import java.util.ResourceBundle;

public class POSScanItem extends POSCashier implements Initializable {


    @FXML
    private StackPane rootPane;

    @FXML
    private TextField tfBarcode;

    @FXML
    private Label lblID;

    @FXML
    private Label lblProduct;

    @FXML
    private Label lblBarcode;

    @FXML
    private Label lblUnitPrice;

    @FXML
    private Label lblStock;

    @FXML
    private Label lblQuantity;

    @FXML
    private TextField tbQuantity;

    @FXML
    private JFXButton btnClose;

    @FXML
    private JFXButton btnAdd;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Robot robot = new Robot();
        robot.keyPress(KeyCode.TAB);
        //FXRobot robot = FXRobotFactory.createRobot(scene);
        //robot.keyPress(javafx.scene.input.KeyCode.A);
        InputRestrictor.numbersInput(tbQuantity);
        InputRestrictor.limitInput(tbQuantity,3);
        InputRestrictor.limitInput(tfBarcode,13);
        InputRestrictor.numbersInput(tfBarcode);
        tbQuantity.setText("1");
        if (rootPane.isFocused()) tfBarcode.requestFocus();
    }

    private ProductOrder existingOrder= null;
    @FXML
    void btnAddOnAction(ActionEvent event) {
        addProduct();
    }

    private void addProduct(){
        if (lblID.getText().split(" : ").length!=1){
            String productID = lblBarcode.getText().split(" : ")[1]+"-"+lblID.getText().split(" : ")[1];
            String product = lblProduct.getText().split(" : ")[1];
            double unitPrice = Double.parseDouble(lblUnitPrice.getText().split(" : ")[1]);
            int quantity = Integer.parseInt(tbQuantity.getText());

            if (orderExist(productID)){
                existingOrder.setQuantity(existingOrder.getQuantity()+quantity);
                existingOrder.setTotal(existingOrder.getQuantity() * existingOrder.getUnitPrice());

            }else {
                POSCashier.addItemToList(new ProductOrder(productID,product,unitPrice,quantity,(unitPrice*quantity)));
            }
        }
    }

    private boolean orderExist(String productID){
        for (ProductOrder order: POSCashier.productList){
            if (order.getProductID().equals(productID)){
                existingOrder = order;
                return true;
            }
        }
        return false;
    }

    @FXML
    void btnCloseOnAction(ActionEvent event) {
        sceneManipulator.closeDialog();//get the scenemanipulator from
                                        // the parent scene and call the closeDialog
    }

    @FXML
    void tfBarcodeOnKeyReleased(KeyEvent event) {
        if (tfBarcode.getText().length()>=12){
            allItem.forEach(item -> {
                if (item.getItemCode().equals(tfBarcode.getText())){
                    lblID.setText("ID : "+item.getItemID());
                    lblBarcode.setText("Barcode : "+item.getItemCode());
                    lblProduct.setText("Product : "+item.getItemName());
                    lblUnitPrice.setText("Unit Price : "+item.getItemPrice());
                    if (item.getStock()<10)
                        lblStock.setStyle("-fx-text-fill: #ff6475");
                    else
                        lblStock.setStyle("-fx-text-fill: #000000");
                    lblStock.setText(item.getStock()+"");
                    tfBarcode.setText("");
                    addProduct();
                }

            });
        }
    }


}
