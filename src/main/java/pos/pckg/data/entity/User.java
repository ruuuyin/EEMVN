package pos.pckg.data.entity;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import pos.pckg.MiscInstances;
import pos.pckg.controller.POSAdminUser;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.data.CacheWriter;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.DirectoryHandler;
import pos.pckg.misc.SceneManipulator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class User extends RecursiveTreeObject<User> implements CacheWriter {
    private String uid,fname,mi,lname,access,fullName;
    private int accountType;
    private HBox hbActionContainer;
    private StackPane rootPane;
    private SceneManipulator manipulator;
    private MiscInstances misc;
    private JFXButton btnpass, btnEdit, btnDelete;


    public User(String uid, String fname, String mi, String lname, String access, int accountType, HBox hbActionContainer) {
        this.uid = uid;
        this.fname = fname;
        this.mi = mi;
        this.lname = lname;
        fullName = (fname + " " + (mi.equals("N/A")?" ":(mi+". ")) + lname);
        String accessArray[] = access.split(",");
        access = "";
        for (String a:accessArray) {
            switch (a){
                case "cashier":
                    a = " Cashier ";
                    break;
                case "inventory":
                    a = " Inventory ";
                    break;
                case "customer":
                    a = " Customer ";
                    break;
                case "transaction":
                    a = " Transaction Logs ";
                    break;
                case "system":
                    a = " System Logs ";
                    break;
                case "admin":
                    a = " Admin Panel ";
                    break;
            }

            access += (a+",");
        }
        this.access = access.substring(0, access.length() - 1);
        this.accountType = accountType;
        this.hbActionContainer = hbActionContainer;
        buildActionContainer();
    }

    private void buildActionContainer() {
        hbActionContainer.setAlignment(Pos.CENTER);
        buildEditButton();
        buildDeleteButton();
        builPassRecoverButton();
        if (accountType == 1) hbActionContainer.setDisable(true);
        hbActionContainer.getChildren().addAll(btnpass,btnEdit, btnDelete);
        HBox.setMargin(btnEdit, new Insets(2, 2, 2, 2));
        HBox.setMargin(btnDelete, new Insets(2, 2, 2, 2));
        HBox.setMargin(btnpass, new Insets(2, 2, 2, 2));
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
            //TODO Edit Action
            writeToCache("etc\\cache-admin-selected-user.file");
            manipulator.openDialog((StackPane) BackgroundProcesses.getRoot(btnEdit),"POSAdminEditUser");
        });

    }

    private void builPassRecoverButton() {
        btnpass = new JFXButton();
        Image trash = new Image(DirectoryHandler.IMG+ "pos-pin-reset.png");
        ImageView imageView = new ImageView(trash);
        imageView.setFitHeight(18);
        imageView.setFitWidth(18);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        btnpass.setGraphic(imageView);
        btnpass.setStyle("-fx-background-color:#1ca8d6;" +
                "-fx-border-radius: 5px;" +
                "-fx-text-fill:#ffffff;");

        btnpass.setOnAction(e->{
            //TODO Edit Action
            writeToCache("etc\\cache-admin-selected-user.file");
            manipulator.openDialog((StackPane) BackgroundProcesses.getRoot(btnpass),"POSAdminPinRecovery");
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
                String sql  = "Update user set deleted = 1 where userID = '"+uid+"'";
                misc.dbHandler.startConnection();
                misc.dbHandler.execUpdate(sql);
                misc.dbHandler.closeConnection();


                POSMessage.closeMessage();
                JFXButton btnOk = new JFXButton("Ok");
                btnOk.setOnAction(evt->{
                    POSMessage.closeMessage();
                    POSAdminUser.queryAllItems();
                });

                POSMessage.showConfirmationMessage((StackPane) BackgroundProcesses.getRoot(btnDelete),
                        "User "+uid+" has been deleted",
                        "Delete Success",
                        POSMessage.MessageType.INFORM,btnOk);
            });

            // Confirmation Message
            POSMessage.showConfirmationMessage((StackPane) BackgroundProcesses.getRoot(btnDelete), "Do you really want to delete the" +
                            "\nselected user from list?"
                    , "Please Confirm", POSMessage.MessageType.ERROR, btnNo, btnYes);
        });

    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        fullName = (fname+" "+mi+". "+lname);
    }


    public void setMisc(MiscInstances misc) {
        this.misc = misc;
    }

    public void setManipulator(SceneManipulator manipulator) {
        this.manipulator = manipulator;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getMi() {
        return mi;
    }

    public void setMi(String mi) {
        this.mi = mi;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public HBox getHbActionContainer() {
        return hbActionContainer;
    }

    public void setHbActionContainer(HBox hbActionContainer) {
        this.hbActionContainer = hbActionContainer;
    }

    @Override
    public void writeToCache(String file) {

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            //    private String uid,fname,mi,lname,access,fullName;
            //    private int accountType;
            String data=uid+"\n"+fname+"\n"+mi+"\n"+lname+"\n"+access+"\n"+accountType;
            writer.write(data);
            writer.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
