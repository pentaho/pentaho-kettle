/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.pentaho.repository.importexport;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.utils.IRepositoryFactory;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.pentaho.di.core.util.Assert.assertNull;
import static org.pentaho.di.core.util.Assert.assertNotNull;

/**
 * Created by nbaker on 11/5/15.
 */
@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
public class PDIImportUtilTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  /**
   * @see <a href="https://en.wikipedia.org/wiki/Billion_laughs" />
   */
  private static final String MALICIOUS_XML =
    "<?xml version=\"1.0\"?>\n"
      + "<!DOCTYPE lolz [\n"
      + " <!ENTITY lol \"lol\">\n"
      + " <!ELEMENT lolz (#PCDATA)>\n"
      + " <!ENTITY lol1 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n"
      + " <!ENTITY lol2 \"&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;\">\n"
      + " <!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n"
      + " <!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">\n"
      + " <!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">\n"
      + " <!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">\n"
      + " <!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\">\n"
      + " <!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">\n"
      + " <!ENTITY lol9 \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">\n"
      + "]>\n"
      + "<lolz>&lol9;</lolz>";

  @BeforeClass
  public static void setUp() throws Exception {
    KettleEnvironment.init();
  }

  @AfterClass
  public static void reset() {
    PDIImportUtil.setRepositoryFactory( new IRepositoryFactory.CachingRepositoryFactory() );
  }

  @Test
  public void testConnectToRepository() throws Exception {
    IRepositoryFactory mock = mock( IRepositoryFactory.class );
    PDIImportUtil.setRepositoryFactory( mock );

    PDIImportUtil.connectToRepository( "foo" );

    verify( mock, times( 1 ) ).connect( "foo" );
  }

  @Test( timeout = 2000 )
  public void whenLoadingMaliciousXmlFromStringParsingEndsWithNoErrorAndNullValueIsReturned() throws Exception {
    assertNull( PDIImportUtil.loadXMLFrom( MALICIOUS_XML ) );
  }

  @Test( timeout = 2000 )
  public void whenLoadingMaliciousXmlFromInputStreamParsingEndsWithNoErrorAndNullValueIsReturned() throws Exception {
    assertNull( PDIImportUtil.loadXMLFrom( MALICIOUS_XML ) );
  }

  @Test
  public void whenLoadingLegalXmlFromStringNotNullDocumentIsReturned() throws Exception {
    final String trans = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<transformation>"
      + "</transformation>";

    assertNotNull( PDIImportUtil.loadXMLFrom( trans ) );

  }
}
