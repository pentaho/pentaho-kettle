/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.di.job.entries.ssh2put;

import com.trilead.ssh2.KnownHosts;
import com.trilead.ssh2.ServerHostKeyVerifier;

/**
 * This example hostkey verifier is used by the
 * UsingKnownHosts.java example.
 *  
 * @author Christian Plattner, plattner@trilead.com
 * @version $Id: SimpleVerifier.java,v 1.4 2007/10/15 12:49:57 cplattne Exp $
 */
class SimpleVerifier implements ServerHostKeyVerifier
{
	KnownHosts database;

	public SimpleVerifier(KnownHosts database)
	{
		if (database == null)
			throw new IllegalArgumentException();

		this.database = database;
	}

	public boolean verifyServerHostKey(String hostname, int port, String serverHostKeyAlgorithm, byte[] serverHostKey)
			throws Exception
	{
		int result = database.verifyHostkey(hostname, serverHostKeyAlgorithm, serverHostKey);

		switch (result)
		{
		case KnownHosts.HOSTKEY_IS_OK:

			return true; // We are happy

		case KnownHosts.HOSTKEY_IS_NEW:

			// Unknown host? Blindly accept the key and put it into the cache.
			// Well, you definitely can do better (e.g., ask the user).

			// The following call will ONLY put the key into the memory cache!
			// To save it in a known hosts file, also call "KnownHosts.addHostkeyToFile(...)"
			database.addHostkey(new String[] { hostname }, serverHostKeyAlgorithm, serverHostKey);

			return true;

		case KnownHosts.HOSTKEY_HAS_CHANGED:

			// The connection if the hostkey has changed.
			// put the key into the memory cache!
			database.addHostkey(new String[] { hostname }, serverHostKeyAlgorithm, serverHostKey);
			
			return true;

		default:
			throw new IllegalStateException();
		}
	}
}