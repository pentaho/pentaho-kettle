/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.job.entries.pgpdecryptfiles;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JobEntryPGPDecryptFilesDialogTest {

    @Test
    public void testFEILDSOrdinal()
    {
        assertEquals(0, JobEntryPGPDecryptFilesDialog.FIELDS.ROWNUM.ordinal());
        assertEquals(1, JobEntryPGPDecryptFilesDialog.FIELDS.SOURCE.ordinal());
        assertEquals(2, JobEntryPGPDecryptFilesDialog.FIELDS.WILDCARD.ordinal());
        assertEquals(3, JobEntryPGPDecryptFilesDialog.FIELDS.PASSPHRASE.ordinal());
        assertEquals(4, JobEntryPGPDecryptFilesDialog.FIELDS.DESTINATION.ordinal());
    }

}