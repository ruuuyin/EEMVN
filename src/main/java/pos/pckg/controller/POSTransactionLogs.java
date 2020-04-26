package pos.pckg.controller;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.StackPane;
import pos.pckg.MiscInstances;
import pos.pckg.controller.message.POSMessage;
import pos.pckg.data.entity.Transactions;
import pos.pckg.misc.BackgroundProcesses;
import pos.pckg.misc.SceneManipulator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class POSTransactionLogs implements Initializable {

    protected static SceneManipulator sceneManipulator = new SceneManipulator();
    protected static MiscInstances misc = new MiscInstances();
    protected static ObservableList<Transactions> logs = FXCollections.observableArrayList();
    private static ArrayList allItem = new ArrayList();
    private PdfWriter writer;
    private PdfDocument pdfDocument;
    private Document document;
    private File file;

    @FXML
    private StackPane rootPane;

    @FXML
    private JFXButton btnHome;

    @FXML
    private TextField tfTransac;

    @FXML
    private ComboBox<String> cbType;

    @FXML
    private ComboBox<String> cbUser;

    @FXML
    private DatePicker dpDate;

    @FXML
    private JFXButton btnSearch;

    @FXML
    private Label lblResult;

    @FXML
    private Label lblAll;

    @FXML
    private JFXButton btnSave;

    @FXML
    private JFXTreeTableView<Transactions> ttvLogTable;

    @FXML
    private TreeTableColumn<Transactions, Integer> chTransactionID;

    @FXML
    private TreeTableColumn<Transactions, String>  chType;

    @FXML
    private TreeTableColumn<Transactions, String>  chUser;

    @FXML
    private TreeTableColumn<Transactions, String>  chCustomer;

    @FXML
    private TreeTableColumn<Transactions, String>  chDate;

    @FXML
    private TreeTableColumn<Transactions, Integer> chSourceId;

    @FXML
    private TreeTableColumn<Transactions, JFXButton> chAction;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        BackgroundProcesses.populateComboFromFile("load-sl-users", cbUser);
        BackgroundProcesses.populateComboFromFile("load-tl-type",cbType);
        queryAllItems();
        loadTable();
    }

    @FXML
    void btnHomeOnAction(ActionEvent event) {
        sceneManipulator.changeScene(rootPane, "POSDashboard", " | Dashboard");
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) throws IOException {
        int fileNum = 1;
        Date d = new Date();
        SimpleDateFormat date = new SimpleDateFormat(BackgroundProcesses.DATE_FORMAT);
        file = new File("C:/POS-Reports/Transaction Logs/TransactionLogs("+date.format(d)+"-)"+fileNum+".pdf");
        do {

            if (file.exists()){
                fileNum++;
                file = new File("C:/POS-Reports/Transaction Logs/TransactionLogs("+date.format(d)+"-)"+fileNum+".pdf");
                continue;
            }else{
                file.createNewFile();
                break;
            }
        }while (true);
        writer = new PdfWriter(file);
        pdfDocument = new PdfDocument(writer);
        pdfDocument.addNewPage();
        document = new Document(pdfDocument);
        Paragraph title = new Paragraph("Transaction Logs");
        title.setFontSize(20);
        title.setBold();


        Paragraph metadata = new Paragraph("Date : "+date.format(d));
        metadata.setFontSize(10);
        metadata.setItalic();

        Paragraph results = new Paragraph("Results : "+lblResult.getText()+"\t\t\tAll : "+lblAll.getText());
        results.setFontSize(10);
        results.setItalic();

        Table table = new Table(UnitValue.createPercentArray(6)).useAllAvailableWidth();
        table.setFontSize(11);
        table.addHeaderCell("Transaction No.");
        table.addHeaderCell("Type");
        table.addHeaderCell("User");
        table.addHeaderCell("Customer");
        table.addHeaderCell("Date");
        table.addHeaderCell("Source ID");
        for (Transactions logs:logs) {
            table.addCell(logs.getTransactionID()+"");
            table.addCell(logs.getType());
            table.addCell(logs.getUser());
            table.addCell(logs.getCustomer());
            table.addCell(logs.getDate());
            table.addCell(logs.getTypeID()+"");
        }
        document.add(title);
        document.add(metadata);
        document.add(table);
        document.add(results);
        document.close();

        JFXButton btnNo = new JFXButton("Close");
        btnNo.setOnAction(ev -> POSMessage.closeMessage());

        JFXButton btnYes = new JFXButton("Open");
        btnYes.setOnAction(ev -> {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + file.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            POSMessage.closeMessage();
        });
        btnYes.setStyle("-fx-background-color:#1ca8d6;" +
                "-fx-border-radius: 5px;" +
                "-fx-text-fill:#ffffff;");

        POSMessage.showConfirmationMessage(rootPane, "Report has been saved"
                , "Saved", POSMessage.MessageType.INFORM, btnNo, btnYes);
    }

    @FXML
    void btnSearchAllOnAction(ActionEvent event){
        logs.clear();
        logs.addAll(allItem);
        lblResult.setText(logs.size()+"");
    }

    @FXML
    void btnSearchOnAction(ActionEvent event) {
        logs.clear();
        logs.addAll(allItem);
        if (dpDate.getValue()!=null){
            searchFilter(e->
                    e.getDate()
                            .equals(dpDate.getValue().format(DateTimeFormatter.ofPattern(BackgroundProcesses.DATE_FORMAT))));
        }
        if (comboHasSelected(cbUser)){
            searchFilter(e->
                    e.getUser().split(" : ")[0]
                            .contains(
                                    cbUser.getSelectionModel().getSelectedItem().split(",")[0]));
        }
        if (comboHasSelected(cbType)){
            searchFilter(e->e.getType().equals(cbType.getSelectionModel().getSelectedItem()));
        }
        if (!tfTransac.getText().equals("")){
            searchFilter(e->
                    String.valueOf(e.getTransactionID()).contains(tfTransac.getText())
                    || e.getCustomer().contains(tfTransac.getText()));
        }
        lblResult.setText(logs.size()+"");
    }


    protected void queryAllItems() {
        logs.clear();
        String sql = "Select * from Transaction";
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);
        try {
            Transactions log;
            while (result.next()) {
                log = new Transactions(result.getInt("transactionID")
                        ,result.getString("type")
                        ,result.getString("userID")
                        ,result.getString("customerID")
                        ,result.getInt("typeID")
                        ,result.getString("date")
                        ,result.getString("time")
                        ,new JFXButton("View"),misc.dbHandler);
                log.setManipulator(sceneManipulator);
                createListButton(log);
                logs.add(log);
            }
        } catch (Exception e) {
            e.printStackTrace();
            misc.dbHandler.closeConnection();
        }
        misc.dbHandler.closeConnection();
        allItem.clear();
        allItem.addAll(logs);
        lblAll.setText(String.valueOf(allItem.size()));
    }

    private void loadTable() {

        chTransactionID.setCellValueFactory(new TreeItemPropertyValueFactory<Transactions, Integer>("transactionID"));
        chSourceId.setCellValueFactory(new TreeItemPropertyValueFactory<Transactions, Integer>("typeID"));
        chType.setCellValueFactory(new TreeItemPropertyValueFactory<Transactions, String>("type"));
        chUser.setCellValueFactory(new TreeItemPropertyValueFactory<Transactions, String>("user"));
        chCustomer.setCellValueFactory(new TreeItemPropertyValueFactory<Transactions, String>("customer"));
        chDate.setCellValueFactory(new TreeItemPropertyValueFactory<Transactions, String>("date"));
        chAction.setCellValueFactory(new TreeItemPropertyValueFactory<Transactions, JFXButton>("btnView"));
        TreeItem<Transactions> dataItem = new RecursiveTreeItem<Transactions>(logs, RecursiveTreeObject::getChildren);
        ttvLogTable.setRoot(dataItem);
        ttvLogTable.setShowRoot(false);
    }

    private void createListButton(RecursiveTreeObject src) {
        Transactions log = (Transactions) src;
        log.getBtnView().setStyle("-fx-background-color:#1ca8d6;" +
                "-fx-border-radius: 5px;" +
                "-fx-border-color:#1994bd;" +
                "-fx-text-fill:#ffffff;");

        log.getBtnView().setOnAction(e -> {
            try {
                BufferedWriter bwriter = new BufferedWriter(new FileWriter("etc\\cache-tl-view.file"));
                String str = "";
                String sql ;
                if (log.getType().equals("Retail")){
                    sql = "Select * from orders where orderID = "+log.getTypeID()+"";
                    misc.dbHandler.startConnection();
                    ResultSet result = misc.dbHandler.execQuery(sql);
                    result.next();
                    str += log.getTransactionID()+"\n"+
                            log.getType()+"\n"+
                            log.getUser()+"\n"+
                            log.getCustomer()+"\n"+
                            log.getDate()+"\n"+
                            log.getTime()+"\n"+
                            log.getTypeID()+"\n"+
                            result.getInt("itemCount")+"\n"+
                            result.getInt("typeCount")+"\n"+
                            result.getDouble("subTotal")+"\n"+
                            result.getDouble("discount")+"\n"+
                            result.getDouble("total")+"\n";
                    misc.dbHandler.closeConnection();
                    bwriter.write(str);
                    bwriter.close();
                    sceneManipulator.openDialog(rootPane,"POSTransactionRetailView");
                }else if(log.getType().equals("Item Return")){
                    sql = "select returnitem.returnID as iid,item.ItemName as itemName, returnitem.reason as reason,returnitem.Quantity as Quantity, returnitem.totalAmount as total \n" +
                            "FROM returnitem \n" +
                            "JOIN item on returnitem.itemID = item.itemID \n" +
                            "where returnID = "+log.getTypeID()+"";
                    misc.dbHandler.startConnection();
                    ResultSet result = misc.dbHandler.execQuery(sql);
                    result.next();
                    str += log.getTransactionID()+"\n"+
                            log.getType()+"\n"+
                            log.getUser()+"\n"+
                            log.getCustomer()+"\n"+
                            log.getDate()+"\n"+
                            log.getTime()+"\n"+
                            log.getTypeID()+"\n"+
                            result.getInt("iid")+"\n"+
                            result.getString("itemName")+"\n"+
                            result.getString("reason")+"\n"+
                            result.getInt("Quantity")+"\n"+
                            result.getDouble("total")+"\n";
                    misc.dbHandler.closeConnection();
                    bwriter.write(str);
                    bwriter.close();
                    sceneManipulator.openDialog(rootPane,"POSTransactionReturn");
                }else if(log.getType().equals("Add Balance")){
                    sql = "Select * from recredit where recreditID = "+log.getTypeID()+"";
                    misc.dbHandler.startConnection();
                    ResultSet result = misc.dbHandler.execQuery(sql);
                    result.next();
                    str += log.getTransactionID()+"\n"+
                            log.getType()+"\n"+
                            log.getUser()+"\n"+
                            log.getCustomer()+"\n"+
                            log.getDate()+"\n"+
                            log.getTime()+"\n"+
                            log.getTypeID()+"\n"+
                            result.getDouble("Amount")+"\n"+
                            result.getString("cardID");
                    misc.dbHandler.closeConnection();
                    bwriter.write(str);
                    bwriter.close();
                    sceneManipulator.openDialog(rootPane,"POSTransactionAddBalance");
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    private boolean comboHasSelected(ComboBox box){
        return !(box.getSelectionModel().getSelectedIndex()== -1 || box.getSelectionModel().getSelectedItem().equals("---"));
    }

    private void searchFilter(Predicate<Transactions> d){
        ArrayList result = new ArrayList() ;
        logs.stream()
                .filter(d).forEach(e->
                result.add(e)
        );
        logs.clear();
        logs.addAll(result);
    }


}
