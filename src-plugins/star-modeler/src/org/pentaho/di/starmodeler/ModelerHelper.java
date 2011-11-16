package org.pentaho.di.starmodeler;

import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.ISpoonMenuController;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.xul.swt.tab.TabItem;

public class ModelerHelper  extends AbstractXulEventHandler implements ISpoonMenuController {
  protected static Class<?> PKG = ModelerHelper.class; // for i18n

  public static final String MODELER_NAME = "Modeler"; 
  
  private static ModelerHelper instance = null;
  
  private String defaultLocale = LanguageChoice.getInstance().getDefaultLocale().getCountry();
    
  private ModelerHelper() {
  }
  
  public static ModelerHelper getInstance() {
    if( instance == null ) {
      instance = new ModelerHelper();
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      spoon.addSpoonMenuController(instance);
    }
    return instance;
  }

  
  protected String getUniqueUntitledTabName(Spoon spoon, String title) {
    int num = 1;
    String tabName = title + " " + num; //$NON-NLS-1$
    // TODO: Add new plugin object type to spoon
    TabItem tabItem = spoon.delegates.tabs.findTabMapEntry(tabName, TabMapEntry.ObjectType.BROWSER).getTabItem();
    while (tabItem != null) {
      tabName = title + " " + (++num); //$NON-NLS-1$
      // TODO: Add new plugin object type to spoon
      tabItem = spoon.delegates.tabs.findTabMapEntry(tabName, TabMapEntry.ObjectType.BROWSER).getTabItem();
    }
    return tabName;
  }

  public String getName(){
    return "starModeler"; //$NON-NLS-1$
  }
  
  public void createEmptyModel() {
    try {
      StarDomain starDomain = new StarDomain();
      
      starDomain.getDomain().setName(new LocalizedString(defaultLocale, "Star Model"));
      StarModelerPerspective.getInstance().createTabForModel(starDomain, MODELER_NAME);
      SpoonPerspectiveManager.getInstance().activatePerspective(StarModelerPerspective.class);
    } catch (Exception e) {
      new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating visualization", e);
    }
  }

  public void updateMenu(Document doc) {
    // Nothing so far.
  }
}
