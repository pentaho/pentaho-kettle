package org.pentaho.di.core.market.place;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.market.Market;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
/**
 * This class is an extension of ArrayList<Marketplace.
 * It will populate itself on construction.
 * 
 * @author sflatley
 * @extends ArrayList<Marketplace>
 */
public class Marketplaces extends ArrayList<Marketplace> {

   private static Class<?> MARKET_PKG = Market.class;
	
   private static final long serialVersionUID = 5561589064643143850L;

	/**
	 * Creates and populates this object.
	 */
   public Marketplaces() {
	   String marketplacesFile = Market.getMarketplacesFile();
	   try {
	      File file = new File(marketplacesFile);
		  if (file.exists()) {
			  Document doc = XMLHandler.loadXMLFile(marketplacesFile);
			  Node placesNode = XMLHandler.getSubNode(doc, "marketplaces");
			  List<Node> nodes = XMLHandler.getNodes(placesNode, Marketplace.XML_TAG);
			  for (Node node : nodes ) {
			     add(new Marketplace(node));
			  }
	       }
		   else {
		      addAll(Marketplace.getDefaultMarketplaces());
		   }
		} 
		catch(KettleXMLException kxe) {
		   new ErrorDialog(Spoon.getInstance().getShell(), 
				   BaseMessages.getString(MARKET_PKG, "Market.error"), 
				   BaseMessages.getString(MARKET_PKG, "Marketplaces.xmlerror.message", marketplacesFile), kxe);
		}
   } 
}

