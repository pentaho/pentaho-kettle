/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.util.serialization;

import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLParserFactoryProducer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static com.google.common.base.Preconditions.checkArgument;
import static org.pentaho.di.core.util.serialization.StepMetaProps.STEP_TAG;

/**
 * Converts StepMetaProps to/from an XML string using JAXB.
 */
@SuppressWarnings( "squid:S00112" )
public class MetaXmlSerializer {
  private MetaXmlSerializer() { }

  public static String serialize( StepMetaProps stepMetaProps ) {
    try ( ByteArrayOutputStream baos = new ByteArrayOutputStream() ) {
      Marshaller marshalObj = JAXBContext.newInstance( StepMetaProps.class ).createMarshaller();
      marshalObj.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
      marshalObj.setProperty( Marshaller.JAXB_FRAGMENT, true );
      marshalObj.marshal( stepMetaProps, baos );
      return baos.toString( StandardCharsets.UTF_8.name() );
    } catch ( JAXBException | IOException e ) {
      throw new RuntimeException( e );
    }
  }

  public static StepMetaProps deserialize( String ser ) {
    try ( ByteArrayInputStream bais = new ByteArrayInputStream( ser.getBytes( StandardCharsets.UTF_8 ) ) ) {
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
      XMLParserFactoryProducer.createSecureTransformerFactory()
        .newTransformer()
        .transform( new DOMSource( node ), new StreamResult( sw ) );
    } catch ( TransformerException te ) {
      throw new RuntimeException( te );
    }
    return sw.toString();
  }

}
