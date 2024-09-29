/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.ui.core.database.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.ui.core.PropsUI;

/**
 * Created by bmorrise on 3/9/16.
 */
public interface WizardPageFactory {
  WizardPage createWizardPage( PropsUI props, DatabaseMeta info );
}
