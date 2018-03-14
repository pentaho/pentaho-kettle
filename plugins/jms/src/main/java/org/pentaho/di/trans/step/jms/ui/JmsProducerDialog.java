/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.step.jms.ui;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class JmsProducerDialog extends BaseStepDialog implements StepDialogInterface {


  private final ModifyListener lsMod;
  private final Shell parent;

  public JmsProducerDialog( Shell parent, BaseStepMeta meta,
                            TransMeta transMeta, String stepname ) {
    super( parent, meta, transMeta, stepname );
    lsMod = e -> meta.setChanged();
    this.parent = parent;
  }

  @Override public String open() {

    new ConnectionForm( parent, props, transMeta, lsMod ).layoutForm();
    return "";
  }
}
