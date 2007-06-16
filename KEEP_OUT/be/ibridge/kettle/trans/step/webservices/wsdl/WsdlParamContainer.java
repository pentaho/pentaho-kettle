package be.ibridge.kettle.trans.step.webservices.wsdl;

public interface WsdlParamContainer
{
	String getContainerName();
	
	String[] getParamNames();
	
	String getParamType(String paramName);
	
	String getItemName();
    
    boolean isArray();
}
