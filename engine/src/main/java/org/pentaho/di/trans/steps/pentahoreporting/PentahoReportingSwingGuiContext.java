/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.pentahoreporting;

import java.awt.Window;
import java.util.Locale;

import org.pentaho.reporting.engine.classic.core.modules.gui.common.IconTheme;
import org.pentaho.reporting.engine.classic.core.modules.gui.common.StatusListener;
import org.pentaho.reporting.engine.classic.core.modules.gui.common.StatusType;
import org.pentaho.reporting.engine.classic.core.modules.gui.commonswing.ReportEventSource;
import org.pentaho.reporting.engine.classic.core.modules.gui.commonswing.SwingGuiContext;
import org.pentaho.reporting.libraries.base.config.Configuration;

public class PentahoReportingSwingGuiContext implements StatusListener, SwingGuiContext {

  private StatusType statusType;
  private String message;
  private Throwable cause;

  @Override
  public Configuration getConfiguration() {
    return null;
  }

  @Override
  public IconTheme getIconTheme() {
    return null;
  }

  @Override
  public Locale getLocale() {
    return Locale.getDefault();
  }

  @Override
  public ReportEventSource getEventSource() {
    return null;
  }

  @Override
  public StatusListener getStatusListener() {
    return this;
  }

  @Override
  public Window getWindow() {
    return null;
  }

  @Override
  public void setStatus( StatusType statusType, String message, Throwable cause ) {
    this.statusType = statusType;
    this.message = message;
    this.cause = cause;
  }

  public StatusType getStatusType() {
    return statusType;
  }

  public String getMessage() {
    return message;
  }

  public Throwable getCause() {
    return cause;
  }
}
