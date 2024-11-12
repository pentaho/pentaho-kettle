/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.gui;

import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;

public interface SpoonInterface extends OverwritePrompter {

  public static final int STATE_CORE_OBJECTS_NONE = 1; // No core objects
  public static final int STATE_CORE_OBJECTS_CHEF = 2; // Chef state: job entries
  public static final int STATE_CORE_OBJECTS_SPOON = 3; // Spoon state: steps

  public static final String XUL_FILE_MENUBAR = "ui/menubar.xul";

  public static final String XUL_FILE_MENUS = "ui/menus.xul";

  public static final String XUL_FILE_MENU_PROPERTIES = "ui/menubar.properties";

  public boolean addSpoonBrowser( String name, String urlString );

  public void addTransGraph( TransMeta transMeta );

  public void addJobGraph( JobMeta jobMeta );

  public Object[] messageDialogWithToggle( String dialogTitle, Object image, String message, int dialogImageType,
    String[] buttonLabels, int defaultIndex, String toggleMessage, boolean toggleState );

  public boolean messageBox( String message, String text, boolean allowCancel, int type );
}
