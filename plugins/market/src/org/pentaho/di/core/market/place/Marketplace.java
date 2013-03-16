/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.market.place;

import java.util.ArrayList;
import java.util.List;

import org.apache.batik.xml.XMLException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.market.place.Marketplace;
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
            "https://raw.github.com/pentaho/marketplace-metadata/master/marketplace.xml"));
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
