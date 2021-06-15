/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.dnd;

import org.eclipse.swt.internal.SerializableCompatibility;

/**
 * <code>Transfer</code> provides a mechanism for converting between a java
 * representation of data and a platform specific representation of data and
 * vice versa. It is used in data transfer operations such as drag and drop and
 * clipboard copy/paste.
 * <p>
 * You should only need to become familiar with this class if you are
 * implementing a Transfer subclass and you are unable to subclass the
 * ByteArrayTransfer class.
 * </p>
 * 
 * @see ByteArrayTransfer
 * @since 1.3
 */
public abstract class Transfer implements SerializableCompatibility {

  /**
   * Returns a list of the platform specific data types that can be converted
   * using this transfer agent.
   * <p>
   * Only the data type fields of the <code>TransferData</code> objects are
   * filled in.
   * </p>
   * 
   * @return a list of the data types that can be converted using this transfer
   *         agent
   */
  abstract public TransferData[] getSupportedTypes();

  /**
   * Returns true if the <code>TransferData</code> data type can be converted
   * using this transfer agent, or false otherwise (including if transferData is
   * <code>null</code>).
   * 
   * @param transferData a platform specific description of a data type; only
   *          the data type fields of the <code>TransferData</code> object need
   *          to be filled in
   * @return true if the transferData data type can be converted using this
   *         transfer agent
   */
  abstract public boolean isSupportedType( TransferData transferData );

  /**
   * Returns the platform specific ids of the data types that can be converted
   * using this transfer agent.
   * 
   * @return the platform specific ids of the data types that can be converted
   *         using this transfer agent
   */
  abstract protected int[] getTypeIds();

  /**
   * Returns the platform specific names of the data types that can be converted
   * using this transfer agent.
   * 
   * @return the platform specific names of the data types that can be converted
   *         using this transfer agent.
   */
  abstract protected String[] getTypeNames();

  /**
   * Converts a java representation of data to a platform specific
   * representation of the data.
   * <p>
   * On a successful conversion, the transferData.result field will be set to
   * 1.
   * If this transfer agent is unable to perform the conversion, the
   * transferData.result field will be set to a failure value of 0.
   * </p>
   * 
   * <p><strong>IMPORTANT:</strong> This method is <em>not</em> part of the RWT
   * public API. It is marked public only so that it can be shared
   * within the packages provided by RWT. It should never be accessed
   * from application code.
   * </p>
   * 
   * @param object a java representation of the data to be converted; the type
   *          of Object that is passed in is dependent on the
   *          <code>Transfer</code> subclass.
   * @param transferData an empty TransferData object; this object will be filled
   *          in on return with the platform specific representation of the data
   * @exception org.eclipse.swt.SWTException <ul>
   *              <li>ERROR_INVALID_DATA - if object does not contain data in a
   *              valid format or is <code>null</code></li>
   *              </ul>
   */
  // [rh] this method is declared as 'protected' in SWT. In RWT, it is declared
  //      public to be accessible from LCA code. Should be fixed in the future.
  abstract public void javaToNative( Object object, TransferData transferData );

  /**
   * Converts a platform specific representation of data to a java
   * representation.
   * 
   * <p><strong>IMPORTANT:</strong> This method is <em>not</em> part of the RWT
   * public API. It is marked public only so that it can be shared
   * within the packages provided by RWT. It should never be accessed
   * from application code.
   * </p>
   * 
   * @param transferData the platform specific representation of the data to be
   *          converted
   * @return a java representation of the converted data if the conversion was
   *         successful; otherwise null. If transferData is <code>null</code>
   *         then <code>null</code> is returned. The type of Object that is
   *         returned is dependent on the <code>Transfer</code> subclass.
   */
  // [rh] this method is declared as 'protected' in SWT. In RWT, it is declared
  //      public to be accessible from LCA code. Should be fixed in the future.
  abstract public Object nativeToJava( TransferData transferData );

  /**
   * Registers a name for a data type and returns the associated unique
   * identifier.
   * <p>
   * You may register the same type more than once, the same unique identifier
   * will be returned if the type has been previously registered.
   * </p>
   * 
   * @param formatName the name of a data type
   * @return the unique identifier associated with this data type
   */
  public static int registerType( String formatName ) {
    return formatName.hashCode();
  }

  /**
   * Test that the object is of the correct format for this Transfer class.
   * 
   * @param object a java representation of the data to be converted
   * @return true if object is of the correct form for this transfer type
   */
  protected boolean validate( Object object ) {
    return true;
  }
}
