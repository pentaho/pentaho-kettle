package be.ibridge.kettle.trans.step.webservices.wsdl;

import java.util.ArrayList;
import java.util.List;

public class WSDLOperation 
{
	private List/*<WSDLArgument>*/ arguments = new ArrayList/*<WSDLArgument>*/();
	
	private List/*<WSDLArgument>*/ returns = new ArrayList/*<WSDLArgument>*/();
	
	private String name;
	
	private String targetNamespace;
	
	public WSDLOperation(String aName)
	{
		name = aName;
	}

	public List/*<WSDLArgument>*/ getArguments() {
		return arguments;
	}

	public void setArguments(List/*<WSDLArgument>*/ arguments) {
		this.arguments = arguments;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	public String getName() {
		return name;
	}

	public List/*<WSDLArgument>*/ getReturns() {
		return returns;
	}

	public void setReturns(List/*<WSDLArgument>*/ returns) {
		this.returns = returns;
	}

		
	
}
