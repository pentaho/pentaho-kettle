/**
 * 
 */
package org.pentaho.di.core.market;

import junit.framework.TestCase;

import org.pentaho.di.core.xml.XMLHandler;

/**
 * @author matt
 *
 */
public class MarketplaceTest extends TestCase {
  
  private static final String ID = "DummyPlugin";
  private static final MarketEntryType type = MarketEntryType.Step;
  private static final String name = "Dummy Plugin";
  private static final String version = "4.4";
  private static final String author = "Pentaho";
  private static final String description = "This is a plugin that is provided as an example on how to create a step plugin for PDI.";
  private static final String documentationUrl = "http://wiki.pentaho.com/display/EAI/The+PDI+SDK";
  private static final String sourceUrl = "http://source.pentaho.org/svnkettleroot/plugins/DummyPlugin/trunk/";
  private static final String forumUrl = "forums.pentaho.com/forumdisplay.php?135-Pentaho-Data-Integration-Kettle";
  private static final String casesUrl = "http://jira.pentaho.com/browse/PDI";
  private static final String packageUrl = "http://wiki.pentaho.com/download/attachments/23531011/DummyPlugin.zip";
  private static final String licenseName = "Apache License 2.0";
  private static final String licenseText = "For more details see: http://www.apache.org/licenses/LICENSE-2.0.html";
  private static final SupportLevel supportLevel = SupportLevel.PROFESSIONALY_SUPPORTED;
  private static final String supportMessage = "Supported by a Pentaho Data Integration Enterprise Edition support contract.";
  private static final String supportOrganization = "Pentaho";
  private static final String supportUrl = "http://www.pentaho.com/services/support/pdi";
  private static final String minPdiVersion = "4.3.0";
  private static final String maxPdiVersion = "4.9";

  public void testMarketEntryCreation() throws Exception {
    MarketEntry entry = new MarketEntry(ID, type, name, version, author, description, 
        documentationUrl, sourceUrl, forumUrl, casesUrl, packageUrl, 
        licenseName, licenseText, supportLevel, supportMessage, supportOrganization, supportUrl, minPdiVersion, maxPdiVersion);
    
    assertEquals(ID, entry.getId());
    assertEquals(type, entry.getType());
    assertEquals(name, entry.getName());
    assertEquals(version, entry.getVersion());
    assertEquals(author, entry.getAuthor());
    assertEquals(description, entry.getDescription());
    assertEquals(documentationUrl, entry.getDocumentationUrl());
    assertEquals(sourceUrl, entry.getSourceUrl());
    assertEquals(forumUrl, entry.getForumUrl());
    assertEquals(casesUrl, entry.getCasesUrl());
    assertEquals(packageUrl, entry.getPackageUrl());
    assertEquals(licenseName, entry.getLicenseName());
    assertEquals(licenseText, entry.getLicenseText());
    assertEquals(supportLevel, entry.getSupportLevel());
    assertEquals(supportMessage, entry.getSupportMessage());
    assertEquals(supportOrganization, entry.getSupportOrganization());
    assertEquals(supportUrl, entry.getSupportUrl());
    assertEquals(minPdiVersion, entry.getMinPdiVersion());
    assertEquals(maxPdiVersion, entry.getMaxPdiVersion());
  }
  
  public void testMarketEntrySerialization() throws Exception {
    MarketEntry originalEntry = new MarketEntry(ID, type, name, version, author, description, 
        documentationUrl, sourceUrl, forumUrl, casesUrl, packageUrl, 
        licenseName, licenseText, supportLevel, supportMessage, supportOrganization, supportUrl, minPdiVersion, maxPdiVersion);
    
    // Serialize & de-serialize and then see if we still have the same content.
    //
    String xml = originalEntry.getXML();
    System.out.println(xml);
    MarketEntry entry = new MarketEntry(XMLHandler.loadXMLString(xml, MarketEntry.XML_TAG));
    
    assertEquals(ID, entry.getId());
    assertEquals(type, entry.getType());
    assertEquals(name, entry.getName());
    assertEquals(version, entry.getVersion());
    assertEquals(author, entry.getAuthor());
    assertEquals(description, entry.getDescription());
    assertEquals(documentationUrl, entry.getDocumentationUrl());
    assertEquals(sourceUrl, entry.getSourceUrl());
    assertEquals(forumUrl, entry.getForumUrl());
    assertEquals(casesUrl, entry.getCasesUrl());
    assertEquals(packageUrl, entry.getPackageUrl());
    assertEquals(licenseName, entry.getLicenseName());
    assertEquals(licenseText, entry.getLicenseText());
    assertEquals(supportLevel, entry.getSupportLevel());
    assertEquals(supportMessage, entry.getSupportMessage());
    assertEquals(supportOrganization, entry.getSupportOrganization());
    assertEquals(supportUrl, entry.getSupportUrl());
    assertEquals(minPdiVersion, entry.getMinPdiVersion());
    assertEquals(maxPdiVersion, entry.getMaxPdiVersion());
  }
}
