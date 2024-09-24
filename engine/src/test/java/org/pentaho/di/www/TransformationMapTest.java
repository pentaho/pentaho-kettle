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

package org.pentaho.di.www;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TransformationMapTest {

  private static final String TEST_HOST = "127.0.0.1";

  private static final String CLUSTERED_RUN_ID = "CLUSTERED_RUN_ID";

  private static final String TEST_TRANSFORMATION_NAME = "TEST_TRANSFORMATION_NAME";

  private static final String TEST_SOURCE_SLAVE_NAME = "TEST_SOURCE_SLAVE_NAME";

  private static final String TEST_SOURCE_STEP_NAME = "TEST_SOURCE_STEP_NAME";

  private static final String TEST_SOURCE_STEP_COPY = "TEST_SOURCE_STEP_COPY";

  private static final String TEST_TARGET_SLAVE_NAME = "TEST_TARGET_SLAVE_NAME";

  private static final String TEST_TARGET_STEP_NAME = "TEST_TARGET_STEP_NAME";

  private static final String TEST_TARGET_STEP_COPY = "TEST_TARGET_STEP_COPY";

  private TransformationMap transformationMap;

  @Before
  public void before() {
    transformationMap = new TransformationMap();
  }

  @Test
  public void getHostServerSocketPorts() {
    transformationMap.allocateServerSocketPort( 1, TEST_HOST, CLUSTERED_RUN_ID, TEST_TRANSFORMATION_NAME,
        TEST_SOURCE_SLAVE_NAME, TEST_SOURCE_STEP_NAME, TEST_SOURCE_STEP_COPY, TEST_TARGET_SLAVE_NAME,
        TEST_TARGET_STEP_NAME, TEST_TARGET_STEP_COPY );
    List<SocketPortAllocation> actualResult = transformationMap.getHostServerSocketPorts( TEST_HOST );

    assertNotNull( actualResult );
    assertEquals( 1, actualResult.size() );
  }

  @Test
  public void getHostServerSocketPortsWithoutAllocatedPorts() {
    List<SocketPortAllocation> actualResult = transformationMap.getHostServerSocketPorts( TEST_HOST );
    assertNotNull( actualResult );
    assertTrue( actualResult.isEmpty() );
  }

}
