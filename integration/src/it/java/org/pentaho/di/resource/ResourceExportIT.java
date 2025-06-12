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


package org.pentaho.di.resource;

import java.util.Hashtable;
import java.util.Map;

import junit.framework.TestCase;

import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.job.JobMeta;

public class ResourceExportIT extends TestCase {

  public void testJobExport() throws Exception {
    KettleEnvironment.init();

    // Load the job metadata
    //
    String filename = "test/org/pentaho/di/resource/top-job.kjb";
    JobMeta jobMeta = new JobMeta( filename, null, null );

    // This job meta object references a few transformations, another job and a mapping
    // All these need to be exported
    // To handle the file-naming, we need a renaming service...
    //
    UUIDResourceNaming resourceNaming = new UUIDResourceNaming();

    // We need a storage facility to keep all the generated code, the filenames, etc.
    //
    Map<String, ResourceDefinition> definitions = new Hashtable<String, ResourceDefinition>();

    // We get back the top-level filename: it's the starting point...
    //
    String topLevelFilename = jobMeta.exportResources( DefaultBowl.getInstance(), jobMeta, definitions, resourceNaming,
      null, null );

    System.out.println( "Top level filename = " + topLevelFilename );

    for ( ResourceDefinition resourceDefinition : definitions.values() ) {
      System.out.println( "Found resource definition: " + resourceDefinition.getFilename() );
    }

    assertEquals( definitions.size(), 3 );

  }
}
