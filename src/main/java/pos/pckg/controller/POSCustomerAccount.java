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
import pos.pckg.data.entity.Customer;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.SceneManipulator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSCustomerAccount implements Initializable {

    protected final static String S_KEY = "POS_CUSTOMER";
    @FXML
    protected  StackPane rootPane;

    @FXML
    private JFXButton btnHome;

    @FXML
    private TextField tfSearch;

    @FXML
    private JFXButton btnNew;

    @FXML
    private JFXTreeTableView<Customer> ttvCustomer;

    @FXML
    private TreeTableColumn<Customer, Integer> chCustomerID;

    @FXML
    private TreeTableColumn<Customer, String> chCustomerName;

    @FXML
    private TreeTableColumn<Customer, String> chAddress;

    @FXML
    private TreeTableColumn<Customer, String> chSex;

    @FXML
    private TreeTableColumn<Customer, String> chMobileNumber;

    @FXML
    private TreeTableColumn<Customer, String> chEmail;

    @FXML
    private TreeTableColumn<Customer, JFXButton> chCardInfo;

    @FXML
    private TreeTableColumn<Customer, HBox> chAction;

    protected static MiscInstances misc = new MiscInstances();
    protected static ObservableList<Customer> itemList = FXCollections.observableArrayList();
    private static ArrayList allItem = new ArrayList();
    public static String userID = "";

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
            BackgroundProcesses.createCacheDir("etc\\cache-selected-account.file");
            BackgroundProcesses.createCacheDir("etc\\cache-new-user.file");
            BackgroundProcesses.createCacheDir("etc\\cache-selected-customer.file");
            BackgroundProcesses.createCacheDir("etc\\cache-card-info.file");
        }),
                new KeyFrame(Duration.millis(300))
        );
        clock.setCycleCount(1);
        clock.play();
        misc = new MiscInstances();
    }

    @FXML
    void btnHomeOnAction(ActionEvent event) {
        sceneManipulator.changeScene(rootPane,"POSDashboard"," | Dashboard");
    }

    @FXML
    void btnSearchOnAction(ActionEvent event) {

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
                            e.getFirstName().toLowerCase().contains(basis)
                                    || e.getLastName().toLowerCase().contains(basis)
                    ).forEach(e->
                    result.add(e)
            );
            itemList.clear();
            itemList.addAll(result);
        }
        System.gc();
    }


    protected static SceneManipulator sceneManipulator = new SceneManipulator();

    @FXML
    void btnNewOnACtion(ActionEvent event) {

        JFXButton selectedButton = (JFXButton) event.getSource();
        if (selectedButton.equals(btnNew)){
            sceneManipulator.openDialog(rootPane,"POSCustomerAccountForm");
        }

    }




    public static void queryAllItems(){
        itemList.clear();
        String sql = "Select * from customer where deleted = 0";
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);

        try{
            Customer customer;
            while(result.next()){
                customer = new Customer(result.getInt("customerID")
                        , result.getString("firstName")
                        , result.getString("middleInitial")
                        , result.getString("lastName")
                        , result.getString("sex")
                        , result.getString("address")
                        , result.getString("phonenumber")
                        , result.getString("emailAddress")
                        , new JFXButton("View"), new HBox());

                customer.setManipulator(sceneManipulator);
                customer.setMisc(misc);
                itemList.add(customer);
            }
        }catch (Exception e){
            e.printStackTrace();
            misc.dbHandler.closeConnection();
        }
        misc.dbHandler.closeConnection();
        allItem.clear();
        allItem.addAll(itemList);
    }

    private void loadTable(){
        chCustomerID.setCellValueFactory(new TreeItemPropertyValueFactory<Customer,Integer>("customerID"));
        chCustomerName.setCellValueFactory(new TreeItemPropertyValueFactory<Customer,String>("fullName"));
        chAddress.setCellValueFactory(new TreeItemPropertyValueFactory<Customer,String>("address"));
        chSex.setCellValueFactory(new TreeItemPropertyValueFactory<Customer,String>("sex"));
        chEmail.setCellValueFactory(new TreeItemPropertyValueFactory<Customer,String>("email"));
        chMobileNumber.setCellValueFactory(new TreeItemPropertyValueFactory<Customer,String>("phoneNumber"));
        chCardInfo.setCellValueFactory(new TreeItemPropertyValueFactory<Customer, JFXButton>("btnViewCard"));
        chAction.setCellValueFactory(new TreeItemPropertyValueFactory<Customer, HBox>("hbActionContainer"));

        TreeItem<Customer> dataItem = new RecursiveTreeItem<Customer>(itemList, RecursiveTreeObject::getChildren);
        ttvCustomer.setRoot(dataItem);
        ttvCustomer.setShowRoot(false);
    }

}
