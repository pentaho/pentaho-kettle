/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
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