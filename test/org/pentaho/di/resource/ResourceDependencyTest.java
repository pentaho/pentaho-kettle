package org.pentaho.di.resource;

import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.trans.TransMeta;

public class ResourceDependencyTest extends TestCase {

  
  /**
   * @param args
   */
  public static void main(String[] args) {
    ResourceDependencyTest test = new ResourceDependencyTest();
    try {
      test.setUp();
      test.testJobDependencyList();
      test.testTransformationDependencyList();
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      try {
        test.tearDown();
      } catch (Exception ignored) {
        //Ignored
      }
    }
  }

  public void testJobDependencyList() throws Exception {
    KettleEnvironment.init();
    
    // Load the first job metadata
    JobMeta jobMeta = new JobMeta("test/org/pentaho/di/resource/processchangelog.kjb", null, null); //$NON-NLS-1$
    List<ResourceReference> resourceReferences = jobMeta.getResourceDependencies();
    // printResourceReferences(resourceReferences);
    assertEquals(5, resourceReferences.size());
    for (int i=0; i<5; i++ ) {
      ResourceReference genRef = resourceReferences.get(i);
      // System.out.println(genRef.toXml());
      ResourceHolderInterface refHolder = genRef.getReferenceHolder();
      boolean checkDatabaseStuff = false;
      if (i == 0) {
        assertEquals("TABLE_EXISTS", refHolder.getTypeId()); //$NON-NLS-1$
        checkDatabaseStuff = true;
      } else if ( (i == 1) || (i == 4)){
        assertEquals("SQL", refHolder.getTypeId()); //$NON-NLS-1$
        checkDatabaseStuff = true; 
      } else if ( (i == 2) || (i == 3) ){
        assertEquals("TRANS", refHolder.getTypeId()); //$NON-NLS-1$
        checkDatabaseStuff = false;
      } 
      if (checkDatabaseStuff) {
        assertEquals(2, genRef.getEntries().size());
        for (int j=0; j<2; j++) {
          ResourceEntry entry = genRef.getEntries().get(j);
          if (j == 0) {
            assertEquals(ResourceType.SERVER, entry.getResourcetype());
            assertEquals("localhost", entry.getResource() ); //$NON-NLS-1$
          } else {
            assertEquals(ResourceType.DATABASENAME, entry.getResourcetype());
            assertEquals("test", entry.getResource()); //$NON-NLS-1$
          }
        }
      } else { // Check Transform Stuff
        assertEquals(1, genRef.getEntries().size()); // Only one entry per ref in this case.
        ResourceEntry entry = genRef.getEntries().get(0);
        assertEquals(ResourceType.ACTIONFILE, entry.getResourcetype());
        assertTrue(entry.getResource().endsWith(".ktr")); //$NON-NLS-1$
      }
    }
    
  }
  
  public void testTransformationDependencyList() throws Exception {
    KettleEnvironment.init();
        
    TransMeta transMeta = new TransMeta("test/org/pentaho/di/resource/trans/General - Change log processing.ktr"); //$NON-NLS-1$
    List<ResourceReference> resourceReferences = transMeta.getResourceDependencies();
    // printResourceReferences(resourceReferences);    
    assertEquals(2, resourceReferences.size());
    ResourceReference genRef = null;
    for (ResourceReference look : resourceReferences) {
    	if (look.getReferenceHolder().getTypeId().equals("TextFileInput")) {
    		genRef = look;
    	}
    }
    assertNotNull(genRef);
    // System.out.println(genRef.toXml());
    
    ResourceHolderInterface refHolder = genRef.getReferenceHolder();
    assertEquals("TextFileInput", refHolder.getTypeId()); //$NON-NLS-1$
    
    List<ResourceEntry> entries = genRef.getEntries();
    assertEquals(1, entries.size());
    ResourceEntry theEntry = entries.get(0);
    assertEquals(ResourceType.FILE, theEntry.getResourcetype());
    assertTrue(theEntry.getResource().endsWith("changelog.txt")); //$NON-NLS-1$
    
  }

  /**
   * Private method for displaying what's coming back from the dependency call.
   * @param resourceReferences
   */
  protected void printResourceReferences(List<ResourceReference> resourceReferences) {
    for (int i=0; i<resourceReferences.size(); i++) {
      ResourceReference genRef = resourceReferences.get(i);
      ResourceHolderInterface refHolder = genRef.getReferenceHolder();
      System.out.println("Reference Holder Information"); //$NON-NLS-1$
        System.out.println("  Name: " + refHolder.getName()); //$NON-NLS-1$
        System.out.println("  Type Id: " + refHolder.getTypeId()); //$NON-NLS-1$
        System.out.println("  Resource Entries"); //$NON-NLS-1$
        List<ResourceEntry> entries = genRef.getEntries();
        for ( int j=0; j<entries.size(); j++) {
          ResourceEntry resEntry = entries.get(j);
          System.out.println("    Resource Entry"); //$NON-NLS-1$
          System.out.println("      Resource Type: " + resEntry.getResourcetype()); //$NON-NLS-1$
          System.out.println("      Resource: " + resEntry.getResource()); //$NON-NLS-1$
        }
    }
  }
  
}
