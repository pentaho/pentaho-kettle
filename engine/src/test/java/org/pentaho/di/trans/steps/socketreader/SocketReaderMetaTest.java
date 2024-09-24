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

package org.pentaho.di.trans.steps.socketreader;

import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class SocketReaderMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testLoadSave() throws KettleException {
    List<String> attributes = Arrays.asList( "Hostname", "Port", "BufferSize", "Compressed" );

    LoadSaveTester loadSaveTester = new LoadSaveTester( SocketReaderMeta.class, attributes );

    loadSaveTester.testSerialization();
  }
}
