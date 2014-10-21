/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

/**
 *
 */

package org.pentaho.di.core.market;

import junit.framework.TestCase;

import org.pentaho.di.core.market.place.Marketplace;
import org.pentaho.di.core.xml.XMLHandler;

/**
 * @author matt
 *
 */
public class MarketEntryTest extends TestCase {

  private static final String name = "Pentaho Data Integration Marketplace";
  private static final String entriesUrl = "https://raw.github.com/pentaho/marketplace-metadata/master/marketplace.xml";

  public void testMarketplaceCreation() throws Exception {
    Marketplace place = new Marketplace( name, entriesUrl );

    assertEquals(name, place.getName() );
    assertEquals(entriesUrl, place.getEntriesUrl() );
  }

  public void testMarketplaceSerialization() throws Exception {
    Marketplace originalPlace = new Marketplace( name, entriesUrl );

    // Serialize & de-serialize and then see if we still have the same content.
    //
    String xml = originalPlace.getXML();
    System.out.println(xml );
    Marketplace place = new Marketplace( XMLHandler.loadXMLString( xml, Marketplace.XML_TAG ) );

    assertEquals(name, place.getName() );
    assertEquals(entriesUrl, place.getEntriesUrl() );
  }
}
