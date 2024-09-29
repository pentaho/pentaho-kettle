/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.job.entries.writetofile;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import org.junit.Test;
import static org.junit.Assert.*;

public class JobEntryWriteToFileTest2 {

  @Test
  public void testCrInContent() throws Exception {
    String content = "this\r\nthat";
    JobEntryWriteToFile je = new JobEntryWriteToFile();
    je.setContent( content );
    String xml = je.getXML();

    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    InputSource is = new InputSource();
    is.setCharacterStream(
      new StringReader( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<job>\n" + xml + "\n</job>" ) );
    Document doc = db.parse( is );

    JobEntryWriteToFile je2 = new JobEntryWriteToFile();
    je2.loadXML( doc.getDocumentElement(), null, null, null, null );
    assertEquals( content, je2.getContent() );
  }
}