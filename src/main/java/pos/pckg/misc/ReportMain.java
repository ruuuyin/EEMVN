package pos.pckg.misc;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.io.FileNotFoundException;

public class ReportMain {
    public static void main(String[] args) throws FileNotFoundException {
        // Creating a PdfWriter
        String dest = "C:/sample.pdf";
        PdfWriter writer = new PdfWriter(new File(dest));

        // Creating a PdfDocument
        PdfDocument pdfDoc = new PdfDocument(writer);
        // Adding a new page
        pdfDoc.addNewPage();
        // Creating a Document
        Document document = new Document(pdfDoc);

        Paragraph paragraph = new Paragraph("Paragraph");
        paragraph.setFontSize(20);
        paragraph.setBold();
        // Closing the document
        Table table = new Table(UnitValue.createPercentArray(8)).useAllAvailableWidth();

        for (int i = 0; i < 16; i++) {
            table.addCell("hi");
        }
        document.add(paragraph);
        document.add(table);
        document.close();
        System.out.println("PDF Created");
    }
}
