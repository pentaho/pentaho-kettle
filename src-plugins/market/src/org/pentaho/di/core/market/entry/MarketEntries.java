package org.pentaho.di.core.market.entry;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.market.Market;
import org.pentaho.di.core.market.place.Marketplace;
import org.pentaho.di.core.market.place.Marketplaces;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class MarketEntries extends ArrayList<MarketEntry> {
   
   private static Class<?> MARKET_PKG = Market.class;
	
   private static final long serialVersionUID = 8998824890650364532L;

   public MarketEntries() {
	  Marketplaces marketplaces = new Marketplaces(); 
	  for (Marketplace marketplace : marketplaces) {
		  try {
		     // Read the content from the given URL...
			 //
			 Document doc = XMLHandler.loadXMLFile( KettleVFS.getInputStream(marketplace.getEntriesUrl()) );     
			 Node marketNode = XMLHandler.getSubNode(doc, "market");
			 List<Node> entryNodes = XMLHandler.getNodes(marketNode, MarketEntry.XML_TAG);
			 for (Node entryNode : entryNodes ) {
			    add(new MarketEntry(entryNode));
			 }
		   }
		   catch(Exception e) {
		      new ErrorDialog(Spoon.getInstance().getShell(), 
		    		  BaseMessages.getString(MARKET_PKG, "Market.error"), 
		    		  BaseMessages.getString(MARKET_PKG, "MarketEntry.read.error", marketplace.getName(), marketplace.getEntriesUrl()), e);
	       }
	   }
   }
}
