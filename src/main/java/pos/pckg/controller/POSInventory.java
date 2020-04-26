package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import pos.pckg.MiscInstances;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.data.CacheWriter;
import pos.pckg.data.entity.Item;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.SceneManipulator;

import java.io.*;
import java.net.URL;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSInventory implements Initializable, CacheWriter {
    //TODO Restock function is not yet working
    @FXML
    private StackPane rootPane;

    @FXML
    private JFXButton btnHome;

    @FXML
    private TextField tfSearch;

    @FXML
    private JFXButton btnNew;


    @FXML
    private JFXTreeTableView<Item> ttvCustomer;

    @FXML
    private TreeTableColumn<Item, Integer> chItemID;

    @FXML
    private TreeTableColumn<Item, String> chItemCode;

    @FXML
    private TreeTableColumn<Item, String> chItemName;


    @FXML
    private TreeTableColumn<Item, Double> chUnitPrice;

    @FXML
    private TreeTableColumn<Item,Integer> chStock;

    @FXML
    private TreeTableColumn<Item, JFXButton> chRestock;

    @FXML
    private TreeTableColumn<Item,HBox> chAction;

    @FXML
    private TreeTableColumn<Item, Double> chTotal;


    protected static SceneManipulator sceneManipulator = new SceneManipulator();
    protected static MiscInstances misc = new MiscInstances();
    protected static ObservableList<Item> itemList = FXCollections.observableArrayList();
    private static ArrayList allItem = new ArrayList();
    public static String userID = "";

    public static void queryAllItems() {
        itemList.clear();
        String sql = "Select * from Item where deleted = 0";
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);
        try {
            Item item;
            while (result.next()) {
                item = new Item(result.getInt("itemID")
                        , result.getString("itemCode")
                        , result.getString("itemName")
                        , result.getDouble("itemPrice")
                        , result.getInt("stock")
                        , new JFXButton("Modify")
                        , new HBox());
                item.setManipulator(sceneManipulator);
                item.setMisc(misc);
                itemList.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            misc.dbHandler.closeConnection();
        }
        misc.dbHandler.closeConnection();
        allItem.clear();
        allItem.addAll(itemList);
    }

    @FXML
    void btnHomeOnAction(ActionEvent event) {
        sceneManipulator.changeScene(rootPane, "POSDashboard", " | Dashboard");
    }

    @FXML
    void btnSearchOnAction(ActionEvent event) {
        String basis = tfSearch.getText().toLowerCase();
        ArrayList result = new ArrayList() ;
        itemList.stream()
                .filter(e->
                    e.getItemCode().toLowerCase().contains(basis)
                            || e.getItemName().toLowerCase().contains(basis)
                ).forEach(e->
                    result.add(e)
                );
        itemList.clear();
        itemList.addAll(result);

    }


    @FXML
    void functionButtonOnAction(ActionEvent event) {
            try {
                writeToCache("etc\\cache-selected-item.file");
                JFXButton selectedButton = (JFXButton) event.getSource();
                if (selectedButton.equals(this.btnNew)) {
                    sceneManipulator.openDialog(rootPane, "POSNewItem");

                }
            }catch (Exception e){
                e.printStackTrace();
                POSMessage.showMessage(rootPane,e.getMessage(),"System Error", POSMessage.MessageType.ERROR);
            }
    }

    private void deleteItemFromButtonAction(ActionEvent e){
        //Creating query for update
        Item selectedItem = ttvCustomer.getSelectionModel().getSelectedItem().getValue();
        String sql = "Delete from item where itemId = "+selectedItem.getItemID();

        //To update the database
        misc.dbHandler.startConnection();
        misc.dbHandler.execUpdate(sql);
        misc.dbHandler.closeConnection();

        //Closing the Message Dialog
        POSMessage.closeMessage();

        //Requery the table
        queryAllItems();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Scanner scan = new Scanner(new FileInputStream("etc\\cache-user.file"));
            userID = scan.nextLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Timeline clock = new Timeline(new KeyFrame(Duration.millis(300), e -> {
            queryAllItems();
            loadTable();
            BackgroundProcesses.createCacheDir("etc\\cache-selected-item.file");
        }),
                new KeyFrame(Duration.millis(300))
        );
        clock.setCycleCount(1);
        clock.play();

    }

    private void loadTable(){
        chItemID.setCellValueFactory(new TreeItemPropertyValueFactory<Item,Integer>("itemID"));
        chItemCode.setCellValueFactory(new TreeItemPropertyValueFactory<Item,String>("itemCode"));
        chItemName.setCellValueFactory(new TreeItemPropertyValueFactory<Item,String>("itemName"));
        chUnitPrice.setCellValueFactory(new TreeItemPropertyValueFactory<Item,Double>("itemPrice"));
        chStock.setCellValueFactory(new TreeItemPropertyValueFactory<Item,Integer>("stock"));
        chTotal.setCellValueFactory(new TreeItemPropertyValueFactory<Item,Double>("subtotal"));
        chRestock.setCellValueFactory(new TreeItemPropertyValueFactory<Item,JFXButton>("btnRestock"));
        chAction.setCellValueFactory(new TreeItemPropertyValueFactory<Item,HBox>("hbActionContainer"));

        TreeItem<Item> dataItem = new RecursiveTreeItem<Item>(itemList, RecursiveTreeObject::getChildren);
        ttvCustomer.setRoot(dataItem);
        ttvCustomer.setShowRoot(false);
    }

     public void writeToCache(String file){
        if (hasSelectedItem()){
            Item selectedItem = ttvCustomer.getSelectionModel().getSelectedItem().getValue();
            String cacheData = "";
            cacheData+=selectedItem.getItemID();
            cacheData+="\n"+selectedItem.getItemCode();
            cacheData+="\n"+selectedItem.getItemName();
            cacheData+="\n"+selectedItem.getItemPrice();
            cacheData+="\n"+selectedItem.getStock();
            cacheData+="\n"+selectedItem.getSubtotal();
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(BackgroundProcesses.getFile(file)));
                writer.write(cacheData);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private boolean hasSelectedItem(){
        return ttvCustomer.getSelectionModel().getSelectedItem() != null;
    }

    @FXML
    private void tfSearchOnKeyReleased(KeyEvent keyEvent) {
        String basis = tfSearch.getText().toLowerCase();
        ArrayList result = new ArrayList() ;
        itemList.clear();
        itemList.addAll(allItem);
        if (!basis.equals("")){
            itemList.parallelStream()
                    .filter(e->
                            e.getItemCode().toLowerCase().contains(basis)
                                    || e.getItemName().toLowerCase().contains(basis)
                    ).forEach(e->
                    result.add(e)
            );
            itemList.clear();
            itemList.addAll(result);
        }
        System.gc();
    }


}
