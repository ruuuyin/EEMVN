package pos.pckg.controller;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.data.entity.Item;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.DataBridgeDirectory;
import pos.pckg.misc.InputRestrictor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Scanner;

public class POSItemEdit extends POSInventory {

    @FXML
    private StackPane rootPane;

    @FXML
    private Label lblItemID;

    @FXML
    private TextField tfItemCode;

    @FXML
    private TextField tfItemName;


    @FXML
    private TextField tfPrice;

    @FXML
    private JFXButton btnCancel;

    @FXML
    private JFXButton btnSave;


    private String oldCode = "";
    private String oldName = "";
    private String oldPrice = "";
    private int itemId = 0;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            /* To get the selected pckg.data from table, it is written to a text file 'cache-selected-item.file'*/
            Scanner scan = new Scanner(new FileInputStream(BackgroundProcesses.getFile(DataBridgeDirectory.DOCUMENT+"etc\\cache-selected-item.file")));
            itemId = Integer.parseInt(scan.nextLine()); // Line 1 of the cache = ItemID
            lblItemID.setText("Item ID : "+itemId);
            oldCode = scan.nextLine();                  // Line 2 of the cache = Item code
            tfItemCode.setText(oldCode);
            oldName = scan.nextLine();                  // Line 3 of the cache = Item name
            tfItemName.setText(oldName);
            oldPrice = scan.nextLine();                 // Line 4 of the cache = Item price
            double price = Double.parseDouble(oldPrice);// Parsing the price to enable mathematical computation
            tfPrice.setText(price+"");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputRestrictor.numbersInput(tfItemCode);
        InputRestrictor.limitInput(tfItemCode,13);

    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        sceneManipulator.closeDialog();
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {

        boolean codeExist = !this.tfItemCode.getText().equals(oldCode) ? codeExist(this.tfItemCode.getText()) : false;

        /*Handling Common Errors are processed within this conditions.
        * 1 - nothing changed
        * 2 - textfields are empty
        * 3 - code already exist in the table
        * 4 - code is invalid
        * */


        /*
         * SCENARIO 1: If the user pressed the save button without editing any of the textfields
         *          Simply close the dialog form for editing
         */
        if (nothingHasChanged()){
            sceneManipulator.closeDialog(); // Closing the dialog if nothing has changed

        /*
         * SCENARIO 2: If there is an empty fields display an error message
         *           that shows Invalid value of the text fields
         */
        }else if (hasEmptyField()){
            //An Error Message if the some of the textboxes are Empty
            POSMessage.showMessage(rootPane
                    ,"Please fill all the Fields"
                    ,"Invalid Value"
                    , POSMessage.MessageType.ERROR);

        /*
        * SCENARIO 3 : Excluding its own item code, if the Item code that is entered
        *            are same with one of the products from table. Display an Error message
        *            that tells the user that the item already exist
        * */
        }else if (codeExist){
            //An Error Message of the Item code that is entered is already exist
            POSMessage.showMessage(rootPane
                    , "Item Code Already Exist"
                    , "Item Code Error"
                    , POSMessage.MessageType.ERROR);

        /*
        * SCENARIO 4 : If the user entered the Wrong Item code. for the
        *              standard item code its consists of 12 or 13 digit,
        *              if the user entered below the standard it will return
        *              an error message
        * */
        }else if (tfItemCode.getText().length()<12){

            //An Error Message if the Item code entered is less than the digit of standard bar code
            POSMessage.showMessage(rootPane
                    , "You've entered an Invalid Code"
                    , "Invalid Code"
                    , POSMessage.MessageType.ERROR);


        /*
        * Finally, the real update procedure will be processed here
        * */
        }else{
            JFXButton btnNo = new JFXButton("No");
            btnNo.setOnAction(e->POSMessage.closeMessage());

            JFXButton btnYes = new JFXButton("Yes");
            btnYes.setOnAction(e->{

                POSMessage.closeMessage();

                //This is where the update will process
                String sql = "Update Item set " +
                        "itemName = '" + tfItemName.getText() + "', " +
                        "itemCode = '" + tfItemCode.getText() + "'," +
                        "itemPrice = " + tfPrice.getText() + "" +
                        " where itemID = " + itemId;//Basis for the update is the Id of the selected item

                misc.dbHandler.startConnection();
                misc.dbHandler.execUpdate(sql);
                misc.dbHandler.closeConnection();

                Date d = new Date();
                SimpleDateFormat date = new SimpleDateFormat(BackgroundProcesses.DATE_FORMAT);
                sql = "INSERT INTO systemlogs(type, eventAction, date, userID, referencedID)" +
                        " VALUES ( 'Stock Management', 'Edit', '" + date.format(d) + "', '" + POSInventory.userID + "', '" + tfItemCode.getText() + "');";

                misc.dbHandler.startConnection();
                misc.dbHandler.execUpdate(sql);
                misc.dbHandler.closeConnection();


                JFXButton btnOk = new JFXButton("Ok");
                btnOk.setOnAction(ev -> {
                    POSMessage.closeMessage();
                    queryAllItems();
                    sceneManipulator.closeDialog();

                });

                POSMessage.showConfirmationMessage(rootPane,
                        "Item "+itemId+" is now updated",
                        "Update Success",
                        POSMessage.MessageType.INFORM,btnOk);

            });

            POSMessage.showConfirmationMessage(rootPane,"Do you really want to update the item?"
                    ,"Please Confirm Update", POSMessage.MessageType.CONFIRM,btnNo,btnYes);

        }


    }

    private boolean hasEmptyField(){
        return tfItemCode.getText().equals("") ||
                tfItemName.getText().equals("") ||
                tfPrice.getText().equals("");
    }

    private boolean codeExist(String itemCode){
        boolean itemExist = false;
        for (Item item:itemList) {
            if (item.getItemCode().equals(itemCode)){
                itemExist = true;
                break;
            }
        }
        return itemExist;
    }

    private boolean nothingHasChanged(){
        return tfPrice.getText().equals(oldPrice)
                && tfItemCode.getText().equals(oldCode)
                && tfItemName.getText().equals(oldName);
    }

}
