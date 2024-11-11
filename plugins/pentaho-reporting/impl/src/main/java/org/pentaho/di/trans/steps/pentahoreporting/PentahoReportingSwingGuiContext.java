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
