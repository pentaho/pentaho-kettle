/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 * **************************************************************************
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
 */

package org.pentaho.di.core.util.serialization;

import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.charset.Charset.defaultCharset;
import static org.pentaho.di.core.util.serialization.StepMetaProps.STEP_TAG;

/**
 * Converts StepMetaProps to/from an XML string using JAXB.
 */
public class MetaXmlSerializer {

  public static String serialize( StepMetaProps stepMetaProps ) {
    try ( ByteArrayOutputStream baos = new ByteArrayOutputStream() ) {
      Marshaller marshalObj = JAXBContext.newInstance( StepMetaProps.class ).createMarshaller();
      marshalObj.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
      marshalObj.setProperty( Marshaller.JAXB_FRAGMENT, true );
      marshalObj.marshal( stepMetaProps, baos );
      return baos.toString( defaultCharset().name() );
    } catch ( JAXBException | IOException e ) {
      throw new RuntimeException( e );
    }
  }

  public static StepMetaProps deserialize( String ser ) {
    try ( ByteArrayInputStream bais = new ByteArrayInputStream( ser.getBytes( defaultCharset() ) ) ) {
      Unmarshaller unmarshaller = JAXBContext.newInstance( StepMetaProps.class ).createUnmarshaller();
      return (StepMetaProps) unmarshaller.unmarshal( bais );
    } catch ( IOException | JAXBException e ) {
      throw new RuntimeException( e );
    }
  }

  public static StepMetaProps deserialize( Node node ) {
    return deserialize( nodeToString( XMLHandler.getSubNode( node, STEP_TAG ) ) );
  }


  /**
   * Sets the namespaces used for deserialization, and converts to a string.
   * <p>
   * Shouldn't need to convert from Node->String, since the Unmarshaller should be able
   * to take the node directly, but hit issues with the namespace not being read properly.
   */
  private static String nodeToString( Node node ) {
    checkArgument( node instanceof Element );

    StringWriter sw = new StringWriter();
    try {
      ( (Element) node )
        .setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi",
          "http://www.w3.org/2001/XMLSchema-instance" );
      ( (Element) node )
        .setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
          "xmlns:xs", "http://www.w3.org/2001/XMLSchema" );
      TransformerFactory.newInstance()
        .newTransformer()
        .transform( new DOMSource( node ), new StreamResult( sw ) );
    } catch ( TransformerException te ) {
      throw new RuntimeException( te );
    }
    return sw.toString();
  }

}
