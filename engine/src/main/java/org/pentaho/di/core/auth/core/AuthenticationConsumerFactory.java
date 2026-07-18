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



package org.pentaho.di.core.auth.core;

public interface AuthenticationConsumerFactory<ReturnType, CreateArgType, ConsumedType> {
  public Class<ConsumedType> getConsumedType();

  public Class<ReturnType> getReturnType();

  public Class<CreateArgType> getCreateArgType();

  public AuthenticationConsumer<ReturnType, ConsumedType> create( CreateArgType createArg );

}
