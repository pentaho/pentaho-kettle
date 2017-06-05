/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.resource;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ResourceDependencyTest {

  /**
   * @param args
   */
  public static void main( String[] args ) {
    ResourceDependencyTest test = new ResourceDependencyTest();
    try {
      test.setUp();
      test.testJobDependencyList();
      test.testTransformationDependencyList();
    } catch ( Exception ex ) {
      ex.printStackTrace();
    } finally {
      try {
        test.tearDown();
      } catch ( Exception ignored ) {
        // Ignored
      }
    }
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @After
  public void tearDown() {
  }


  @Test
  public void testJobDependencyList() throws Exception {
    // Load the first job metadata
    JobMeta jobMeta = new JobMeta( "test/org/pentaho/di/resource/processchangelog.kjb", null, null );
    List<ResourceReference> resourceReferences = jobMeta.getResourceDependencies();
    assertEquals( 5, resourceReferences.size() );
    for ( int i = 0; i < 5; i++ ) {
      ResourceReference genRef = resourceReferences.get( i );
      ResourceHolderInterface refHolder = genRef.getReferenceHolder();
      boolean checkDatabaseStuff = false;
      if ( i == 0 ) {
        assertEquals( "TABLE_EXISTS", refHolder.getTypeId() );
        checkDatabaseStuff = true;
      } else if ( ( i == 1 ) || ( i == 4 ) ) {
        assertEquals( "SQL", refHolder.getTypeId() );
        checkDatabaseStuff = true;
      } else if ( ( i == 2 ) || ( i == 3 ) ) {
        assertEquals( "TRANS", refHolder.getTypeId() );
        checkDatabaseStuff = false;
      }
      if ( checkDatabaseStuff ) {
        assertEquals( 2, genRef.getEntries().size() );
        for ( int j = 0; j < 2; j++ ) {
          ResourceEntry entry = genRef.getEntries().get( j );
          if ( j == 0 ) {
            assertEquals( ResourceType.SERVER, entry.getResourcetype() );
            assertEquals( "localhost", entry.getResource() );
          } else {
            assertEquals( ResourceType.DATABASENAME, entry.getResourcetype() );
            assertEquals( "test", entry.getResource() );
          }
        }
      } else { // Check Transform Stuff
        assertEquals( 1, genRef.getEntries().size() ); // Only one entry per ref in this case.
        ResourceEntry entry = genRef.getEntries().get( 0 );
        assertEquals( ResourceType.ACTIONFILE, entry.getResourcetype() );
        assertTrue( entry.getResource().endsWith( ".ktr" ) );
      }
    }

  }

  @Test
  public void testTransformationDependencyList() throws Exception {
    TransMeta transMeta = new TransMeta( "test/org/pentaho/di/resource/trans/General - Change log processing.ktr" );
    List<ResourceReference> resourceReferences = transMeta.getResourceDependencies();
    assertEquals( 2, resourceReferences.size() );
    ResourceReference genRef = null;
    for ( ResourceReference look : resourceReferences ) {
      if ( look.getReferenceHolder().getTypeId().equals( "TextFileInput" ) ) {
        genRef = look;
      }
    }
    assertNotNull( genRef );

    ResourceHolderInterface refHolder = genRef.getReferenceHolder();
    assertEquals( "TextFileInput", refHolder.getTypeId() );

    List<ResourceEntry> entries = genRef.getEntries();
    assertEquals( 1, entries.size() );
    ResourceEntry theEntry = entries.get( 0 );
    assertEquals( ResourceType.FILE, theEntry.getResourcetype() );
    assertTrue( theEntry.getResource().endsWith( "changelog.txt" ) );
  }

  /**
   * Private method for displaying what's coming back from the dependency call.
   *
   * @param resourceReferences A list of resource reference to print out
   */
  protected void printResourceReferences( List<ResourceReference> resourceReferences ) {
    for ( ResourceReference genRef : resourceReferences ) {
      ResourceHolderInterface refHolder = genRef.getReferenceHolder();
      System.out.println( "Reference Holder Information" );
      System.out.println( "  Name: " + refHolder.getName() );
      System.out.println( "  Type Id: " + refHolder.getTypeId() );
      System.out.println( "  Resource Entries" );
      List<ResourceEntry> entries = genRef.getEntries();
      for ( ResourceEntry resEntry : entries ) {
        System.out.println( "    Resource Entry" );
        System.out.println( "      Resource Type: " + resEntry.getResourcetype() );
        System.out.println( "      Resource: " + resEntry.getResource() );
      }
    }
  }
}
