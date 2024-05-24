/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.job.entries.trans;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

/**
 * @author Vadim_Polynkov
 */
public class JobEntryTransDialogTest {

  private static final String FILE_NAME =  "TestTrans.ktr";

  JobEntryTransDialog dialog;

  @Test
  public void testEntryName() {
    dialog = mock( JobEntryTransDialog.class );
    doCallRealMethod().when( dialog ).getEntryName( any() );
    assertEquals( dialog.getEntryName( FILE_NAME ), "${Internal.Entry.Current.Directory}/" + FILE_NAME );
  }
}
