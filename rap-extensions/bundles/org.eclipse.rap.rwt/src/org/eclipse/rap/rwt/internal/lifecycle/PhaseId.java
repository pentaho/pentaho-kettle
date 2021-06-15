/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.lifecycle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * A type-safe enumeration that represents all standard life cycle phases. The
 * instances can be used to refer to a phase in implementations of
 * {@link PhaseListener}.
 *
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 * @deprecated Support for PhaseListeners is going to be removed in the future.
 */
@Deprecated
public class PhaseId implements Comparable<PhaseId> {

  private static int nextOrdinal;

  /**
   * The PhaseId <code>ANY</code> is used by the {@link PhaseListener} to
   * signal interest in all phases.
   */
  public static final PhaseId ANY = new PhaseId( "ANY" );

  /**
   * The PhaseId <code>PREPARE_UI_ROOT</code> is used by the
   * {@link PhaseListener} to signal interest in the <em>Prepare UI Root</em>
   * phase.
   */
  public static final PhaseId PREPARE_UI_ROOT = new PhaseId( "PREPARE_UI_ROOT" );

  /**
   * The PhaseId <code>READ_DATA</code> is used by the {@link PhaseListener}
   * to signal interest in the <em>Read Data</em> phase.
   */
  public static final PhaseId READ_DATA = new PhaseId( "READ_DATA" );

  /**
   * The PhaseId <code>PROCESS_ACTION</code> is used by the
   * {@link PhaseListener} to signal interest in the <em>Process Action</em>
   * phase.
   */
  public static final PhaseId PROCESS_ACTION = new PhaseId( "PROCESS_ACTION" );

  /**
   * The PhaseId <code>RENDER</code> is used by the {@link PhaseListener} to
   * signal interest in the <em>Render</em> phase.
   */
  public static final PhaseId RENDER = new PhaseId( "RENDER" );

  private final static PhaseId[] values = {
    ANY,
    PREPARE_UI_ROOT,
    READ_DATA,
    PROCESS_ACTION,
    RENDER
  };

  /**
   * A list containing the instances of this enumeration.
   */
  public static final List<PhaseId> VALUES = Collections.unmodifiableList( Arrays.asList( values ) );

  private final String name;
  private final int ordinal;

  private PhaseId( String name ) {
    this.name = name;
    ordinal = nextOrdinal++;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int compareTo( PhaseId toCompare ) {
    return ordinal - toCompare.ordinal;
  }

  /**
   * Returns the ordinal number that is used for comparison of PhaseIds.
   *
   * @return the ordinal number
   */
  public int getOrdinal() {
    return ordinal;
  }

}
