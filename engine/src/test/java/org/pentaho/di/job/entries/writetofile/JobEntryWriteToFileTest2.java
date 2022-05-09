/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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