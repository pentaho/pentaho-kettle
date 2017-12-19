/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans;

import java.io.IOException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class TransConfiguration {
  public static final String XML_TAG = "transformation_configuration";

  private TransMeta transMeta;
  private TransExecutionConfiguration transExecutionConfiguration;

  /**
   * @param transMeta
   * @param transExecutionConfiguration
   */
  public TransConfiguration( TransMeta transMeta, TransExecutionConfiguration transExecutionConfiguration ) {
    this.transMeta = transMeta;
    this.transExecutionConfiguration = transExecutionConfiguration;
  }

  public String getXML() throws IOException, KettleException {
    StringBuilder xml = new StringBuilder( 200 );

    xml.append( "<" + XML_TAG + ">" ).append( Const.CR );

    xml.append( transMeta.getXML() );
    xml.append( transExecutionConfiguration.getXML() );

    xml.append( "</" + XML_TAG + ">" ).append( Const.CR );

    return xml.toString();
  }

  public TransConfiguration( Node configNode ) throws KettleException {
    Node trecNode = XMLHandler.getSubNode( configNode, TransExecutionConfiguration.XML_TAG );
    transExecutionConfiguration = new TransExecutionConfiguration( trecNode );
    Node transNode = XMLHandler.getSubNode( configNode, TransMeta.XML_TAG );
    transMeta = new TransMeta( transNode, transExecutionConfiguration.getRepository() );
  }

  public static final TransConfiguration fromXML( String xml ) throws KettleException {
    Document document = XMLHandler.loadXMLString( xml );
    Node configNode = XMLHandler.getSubNode( document, XML_TAG );
    return new TransConfiguration( configNode );
  }

  /**
   * @return the transExecutionConfiguration
   */
  public TransExecutionConfiguration getTransExecutionConfiguration() {
    return transExecutionConfiguration;
  }

  /**
   * @param transExecutionConfiguration
   *          the transExecutionConfiguration to set
   */
  public void setTransExecutionConfiguration( TransExecutionConfiguration transExecutionConfiguration ) {
    this.transExecutionConfiguration = transExecutionConfiguration;
  }

  /**
   * @return the transMeta
   */
  public TransMeta getTransMeta() {
    return transMeta;
  }

  /**
   * @param transMeta
   *          the transMeta to set
   */
  public void setTransMeta( TransMeta transMeta ) {
    this.transMeta = transMeta;
  }

}
