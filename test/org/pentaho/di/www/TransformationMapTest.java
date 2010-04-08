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
