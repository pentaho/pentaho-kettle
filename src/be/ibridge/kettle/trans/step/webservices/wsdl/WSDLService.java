package be.ibridge.kettle.trans.step.webservices.wsdl;

import java.util.HashSet;
import java.util.Set;

public class WSDLService 
{
	private Set/*<WSDLOperation>*/ operations = new HashSet/*<WSDLOperation>*/();
	
	private String name;
	
	public WSDLService(String aName)
	{
		name = aName;
	}

	public String getName() 
	{
		return name;
	}

	public Set/*<WSDLOperation>*/ getOperations() {
		return operations;
	}

	public void setOperations(Set/*<WSDLOperation>*/ operations) {
		this.operations = operations;
	}
	
	
}
