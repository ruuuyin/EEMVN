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
import pos.pckg.data.entity.SystemLog;
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

public class POSSystemLogs implements Initializable {

    protected static SceneManipulator sceneManipulator = new SceneManipulator();
    protected static MiscInstances misc = new MiscInstances();
    protected static ObservableList<SystemLog> logs = FXCollections.observableArrayList();
    private static ArrayList allItem = new ArrayList();
    private PdfWriter writer;
    private PdfDocument pdfDocument;
    private Document document;

    @FXML
    private StackPane rootPane;
    @FXML
    private JFXButton btnHome;
    @FXML
    private ComboBox<String> cbUser;
    @FXML
    private ComboBox<String> cbType;
    @FXML
    private ComboBox<String> cbAction;
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
    private JFXTreeTableView<SystemLog> ttvLogTable;
    @FXML
    private TreeTableColumn<SystemLog, Integer> chLogID;
    @FXML
    private TreeTableColumn<SystemLog, String> chType;
    @FXML
    private TreeTableColumn<SystemLog, String> chEvent;
    @FXML
    private TreeTableColumn<SystemLog, String> chDate;
    @FXML
    private TreeTableColumn<SystemLog, String> chUser;
    @FXML
    private TreeTableColumn<SystemLog, JFXButton> chAction;
    @FXML
    private TreeTableColumn<SystemLog, String> chReference;
    @FXML
    private TextField tfReferencedID;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        queryAllItems();
        loadTable();
        BackgroundProcesses.populateComboFromFile("load-sl-users", cbUser);
        BackgroundProcesses.populateComboFromFile("load-sl-type", cbType);
        BackgroundProcesses.populateComboFromFile("load-sl-all-action", cbAction);

    }

    @FXML
    void btnHomeOnAction(ActionEvent event) {
        sceneManipulator.changeScene(rootPane, "POSDashboard", " | Dashboard");
    }

    private File file;
    @FXML
    void btnSaveOnAction(ActionEvent event) throws IOException {
        int fileNum = 1;
        Date d = new Date();
        SimpleDateFormat date = new SimpleDateFormat(BackgroundProcesses.DATE_FORMAT);
        file = new File("C:/POS-Reports/System Logs/SystemLogs("+date.format(d)+"-)"+fileNum+".pdf");
        do {

            if (file.exists()){
                fileNum++;
                file = new File("C:/POS-Reports/System Logs/SystemLogs("+date.format(d)+"-)"+fileNum+".pdf");
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
        Paragraph title = new Paragraph("System Logs");
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
        table.addHeaderCell("Log ID");
        table.addHeaderCell("Type");
        table.addHeaderCell("Event Action");
        table.addHeaderCell("Date");
        table.addHeaderCell("User");
        table.addHeaderCell("Referenced ID");
        for (SystemLog logs:logs) {
            table.addCell(logs.getLogID()+"");
            table.addCell(logs.getType());
            table.addCell(logs.getEventAction());
            table.addCell(logs.getDate());
            table.addCell(logs.getUserID());
            table.addCell(logs.getReferencedID());
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
                    e.getUserID().split(" : ")[0]
                            .contains(
                                    cbUser.getSelectionModel().getSelectedItem().split(",")[0]));
        }
        if (comboHasSelected(cbAction)){
            searchFilter(e->
                e.getEventAction().equals(cbAction.getSelectionModel().getSelectedItem())
            );
        }
        if (comboHasSelected(cbType)){
            searchFilter(e->e.getType().equals(cbType.getSelectionModel().getSelectedItem()));
        }
        if (!tfReferencedID.getText().equals("")){
            searchFilter(e->e.getReferencedID().contains(tfReferencedID.getText()));
        }
        lblResult.setText(logs.size()+"");
    }


    private void loadTable() {

        chLogID.setCellValueFactory(new TreeItemPropertyValueFactory<SystemLog, Integer>("logID"));
        chType.setCellValueFactory(new TreeItemPropertyValueFactory<SystemLog, String>("type"));
        chEvent.setCellValueFactory(new TreeItemPropertyValueFactory<SystemLog, String>("eventAction"));
        chDate.setCellValueFactory(new TreeItemPropertyValueFactory<SystemLog, String>("date"));
        chUser.setCellValueFactory(new TreeItemPropertyValueFactory<SystemLog, String>("userID"));
        chReference.setCellValueFactory(new TreeItemPropertyValueFactory<SystemLog, String>("referencedID"));
        chAction.setCellValueFactory(new TreeItemPropertyValueFactory<SystemLog, JFXButton>("btnView"));
        TreeItem<SystemLog> dataItem = new RecursiveTreeItem<SystemLog>(logs, RecursiveTreeObject::getChildren);
        ttvLogTable.setRoot(dataItem);
        ttvLogTable.setShowRoot(false);
    }

    protected void queryAllItems() {
        logs.clear();
        String sql = "Select * from systemlogs";
        misc.dbHandler.startConnection();
        ResultSet result = misc.dbHandler.execQuery(sql);
        try {
            SystemLog log;
            while (result.next()) {
                log = new SystemLog(result.getInt("logID")
                        , result.getString("type")
                        , result.getString("eventAction")
                        , result.getString("date")
                        , result.getString("userID")
                        , result.getString("referencedID")
                        , new JFXButton("View"),misc.dbHandler);
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


    private void createListButton(RecursiveTreeObject src) {
        SystemLog log = (SystemLog) src;
        log.getBtnView().setStyle("-fx-background-color:#1ca8d6;" +
                "-fx-border-radius: 5px;" +
                "-fx-border-color:#1994bd;" +
                "-fx-text-fill:#ffffff;");

        log.getBtnView().setOnAction(e -> {
            try {
                BufferedWriter bwriter = new BufferedWriter(new FileWriter("etc\\cache-sl-view.file"));
                String str = "";
                String sql ;
                if (log.getType().equals("Stock Management")){
                    sql = "Select itemName from item where itemCode = '"+log.getReferencedID()+"'";
                    misc.dbHandler.startConnection();
                    ResultSet result = misc.dbHandler.execQuery(sql);
                    result.next();
                    str+=log.getLogID()+"\n"+
                            log.getType()+"\n"+
                            log.getEventAction()+"\n"+
                            log.getDate()+"\n"+
                            log.getUserID()+"\n"+
                            log.getReferencedID()+"\n"+
                            result.getString("itemName");
                    misc.dbHandler.closeConnection();
                    bwriter.write(str);
                    bwriter.close();
                }else if(log.getType().equals("Customer Management")){
                    sql = "Select firstName,lastName from customer where customerID = "+log.getReferencedID()+"";
                    misc.dbHandler.startConnection();
                    ResultSet result = misc.dbHandler.execQuery(sql);
                    result.next();
                    str+=log.getLogID()+"\n"+
                            log.getType()+"\n"+
                            log.getEventAction()+"\n"+
                            log.getDate()+"\n"+
                            log.getUserID()+"\n"+
                            log.getReferencedID()+"\n"+
                            result.getString("firstName")+" "+result.getString("lastName").charAt(0)+".";
                    misc.dbHandler.closeConnection();
                    bwriter.write(str);
                    bwriter.close();
                }
                sceneManipulator.openDialog(rootPane,"POSSystemLogsView");
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


    private void searchFilter(Predicate<SystemLog> d){
        ArrayList result = new ArrayList() ;
        logs.stream()
                .filter(d).forEach(e->
                result.add(e)
        );
        logs.clear();
        logs.addAll(result);
    }


}
