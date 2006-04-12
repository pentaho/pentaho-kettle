package be.ibridge.kettle.trans;

import junit.framework.TestCase;

public class MessagesTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'be.ibridge.kettle.trans.Messages.getString(String, String, String, String, String, String)'
	 */
	public void testGetString5() {
		// TODO Auto-generated method stub
		System.out.println(be.ibridge.kettle.trans.Messages.getString("Trans.Log.ProcessErrorInfo","copy table","1","1","1","1"));
		System.out.println(be.ibridge.kettle.trans.Messages.getString("Trans.Log.ProcessErrorInfo2","copy table","1","1","1","1"));
		
	}
	
	/*
	 * Test method for 'be.ibridge.kettle.trans.Messages.getString(String, String, String, String, String)'
	 */
	public void testGetString4() {
		// TODO Auto-generated method stub
		System.out.println(be.ibridge.kettle.trans.Messages.getString("Trans.Log.ProcessSuccessfullyInfo","copy table","1","1","1"));
		
	}
	
	/*
	 * Test method for 'be.ibridge.kettle.trans.Messages.getString(String, String, String, String)'
	 */
	public void testGetString3() {
		// TODO Auto-generated method stub
		System.out.println(be.ibridge.kettle.trans.Messages.getString("Trans.Log.AllocatedRowsets","10","copy table","1"));
		
	}

}
