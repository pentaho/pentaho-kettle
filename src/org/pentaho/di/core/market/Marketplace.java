package org.pentaho.di.core.market;

import java.util.ArrayList;
import java.util.List;

import org.apache.batik.xml.XMLException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.w3c.dom.Node;

/**
 * This class contains information about a certain PDI market place, located on a certain location.
 * 
 * @author matt
 */
public class Marketplace implements XMLInterface {
  public static String XML_TAG = "marketplace"; 
  
  private String name;
  private String entriesUrl;
  
  public Marketplace() {
  }
  
  /**
   * @param name
   * @param entriesUrl
   */
  public Marketplace(String name, String entriesUrl) {
    this();
    this.name = name;
    this.entriesUrl = entriesUrl;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Marketplace)) return false;
    if (obj == this) return true;
    
    return ((Marketplace)obj).getName().equalsIgnoreCase(name);
  }
  
  @Override
  public int hashCode() {
    return name.hashCode();
  }
  
  @Override
  public String getXML() throws KettleException {
    StringBuilder xml = new StringBuilder();
    xml.append(XMLHandler.openTag(XML_TAG)).append(Const.CR);
    xml.append(XMLHandler.addTagValue("name", name));
    xml.append(XMLHandler.addTagValue("entries_url", entriesUrl));
    xml.append(XMLHandler.closeTag(XML_TAG)).append(Const.CR);
    return xml.toString();
  }
  
  public Marketplace(Node node) throws XMLException {
    this();
    name = XMLHandler.getTagValue(node, "name");
    entriesUrl = XMLHandler.getTagValue(node, "entries_url");
  }
  
  public static final List<Marketplace> getDefaultMarketplaces() {
    List<Marketplace> marketplaces = new ArrayList<Marketplace>();
    marketplaces.add( new Marketplace("Pentaho Data Integration Marketplace", 
            "http://s3.amazonaws.com/kettle/market.xml"));
    return marketplaces;
  }
  
  /**
   * @return the name
   */
  public String getName() {
    return name;
  }
  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }
  /**
   * @return the entriesUrl
   */
  public String getEntriesUrl() {
    return entriesUrl;
  }
  /**
   * @param entriesUrl the entriesUrl to set
   */
  public void setEntriesUrl(String entriesUrl) {
    this.entriesUrl = entriesUrl;
  }
  
  
}
