/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.pan;

public interface CommandExecutorResult {


//  public void setResult( CommandExecutorResult.Result result );
//
//  public CommandExecutorResult.Result getResult();

  public int getCode();

  public String getDescription ();
}
