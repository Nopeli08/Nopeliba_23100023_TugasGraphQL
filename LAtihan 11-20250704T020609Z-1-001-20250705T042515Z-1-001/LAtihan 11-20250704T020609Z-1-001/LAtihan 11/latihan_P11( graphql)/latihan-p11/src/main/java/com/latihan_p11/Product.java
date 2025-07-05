package com.latihan_p11;

public class Product {
    public Long id;
    public String name;
    public Double price;
    public String  category;

    public Product (Long id, String name, Double price, String category) {
        this.id = id;
        this.name = name;
        this.price= price;
        this.category= category;
    }
    //Kita mulai mengoding
    public Long getId() {return id; }
    public void setId(Long id) {this.id = id;}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

}
    

