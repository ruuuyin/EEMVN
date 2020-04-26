package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import pos.pckg.data.entity.ProductOrder;

import java.net.URL;
import java.util.ResourceBundle;

public class POSPriceInquiry extends POSCashier {

    private ObservableList<ProductOrder> result = FXCollections.observableArrayList();

    @FXML
    private StackPane rootPane;

    @FXML
    private JFXTreeTableView<ProductOrder> ttvProductResult;

    @FXML
    private TreeTableColumn<ProductOrder, String> chProductCode;

    @FXML
    private TreeTableColumn<ProductOrder, String> chProductName;

    @FXML
    private Label lblItemID;

    @FXML
    private Label lblItemCode;

    @FXML
    private Label lblItemName;

    @FXML
    private Label lblUnitPrice;

    @FXML
    private Label lblStocks;

    @FXML
    private JFXButton btnClose;

    @FXML
    private TextField tfSearch;

    @FXML
    private JFXButton btnSearch;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadTable();
    }

    @FXML
    void btnCloseOnAction(ActionEvent event) {
        sceneManipulator.closeDialog();
    }

    @FXML
    void btnSearchOnAction(ActionEvent event) {
        result.clear();
        allItem.forEach(item->{
            if (item.getItemCode().toLowerCase().contains(tfSearch.getText().toLowerCase())
                    || item.getItemName().toLowerCase().contains(tfSearch.getText().toLowerCase())){
                result.add(new ProductOrder(item.getItemCode()+"-"+item.getItemID()
                        ,item.getItemName(),item.getItemPrice(),item.getStock(),0));
            }
        });
    }

    private void loadTable(){
        chProductCode.setCellValueFactory(new TreeItemPropertyValueFactory<ProductOrder,String>("productID"));
        chProductName.setCellValueFactory(new TreeItemPropertyValueFactory<ProductOrder,String>("product"));
        TreeItem <ProductOrder>dataItem = new RecursiveTreeItem<ProductOrder>(result, RecursiveTreeObject::getChildren);
        ttvProductResult.setRoot(dataItem);
        ttvProductResult.setShowRoot(false);
    }


    @FXML
    private void ttvProductResultOnKeyReleased(KeyEvent keyEvent) {
        if (!ttvProductResult.getSelectionModel().isEmpty())
            if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.UP)
                populateInformation();
    }

    @FXML
    private void ttvProductResultOnMouseClicked(MouseEvent mouseEvent) {
        if (!ttvProductResult.getSelectionModel().isEmpty())
            populateInformation();
    }

    private void populateInformation(){
        ProductOrder selectedProduct = ttvProductResult.getSelectionModel().getSelectedItem().getValue();
        lblItemID.setText(selectedProduct.getProductID().split("-")[1]);
        lblItemCode.setText(selectedProduct.getProductID().split("-")[0]);
        lblItemName.setText(selectedProduct.getProduct());
        lblStocks.setText(String.valueOf(selectedProduct.getQuantity()));
        lblUnitPrice.setText(String.valueOf(selectedProduct.getUnitPrice()));
    }
}
