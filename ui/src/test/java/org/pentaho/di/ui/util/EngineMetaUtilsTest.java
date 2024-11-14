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


package org.pentaho.di.ui.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.trans.TransMeta;

public class EngineMetaUtilsTest {

  @BeforeClass
  public static void beforeClass() throws Exception {
    KettleEnvironment.init();
  }

  @Test
  public void isJobOrTransformation_withJob() {
    JobMeta jobInstance = new JobMeta();
    assertTrue( EngineMetaUtils.isJobOrTransformation( jobInstance ) );
  }

  @Test
  public void isJobOrTransformation_withTransformation() {
    TransMeta transfromataionInstance = new TransMeta();
    assertTrue( EngineMetaUtils.isJobOrTransformation( transfromataionInstance ) );
  }

  @Test
  public void isJobOrTransformationReturnsFalse_withDatabase() {
    EngineMetaInterface testMetaInstance = mock( EngineMetaInterface.class );
    when( testMetaInstance.getRepositoryElementType() ).thenReturn( RepositoryObjectType.DATABASE );
    assertFalse( EngineMetaUtils.isJobOrTransformation( testMetaInstance ) );
  }

}
