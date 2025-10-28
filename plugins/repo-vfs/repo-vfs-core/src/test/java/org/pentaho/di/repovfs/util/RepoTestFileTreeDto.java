package org.pentaho.di.repovfs.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * To mock a file tree response from the server
 */
public class RepoTestFileTreeDto {

  private static String FILE_TEMPLATE = "<file>\n" +
    "\t<aclNode>false</aclNode>\n" +
    "\t<createdDate>1745250892002</createdDate>\n" +
    "\t<deletedDate></deletedDate>\n" +
    "\t<fileSize>13824</fileSize>\n" +
    "\t<folder>false</folder>\n" +
    "\t<hidden>false</hidden>\n" +
    "\t<id>8bce2b16-b4c4-4606-9b25-b5549c102323</id>\n" +
    "\t<lastModifiedDate>1745337510924</lastModifiedDate>\n" +
    "\t<locale>en</locale>\n" +
    "\t<lockDate></lockDate>\n" +
    "\t<locked>false</locked>\n" +
    "\t<name>file.ext</name>\n" +
    "\t<notSchedulable>false</notSchedulable>\n" +
    "\t<ownerType>-1</ownerType>\n" +
    "\t<path>/home/public/somewhere</path>\n" +
    "\t<title>file.ext</title>\n" +
    "\t<versionCommentEnabled>false</versionCommentEnabled>\n" +
    "\t<versionId>1.1</versionId>\n" +
    "\t<versioned>true</versioned>\n" +
    "\t<versioningEnabled>false</versioningEnabled>\n" +
    "</file>";

  public static Element createFileXml( String id, String name, String path, boolean isFolder )
    throws DocumentException {
    Document docTemp = DocumentHelper.parseText( FILE_TEMPLATE );
    Element file = docTemp.getRootElement();
    replaceOrAddElement( file, "id", id );
    replaceOrAddElement( file, "name", name );
    replaceOrAddElement( file, "title", name );
    replaceOrAddElement( file, "path", path );
    replaceOrAddElement( file, "folder", Boolean.toString( isFolder ) );
    return file;
  }

  public static Document createFileTreeDto( Element file, Element ... children ) {
    Document doc = DocumentHelper.createDocument();
    Element fileTreeDto = doc.addElement( "repositoryFileTreeDto" );
    for ( Element child : children ) {
      // each child is a "children" element with a single file inside (for some reason)
      Element childWrapper = fileTreeDto.addElement( "children" );
      childWrapper.add( child.detach() );
    }
    fileTreeDto.add( file.detach() );
    return doc;
  }


  private static void replaceOrAddElement( Element parent, String name, String text ) {
    Element element = parent.element( name );
    if ( element == null ) {
      element = parent.addElement( name );
    }
    element.setText( text );
  }
}
