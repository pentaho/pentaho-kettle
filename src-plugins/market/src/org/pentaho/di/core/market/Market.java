package org.pentaho.di.core.market;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.market.entry.MarketEntry;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.pkg.JarfileGenerator;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.di.ui.spoon.dialog.MarketplaceController;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

@SpoonPlugin(id = "Market", image = "")
@SpoonPluginCategories({"spoon"})
public class Market implements SpoonPluginInterface {

	private static Class<?> PKG = Market.class;
	
    /**
     * Determines the location of the marketplaces file
     * @return the name of the marketplaces file
     */
    public static final String getMarketplacesFile() {
        return Const.getKettleDirectory() + Const.FILE_SEPARATOR + "marketplaces.xml";
    }
	
    /**
     * Applies the XUL overlay specified in the loadOverlay call.
     */
	@Override
	public void applyToContainer(String category, XulDomContainer container)
			throws XulException {
	
		if(category.equals("spoon")){
			container.registerClassLoader(getClass().getClassLoader());
			container.loadOverlay("org/pentaho/di/core/market/spoon_overlays.xul");
			container.addEventHandler(new MarketplaceController());
		}
	}

	@Override
	public SpoonLifecycleListener getLifecycleListener() {
		return null;
	}

	@Override
	public SpoonPerspective getPerspective() {
		return null;
	}
	
	/**
	 * @param pluginName
	 * @return boolean true if plugin is installed, false if not.
	 */
	public static boolean isInstalled(String pluginName) {
		if (  PluginRegistry.getInstance().findPluginWithName(StepPluginType.class, pluginName) == null) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Builds and returns the path to the plugins folder.
	 * 
	 * @param marketEntry
	 * @return String the path to the plugins folder.
	 */
    private static String buildPluginsFolderPath(final MarketEntry marketEntry) {
		  String subfolder = getInstallationSubfolder(marketEntry);
	      
		  // ~/.kettle/plugins/steps/
	      // ~/.kettle/plugins/
	      //
	      return Const.getKettleDirectory() + Const.FILE_SEPARATOR + "plugins" + ( subfolder==null ? "" : Const.FILE_SEPARATOR + subfolder );
	}
	  
    /**
     * Installs or uninstalls the MarketEntry.
     * 
     * @param marketEntry The market entry to install/uninstall.
     * @param isInstalled Boolean indicating if the plugin is installed.  If so then it is uninstalled.  Otherwise it is installed.
     * @throws KettleException
     */
	public static void installUninstall(final MarketEntry marketEntry, boolean isInstalled) throws KettleException {
	  
	  if (isInstalled) {
		  Market.uninstall(marketEntry);
	  }
	  else {
		  Market.install(marketEntry);
	  }
	}
	
	/**
	* Installs the passed MarketEntry into the folder built by 
	* buildPluginsFolderPath(marketEntry).
	* 
	* A warning dialog box is displayed if the plugin folder already exists.
	* If the user chooses to install then the plugin folder is deleted and
	* then recreated.
	* 
	* @param marketEntry
	* @throws KettleException
	*/
    private static void install(final MarketEntry marketEntry) throws KettleException {
	   String parentFolderName = buildPluginsFolderPath(marketEntry);
	   File pluginFolder = new File(parentFolderName  + File.separator + marketEntry.getId());
	   if (pluginFolder.exists()) {
	      MessageBox mb = new MessageBox(Spoon.getInstance().getShell(), SWT.NO | SWT.YES | SWT.ICON_WARNING);
	      mb.setMessage(BaseMessages.getString(PKG, "Marketplace.Dialog.PromptOverwritePlugin.Message", pluginFolder.getAbsolutePath()));
	      mb.setText(BaseMessages.getString(PKG, "Marketplace.Dialog.PromptOverwritePlugin.Title"));
	      int answer = SWT.NO;
	      answer = mb.open();
	      if (answer == SWT.YES) {
	   	   deleteDirectory(pluginFolder);
	   	   unzipMarketEntry(parentFolderName, marketEntry);
	      }
	   }
	   else {
	   	unzipMarketEntry(parentFolderName, marketEntry);
	   }
	}
	 
	/**
	* Unzips the plugin to the file system  The passed MarkeyEntry has the 
	* URL of the zip file.
	* 
	* @param folderName
	* @param marketEntry
	* @throws KettleException
	*/
    private static void unzipMarketEntry(String folderName, MarketEntry marketEntry) throws KettleException {
	     // Read the package, extract in folder
	     //
	     InputStream inputStream = KettleVFS.getInputStream(marketEntry.getPackageUrl());
	     ZipInputStream zis = new ZipInputStream(inputStream);
	     ZipEntry zipEntry = null;
	     try {
	   	  zipEntry = zis.getNextEntry();
	     }
	     catch (IOException ioe) {
	   	  throw new KettleException(ioe);
	     }
	     byte[] buffer = new byte[1024];
	     int bytesRead = 0;
	     FileOutputStream fos=null;
	     
	     while (zipEntry!=null) {
	       try {
	         File file = new File(folderName + File.separator + zipEntry.getName());
	         
	         if (zipEntry.isDirectory()) {
	           file.mkdirs();
	         } else {
	           file.getParentFile().mkdirs();
	           
	           fos = new FileOutputStream(file);
	           while( (bytesRead=zis.read(buffer))!=-1) {
	             fos.write(buffer, 0, bytesRead);
	           }
	         }
	         
	         zipEntry = zis.getNextEntry();
	       } 
	       catch (FileNotFoundException fnfe) {
	       	throw new KettleException(fnfe);
	       }
	       catch (IOException ioe) {
	       	throw new KettleException(ioe);
	       }
	       finally {
	         if (fos!=null) {
	           try {
	             fos.close();
	           } catch(IOException e) {
	             // Ignore.
	           }
	         }
	       }
	     }
	      
	    refreshSpoon();
	      
	}

	/**
	 * Uninstalls the passed MarketEntry.
	 * 
	 * @param marketEntry
	 * @throws KettleException
	 */
	 private static void uninstall(final MarketEntry marketEntry) throws KettleException {
	     String pluginFolderName = buildPluginsFolderPath(marketEntry) + File.separator + marketEntry.getId();
	     File folder = new File(pluginFolderName);
	     deleteDirectory(folder);
	     refreshSpoon();
      }
	  
	 /**
	  * Refreshes Spoons plugin registry, core objects and some UI things. 
	  * @throws KettleException
	  */
	 private static void refreshSpoon() throws KettleException {
	     PluginRegistry.init();
	     Spoon spoon = Spoon.getInstance(); 
	     spoon.refreshCoreObjects();
	     spoon.refreshTree();
	     spoon.refreshGraph();
	     spoon.enableMenus();
	     GUIResource.getInstance().reload();	  
	     spoon.selectionFilter.setText(spoon.selectionFilter.getText());
	 }
	 
	 /**
	  * Returns the folder name for the MarketEntries type.
	  * 
	  * @param marketEntry
	  * @return
	  */
	 public static String getInstallationSubfolder(MarketEntry marketEntry) {
	    String subfolder;
		switch(marketEntry.getType()) {
		   case Step : subfolder = "steps"; break;
		   case JobEntry : subfolder = "jobentries"; break;
		   case Partitioner: subfolder = "steps"; break;
		   case SpoonPlugin: subfolder = "spoon"; break;
		   case Database: subfolder = "databases"; break;
		   case Repository: subfolder = "repositories"; break;
		   
		   //TODO: The KFF project has a type of "MarketEntryType" which will 
		   //      default to null.  the plugin will not be installed.
		   default: subfolder = null;
		 }
	     return subfolder;
	 }

	 /**
	  * This is a copy of JarfileGenerator's method.  That method works fine if the 
	  * plugin is used in the same version it is built from.  When the plugin was dropped
	  * into PDI 4.2.1 then an invokation target exception was thrown when invoking
	  * JarfileGenerator.deleteDirectory().
	  * 
	  * I placed the method here even though the cause of the exception is not that obvious.
	  * The JarfileGenerator.deleteDirectory method has not changed since 4.2.1.
	  * 
	  * @param dir
	  */
	 private static void deleteDirectory(File dir) {
	        File[] files = dir.listFiles();
	        for (int i=0;i<files.length;i++)
	        {
	            if (files[i].isDirectory()) deleteDirectory(files[i]);
	            files[i].delete();
	        }
	        dir.delete();
	    }
}