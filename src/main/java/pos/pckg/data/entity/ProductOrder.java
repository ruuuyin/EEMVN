package pos.pckg.data.entity;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ProductOrder extends RecursiveTreeObject<ProductOrder> {
    private SimpleStringProperty productID;
    private SimpleStringProperty product;
    private SimpleDoubleProperty unitPrice;
    private SimpleIntegerProperty quantity;
    private SimpleDoubleProperty total;

    public ProductOrder(String productID, String product, double unitPrice, int quantity, double total) {
        this.productID = new SimpleStringProperty(productID);
        this.product = new SimpleStringProperty(product);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.total = new SimpleDoubleProperty(total);
    }

    public String getProductID() {
        return productID.get();
    }

    public SimpleStringProperty productIDProperty() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID.set(productID);
    }

    public String getProduct() {
        return product.get();
    }

    public SimpleStringProperty productProperty() {
        return product;
    }

    public void setProduct(String product) {
        this.product.set(product);
    }

    public double getUnitPrice() {
        return unitPrice.get();
    }

    public SimpleDoubleProperty unitPriceProperty() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice.set(unitPrice);
    }

    public int getQuantity() {
        return quantity.get();
    }

    public SimpleIntegerProperty quantityProperty() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public double getTotal() {
        return total.get();
    }

    public SimpleDoubleProperty totalProperty() {
        return total;
    }

    public void setTotal(double total) {
        this.total.set(total);
    }
}
