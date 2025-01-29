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

package org.pentaho.di.repository;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.pur.PurRepository;

import java.util.Arrays;
import java.util.Collection;

@RunWith( Parameterized.class )
public class RepositoryTestLazySupport {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  public RepositoryTestLazySupport( Boolean lazyRepo ) {
    System.setProperty( PurRepository.LAZY_REPOSITORY, lazyRepo.toString() );
  }

  @Parameterized.Parameters
  public static Collection primeNumbers() {
    return Arrays.asList( new Object[][] { { true }, { false } } );
  }
}
