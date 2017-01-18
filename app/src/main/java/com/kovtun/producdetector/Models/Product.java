package com.kovtun.producdetector.Models;

/**
 * Created by kovtun on 23.06.2016.
 */
public class Product {
    private int id;
    private String name;
    private String barcode;
    private Double price;

    public Product(int id, String name, String barcode, Double price) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.price = price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBarcode() {
        return barcode;
    }

    public Double getPrice() {
        return price;
    }
}
