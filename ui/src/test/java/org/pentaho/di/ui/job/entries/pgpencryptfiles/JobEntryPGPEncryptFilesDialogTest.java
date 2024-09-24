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

package org.pentaho.di.ui.job.entries.pgpencryptfiles;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JobEntryPGPEncryptFilesDialogTest {

    @Test
    public void testFEILDSOrdinal()
    {
        assertEquals(0, JobEntryPGPEncryptFilesDialog.FIELDS.ROWNUM.ordinal());
        assertEquals(1, JobEntryPGPEncryptFilesDialog.FIELDS.ACTION.ordinal());
        assertEquals(2, JobEntryPGPEncryptFilesDialog.FIELDS.SOURCE.ordinal());
        assertEquals(3, JobEntryPGPEncryptFilesDialog.FIELDS.WILDCARD.ordinal());
        assertEquals(4, JobEntryPGPEncryptFilesDialog.FIELDS.USERID.ordinal());
        assertEquals(5, JobEntryPGPEncryptFilesDialog.FIELDS.DESTINATION.ordinal());
    }

}