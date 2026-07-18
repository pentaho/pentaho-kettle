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



package org.pentaho.di.core;

/**
 * Defines that the class implementing this interface contains a description
 *
 * @author Matt
 * @since 21-aug-2006
 */
public interface DescriptionInterface {
  public void setDescription( String description );

  public String getDescription();
}
