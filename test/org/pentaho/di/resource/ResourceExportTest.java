package org.pentaho.di.resource;

import java.util.Hashtable;
import java.util.Map;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.job.JobMeta;

public class ResourceExportTest extends TestCase {

	public void testJobExport() throws Exception {
	    KettleEnvironment.init();
                
        // Load the job metadata
        //
        String filename = "test/org/pentaho/di/resource/top-job.kjb";
        JobMeta jobMeta = new JobMeta(filename, null, null);
        
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
		String topLevelFilename = jobMeta.exportResources(jobMeta, definitions, resourceNaming, null);
		
		System.out.println("Top level filename = "+topLevelFilename);
		
		for (ResourceDefinition resourceDefinition : definitions.values()) {
			System.out.println("Found resource definition: "+resourceDefinition.getFilename());
		}
		
		assertEquals(definitions.size(), 3);
        
	}
}
