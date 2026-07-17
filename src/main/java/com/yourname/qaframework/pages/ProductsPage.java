package com.yourname.qaframework.pages;

import org.openqa.selenium.WebDriver;

public class ProductsPage {

    private final WebDriver driver;

    public ProductsPage(WebDriver driver) {
        this.driver = driver;
    }

    public boolean isProductsPageDisplayed() {
        return driver.getCurrentUrl().contains("/inventory.html");
    }
}