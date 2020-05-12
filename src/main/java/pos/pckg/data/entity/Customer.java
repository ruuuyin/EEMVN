package pos.pckg.data.entity;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import pos.pckg.MiscInstances;
import pos.pckg.controller.POSCustomerAccount;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.data.CacheWriter;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.DataBridgeDirectory;
import pos.pckg.misc.DirectoryHandler;
import pos.pckg.misc.SceneManipulator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Customer extends RecursiveTreeObject<Customer> implements CacheWriter {

    private Integer customerID;
    private String fullName, firstName, middleInitial, lastName, sex, address, phoneNumber, email;
    private JFXButton btnViewCard, btnEdit, btnDelete;
    private HBox hbActionContainer;
    private StackPane rootPane;
    private SceneManipulator manipulator;
    private MiscInstances misc;


    public Customer(int customerID, String firstName, String middleInitial, String lastName, String sex, String address, String phoneNumber, String email, JFXButton btnViewCard, HBox hbActionContainer) {
        this.customerID = new Integer(customerID);
        this.firstName = firstName;
        this.middleInitial = middleInitial;
        this.lastName = lastName;
        fullName = (firstName + " " + (middleInitial.equals("N/A")?" ":(middleInitial+". ")) + lastName);
        this.sex = sex;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        buildViewButton(btnViewCard);
        this.btnViewCard = btnViewCard;

        this.hbActionContainer = hbActionContainer;
        buildActionContainer();

    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        fullName = (firstName+" "+middleInitial+". "+lastName);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public JFXButton getBtnViewCard() {
        return btnViewCard;
    }

    public void setBtnViewCard(JFXButton btnViewCard) {
        this.btnViewCard = btnViewCard;
    }

    public HBox getHbActionContainer() {
        return hbActionContainer;
    }

    public void setMisc(MiscInstances misc) {
        this.misc = misc;
    }

    public void setManipulator(SceneManipulator manipulator) {
        this.manipulator = manipulator;
    }

    private void buildViewButton(JFXButton button) {
        button.setStyle("-fx-background-color:#1ca8d6;" +
                "-fx-border-radius: 5px;" +
                "-fx-border-color:#1994bd;" +
                "-fx-text-fill:#ffffff;");

        button.setOnAction(e->{
            writeToCache(DataBridgeDirectory.DOCUMENT+"etc\\cache-selected-customer.file@@@"+DataBridgeDirectory.DOCUMENT+"etc\\cache-card-info.file");
            manipulator.openDialog((StackPane) getRoot(button), "POSSelectedCardInfo");
        });
    }

    private Node getRoot(Node control){
        Node node = control;
        while (true) {
            node = node.getParent();
            if (node.getId() != null && node.getId().equals("rootPane")) break;
        }
        return node;
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
            writeToCache(DataBridgeDirectory.DOCUMENT+"etc\\cache-selected-customer.file@@@"+DataBridgeDirectory.DOCUMENT+"etc\\cache-card-info.file");
            manipulator.openDialog((StackPane) this.getRoot(btnEdit),"POSCustomerEdit");
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
                String sql ="Update card set isActive = 0 where customerID = "+customerID.intValue();
                misc.dbHandler.startConnection();
                misc.dbHandler.execUpdate(sql);
                misc.dbHandler.closeConnection();

                sql = "Update customer set deleted = 1 where customerID = "+customerID.intValue();
                misc.dbHandler.startConnection();
                misc.dbHandler.execUpdate(sql);
                misc.dbHandler.closeConnection();

                Date d = new Date();
                SimpleDateFormat date = new SimpleDateFormat(BackgroundProcesses.DATE_FORMAT);
                sql = "INSERT INTO systemlogs(type, eventAction, date, userID, referencedID)" +
                        " VALUES ( 'Customer Management'" +
                        ", 'Delete'" +
                        ", '" + date.format(d) + "'" +
                        ", '" + POSCustomerAccount.userID + "'" +
                        ", " + customerID.intValue() + ");";

                misc.dbHandler.startConnection();
                misc.dbHandler.execUpdate(sql);
                misc.dbHandler.closeConnection();

                POSMessage.closeMessage();
                JFXButton btnOk = new JFXButton("Ok");
                btnOk.setOnAction(evt->{
                    POSMessage.closeMessage();
                    POSCustomerAccount.queryAllItems();
                });

                POSMessage.showConfirmationMessage((StackPane) getRoot(btnDelete),
                        "Customer "+customerID+" has been deleted",
                        "Delete Success",
                        POSMessage.MessageType.INFORM,btnOk);
            });

            // Confirmation Message
            POSMessage.showConfirmationMessage((StackPane) getRoot(btnDelete), "Do you really want to delete the" +
                            "\nselected customer from list?"
                    , "Please Confirm", POSMessage.MessageType.ERROR, btnNo, btnYes);
        });

    }

    @Override
    public void writeToCache(String files) {
        String file[] = files.split("@@@");

        String cache = customerID +
                "\n" + firstName +
                "\n" + (middleInitial.equals("") ? "N/A" : middleInitial) +
                "\n" + lastName +
                "\n" + sex +
                "\n"+address+
                "\n"+phoneNumber+
                "\n"+email;
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(BackgroundProcesses.getFile(file[0])));
            writer.write(cache);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String sql = "Select * from Card where customerID = "+customerID;
        misc.dbHandler.startConnection();
        ResultSet resultSet = misc.dbHandler.execQuery(sql);
        try {
            if (resultSet.next()){
                cache = resultSet.getString("cardID")+
                        "\n"+resultSet.getDouble("credits")+
                        "\n"+resultSet.getByte("isActive")+
                        "\n"+resultSet.getString("activationDate")+
                        "\n"+resultSet.getString("expiryDate")+
                        "\n"+resultSet.getString("PIN")+
                        "\n"+resultSet.getInt("customerID");
                writer = new BufferedWriter(new FileWriter(BackgroundProcesses.getFile(file[1])));
                writer.write(cache);
                writer.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            misc.dbHandler.closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}
