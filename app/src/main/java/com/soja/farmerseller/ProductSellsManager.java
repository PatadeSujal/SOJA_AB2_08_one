package com.soja.farmerseller;

public class ProductSellsManager {
    String productName,productSells,productEarnings,image;
    public ProductSellsManager(){ }
    public ProductSellsManager(String productName, String productSells,String image, String productEarnings) {
        this.productName = productName;
        this.productSells = productSells;
        this.image = image;
        this.productEarnings = productEarnings;
    }

    public String getImageUrl() {
        return image;
    }
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSells() {
        return productSells;
    }

    public void setProductSells(String productSells) {
        this.productSells = productSells;
    }

    public String getProductEarnings() {
        return productEarnings;
    }

    public void setProductEarnings(String productEarnings) {
        this.productEarnings = productEarnings;
    }
}
