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


package org.pentaho.di.ui.spoon.tree.provider;

import org.eclipse.swt.graphics.Image;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.Optional;


/**
 * Created by bmorrise on 7/2/18.
 */
public class JobEntriesFolderProvider extends AutomaticTreeFolderProvider {

  private static Class<?> PKG = Spoon.class;
  public static final String STRING_JOB_ENTRIES = BaseMessages.getString( PKG, "Spoon.STRING_JOB_ENTRIES" );

  @Override
  public String getTitle() {
    return STRING_JOB_ENTRIES;
  }

  @Override
  public void refresh( Optional<AbstractMeta> meta, TreeNode treeNode, String filter ) {
    if ( !( meta.isPresent() && meta.get() instanceof JobMeta ) ) {
      return;
    }
    JobMeta jobMeta = (JobMeta) meta.get();
    for ( int i = 0; i < jobMeta.nrJobEntries(); i++ ) {
      JobEntryCopy jobEntry = jobMeta.getJobEntry( i );

      if ( !filterMatch( jobEntry.getName(), filter ) && !filterMatch( jobEntry.getDescription(), filter ) ) {
        continue;
      }

      Image icon;
      if ( jobEntry.isStart() ) {
        icon = GUIResource.getInstance().getImageStartMedium();
      } else if ( jobEntry.isDummy() ) {
        icon = GUIResource.getInstance().getImageDummyMedium();
      } else {
        String key = jobEntry.getEntry().getPluginId();
        icon = GUIResource.getInstance().getImagesJobentriesSmall().get( key );
      }

      createTreeNode( treeNode, jobEntry.getName(), icon );
    }
  }

  @Override
  public Class getType() {
    return JobEntryCopy.class;
  }
}
