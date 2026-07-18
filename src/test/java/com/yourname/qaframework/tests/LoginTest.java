package com.yourname.qaframework.tests;

import com.yourname.qaframework.base.BaseTest;
import com.yourname.qaframework.pages.LoginPage;
import com.yourname.qaframework.pages.ProductsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test(description = "TC_LOGIN_001 - Valid login succeeds")
    public void shouldLoginSuccessfullyWithValidCredentials() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "secret_sauce");

        Assert.assertTrue(new ProductsPage(driver).isProductsPageDisplayed(), "Products page was not loaded");
    }

    @Test(description = "TC_LOGIN_002 - Invalid password shows error")
    public void shouldShowErrorWithInvalidPassword() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("standard_user", "wrong_password");

        String expectedError = "Epic sadface: Username and password do not match any user in this service";
        Assert.assertEquals(loginPage.getErrorMessage(), expectedError);
    }

    @Test(description = "TC_LOGIN_003 - Locked out user is blocked")
    public void shouldBlockLockedOutUser() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("locked_out_user", "secret_sauce");

        String expectedError = "Epic sadface: Sorry, this user has been locked out.";
        Assert.assertEquals(loginPage.getErrorMessage(), expectedError);
    }

    @Test(description = "TC_LOGIN_004 - Empty credentials shows error")
    public void shouldShowErrorWithEmptyCredentials() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("", "");

        String expectedError = "Epic sadface: Username is required";
        Assert.assertEquals(loginPage.getErrorMessage(), expectedError);
    }
}