package pos.pckg.data.entity;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

public class RetailOrder extends RecursiveTreeObject<RetailOrder> {
    private int id,quantity;
    private String name;
    private double subtotal;

    public RetailOrder(int id, int quantity, String name, double subtotal) {
        this.id = id;
        this.quantity = quantity;
        this.name = name;
        this.subtotal = subtotal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}
