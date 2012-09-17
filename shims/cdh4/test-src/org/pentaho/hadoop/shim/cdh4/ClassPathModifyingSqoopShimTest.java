/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.hadoop.shim.cdh4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class ClassPathModifyingSqoopShimTest {
  private static final String TEST_CLASS_PATH = "testing";

  @Test
  public void runWithModifiedClassPathProperty_not_null() {
    ClassPathModifyingSqoopShim shim = new ClassPathModifyingSqoopShim() {
      protected String getClassPathString() {
        return TEST_CLASS_PATH;
      };
    };
    final AtomicBoolean isSet = new AtomicBoolean();
    int returnVal = shim.runWithModifiedClassPathProperty(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        if (TEST_CLASS_PATH.equals(System.getProperty("java.class.path"))) {
          isSet.set(true);
        }
        return null;
      }
    });
    assertTrue("Class path not modified when it should have been", isSet.get());
    assertEquals("Invalid return code when callable returns null", Integer.MIN_VALUE, returnVal);
  }

  @Test
  public void runWithModifiedClassPathProperty_null() {
    ClassPathModifyingSqoopShim shim = new ClassPathModifyingSqoopShim() {
      protected String getClassPathString() {
        return null;
      };
    };
    final AtomicBoolean isSet = new AtomicBoolean();
    int returnVal = shim.runWithModifiedClassPathProperty(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        if (TEST_CLASS_PATH.equals(System.getProperty("java.class.path"))) {
          isSet.set(true);
        }
        return null;
      }
    });
    assertFalse("Class path modified when it should not have been", isSet.get());
    assertEquals("Invalid return code when callable returns null", Integer.MIN_VALUE, returnVal);
  }

}
