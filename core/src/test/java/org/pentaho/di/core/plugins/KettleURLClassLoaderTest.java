/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
package org.pentaho.di.core.plugins;

import org.junit.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class KettleURLClassLoaderTest {

    @Test
    public void classExistsReturnsTrueForExistingClass() {
        KettleURLClassLoader classLoader = new KettleURLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
        boolean exists = classLoader.getResource("java/lang/String.class") != null;
        assertTrue(exists);
    }

    @Test
    public void classExistsReturnsFalseForNonExistingClass() {
        KettleURLClassLoader classLoader = new KettleURLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
        boolean exists = classLoader.getResource("com/example/NonExistentClass.class") != null;
        assertFalse(exists);
    }

    @Test
    public void resourcePathIsCorrectlyResolvedForClassName() {
        String className = "org.pentaho.di.core.plugins.KettleURLClassLoader";
        String resourcePath = className.replace('.', '/') + ".class";
        assertEquals("org/pentaho/di/core/plugins/KettleURLClassLoader.class", resourcePath);
    }

    @Test
    public void getResourceReturnsNullForInvalidPath() {
        KettleURLClassLoader classLoader = new KettleURLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
        assertNull(classLoader.getResource("invalid/path/ToClass.class"));
    }
}