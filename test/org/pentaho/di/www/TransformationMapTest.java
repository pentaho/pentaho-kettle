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

package org.pentaho.di.www;

import junit.framework.TestCase;

public class TransformationMapTest extends TestCase {
	
	public void testServerSocketPort() throws Exception {
		
		TransformationMap map = new TransformationMap();
		SocketPortAllocation port;

		// A : Give back start of range
		//
		port = map.allocateServerSocketPort(40000, "host1", "id1", "trans1", "slave-1", "source1", "0", "slave-2", "target1", "0");
		assertEquals(40000, port.getPort());
		
		// B : Different host: give back start of range...
		//
		port = map.allocateServerSocketPort(40000, "host2", "id2", "trans1", "slave-2", "source1", "0", "slave-3", "target1", "0");
		assertEquals(40000, port.getPort());

		// C : Same host, different slave, new port
		//
		port = map.allocateServerSocketPort(40000, "host1", "id1", "trans1", "slave-2", "source1", "0", "slave-3", "target1", "0");
		assertEquals(40001, port.getPort());

		// D : Same host, same slave, different transformation
		//
		port = map.allocateServerSocketPort(40000, "host1", "id2", "trans2", "slave-2", "source1", "0", "slave-3", "target1", "0");
		assertEquals(40002, port.getPort());

		// E : Same host, same slave, same transformation, different step copy
		//
		port = map.allocateServerSocketPort(40000, "host1", "id2", "trans2", "slave-2", "source1", "1", "slave-3", "target1", "1");
		assertEquals(40003, port.getPort());

		// Test memory, look up A-E again
		//
		assertEquals(40000, map.allocateServerSocketPort(40000, "host1", "id1", "trans1", "slave-1", "source1", "0", "slave-2", "target1", "0").getPort());
		assertEquals(40000, map.allocateServerSocketPort(40000, "host2", "id1", "trans1", "slave-2", "source1", "0", "slave-3", "target1", "0").getPort());
		assertEquals(40001, map.allocateServerSocketPort(40000, "host1", "id1", "trans1", "slave-2", "source1", "0", "slave-3", "target1", "0").getPort());
		assertEquals(40002, map.allocateServerSocketPort(40000, "host1", "id2", "trans2", "slave-2", "source1", "0", "slave-3", "target1", "0").getPort());
		assertEquals(40003, map.allocateServerSocketPort(40000, "host1", "id2", "trans2", "slave-2", "source1", "1", "slave-3", "target1", "1").getPort());

		// Ignore the carte object ID!
		//
    assertEquals(40000, map.allocateServerSocketPort(40000, "host1", "id2", "trans1", "slave-1", "source1", "0", "slave-2", "target1", "0").getPort());

		// Remove 40002 from the map...
		//
		map.deallocateServerSocketPort(40002, "host1");
		
		// See if we get the same port back by filling up the gap in the rage again...
		//
		assertEquals(40002, map.allocateServerSocketPort(40000, "host1", "id2", "trans2", "slave-2", "source1", "0", "slave-3", "target1", "0").getPort());
	}
}
