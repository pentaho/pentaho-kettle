package org.pentaho.di.ui.spoon.dialog;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class MarketplaceController extends AbstractXulEventHandler  {
	
   public void openMarketPlace() throws KettleException {
	  MarketplaceDialog marketplaceDialog = new MarketplaceDialog(Spoon.getInstance().getShell());
   	  marketplaceDialog.open();
   }
   
   public String getName() {
	   return "MarketplaceController";
   }
}
