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

package org.pentaho.di.job.entries.hadoopjobexecutor;

import java.util.Deque;
import java.util.LinkedList;

/**
 * <p>
 * A thread-safe utility to help manage the replacing of a {@link SecurityManager}. To
 * replace a Security Manager use {@link #setSecurityManager(SecurityManager)}.
 * To remove a Security Manager use {@link #removeSecurityManager(SecurityManager)}.
 * </p>
 * <p>
 * General use of this class should be through a singleton so the same stack
 * is shared across all uses.
 * </p>
 *
 */
public class SecurityManagerStack {
  private Deque<StackElement> stack;

  /**
   * Encapsulates the state for setting of a Security Manager so the original
   * may be restored at a later time.
   */
  private class StackElement {
    SecurityManager sm;
    SecurityManager original;
    boolean released;

    StackElement(SecurityManager sm, SecurityManager original) {
      this.sm = sm;
      this.original = original;
      released = false;
    }
  }

  public SecurityManagerStack() {
    stack = new LinkedList<StackElement>();
  }

  /**
   * Sets a new Security Manager. This will stash the current Security Manager 
   * so it may be restored when {@link #removeSecurityManager(SecurityManager)} 
   * with {@code sm} is called.
   *  
   * @param sm Security Manager to set
   * @throws IllegalArgumentException if {@code sm} is null.
   */
  public void setSecurityManager(SecurityManager sm) {
    if (sm == null) {
      throw new IllegalArgumentException("SecurityManager is required");
    }
    synchronized (stack) {
      stack.push(new StackElement(sm, getCurrentSecurityManager()));
      System.setSecurityManager(sm);
    }
  }

  /**
   * Remove a Security Manager and restore the Security Manager it replaced. This 
   * handles the case where another Security Manager was set in place of this 
   * one to insure the previous security manager is not replaced prematurely.
   * 
   * @param sm Security Manager to remove
   */
  public void removeSecurityManager(SecurityManager sm) {
    if (sm == null) {
      throw new IllegalArgumentException("SecurityManager is required");
    }
    synchronized (stack) {
      if (stack.peek() == null) {
        throw new IllegalStateException("empty stack");
      }
      if (sm.equals(stack.peek().sm)) {
        // If sm was the last SecurityManager to be registered restore an older
        // one by finding the last contiguous element which has been released 
        // and restore the Security Manager that one replaced
        SecurityManager smToRestore = stack.pop().original;
        while (stack.peek() != null && stack.peek().released == true) {
          smToRestore = stack.pop().original;
        }
        restore(smToRestore);
      } else {
        // If another Security Manager has been registered since we were called
        // mark ourself as being released so when the more recent ones are removed
        // our original is restored properly
        for (StackElement e : stack) {
          if (e.sm.equals(sm)) {
            e.released = true;
          }
        }
      }
    }
  }

  /**
   * @return the current {@link SecurityManager} registered with the System.
   * @see System#getSecurityManager()
   */
  private SecurityManager getCurrentSecurityManager() {
    return System.getSecurityManager();
  }

  /**
   * Restores a security manager
   * 
   * @param sm Security manager to restore
   */
  private void restore(SecurityManager sm) {
    System.setSecurityManager(sm);
  }
}
