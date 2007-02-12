package be.ibridge.kettle.trans.step.webservices.wsdl;

import java.util.ArrayList;
import java.util.List;

public class WSDLArgument 
{
	private String name;
	
	private String targetNamespace;
	
	private List/*<WSDLParameter>*/ parameters = new ArrayList/*<WSDLParameter>*/();
	
	private boolean multiple = true;
	
	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public WSDLArgument(String aName)
	{
		name = aName;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	public List/*<WSDLParameter>*/ getParameters() 
	{
		return parameters;
	}

	public String getName() {
		return name;
	}

	public void setName(String string) 
	{
		name = string;
		
	}
}
