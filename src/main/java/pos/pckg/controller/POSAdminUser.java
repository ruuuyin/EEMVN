package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
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
import pos.pckg.MiscInstances;
import pos.pckg.data.entity.User;

import java.net.URL;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class POSAdminUser extends POSAdminPanel implements Initializable {

    @FXML
    private StackPane rootPane;

    @FXML
    private JFXTreeTableView<User> ttvCustomer;

    @FXML
    private TreeTableColumn<User, String> chCustomerID;

    @FXML
    private TreeTableColumn<User, String> chCustomerName;

    @FXML
    private TreeTableColumn<User, HBox> chAction;

    @FXML
    private TreeTableColumn<User, String> chAccess;

    @FXML
    private TextField tfSearch;

    @FXML
    private JFXButton btnNew;

    protected static MiscInstances misc = new MiscInstances();
    protected static ObservableList<User> itemList = FXCollections.observableArrayList();
    private static ArrayList allItem = new ArrayList();


    @FXML
    void btnNewOnACtion(ActionEvent event) {
        misc.sceneManipulator.openDialog(rootPane, "POSAdminNewUser");
    }

    @FXML
    void tfSearchOnKeyReleased(KeyEvent event) {
        String basis = tfSearch.getText().toLowerCase();
        ArrayList result = new ArrayList() ;
        itemList.clear();
        itemList.addAll(allItem);
        if (!basis.equals("")){
            itemList.parallelStream()
                    .filter(e->
                            e.getFullName().toLowerCase().contains(basis)
                                    || e.getUid().toLowerCase().contains(basis)
                    ).forEach(e->
                    result.add(e)
            );
            itemList.clear();
            itemList.addAll(result);
        }
        System.gc();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        queryAllItems();
        loadTable();
    }

    public static void queryAllItems(){
        itemList.clear();
        String sql = "Select * from user where deleted = 0";
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);

        try{
            User user;
            while(result.next()){
                user = new User(result.getString("userID"),
                        result.getString("firstName"),
                        result.getString("middleInitial"),
                        result.getString("lastName"),
                        result.getString("access"),
                        result.getInt("accountType"),
                        new HBox());

                user.setManipulator(misc.sceneManipulator);
                user.setMisc(misc);
                itemList.add(user);
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
        chCustomerID.setCellValueFactory(new TreeItemPropertyValueFactory<User,String>("uid"));
        chCustomerName.setCellValueFactory(new TreeItemPropertyValueFactory<User,String>("fullName"));
        chAccess.setCellValueFactory(new TreeItemPropertyValueFactory<User,String>("access"));
        chAction.setCellValueFactory(new TreeItemPropertyValueFactory<User, HBox>("hbActionContainer"));

        TreeItem<User> dataItem = new RecursiveTreeItem<User>(itemList, RecursiveTreeObject::getChildren);
        ttvCustomer.setRoot(dataItem);
        ttvCustomer.setShowRoot(false);
    }

}
