package org.pentaho.di.resource;

import java.util.Hashtable;
import java.util.Map;

import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.job.JobEntryLoader;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.StepLoader;

import junit.framework.TestCase;

public class ResourceExportTest extends TestCase {

	public void testJobExport() throws Exception {
        EnvUtil.environmentInit();
        LogWriter.getInstance(LogWriter.LOG_LEVEL_BASIC);
        
        StepLoader.init();
        JobEntryLoader.init();
        
        // Load the job metadata
        //
        JobMeta jobMeta = new JobMeta(LogWriter.getInstance(), "test/org/pentaho/di/resource/top-job.kjb", null, null);
        
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
		String topLevelFilename = jobMeta.exportResources(jobMeta, definitions, resourceNaming);
		
		System.out.println("Top level filename = "+topLevelFilename);
		
		for (ResourceDefinition resourceDefinition : definitions.values()) {
			System.out.println("Found resource definition: "+resourceDefinition.getFilename());
		}
		
		assertEquals(definitions.size(), 3);
        
	}
}
