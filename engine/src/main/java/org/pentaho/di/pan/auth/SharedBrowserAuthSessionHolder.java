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

package org.pentaho.di.pan.auth;

import com.pentaho.oauth.client.BrowserAuthSessionHolder;
import org.pentaho.di.pan.auth.store.TokenStoreFactory;

/**
 * Kettle-owned access point for the shared browser-auth session holder used by
 * Pan and Kitchen command flows.
 */
public final class SharedBrowserAuthSessionHolder {

    private static final BrowserAuthSessionHolder SESSION_HOLDER = new BrowserAuthSessionHolder(
            TokenStoreFactory.create());

    private SharedBrowserAuthSessionHolder() {
    }

    public static BrowserAuthSessionHolder get() {
        return SESSION_HOLDER;
    }
}
