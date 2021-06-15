/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme.css;

import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionFactory;
import org.w3c.css.sac.ContentCondition;
import org.w3c.css.sac.LangCondition;
import org.w3c.css.sac.NegativeCondition;
import org.w3c.css.sac.PositionalCondition;


/**
 * ConditionFactory implementation for parsing RAP theme files. All returned
 * conditions implement the interface {@link ConditionExt}.
 */
public class ConditionFactoryImpl implements ConditionFactory {

  private final CssFileReader reader;

  public ConditionFactoryImpl( CssFileReader reader ) {
    this.reader = reader;
  }

  @Override
  public AttributeCondition createClassCondition( String namespaceURI, String value )
    throws CSSException
  {
    return new ClassConditionImpl( value );
  }

  @Override
  public AttributeCondition createPseudoClassCondition( String namespaceURI, String value )
    throws CSSException
  {
    return new PseudoClassConditionImpl( value );
  }

  @Override
  public AttributeCondition createAttributeCondition( String localName,
                                                      String namespaceURI,
                                                      boolean specified,
                                                      String value ) throws CSSException
  {
    return new AttributeConditionImpl( localName, value, specified );
  }

  @Override
  public AttributeCondition createOneOfAttributeCondition( String localName,
                                                           String namespaceURI,
                                                           boolean specified,
                                                           String value ) throws CSSException
  {
    return new OneOfAttributeCondition( localName, value, specified );
  }

  @Override
  public CombinatorCondition createAndCondition( Condition first, Condition second )
    throws CSSException
  {
    return new AndConditionImpl( first, second );
  }

  // ==========================================================================
  // Not supported by RAP

  @Override
  public LangCondition createLangCondition( String lang ) throws CSSException {
    String mesg = "Lang conditions not supported by RAP - ignored";
    reader.addProblem( new CSSException( mesg ) );
    return new NullLangCondition();
  }

  @Override
  public AttributeCondition createIdCondition( String value ) throws CSSException {
    String mesg = "Id conditions not supported by RAP - ignored";
    reader.addProblem( new CSSException( mesg ) );
    return new NullAttributeCondition();
  }

  @Override
  public AttributeCondition createBeginHyphenAttributeCondition( String localName,
                                                                 String namespaceURI,
                                                                 boolean specified,
                                                                 String value ) throws CSSException
  {
    String mesg = "Begin hyphen attribute conditions not supported by RAP - ignored";
    reader.addProblem( new CSSException( mesg ) );
    return new NullAttributeCondition();
  }

  // ==========================================================================
  // Not supported by CSS 2

  @Override
  public CombinatorCondition createOrCondition( Condition first, Condition second )
    throws CSSException
  {
    throw new CSSException( "Or conditions not supported by CSS2" );
  }

  @Override
  public NegativeCondition createNegativeCondition( Condition condition ) throws CSSException {
    throw new CSSException( "Negative conditions not supported by CSS2" );
  }

  @Override
  public PositionalCondition createPositionalCondition( int position, boolean typeNode, boolean type )
    throws CSSException
  {
    throw new CSSException( "Positional conditions not supported by CSS2" );
  }

  @Override
  public Condition createOnlyChildCondition() throws CSSException {
    throw new CSSException( "Only-one-child conditions not supported by CSS2" );
  }

  @Override
  public Condition createOnlyTypeCondition() throws CSSException {
    throw new CSSException( "Only-one-type conditions not supported by CSS2" );
  }

  @Override
  public ContentCondition createContentCondition( String data ) throws CSSException {
    throw new CSSException( "Content conditions not supported by CSS2" );
  }

}
