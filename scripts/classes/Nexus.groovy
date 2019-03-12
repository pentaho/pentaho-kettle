@Grab(group='org.apache.httpcomponents', module='httpclient', version='4.4')
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import org.xml.sax.InputSource;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;



public class Nexus {
  
  
  static String NEXUS_USERNAME            = System.getProperty("NEXUS_USERNAME");
  static String NEXUS_PASSWORD            = System.getProperty("NEXUS_PASSWORD");
  static String NEXUS_STORAGE_PATH_URI    = System.getProperty("NEXUS_STORAGE_PATH_URI");
  static String NEXUS_HOST                = System.getProperty("NEXUS_HOST");
  static String NEXUS_PORT                = System.getProperty("NEXUS_PORT");
  static String NIGHTLY_FAILSAFE_OVERRIDE = System.getProperty("NIGHTLY_FAILSAFE_OVERRIDE");
  

  public static void createRepo(String repoName) {
    
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.newDocument();
    Element repositoryElement = doc.createElement("repository");
    doc.appendChild( repositoryElement );
    Element dataElement = doc.createElement( "data" );
    repositoryElement.appendChild( dataElement );
    
    Element idElement = doc.createElement( "id" );
    idElement.appendChild(doc.createTextNode( repoName ));
    dataElement.appendChild( idElement );
    
    Element nameElement = doc.createElement( "name" );
    nameElement.appendChild(doc.createTextNode( repoName ));
    dataElement.appendChild( nameElement );
    
    Element providerElement = doc.createElement( "provider" );
    providerElement.appendChild(doc.createTextNode( "maven2" ));
    dataElement.appendChild( providerElement );
    
    Element contentResourceURIElement = doc.createElement( "contentResourceURI" );
    contentResourceURIElement.appendChild(doc.createTextNode( "http://" + NEXUS_HOST + ":" + NEXUS_PORT + "/content/repositories/" + repoName ));
    dataElement.appendChild( contentResourceURIElement );
    
    Element providerRoleElement = doc.createElement( "providerRole" );
    providerRoleElement.appendChild(doc.createTextNode( "org.sonatype.nexus.proxy.repository.Repository" ));
    dataElement.appendChild( providerRoleElement );
    
    Element repoTypeElement = doc.createElement( "repoType" );
    repoTypeElement.appendChild(doc.createTextNode( "hosted" ));
    dataElement.appendChild( repoTypeElement );
    
    Element formatElement = doc.createElement( "format" );
    formatElement.appendChild(doc.createTextNode( "maven2" ));
    dataElement.appendChild( formatElement );
    
    Element exposedElement = doc.createElement( "exposed" );
    exposedElement.appendChild(doc.createTextNode( "true" ));
    dataElement.appendChild( exposedElement );
    
    Element writePolicyElement = doc.createElement( "writePolicy" );
    writePolicyElement.appendChild(doc.createTextNode( "ALLOW_WRITE" ));
    dataElement.appendChild( writePolicyElement );
    
    Element browseableElement = doc.createElement( "browseable" );
    browseableElement.appendChild(doc.createTextNode( "true" ));
    dataElement.appendChild( browseableElement );
    
    Element indexableElement = doc.createElement( "indexable" );
    indexableElement.appendChild(doc.createTextNode( "true" ));
    dataElement.appendChild( indexableElement );
    
    Element notFoundCacheTTLElement = doc.createElement( "notFoundCacheTTL" );
    notFoundCacheTTLElement.appendChild(doc.createTextNode( "1440" ));
    dataElement.appendChild( notFoundCacheTTLElement );
    
    Element repoPolicyElement = doc.createElement( "repoPolicy" );
    repoPolicyElement.appendChild(doc.createTextNode( "RELEASE" ));
    dataElement.appendChild( repoPolicyElement );
    
    Element downloadRemoteIndexesElement = doc.createElement( "downloadRemoteIndexes" );
    downloadRemoteIndexesElement.appendChild(doc.createTextNode( "false" ));
    dataElement.appendChild( downloadRemoteIndexesElement );
    
    Element defaultLocalStorageUrlElement = doc.createElement( "defaultLocalStorageUrl" );
    defaultLocalStorageUrlElement.appendChild(doc.createTextNode( NEXUS_STORAGE_PATH_URI + "/" + repoName ));
    dataElement.appendChild( defaultLocalStorageUrlElement );
    
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
    DOMSource source = new DOMSource(doc);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintWriter printWriter = new PrintWriter(outputStream);
    StreamResult result = new StreamResult(printWriter);
    transformer.transform(source, result);
    String repositoryXml = outputStream.toString();
    
    System.out.println("creating repo " + repoName + "...");
    
    CredentialsProvider credsProvider = new BasicCredentialsProvider();
    credsProvider.setCredentials(
            new AuthScope(NEXUS_HOST, Integer.parseInt(NEXUS_PORT)),
            new UsernamePasswordCredentials(NEXUS_USERNAME, NEXUS_PASSWORD));
    CloseableHttpClient httpclient = HttpClients.custom()
            .setDefaultCredentialsProvider(credsProvider)
            .build();
    HttpPost httpPost = new HttpPost("http://" + NEXUS_HOST + ":" + NEXUS_PORT + "/service/local/repositories");
    httpPost.setHeader("Content-type", "application/xml; charset=UTF-8");
    httpPost.setHeader("Accept", "application/xml");
    httpPost.setEntity(new StringEntity(repositoryXml));
    CloseableHttpResponse response = httpclient.execute(httpPost);
    System.out.println(response.getStatusLine());
    String responseString = new BasicResponseHandler().handleResponse(response);
    //System.out.println(responseString);
    response.close();
    httpclient.close();
    
  }
  
  
  public static void addRepoToGroup( String repoName, String repoGroupName ) {
    
    CredentialsProvider credsProvider = new BasicCredentialsProvider();
    credsProvider.setCredentials(
            new AuthScope(NEXUS_HOST, Integer.parseInt(NEXUS_PORT)),
            new UsernamePasswordCredentials(NEXUS_USERNAME, NEXUS_PASSWORD));
    CloseableHttpClient httpclient = HttpClients.custom()
            .setDefaultCredentialsProvider(credsProvider)
            .build();
    HttpGet httpGet = new HttpGet("http://" + NEXUS_HOST + ":" + NEXUS_PORT + "/service/local/repo_groups/" + repoGroupName);
    httpGet.setHeader("Content-type", "application/xml; charset=UTF-8");
    CloseableHttpResponse response = httpclient.execute(httpGet);       
    System.out.println(response.getStatusLine());
    String responseString = new BasicResponseHandler().handleResponse(response);
    //System.out.println(responseString);
    
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document repoGroupDoc = docBuilder.parse( new InputSource( new StringReader( responseString ) ) );
    
    NodeList repositoriesElements = repoGroupDoc.getElementsByTagName("repositories");
    Element repositoriesElement = (Element) repositoriesElements.item(0);
    
    Element repoGroupMemberElement = repoGroupDoc.createElement( "repo-group-member" );
    repositoriesElement.appendChild( repoGroupMemberElement );
    Element idElement = repoGroupDoc.createElement( "id" );
    idElement.appendChild(repoGroupDoc.createTextNode( repoName ));
    repoGroupMemberElement.appendChild( idElement );
    Element nameElement = repoGroupDoc.createElement( "name" );
    nameElement.appendChild(repoGroupDoc.createTextNode( repoName ));
    repoGroupMemberElement.appendChild( nameElement );
    Element resourceURIElement = repoGroupDoc.createElement( "resourceURI" );
    resourceURIElement.appendChild(repoGroupDoc.createTextNode( "http://" + NEXUS_HOST + ":" + NEXUS_PORT + "/service/local/repo_groups/" + repoGroupName + "/" + repoName ));
    repoGroupMemberElement.appendChild( resourceURIElement );
    
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
    DOMSource source = new DOMSource(repoGroupDoc);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintWriter printWriter = new PrintWriter(outputStream);
    StreamResult result = new StreamResult(printWriter);
    transformer.transform(source, result);
    String updatedRepoGroupXml = outputStream.toString();

    System.out.println("adding " + repoName + " to group " + repoGroupName + "...");
    
    HttpPut httpPut = new HttpPut("http://" + NEXUS_HOST + ":" + NEXUS_PORT + "/service/local/repo_groups/" + repoGroupName);
    HttpParams httpParams = new BasicHttpParams();
    httpParams.setParameter("groupId", repoGroupName );
    httpPut.setParams(httpParams);
    httpPut.setHeader("Content-type", "application/xml; charset=UTF-8");
    httpPut.setHeader("Accept", "application/xml");
    httpPut.setEntity(new StringEntity(updatedRepoGroupXml));
    response = httpclient.execute(httpPut);
    System.out.println(response.getStatusLine());
    responseString = new BasicResponseHandler().handleResponse(response);
    //System.out.println(responseString);
    response.close();
    httpclient.close();

  }
  
  public static void deleteRepo(String repoName) {
    
    CloseableHttpResponse response = null;
    CloseableHttpClient httpclient = null;
    
    if ( ! ( repoName.contains("nightly") || repoName.contains("test") || (NIGHTLY_FAILSAFE_OVERRIDE != null) ) ) {
      throw new RuntimeException( "*** FAILURE! You are trying to delete non-nightly repo " + repoName + " and have not issued a NIGHTLY_FAILSAFE_OVERRIDE! ***" );
    }
    
    try {
      CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(
              new AuthScope(NEXUS_HOST, Integer.parseInt(NEXUS_PORT)),
              new UsernamePasswordCredentials(NEXUS_USERNAME, NEXUS_PASSWORD));
      httpclient = HttpClients.custom()
              .setDefaultCredentialsProvider(credsProvider)
              .build();
              
      HttpDelete httpDelete = new HttpDelete( "http://" + NEXUS_HOST + ":" + NEXUS_PORT + "/service/local/repositories/" + repoName );        
      
      response = httpclient.execute(httpDelete);       
      System.out.println(response.getStatusLine());
      String responseString = new BasicResponseHandler().handleResponse(response);
      System.out.println(responseString);
    } catch (HttpResponseException hre) {
      System.out.println(hre.getMessage());
      System.out.println("DON'T PANIC! ... " + repoName + " was probably not found");
    } finally {   
      response.close();
      httpclient.close();
    }
    
  }
  
  
}