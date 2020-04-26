package pos.pckg.data.entity;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import pos.pckg.MiscInstances;
import pos.pckg.controller.POSInventory;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.data.CacheWriter;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.DirectoryHandler;
import pos.pckg.misc.SceneManipulator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Item extends RecursiveTreeObject<Item> implements CacheWriter {
    private SimpleIntegerProperty itemID;
    private SimpleStringProperty itemCode;
    private SimpleStringProperty itemName;
    private SimpleDoubleProperty itemPrice;
    private SimpleIntegerProperty stock;
    private SimpleDoubleProperty subtotal;
    private JFXButton btnRestock;

    public void setHbActionContainer(HBox hbActionContainer) {
        this.hbActionContainer = hbActionContainer;
    }

    public JFXButton getBtnRestock() {
        return btnRestock;
    }

    private JFXButton btnEdit;
    private JFXButton btnDelete;
    private HBox hbActionContainer;
    private StackPane rootPane;
    private SceneManipulator manipulator;
    private MiscInstances misc;

    public Item(int itemID, String itemCode, String itemName, double itemPrice, int stock, JFXButton btnRestock, HBox hbActionContainer) {
        this.itemID = new SimpleIntegerProperty(itemID);
        this.itemCode = new SimpleStringProperty(itemCode);
        this.itemName = new SimpleStringProperty(itemName);
        this.itemPrice = new SimpleDoubleProperty(itemPrice);
        this.stock = new SimpleIntegerProperty(stock);
        this.subtotal = new SimpleDoubleProperty(calculateTotal(this.itemPrice,this.stock));

        this.btnRestock = btnRestock;
        buildRestockButton(this.btnRestock);

        this.hbActionContainer = hbActionContainer;
        buildActionContainer();
    }

    private double calculateTotal(SimpleDoubleProperty itemPrice,SimpleIntegerProperty stock){
        return itemPrice.get()*stock.get();
    }

    public int getItemID() {
        return itemID.get();
    }

    public SimpleIntegerProperty itemIDProperty() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID.set(itemID);
    }

    public String getItemCode() {
        return itemCode.get();
    }

    public SimpleStringProperty itemCodeProperty() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode.set(itemCode);
    }

    public String getItemName() {
        return itemName.get();
    }

    public SimpleStringProperty itemNameProperty() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName.set(itemName);
    }

    public double getItemPrice() {
        return itemPrice.get();
    }

    public SimpleDoubleProperty itemPriceProperty() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice.set(itemPrice);
    }

    public int getStock() {
        return stock.get();
    }

    public SimpleIntegerProperty stockProperty() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock.set(stock);
    }

    public void setSubtotal(double subtotal) {
        this.subtotal.set(subtotal);
    }

    public double getSubtotal() {
        return subtotal.get();
    }

    public void setMisc(MiscInstances misc) {
        this.misc = misc;
    }

    public void setManipulator(SceneManipulator manipulator) {
        this.manipulator = manipulator;
    }

    public HBox getHbActionContainer() {
        return hbActionContainer;
    }

    private void buildRestockButton(JFXButton button) {
        button.setStyle("-fx-background-color:#1ca8d6;" +
                "-fx-border-radius: 5px;" +
                "-fx-border-color:#1994bd;" +
                "-fx-text-fill:#ffffff;");

        button.setOnAction(e->{
            writeToCache("etc\\cache-selected-item.file");
            manipulator.openDialog((StackPane) getRoot(button), "POSRestock");
        });
    }

    private void buildActionContainer() {
        hbActionContainer.setAlignment(Pos.CENTER);
        buildEditButton();
        buildDeleteButton();
        hbActionContainer.getChildren().addAll(btnEdit, btnDelete);
        HBox.setMargin(btnEdit, new Insets(0, 2, 0, 2));
        HBox.setMargin(btnDelete, new Insets(2, 2, 2, 2));
    }

    private void buildEditButton() {
        btnEdit = new JFXButton();
        Image trash = new Image(DirectoryHandler.IMG+ "pos-edit.png");
        ImageView imageView = new ImageView(trash);
        imageView.setFitHeight(18);
        imageView.setFitWidth(18);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        btnEdit.setGraphic(imageView);
        btnEdit.setStyle("-fx-background-color:#1ca8d6;" +
                "-fx-border-radius: 5px;" +
                "-fx-text-fill:#ffffff;");

        btnEdit.setOnAction(e->{
            writeToCache("etc\\cache-selected-item.file");
            manipulator.openDialog((StackPane) this.getRoot(btnEdit),"POSItemEdit");
        });

    }

    private void buildDeleteButton() {
        btnDelete = new JFXButton();
        Image trash = new Image(DirectoryHandler.IMG+ "pos-trash.png");
        ImageView imageView = new ImageView(trash);
        imageView.setFitHeight(18);
        imageView.setFitWidth(18);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        btnDelete.setGraphic(imageView);
        btnDelete.getStyleClass().add("btn-danger");

        btnDelete.setOnAction(e->{
            //When the function is pressed, a confirmation message will appear

            JFXButton btnNo = new JFXButton("No");// Confirmation button - "No"
            btnNo.setOnAction(ev -> POSMessage.closeMessage());// After pressing the No button, it simply close the messgae

            JFXButton btnYes = new JFXButton("Yes");// Confirmation button - "Yes"
            btnYes.setOnAction(ev -> {
                String sql = "update item set deleted = 1 where itemID = " + itemID.intValue();
                misc.dbHandler.startConnection();
                misc.dbHandler.execUpdate(sql);
                misc.dbHandler.closeConnection();

                Date d = new Date();
                SimpleDateFormat date = new SimpleDateFormat(BackgroundProcesses.DATE_FORMAT);
                sql = "INSERT INTO systemlogs(type, eventAction, date, userID, referencedID)" +
                        " VALUES ( 'Stock Management'" +
                        ", 'Delete'" +
                        ", '" + date.format(d) + "'" +
                        ", '" + POSInventory.userID + "'" +
                        ", '" + this.itemCode.getValue() + "');";

                misc.dbHandler.startConnection();
                misc.dbHandler.execUpdate(sql);
                misc.dbHandler.closeConnection();

                POSMessage.closeMessage();
                JFXButton btnOk = new JFXButton("Ok");
                btnOk.setOnAction(evt -> {
                    POSMessage.closeMessage();
                    POSInventory.queryAllItems();
                });

                POSMessage.showConfirmationMessage((StackPane) BackgroundProcesses.getRoot(btnRestock),
                        "Item " + this.itemID.intValue() + " is now deleted",
                        "Delete Success",
                        POSMessage.MessageType.INFORM, btnOk);


            });

            // Confirmation Message
            POSMessage.showConfirmationMessage((StackPane) getRoot(btnDelete), "Do you really want to delete selected\nitem?"
                    , "Please confirm", POSMessage.MessageType.ERROR, btnNo, btnYes);
        });

    }

    @Override
    public void writeToCache(String file){
        String cacheData = "";
        cacheData+=itemID.get();
        cacheData+="\n"+itemCode.get();
        cacheData+="\n"+itemName.get();
        cacheData+="\n"+itemPrice.get();
        cacheData+="\n"+stock.get();
        cacheData+="\n"+subtotal.get();
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(BackgroundProcesses.getFile(file)));
            writer.write(cacheData);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Node getRoot(Node control){
        Node node = control;
        while (true) {
            node = node.getParent();
            if (node.getId() != null && node.getId().equals("rootPane")) break;
        }
        return node;
    }

}
