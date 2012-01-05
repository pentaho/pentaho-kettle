/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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
