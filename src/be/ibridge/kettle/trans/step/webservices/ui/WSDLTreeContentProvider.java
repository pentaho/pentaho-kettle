package be.ibridge.kettle.trans.step.webservices.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import be.ibridge.kettle.trans.step.webservices.wsdl.WSDLOperation;
import be.ibridge.kettle.trans.step.webservices.wsdl.WSDLService;

public class WSDLTreeContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object arg0) {
		Object[] vRet  = new Object[]{};
		if(arg0 instanceof WSDLService)
		{
			//Ajout des opérations
			List/*<WSDLOperation>*/ vListOperations = new ArrayList/*<WSDLOperation>*/(((WSDLService)arg0).getOperations());
			Collections.sort(vListOperations, new Comparator/*<WSDLOperation>*/(){
				public int compare(Object o1, Object o2) {
					return ((WSDLOperation)o1).getName().compareTo(((WSDLOperation)o2).getName());
				}
			});
			vRet = vListOperations.toArray();
		}
		return vRet;
	}

	public Object getParent(Object arg0) {
		Object vRet = null;
		return vRet;
	}

	public boolean hasChildren(Object arg0) {
		return arg0 instanceof WSDLService;
	}

	public Object[] getElements(Object arg0) {
		Object[] vRet = null;
		if(arg0 instanceof Collection)
		{
			//On tri les services par ordre alphabétique
			List/*<WSDLService>*/ vList = new ArrayList/*<WSDLService>*/((Collection)arg0);
			Collections.sort(vList, new Comparator/*<WSDLService>*/(){
				public int compare(Object o1, Object o2) {
					return ((WSDLService)o1).getName().compareTo(((WSDLService)o2).getName());
				}
			});
			vRet = vList.toArray();
		}
		else 
		{
			getChildren(arg0);
		}
		return vRet;
	}

	public void dispose() {}

	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}
	

}
