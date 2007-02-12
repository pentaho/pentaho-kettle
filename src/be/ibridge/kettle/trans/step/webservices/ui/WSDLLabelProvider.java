package be.ibridge.kettle.trans.step.webservices.ui;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import be.ibridge.kettle.trans.step.webservices.wsdl.WSDLOperation;
import be.ibridge.kettle.trans.step.webservices.wsdl.WSDLService;

public class WSDLLabelProvider implements ILabelProvider {

	public Image getImage(Object arg0) {
		return null;
	}

	public String getText(Object arg0) {
		String vRet = "";
		if(arg0 instanceof WSDLOperation)
		{
			vRet = ((WSDLOperation)arg0).getName();
		}
		else if(arg0 instanceof WSDLService)
		{
			vRet = ((WSDLService)arg0).getName();
		}
		return vRet;
	}

	public void addListener(ILabelProviderListener arg0) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	public void removeListener(ILabelProviderListener arg0) {}
}
