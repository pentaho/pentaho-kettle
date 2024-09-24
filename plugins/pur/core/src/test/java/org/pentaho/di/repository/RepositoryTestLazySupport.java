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
