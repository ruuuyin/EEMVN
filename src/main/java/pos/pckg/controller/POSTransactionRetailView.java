package pos.pckg.controller;

import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import pos.pckg.data.entity.RetailOrder;
import pos.pckg.misc.DataBridgeDirectory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSTransactionRetailView extends POSTransactionLogs implements Initializable {
    private ObservableList<RetailOrder> productList = FXCollections.observableArrayList();
    @FXML
    private Label lblTransactionNo;

    @FXML
    private TextField tfType;

    @FXML
    private TextField tfUser;

    @FXML
    private TextField tfCustomer;

    @FXML
    private TextField tfDate;

    @FXML
    private TextField tfTime;

    @FXML
    private TextField tfOrder;

    @FXML
    private JFXTreeTableView<RetailOrder> ttvOrders;

    @FXML
    private TreeTableColumn<RetailOrder, Integer> chItemID;

    @FXML
    private TreeTableColumn<RetailOrder, String> chItemName;

    @FXML
    private TreeTableColumn<RetailOrder, Integer> chQuantity;

    @FXML
    private TreeTableColumn<RetailOrder, Double> chTotal;

    @FXML
    private Label lblNumItems;

    @FXML
    private Label lblNumTypes;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblDiscount;

    @FXML
    private Label lblTotal;

    @FXML
    void btnCloseOnAction(ActionEvent event) {
        sceneManipulator.closeDialog();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Scanner scan = new Scanner(new FileInputStream(DataBridgeDirectory.DOCUMENT+"etc\\cache-tl-view.file"));
            lblTransactionNo.setText(scan.nextLine());
            tfType.setText(scan.nextLine());
            tfUser.setText(scan.nextLine());
            tfCustomer.setText(scan.nextLine());
            tfDate.setText(scan.nextLine());
            tfTime.setText(scan.nextLine());
            tfOrder.setText(scan.nextLine());
            lblNumItems.setText("Number of Items : "+scan.nextLine());
            lblNumTypes.setText("Number of Types : "+scan.nextLine());
            lblSubtotal.setText("Subtotal : "+scan.nextLine());
            lblDiscount.setText("Discount(%) : "+scan.nextLine());
            lblTotal.setText("Total : "+scan.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String sql = "Select item.itemID as iid" +
                ", item.ItemName as name" +
                ",orderItem.quantity as quantity" +
                ",orderItem.subtotal as total from orderItem JOIN item on orderItem.itemID = item.itemID where orderItem.orderId = "+tfOrder.getText();
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);
        try{
            while(result.next()){
                RetailOrder order = new RetailOrder(result.getInt("iid"),result.getInt("quantity"),result.getString("name"),result.getDouble("total"));
                productList.add(order);
            }
            misc.dbHandler.closeConnection();
        }catch (Exception e){
            e.printStackTrace();
            misc.dbHandler.closeConnection();
        }

        loadTable();
    }

    private void loadTable(){
        chItemID.setCellValueFactory(new TreeItemPropertyValueFactory<RetailOrder,Integer>("id"));
        chItemName.setCellValueFactory(new TreeItemPropertyValueFactory<RetailOrder,String>("name"));
        chQuantity.setCellValueFactory(new TreeItemPropertyValueFactory<RetailOrder,Integer>("quantity"));
        chTotal.setCellValueFactory(new TreeItemPropertyValueFactory<RetailOrder,Double>("subtotal"));
        TreeItem<RetailOrder> dataItem = new RecursiveTreeItem<RetailOrder>(productList, RecursiveTreeObject::getChildren);
        ttvOrders.setRoot(dataItem);
        ttvOrders.setShowRoot(false);
    }

}
