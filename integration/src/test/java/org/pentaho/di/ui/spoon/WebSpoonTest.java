/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.spoon;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebSpoonTest {
  private WebDriver driver;
  private Actions actions;
  private String baseUrl;
  private WebElement element;
  private WebDriverWait wait;

  @Before
  public void setUp() throws Exception {
    boolean isHeadless = Boolean.parseBoolean( System.getProperty( "headless.unittest", "true" ) );
    ChromeOptions options = new ChromeOptions();
    if ( isHeadless ) {
      options.addArguments( "headless" );
    }
    options.addArguments( "--window-size=1280,800" );
    driver = new ChromeDriver( options );
    actions = new Actions( driver );
    wait = new WebDriverWait( driver, 5 );
    baseUrl = System.getProperty( "test.baseurl", "http://localhost:8080/spoon" );
    driver.get( baseUrl );
    driver.manage().timeouts().implicitlyWait( 5, TimeUnit.SECONDS );

    // Login with username and password
//    if ( driver.findElements( By.xpath( "//input[@name = 'username']" ) ).size() != 0 ) {
//      driver.findElement( By.xpath( "//input[@name = 'username']" ) ).sendKeys( "user" );
//      driver.findElement( By.xpath( "//input[@name = 'password']" ) ).sendKeys( "password" );
//      clickElement( "//input[@name = 'submit']" );
//    }
  }

  @Test
  public void testAppLoading() {
    assertEquals( driver.getTitle(), "Spoon" );
  }

  @Test
  public void testGetFields() throws Exception {
    String filePath = "file:///home/tomcat/.kettle/data/samples/transformations/files/jsonfile.js";
    filePath = java.net.URLEncoder.encode( filePath, "ISO-8859-1" );
    String url = baseUrl + "/osgi/cxf/get-fields/sample/" + filePath + "/json";
    HttpUriRequest request = new HttpGet( url );

    HttpResponse response = HttpClientBuilder.create().build().execute( request );

    assert response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
  }

  @Test
  public void testNewTransformation() {
    // Create a new transformation
    createNewTrans();

    // Drag & drop a step
    clickElement( "//div[text() = 'Input']" );
    element = driver.findElement( By.xpath( "//div[text() = 'Generate rows']" ) );
    actions.clickAndHold( element ).moveByOffset( 300, 0 ).release().build().perform();

    // Open a step dialog
    clickElement( "//div[@test-id = 'exploreSolution']" );
    clickElement( "//div[@test-id = 'view_expandAll']" );
    doubleClickElement( "//div[@test-id = 'view_Steps']/..//div[text() = 'Generate rows']" );

    assertEquals( 1, driver.findElements( By.xpath( "//div[text() = 'Never stop generating rows']" ) ).size() );
  }

  /*
   * testModifiedJavaScriptValue1 and 2 collectively demonstrate multi-session use.
   */
  @Test
  public void testModifiedJavaScriptValue1() {
    testModifiedJavaScriptValue();
  }

  @Test
  public void testModifiedJavaScriptValue2() {
    testModifiedJavaScriptValue();
  }

  private void testModifiedJavaScriptValue() {
    createNewTrans();
    drawStep( "Modified JavaScript value" );
    openDialog( "Modified JavaScript value" );
    wait.until( ExpectedConditions.presenceOfElementLocated( By.xpath( "//div[text() = 'Java script functions :']" ) ) );

    assertEquals( 1, driver.findElements( By.xpath( "//div[text() = 'Java script functions :']" ) ).size() );
  }

  @Test
  public void testOpenSaveMenus() {
    clickElement( "//div[text() = 'File']" );
    assertFalse( isMenuItemDisabled( "//div[text() = 'Open...']" ) );
    clickElement( "//div[text() = 'File']" ); // Close the menu

    createNewTrans();
    clickElement( "//div[text() = 'File']" );
    assertFalse( isMenuItemDisabled( "//div[text() = 'Save']" ) );
    assertFalse( isMenuItemDisabled( "//div[text() = 'Save as (VFS)...']" ) );
  }

  @Test
  public void testOpenSaveNewDialog() {
    clickElement( "//div[text() = 'File']" );
    assertFalse( isMenuItemDisabled( "//div[text() = 'Open URL...']" ) );
    clickElement( "//div[text() = 'Open URL...']" ); // Open the new dialog
    driver.switchTo().frame( driver.findElement( By.xpath(".//iframe[starts-with(@src, '/spoon/osgi/@pentaho/di-plugin-file-open-save-new@9.2.0.0-SNAPSHOT/index.html')]") ) );
    wait.until( ExpectedConditions.presenceOfElementLocated( By.xpath( "//div[contains(text(),'Local')]" ) ) );
    assertEquals( 1, driver.findElements( By.xpath( "//div[contains(text(),'Local')]" ) ).size() );
  }

  @Test
  public void testDatabaseConnectionDialog() throws Exception {
    // Create a new transformation
    createNewTrans();
    // Draw a step
    drawStep( "Table input" );

    // Open a step dialog
    clickElement( "//div[@test-id = 'exploreSolution']" );
    clickElement( "//div[@test-id = 'view_expandAll']" );
    doubleClickElement( "//div[@test-id = 'view_Steps']/..//div[text() = 'Table input']" );

    /* TODO
     * Cancel button does not become clickable unless thread.sleep and window.setSize.
     * The wait duration might depend on an environment.
     */
    clickElement( "//div[text() = 'New...']" );
    Thread.sleep( 1000 );
    // Close the "Database Connection" dialog
    clickElement( "//div[text() = 'Database Connection']/../div[@tabindex = '0']" );
    Thread.sleep( 1000 );
    clickElement( "//div[text() = 'New...']" );
    Thread.sleep( 1000 );
    clickElement( "//div[text() = 'Database Connection']/../div[@tabindex = '0']" );
    Thread.sleep( 1000 );
    assertEquals( "4", driver.switchTo().activeElement().getAttribute( "tabindex" ) );
  }

  @Test
  public void testContextMenu() {
    // Create a new transformation
    createNewTrans();
    drawStep( "Table input" );

    // Open the View tab
    clickElement( "//div[@test-id = 'exploreSolution']" );
    clickElement( "//div[@test-id = 'view_expandAll']" );

    // Right-click on a step
    rightClickElement( "//div[@test-id = 'view_Steps']/..//div[text() = 'Table input']" );

    assertEquals( 1, driver.findElements( By.xpath( "//div[text() = 'Duplicate']" ) ).size() );
  }

  /**
   * Test if the repository dialog is loaded.
   * @throws Exception
   */
  @Test
  public void testConnect() {
    clickElement( "//div[text() = 'Connect']" );
    // if any repository is already registered
    if ( driver.findElements( By.xpath( "//div[text() = 'Repository Manager...']" ) ).size() == 1 ) {
      clickElement( "//div[text() = 'Repository Manager...']" );
      driver.switchTo().frame( driver.findElement( By.xpath(".//iframe[@src='/spoon/osgi/@pentaho/di-plugin-repositories@9.2.0.0-SNAPSHOT/index.html']") ) );
    } else {
      driver.switchTo().frame( driver.findElement( By.xpath(".//iframe[@src='/spoon/osgi/@pentaho/di-plugin-repositories@9.2.0.0-SNAPSHOT/index.html#/add']") ) );
    }
    assertEquals( 1, driver.findElements( By.xpath( "//button[@id = 'btnClose']" ) ).size() );
  }

  /**
   * Test if the marketplace can be loaded.
   */
  @Test
  public void testMarketplace() {
    clickElement( "//div[text() = 'Tools']" );
    clickElement( "//div[text() = 'Marketplace']" );
    driver.switchTo().frame( driver.findElement( By.xpath(".//iframe[@src='osgi/@pentaho/marketplace@9.2.0.0-SNAPSHOT/main.html']") ) );
    assertEquals( 1, driver.findElements( By.xpath( "//div[text() = ' Available ']" ) ).size() );
    assertEquals( 0, driver.findElements( By.xpath( "//div[contains(text(), 'Error getting plugins from server')]" ) ).size() );
  }

  /**
   * Test if multi-session is enabled
   */
  @Test
  public void testFirstSession() {
    createNewTrans();
    testRunConfigurationPopupMenu();
    testHadoopClusterPopupMenu();
  }

  @Test
  public void testSecondSession() {
    createNewTrans();
    testRunConfigurationPopupMenu();
    testHadoopClusterPopupMenu();
  }

  private void testRunConfigurationPopupMenu() {
    // Click View
    clickElement( "//div[@test-id = 'exploreSolution']" );
    rightClickElement( "//div[text() = 'Run configurations']" );
    assertEquals( 1, driver.findElements( By.xpath( "//div[text() = 'New...' and not(contains(@style,'visibility: hidden'))]" ) ).size() );
  }

  private void testHadoopClusterPopupMenu() {
    // Click View
    clickElement( "//div[@test-id = 'exploreSolution']" );
    rightClickElement( "//div[text() = 'Hadoop clusters']" );
    assertEquals( 1, driver.findElements( By.xpath( "//div[text() = 'New cluster' and not(contains(@style,'visibility: hidden'))]" ) ).size() );
  }

  private void createNewTrans() {
    // Create a new transformation
    clickElement( "//div[text() = 'File']" );
    clickElement( "//div[text() = 'New']" );
    clickElement( "//div[text() = 'Transformation']" );
    wait.until( ExpectedConditions.presenceOfElementLocated( By.xpath( "//div[text() = 'Transformation 1']" ) ) );
  }

  private void drawStep( String stepName ) {
    // Filter a step
    element = wait.until( ExpectedConditions.visibilityOfElementLocated( By.xpath( "//input[@test-id = 'design_selectionFilter']" ) ) );
    element.sendKeys( stepName );

    // Draw a step
    doubleClickElement( "//div[text() = '" + stepName + "']" );
  }

  private void openDialog( String stepName ) {
    // Open a step dialog
    clickElement( "//div[@test-id = 'exploreSolution']" );
    clickElement( "//div[@test-id = 'view_expandAll']" );
    doubleClickElement( "//div[@test-id = 'view_Steps']/..//div[text() = '" + stepName + "']" );
  }

  private void clickElement( String xpath ) {
    element = wait.until( ExpectedConditions.elementToBeClickable( By.xpath( xpath ) ) );
    element.click();
  }

  private void doubleClickElement( String xpath ) {
    element = wait.until( ExpectedConditions.elementToBeClickable( By.xpath( xpath ) ) );
    actions.click( element ).click( element ).build().perform();
  }

  private void rightClickElement( String xpath ) {
    element = wait.until( ExpectedConditions.elementToBeClickable( By.xpath( xpath ) ) );
    actions.contextClick( element ).build().perform();
  }

  private boolean isMenuItemDisabled( String xpath ) {
    /*
     *  Determine if a menu item is grayed out (=disabled)
     *  ExpectedConditions.elementToBeClickable does not work here because it is clickable.
     */
    String color = driver.findElement( By.xpath( xpath ) ).getCssValue( "color" );
    return color.equals( "rgba(189, 189, 189, 1)" );
  }

  @After
  public void tearDown() {
    driver.quit();
  }
}
