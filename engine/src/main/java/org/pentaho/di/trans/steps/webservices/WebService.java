/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.webservices;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.HttpClientManager;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLParserFactoryProducer;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.webservices.wsdl.Wsdl;
import org.pentaho.di.trans.steps.webservices.wsdl.WsdlOpParameter;
import org.pentaho.di.trans.steps.webservices.wsdl.WsdlOpParameterList;
import org.pentaho.di.trans.steps.webservices.wsdl.WsdlOperation;
import org.pentaho.di.trans.steps.webservices.wsdl.XsdType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;


public class WebService extends BaseStep implements StepInterface {
  private static Class<?> PKG = WebServiceMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String NS_PREFIX = "ns";

  private WebServiceData data;

  private WebServiceMeta meta;

  private int nbRowProcess;

  // private long requestTime;

  private SimpleDateFormat timeFormat = new SimpleDateFormat( "HH:mm:ss" );

  private SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );

  private SimpleDateFormat dateTimeFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );

  private DecimalFormat decFormat = new DecimalFormat( "00" );

  private Date dateRef;

  private Wsdl cachedWsdl;

  private HttpClientContext cachedHostConfiguration;

  private HttpClient cachedHttpClient;

  private String cachedURLService;

  private WsdlOperation cachedOperation;

  private WebServiceMeta cachedMeta;

  public WebService( StepMeta aStepMeta, StepDataInterface aStepData, int value, TransMeta aTransMeta, Trans aTrans ) {
    super( aStepMeta, aStepData, value, aTransMeta, aTrans );

    // Reference date used to format hours
    try {
      dateRef = timeFormat.parse( "00:00:00" );
    } catch ( ParseException e ) {
      logError( "Unexpected error in WebService constructor: ", e );
      setErrors( 1 );
      stopAll();
    }
  }

  public boolean processRow( StepMetaInterface metaInterface, StepDataInterface dataInterface ) throws KettleException {
    meta = (WebServiceMeta) metaInterface;

    // if a URL is not specified, throw an exception
    if ( Utils.isEmpty( meta.getUrl() ) ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "WebServices.ERROR0014.urlNotSpecified", getStepname() ) );
    }

    // if an operation is not specified, throw an exception
    if ( Utils.isEmpty( meta.getOperationName() ) ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "WebServices.ERROR0015.OperationNotSelected", getStepname() ) );
    }

    data = (WebServiceData) dataInterface;
    Object[] vCurrentRow = getRow();

    if ( first ) {
      first = false;

      if ( getInputRowMeta() != null ) {
        data.outputRowMeta = getInputRowMeta().clone();
      } else {
        data.outputRowMeta = new RowMeta();
      }
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      defineIndexList( getInputRowMeta(), vCurrentRow );
    } else {
      // Input from previous steps, no longer getting any rows, call it a day...
      //
      if ( vCurrentRow == null ) {
        setOutputDone();
        return false;
      }
    }

    if ( vCurrentRow != null ) {
      nbRowProcess++;
      data.argumentRows.add( vCurrentRow );
    }

    if ( ( vCurrentRow == null && ( nbRowProcess % meta.getCallStep() != 0 ) )
      || ( vCurrentRow != null && ( ( nbRowProcess > 0 && nbRowProcess % meta.getCallStep() == 0 ) ) )
      || ( vCurrentRow == null && ( !meta.hasFieldsIn() ) ) ) {
      requestSOAP( vCurrentRow, getInputRowMeta() );
    }

    // No input received, this one lookup execution is all we're going to do.
    //
    if ( vCurrentRow == null ) {
      setOutputDone();
    }
    return vCurrentRow != null;
  }

  private List<Integer> indexList;

  private void defineIndexList( RowMetaInterface rowMeta, Object[] vCurrentRow ) throws KettleException {
    // Create an index list for the input fields
    //
    indexList = new ArrayList<Integer>();
    if ( rowMeta != null ) {
      for ( WebServiceField curField : meta.getFieldsIn() ) {
        int index = rowMeta.indexOfValue( curField.getName() );
        if ( index >= 0 ) {
          indexList.add( index );
        } else {
          throw new KettleException( "Required input field ["
            + curField.getName() + "] couldn't be found in the step input" );
        }
      }
    }

    // Create a map for the output values too
    //
    for ( WebServiceField curField : meta.getFieldsOut() ) {
      int index = data.outputRowMeta.indexOfValue( curField.getName() );
      if ( index >= 0 ) {
        // Keep a mapping between the web service name and the index of the target field.
        // This makes it easier to populate the fields later on, reading back the result.
        //
        data.indexMap.put( curField.getWsName(), index );
      }
    }
  }

  private String getRequestXML( WsdlOperation operation, boolean qualifyWSField ) throws KettleException {
    WsdlOpParameterList parameters = operation.getParameters();
    String requestOperation = Const.NVL( meta.getOperationRequestName(), meta.getOperationName() );
    Iterator<WsdlOpParameter> iterator = parameters.iterator();

    List<String> bodyNames = new ArrayList<String>();

    while ( iterator.hasNext() ) {
      WsdlOpParameter wsdlOpParameter = iterator.next();
      bodyNames.add( wsdlOpParameter.getName().getLocalPart() );
    }

    List<String> headerNames = new ArrayList<String>( parameters.getHeaderNames() );

    StringBuilder xml = new StringBuilder();

    // TODO We only manage one name space for all the elements. See in the
    // future how to manage multiple name spaces
    //
    xml.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
    xml
      .append( "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:"
        + NS_PREFIX + "=\"" );
    xml.append( meta.getOperationNamespace() );
    xml.append( "\">\n" );

    xml.append( "  <soapenv:Header>\n" );
    addParametersToXML( xml, headerNames, qualifyWSField );
    xml.append( "  </soapenv:Header>\n" );

    xml.append( "  <soapenv:Body>\n" );

    xml.append( "    <" + NS_PREFIX + ":" ).append( requestOperation ).append( ">\n" ); // OPEN request operation
    if ( meta.getInFieldContainerName() != null ) {
      xml.append( "      <" + NS_PREFIX + ":" + meta.getInFieldContainerName() + ">\n" );
    }

    addParametersToXML( xml, bodyNames, qualifyWSField );

    if ( meta.getInFieldContainerName() != null ) {
      xml.append( "      </" + NS_PREFIX + ":" + meta.getInFieldContainerName() + ">\n" );
    }
    xml.append( "    </" + NS_PREFIX + ":" ).append( requestOperation ).append( ">\n" ); // CLOSE request operation
    xml.append( "  </soapenv:Body>\n" );
    xml.append( "</soapenv:Envelope>\n" );

    return xml.toString();
  }

  /**
   * @param xml            the XML this method is appending to.
   * @param names          the header names
   * @param qualifyWSField indicates if the we are to use the namespace prefix when writing the WS field name
   * @throws KettleException
   */
  private void addParametersToXML( StringBuilder xml, List<String> names, boolean qualifyWSField )
    throws KettleException {

    // Add the row parameters...
    //
    for ( Object[] vCurrentRow : data.argumentRows ) {

      if ( meta.getInFieldArgumentName() != null ) {
        xml.append( "        <" + NS_PREFIX + ":" ).append( meta.getInFieldArgumentName() ).append( ">\n" );
      }

      for ( Integer index : indexList ) {
        ValueMetaInterface vCurrentValue = getInputRowMeta().getValueMeta( index );
        Object data = vCurrentRow[ index ];

        WebServiceField field = meta.getFieldInFromName( vCurrentValue.getName() );
        if ( field != null && names.contains( field.getWsName() ) ) {
          if ( !vCurrentValue.isNull( data ) ) {
            xml.append( "          <" );
            if ( qualifyWSField ) {
              xml.append( NS_PREFIX ).append( ":" );
            }
            xml.append( field.getWsName() ).append( ">" );

            if ( XsdType.TIME.equals( field.getXsdType() ) ) {
              // Allow to deal with hours like 36:12:12 (> 24h)
              long millis = vCurrentValue.getDate( data ).getTime() - dateRef.getTime();
              xml.append( decFormat.format( millis / 3600000 )
                + ":" + decFormat.format( ( millis % 3600000 ) / 60000 ) + ":"
                + decFormat.format( ( ( millis % 60000 ) / 1000 ) ) );
            } else if ( XsdType.DATE.equals( field.getXsdType() ) ) {
              xml.append( dateFormat.format( vCurrentValue.getDate( data ) ) );
            } else if ( XsdType.BOOLEAN.equals( field.getXsdType() ) ) {
              xml.append( vCurrentValue.getBoolean( data ) ? "true" : "false" );
            } else if ( XsdType.DATE_TIME.equals( field.getXsdType() ) ) {
              xml.append( dateTimeFormat.format( vCurrentValue.getDate( data ) ) );
            } else if ( vCurrentValue.isNumber() ) {
              // TODO: To Fix !! This is very bad coding...
              //
              xml.append( vCurrentValue.getString( data ).trim().replace( ',', '.' ) );
            } else {
              xml.append( Const.trim( vCurrentValue.getString( data ) ) );
            }

            xml.append( "</" );
            if ( qualifyWSField ) {
              xml.append( NS_PREFIX ).append( ":" );
            }
            xml.append( field.getWsName() ).append( ">\n" );

          } else {
            xml.append( "          <" ).append( NS_PREFIX ).append( ":" ).append( field.getWsName() ).append(
              " xsi:nil=\"true\"/>\n" );
          }
        }
      }
      if ( meta.getInFieldArgumentName() != null ) {
        xml.append( "        </" + NS_PREFIX + ":" ).append( meta.getInFieldArgumentName() ).append( ">\n" );
      }
    }
  }

  private synchronized void requestSOAP( Object[] rowData, RowMetaInterface rowMeta ) throws KettleException {
    initWsdlEnv();
    HttpPost vHttpMethod = null;
    HttpEntity httpEntity = null;
    Charset charSet = Charset.defaultCharset();
    try {
      String xml =
        getRequestXML( cachedOperation, cachedWsdl.getWsdlTypes().isElementFormQualified(
          cachedWsdl.getTargetNamespace() ) );

      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "WebServices.Log.SOAPEnvelope" ) );
        logDetailed( xml );
      }

      vHttpMethod = getHttpMethod( cachedURLService );

      HttpEntity requestEntity = new ByteArrayEntity( xml.toString().getBytes( "UTF-8" ) );
      vHttpMethod.setEntity( requestEntity );
      // long currentRequestTime = Const.nanoTime();
      HttpResponse httpResponse = cachedHttpClient.execute( vHttpMethod );
      int responseCode = httpResponse.getStatusLine().getStatusCode();
      if ( responseCode == HttpStatus.SC_MOVED_PERMANENTLY ) {
        String newLocation = getLocationFrom( vHttpMethod );
        vHttpMethod = getHttpMethod( newLocation );
        vHttpMethod.setEntity( requestEntity );
        httpResponse = cachedHttpClient.execute( vHttpMethod );
        responseCode = httpResponse.getStatusLine().getStatusCode();
      }
      if ( responseCode == HttpStatus.SC_OK ) {
        httpEntity = httpResponse.getEntity();
        ContentType contentType = ContentType.getOrDefault( httpEntity );
        charSet = contentType.getCharset();
        processRows( httpEntity.getContent(), rowData, rowMeta, cachedWsdl.getWsdlTypes()
          .isElementFormQualified( cachedWsdl.getTargetNamespace() ), charSet.toString() );
      } else if ( responseCode == HttpStatus.SC_UNAUTHORIZED ) {
        throw new KettleStepException( BaseMessages.getString( PKG, "WebServices.ERROR0011.Authentication",
          cachedURLService ) );
      } else if ( responseCode == HttpStatus.SC_NOT_FOUND ) {
        throw new KettleStepException( BaseMessages.getString( PKG,
          "WebServices.ERROR0012.NotFound", cachedURLService ) );
      } else {
        throw new KettleStepException( BaseMessages.getString( PKG,
          "WebServices.ERROR0001.ServerError", Integer.toString( responseCode ),
          Const.NVL( new String( EntityUtils.toString( httpEntity, charSet.toString() ) ), "" ), cachedURLService ) );
      }
      // requestTime += Const.nanoTime() - currentRequestTime;
    } catch ( UnknownHostException e ) {
      throw new KettleStepException( BaseMessages
        .getString( PKG, "WebServices.ERROR0013.UnknownHost", cachedURLService ), e );
    } catch ( IOException e ) {
      throw new KettleStepException( BaseMessages
        .getString( PKG, "WebServices.ERROR0005.IOException", cachedURLService ), e );
    } catch ( URISyntaxException e ) {
      throw new KettleStepException( BaseMessages.getString( PKG, "WebServices.ERROR0002.InvalidURI",
        cachedURLService ), e );
    } finally {
      data.argumentRows.clear(); // ready for the next batch.
      if ( vHttpMethod != null ) {
        vHttpMethod.releaseConnection();
      }
    }
  }

  private void initWsdlEnv() throws KettleException {
    if ( meta.equals( cachedMeta ) ) {
      return;
    }
    cachedMeta = meta;

    try {
      cachedWsdl = new Wsdl( new java.net.URI( data.realUrl ),
        null, null, meta.getHttpLogin(), meta.getHttpPassword() );
    } catch ( Exception e ) {
      throw new KettleStepException( BaseMessages.getString( PKG, "WebServices.ERROR0013.ExceptionLoadingWSDL" ), e );
    }

    cachedURLService = cachedWsdl.getServiceEndpoint();
    cachedHostConfiguration = HttpClientContext.create();
    cachedHttpClient = getHttpClient( cachedHostConfiguration );
    // Generate the XML to send over, determine the correct name for the request...
    //
    cachedOperation = cachedWsdl.getOperation( meta.getOperationName() );
    if ( cachedOperation == null ) {
      throw new KettleException( BaseMessages.getString( PKG, "WebServices.Exception.OperarationNotSupported", meta
        .getOperationName(), meta.getUrl() ) );
    }

  }

  static String getLocationFrom( HttpPost method ) {
    Header locationHeader = method.getFirstHeader( "Location" );
    return locationHeader.getValue();
  }

  HttpPost getHttpMethod( String vURLService ) throws URISyntaxException {
    URIBuilder uriBuilder = new URIBuilder( vURLService );
    HttpPost vHttpMethod = new HttpPost( uriBuilder.build() );

    vHttpMethod.setHeader( "Content-Type", "text/xml;charset=UTF-8" );

    String soapAction = "\"" + meta.getOperationNamespace();
    if ( !meta.getOperationNamespace().endsWith( "/" ) ) {
      soapAction += "/";
    }
    soapAction += meta.getOperationName() + "\"";
    logDetailed( BaseMessages.getString( PKG, "WebServices.Log.UsingRequestHeaderSOAPAction", soapAction ) );
    vHttpMethod.setHeader( "SOAPAction", soapAction );

    return vHttpMethod;
  }

  private HttpClient getHttpClient( HttpClientContext context ) {
    HttpClientManager.HttpClientBuilderFacade clientBuilder = HttpClientManager.getInstance().createBuilder();

    String login = environmentSubstitute( meta.getHttpLogin() );
    if ( StringUtils.isNotBlank( login ) ) {
      clientBuilder.setCredentials( login, environmentSubstitute( meta.getHttpPassword() ) );
    }
    int proxyPort = 0;
    if ( StringUtils.isNotBlank( meta.getProxyHost() ) ) {
      proxyPort = Const.toInt( environmentSubstitute( meta.getProxyPort() ), 8080 );
      clientBuilder.setProxy( meta.getProxyHost(), proxyPort );
    }
    CloseableHttpClient httpClient = clientBuilder.build();

    if ( proxyPort != 0 ) {
      // Preemptive authentication
      HttpHost target = new HttpHost( meta.getProxyHost(), proxyPort, "http" );
      // Create AuthCache instance
      AuthCache authCache = new BasicAuthCache();
      // Generate BASIC scheme object and add it to the local auth cache
      BasicScheme basicAuth = new BasicScheme();
      authCache.put( target, basicAuth );
      // Add AuthCache to the execution context
      context.setAuthCache( authCache );
    }

    return httpClient;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (WebServiceMeta) smi;
    data = (WebServiceData) sdi;

    data.indexMap = new Hashtable<String, Integer>();
    data.realUrl = environmentSubstitute( meta.getUrl() );

    return super.init( smi, sdi );
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (WebServiceMeta) smi;
    data = (WebServiceData) sdi;

    super.dispose( smi, sdi );
  }

  private String readStringFromInputStream( InputStream is, String encoding ) throws KettleStepException {

    try {

      StringBuilder sb = new StringBuilder( Math.max( 16, is.available() ) );
      char[] tmp = new char[ 4096 ];

      try {
        InputStreamReader reader = new InputStreamReader( is, encoding != null ? encoding : "UTF-8" );
        for ( int cnt; ( cnt = reader.read( tmp ) ) > 0; ) {
          sb.append( tmp, 0, cnt );
        }
      } finally {
        is.close();
      }
      return sb.toString();

    } catch ( Exception e ) {
      throw new KettleStepException( "Unable to read web service response data from input stream", e );
    }

  }

  private void processRows( InputStream anXml, Object[] rowData, RowMetaInterface rowMeta,
                            boolean ignoreNamespacePrefix, String encoding ) throws KettleException {
    // Just to make sure the old transformations keep working...
    //
    if ( meta.isCompatible() ) {
      compatibleProcessRows( anXml, rowData, rowMeta, ignoreNamespacePrefix, encoding );
      return;
    }

    // First we should get the complete string
    // The problem is that the string can contain XML or any other format such as HTML saying the service is no longer
    // available.
    // We're talking about a WEB service here.
    // As such, to keep the original parsing scheme, we first read the content.
    // Then we create an input stream from the content again.
    // It's elaborate, but that way we can report on the failure more correctly.
    //
    String response = readStringFromInputStream( anXml, encoding );

    try {

      // What is the expected response object for the operation?
      //
      DocumentBuilderFactory documentBuilderFactory = XMLParserFactoryProducer.createSecureDocBuilderFactory();
      documentBuilderFactory.setNamespaceAware( true );

      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

      Document doc = documentBuilder.parse( new InputSource( new StringReader( response ) ) );

      Node envelopeNode = doc.getFirstChild();
      String nsPrefix = envelopeNode.getPrefix();
      Node bodyNode = XMLHandler.getSubNode( envelopeNode, nsPrefix + ":Body" );
      if ( bodyNode == null ) {
        XMLHandler.getSubNode( envelopeNode, nsPrefix + ":body" ); // retry, just in case!
      }

      // Create a few objects to help do the layout of XML snippets we find along the way
      //
      Transformer transformer = null;
      /*
       * as of BACKLOG-4068, explicit xalan factory references have been deprecated; we use the javax.xml factory
       * and let java's SPI determine the proper transformer implementation. In addition, tests has been made to
       * safeguard that https://github.com/pentaho/pentaho-kettle/commit/3b57f7a9aac657fe77cc4f08e8d4287fcccbc073
       * continues working as intended
       */

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformer = transformerFactory.newTransformer();

      transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
      transformer.setOutputProperty( OutputKeys.INDENT, "yes" );

      if ( log.isDetailed() ) {
        StringWriter bodyXML = new StringWriter();
        transformer.transform( new DOMSource( bodyNode ), new StreamResult( bodyXML ) );

        logDetailed( bodyXML.toString() );
      }

      // The node directly below the body is the response node
      // It's apparently a hassle to get the name in a consistent way, but we know it's the first element node
      //
      Node responseNode = null;
      NodeList nodeList = null;
      if ( !Utils.isEmpty( meta.getRepeatingElementName() ) ) {

        // We have specified the repeating element name : use it
        //
        nodeList = ( (Element) bodyNode ).getElementsByTagName( meta.getRepeatingElementName() );

      } else {

        if ( meta.isReturningReplyAsString() ) {

          // Just return the body node as an XML string...
          //
          StringWriter nodeXML = new StringWriter();
          transformer.transform( new DOMSource( bodyNode ), new StreamResult( nodeXML ) );
          String xml = response; // nodeXML.toString();
          Object[] outputRowData = createNewRow( rowData );
          int index = rowData == null ? 0 : getInputRowMeta().size();
          outputRowData[ index++ ] = xml;
          putRow( data.outputRowMeta, outputRowData );

        } else {

          // We just grab the list of nodes from the children of the body
          // Look for the first element node (first real child) and take that one.
          // For that child-element, we consider all the children below
          //
          NodeList responseChildren = bodyNode.getChildNodes();
          for ( int i = 0; i < responseChildren.getLength(); i++ ) {
            Node responseChild = responseChildren.item( i );
            if ( responseChild.getNodeType() == Node.ELEMENT_NODE ) {
              responseNode = responseChild;
              break;
            }
          }

          // See if we want the whole block returned as XML...
          //
          if ( meta.getFieldsOut().size() == 1 ) {
            WebServiceField field = meta.getFieldsOut().get( 0 );
            if ( field.getWsName().equals( responseNode.getNodeName() ) ) {
              // Pass the data as XML
              //
              StringWriter nodeXML = new StringWriter();
              transformer.transform( new DOMSource( responseNode ), new StreamResult( nodeXML ) );
              String xml = nodeXML.toString();

              Object[] outputRowData = createNewRow( rowData );
              int index = rowData == null ? 0 : getInputRowMeta().size();
              outputRowData[ index++ ] = xml;
              putRow( data.outputRowMeta, outputRowData );

            } else {
              if ( responseNode != null ) {
                nodeList = responseNode.getChildNodes();
              }
            }

          } else {
            if ( responseNode != null ) {
              nodeList = responseNode.getChildNodes();
            }
          }
        }
      }

      // The section below is just for repeating nodes. If we don't have those it ends here.
      //
      if ( nodeList == null || meta.isReturningReplyAsString() ) {
        return;
      }

      // Allocate a result row in case we are dealing with a single result row
      //
      Object[] outputRowData = createNewRow( rowData );

      // Now loop over the node list found above...
      //
      boolean singleRow = false;
      int fieldsFound = 0;
      for ( int i = 0; i < nodeList.getLength(); i++ ) {
        Node node = nodeList.item( i );

        if ( meta.isReturningReplyAsString() ) {

          // Just return the body node as an XML string...
          //
          StringWriter nodeXML = new StringWriter();
          transformer.transform( new DOMSource( bodyNode ), new StreamResult( nodeXML ) );
          String xml = nodeXML.toString();
          outputRowData = createNewRow( rowData );
          int index = rowData == null ? 0 : getInputRowMeta().size();
          outputRowData[ index++ ] = xml;
          putRow( data.outputRowMeta, outputRowData );

        } else {

          // This node either contains the data for a single row or it contains the first element of a single result
          // response
          // If we find the node name in out output result fields list, we are going to consider it a single row result.
          //
          WebServiceField field = meta.getFieldOutFromWsName( node.getNodeName(), ignoreNamespacePrefix );
          if ( field != null ) {
            if ( getNodeValue( outputRowData, node, field, transformer, true ) ) {
              // We found a match.
              // This means that we are dealing with a single row
              // It also means that we need to update the output index pointer
              //
              singleRow = true;
              fieldsFound++;
            }
          } else {
            // If we didn't already get data in the previous block we'll assume multiple rows coming back.
            //
            if ( !singleRow ) {
              // Sticking with the multiple-results scenario...
              //

              // TODO: remove next 2 lines, added for debug reasons.
              //
              if ( log.isDetailed() ) {
                StringWriter nodeXML = new StringWriter();
                transformer.transform( new DOMSource( node ), new StreamResult( nodeXML ) );
                logDetailed( BaseMessages
                  .getString( PKG, "WebServices.Log.ResultRowDataFound", nodeXML.toString() ) );
              }

              // Allocate a new row...
              //
              outputRowData = createNewRow( rowData );

              // Let's see what's in there...
              //
              NodeList childNodes = node.getChildNodes();
              for ( int j = 0; j < childNodes.getLength(); j++ ) {
                Node childNode = childNodes.item( j );

                field = meta.getFieldOutFromWsName( childNode.getNodeName(), ignoreNamespacePrefix );
                if ( field != null ) {

                  if ( getNodeValue( outputRowData, childNode, field, transformer, false ) ) {
                    // We found a match.
                    // This means that we are dealing with a single row
                    // It also means that we need to update the output index pointer
                    //
                    fieldsFound++;
                  }
                }
              }

              // Prevent empty rows from being sent out.
              //
              if ( fieldsFound > 0 ) {
                // Send a row in a series of rows on its way.
                //
                putRow( data.outputRowMeta, outputRowData );
              }
            }
          }
        }
      }

      if ( singleRow && fieldsFound > 0 ) {
        // Send the single row on its way.
        //
        putRow( data.outputRowMeta, outputRowData );
      }
    } catch ( Exception e ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "WebServices.ERROR0010.OutputParsingError", response.toString() ), e );
    }
  }

  private Object[] createNewRow( Object[] inputRowData ) {
    return inputRowData == null ? RowDataUtil.allocateRowData( data.outputRowMeta.size() ) : RowDataUtil
      .createResizedCopy( inputRowData, data.outputRowMeta.size() );
  }

  private void compatibleProcessRows( InputStream anXml, Object[] rowData, RowMetaInterface rowMeta,
                                      boolean ignoreNamespacePrefix, String encoding ) throws KettleException {

    // First we should get the complete string
    // The problem is that the string can contain XML or any other format such as HTML saying the service is no longer
    // available.
    // We're talking about a WEB service here.
    // As such, to keep the original parsing scheme, we first read the content.
    // Then we create an input stream from the content again.
    // It's elaborate, but that way we can report on the failure more correctly.
    //
    String response = readStringFromInputStream( anXml, encoding );

    // Create a new reader to feed into the XML Input Factory below...
    //
    StringReader stringReader = new StringReader( response.toString() );

    // TODO Very empirical : see if we can do something better here
    try {
      XMLInputFactory vFactory = XMLInputFactory.newInstance();
      XMLStreamReader vReader = vFactory.createXMLStreamReader( stringReader );

      Object[] outputRowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
      int outputIndex = 0;

      boolean processing = false;
      boolean oneValueRowProcessing = false;
      for ( int event = vReader.next(); vReader.hasNext(); event = vReader.next() ) {
        switch ( event ) {
          case XMLStreamConstants.START_ELEMENT:

            // Start new code
            // START_ELEMENT= 1
            //
            if ( log.isRowLevel() ) {
              logRowlevel( "START_ELEMENT / " + vReader.getAttributeCount() + " / " + vReader.getNamespaceCount() );
            }

            // If we start the xml element named like the return type,
            // we start a new row
            //
            if ( log.isRowLevel() ) {
              logRowlevel( "vReader.getLocalName = " + vReader.getLocalName() );
            }
            if ( Utils.isEmpty( meta.getOutFieldArgumentName() ) ) {
              // getOutFieldArgumentName() == null
              if ( oneValueRowProcessing ) {
                WebServiceField field = meta.getFieldOutFromWsName( vReader.getLocalName(), ignoreNamespacePrefix );
                if ( field != null ) {
                  outputRowData[ outputIndex++ ] = getValue( vReader.getElementText(), field );
                  putRow( data.outputRowMeta, outputRowData );
                  oneValueRowProcessing = false;
                } else {
                  if ( meta.getOutFieldContainerName().equals( vReader.getLocalName() ) ) {
                    // meta.getOutFieldContainerName() = vReader.getLocalName()
                    if ( log.isRowLevel() ) {
                      logRowlevel( "OutFieldContainerName = " + meta.getOutFieldContainerName() );
                    }
                    oneValueRowProcessing = true;
                  }
                }
              }
            } else {
              // getOutFieldArgumentName() != null
              if ( log.isRowLevel() ) {
                logRowlevel( "OutFieldArgumentName = " + meta.getOutFieldArgumentName() );
              }
              if ( meta.getOutFieldArgumentName().equals( vReader.getLocalName() ) ) {
                if ( log.isRowLevel() ) {
                  logRowlevel( "vReader.getLocalName = " + vReader.getLocalName() );
                }
                if ( log.isRowLevel() ) {
                  logRowlevel( "OutFieldArgumentName = " );
                }
                if ( processing ) {
                  WebServiceField field =
                    meta.getFieldOutFromWsName( vReader.getLocalName(), ignoreNamespacePrefix );
                  if ( field != null ) {
                    int index = data.outputRowMeta.indexOfValue( field.getName() );
                    if ( index >= 0 ) {
                      outputRowData[ index ] = getValue( vReader.getElementText(), field );
                    }
                  }
                  processing = false;
                } else {
                  WebServiceField field =
                    meta.getFieldOutFromWsName( vReader.getLocalName(), ignoreNamespacePrefix );
                  if ( meta.getFieldsOut().size() == 1 && field != null ) {
                    // This can be either a simple return element, or a complex type...
                    //
                    try {
                      if ( meta.isPassingInputData() ) {
                        for ( int i = 0; i < rowMeta.getValueMetaList().size(); i++ ) {
                          ValueMetaInterface valueMeta = getInputRowMeta().getValueMeta( i );
                          outputRowData[ outputIndex++ ] = valueMeta.cloneValueData( rowData[ i ] );

                        }
                      }

                      outputRowData[ outputIndex++ ] = getValue( vReader.getElementText(), field );
                      putRow( data.outputRowMeta, outputRowData );
                    } catch ( XMLStreamException e ) {
                      throw new KettleStepException( "Unable to get value for field ["
                        + field.getName()
                        + "].  Verify that this is not a complex data type by looking at the response XML.", e );
                    }
                  } else {
                    for ( WebServiceField curField : meta.getFieldsOut() ) {
                      if ( !Utils.isEmpty( curField.getName() ) ) {
                        outputRowData[ outputIndex++ ] = getValue( vReader.getElementText(), curField );
                      }
                    }
                    processing = true;
                  }
                }

              } else {
                if ( log.isRowLevel() ) {
                  logRowlevel( "vReader.getLocalName = " + vReader.getLocalName() );
                }
                if ( log.isRowLevel() ) {
                  logRowlevel( "OutFieldArgumentName = " + meta.getOutFieldArgumentName() );
                }
              }
            }
            break;

          case XMLStreamConstants.END_ELEMENT:
            // END_ELEMENT= 2
            if ( log.isRowLevel() ) {
              logRowlevel( "END_ELEMENT" );
            }
            // If we end the xml element named as the return type, we
            // finish a row
            if ( ( meta.getOutFieldArgumentName() == null && meta.getOperationName().equals(
              vReader.getLocalName() ) ) ) {
              oneValueRowProcessing = false;
            } else if ( meta.getOutFieldArgumentName() != null
              && meta.getOutFieldArgumentName().equals( vReader.getLocalName() ) ) {
              putRow( data.outputRowMeta, outputRowData );
              processing = false;
            }
            break;
          case XMLStreamConstants.PROCESSING_INSTRUCTION:
            // PROCESSING_INSTRUCTION= 3
            if ( log.isRowLevel() ) {
              logRowlevel( "PROCESSING_INSTRUCTION" );
            }
            break;
          case XMLStreamConstants.CHARACTERS:
            // CHARACTERS= 4
            if ( log.isRowLevel() ) {
              logRowlevel( "CHARACTERS" );
            }
            break;
          case XMLStreamConstants.COMMENT:
            // COMMENT= 5
            if ( log.isRowLevel() ) {
              logRowlevel( "COMMENT" );
            }
            break;
          case XMLStreamConstants.SPACE:
            // PROCESSING_INSTRUCTION= 6
            if ( log.isRowLevel() ) {
              logRowlevel( "PROCESSING_INSTRUCTION" );
            }
            break;
          case XMLStreamConstants.START_DOCUMENT:
            // START_DOCUMENT= 7
            if ( log.isRowLevel() ) {
              logRowlevel( "START_DOCUMENT" );
            }
            if ( log.isRowLevel() ) {
              logRowlevel( vReader.getText() );
            }
            break;
          case XMLStreamConstants.END_DOCUMENT:
            // END_DOCUMENT= 8
            if ( log.isRowLevel() ) {
              logRowlevel( "END_DOCUMENT" );
            }
            break;
          case XMLStreamConstants.ENTITY_REFERENCE:
            // ENTITY_REFERENCE= 9
            if ( log.isRowLevel() ) {
              logRowlevel( "ENTITY_REFERENCE" );
            }
            break;
          case XMLStreamConstants.ATTRIBUTE:
            // ATTRIBUTE= 10
            if ( log.isRowLevel() ) {
              logRowlevel( "ATTRIBUTE" );
            }
            break;
          case XMLStreamConstants.DTD:
            // DTD= 11
            if ( log.isRowLevel() ) {
              logRowlevel( "DTD" );
            }
            break;
          case XMLStreamConstants.CDATA:
            // CDATA= 12
            if ( log.isRowLevel() ) {
              logRowlevel( "CDATA" );
            }
            break;
          case XMLStreamConstants.NAMESPACE:
            // NAMESPACE= 13
            if ( log.isRowLevel() ) {
              logRowlevel( "NAMESPACE" );
            }
            break;
          case XMLStreamConstants.NOTATION_DECLARATION:
            // NOTATION_DECLARATION= 14
            if ( log.isRowLevel() ) {
              logRowlevel( "NOTATION_DECLARATION" );
            }
            break;
          case XMLStreamConstants.ENTITY_DECLARATION:
            // ENTITY_DECLARATION= 15
            if ( log.isRowLevel() ) {
              logRowlevel( "ENTITY_DECLARATION" );
            }
            break;
          default:
            break;
        }
      }
    } catch ( Exception e ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "WebServices.ERROR0010.OutputParsingError", response ), e );
    }
  }

  private boolean getNodeValue( Object[] outputRowData, Node node, WebServiceField field, Transformer transformer,
                                boolean singleRowScenario ) throws KettleException {

    Integer outputIndex = data.indexMap.get( field.getWsName() );
    if ( outputIndex == null ) {
      // Unknown field : don't look any further, it's not a field we want to use.
      //
      return false;
    }

    // if it's a text node or if we recognize the field type, we just grab the value
    //
    if ( node.getNodeType() == Node.TEXT_NODE || !field.isComplex() ) {
      Object rowValue = null;

      // See if this is a node we expect as a return value...
      //
      String textContent = node.getTextContent();
      try {
        rowValue = getValue( textContent, field );
        outputRowData[ outputIndex ] = rowValue;
        return true;
      } catch ( Exception e ) {
        throw new KettleException( "Unable to convert value ["
          + textContent + "] for field [" + field.getWsName() + "], type [" + field.getXsdType() + "]", e );
      }
    } else if ( node.getNodeType() == Node.ELEMENT_NODE ) {
      // Perhaps we're dealing with complex data types.
      // Perhaps we can just ship the XML snippet over to the next steps.
      //
      try {
        StringWriter childNodeXML = new StringWriter();
        transformer.transform( new DOMSource( node ), new StreamResult( childNodeXML ) );
        outputRowData[ outputIndex ] = childNodeXML.toString();
        return true;
      } catch ( Exception e ) {
        throw new KettleException( "Unable to transform DOM node with name [" + node.getNodeName() + "] to XML", e );
      }
    }

    // Nothing found, return false
    //
    return false;
  }

  private Object getValue( String vNodeValue, WebServiceField field ) throws XMLStreamException, ParseException {
    if ( vNodeValue == null ) {
      return null;
    } else {
      if ( XsdType.BOOLEAN.equals( field.getXsdType() ) ) {
        return Boolean.valueOf( vNodeValue );
      } else if ( XsdType.DATE.equals( field.getXsdType() ) ) {
        try {
          return dateFormat.parse( vNodeValue );
        } catch ( ParseException e ) {
          logError( Const.getStackTracker( e ) );
          setErrors( 1 );
          stopAll();
          return null;
        }
      } else if ( XsdType.TIME.equals( field.getXsdType() ) ) {
        try {
          return timeFormat.parse( vNodeValue );
        } catch ( ParseException e ) {
          logError( Const.getStackTracker( e ) );
          setErrors( 1 );
          stopAll();
          return null;
        }
      } else if ( XsdType.DATE_TIME.equals( field.getXsdType() ) ) {
        try {
          return dateTimeFormat.parse( vNodeValue );
        } catch ( ParseException e ) {
          logError( Const.getStackTracker( e ) );
          setErrors( 1 );
          stopAll();
          return null;
        }
      } else if ( XsdType.INTEGER.equals( field.getXsdType() )
        || XsdType.SHORT.equals( field.getXsdType() ) || XsdType.INTEGER_DESC.equals( field.getXsdType() ) ) {
        try {
          return Long.parseLong( vNodeValue );
        } catch ( NumberFormatException e ) {
          logError( Const.getStackTracker( e ) );
          setErrors( 1 );
          stopAll();
          return null;
        }
      } else if ( XsdType.FLOAT.equals( field.getXsdType() ) || XsdType.DOUBLE.equals( field.getXsdType() ) ) {
        try {
          return Double.parseDouble( vNodeValue );
        } catch ( NumberFormatException e ) {
          logError( Const.getStackTracker( e ) );
          setErrors( 1 );
          stopAll();
          return null;
        }
      } else if ( XsdType.BINARY.equals( field.getXsdType() ) ) {
        return Base64.decodeBase64( vNodeValue.getBytes() );
      } else if ( XsdType.DECIMAL.equals( field.getXsdType() ) ) {
        return new BigDecimal( vNodeValue );
      } else {
        return vNodeValue;
      }
    }
  }

}
