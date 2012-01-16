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
public class MarketEntryTest extends TestCase {
  
  private static final String name = "Pentaho Data Integration Marketplace";
  private static final String entriesUrl = "http://s3.amazonaws.com/kettle/market.xml";

  public void testMarketplaceCreation() throws Exception {
    Marketplace place = new Marketplace(name, entriesUrl);
    
    assertEquals(name, place.getName());
    assertEquals(entriesUrl, place.getEntriesUrl());
  }
  
  public void testMarketplaceSerialization() throws Exception {
    Marketplace originalPlace= new Marketplace(name, entriesUrl);
    
    // Serialize & de-serialize and then see if we still have the same content.
    //
    String xml = originalPlace.getXML();
    System.out.println(xml);
    Marketplace place = new Marketplace(XMLHandler.loadXMLString(xml, Marketplace.XML_TAG));
    
    assertEquals(name, place.getName());
    assertEquals(entriesUrl, place.getEntriesUrl());
  }
}
