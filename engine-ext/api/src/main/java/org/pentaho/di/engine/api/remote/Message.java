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



package org.pentaho.di.engine.api.remote;

import java.io.Serializable;

/**
 * Message Interface
 *
 * Generic interface used for all websocket messages used in AEL.  The JSR356 spec does not allow multiple "onMessage"
 * calls of different types for the same endpoint; thus we use a generic interface.
 *
 * Created by ccaspanello on 8/2/17.
 */
public interface Message extends Serializable {
}
