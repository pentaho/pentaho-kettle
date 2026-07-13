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

package org.pentaho.di.trans.steps.autodoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.jfree.ui.Drawable;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.trans.steps.autodoc.KettleReportBuilder.OutputType;
import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.GroupDataBody;
import org.pentaho.reporting.engine.classic.core.ItemBand;
import org.pentaho.reporting.engine.classic.core.RelationalGroup;
import org.pentaho.reporting.engine.classic.core.Section;
import org.pentaho.reporting.engine.classic.core.function.Expression;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;
import org.pentaho.reporting.libraries.fonts.LibFontBoot;
import org.pentaho.reporting.libraries.resourceloader.LibLoaderBoot;

public class KettleReportBuilderTest {

  @Before
  public void initReportEnv() {
    if ( !ClassicEngineBoot.getInstance().isBootDone() ) {
      ObjectUtilities.setClassLoader( getClass().getClassLoader() );
      ObjectUtilities.setClassLoaderSource( ObjectUtilities.CLASS_CONTEXT );

      LibLoaderBoot.getInstance().start();
      LibFontBoot.getInstance().start();
      ClassicEngineBoot.getInstance().start();
    }
  }

  @Test
  public void createReportBindsImageFieldForNonPdfOutput() throws Exception {
    KettleReportBuilder builder = createReportBuilder( OutputType.HTML );
    builder.createReport();

    assertNotNull( builder.getReport() );
    assertNotNull( builder.getReport().getDataFactory() );
    assertNotNull( builder.getReport().getReportHeader() );
    assertNotNull( builder.getReport().getReportFooter() );
    assertNotNull( builder.getReport().getRootGroup() );
    assertNotNull( builder.getReport().getPageDefinition() );
    assertNoGetImageExpression( builder );

    Element imageElement = findImageElement( builder );
    assertNotNull( imageElement );
    assertEquals( KettleFileTableModel.Field.image.name(),
      imageElement.getAttribute( AttributeNames.Core.NAMESPACE, "field" ) );
  }

  @Test
  public void createReportBindsPdfImageFieldForPdfOutput() throws Exception {
    KettleReportBuilder builder = createReportBuilder( OutputType.PDF );
    builder.createReport();

    assertNoGetImageExpression( builder );

    Element imageElement = findImageElement( builder );
    assertNotNull( imageElement );
    assertEquals( KettleFileTableModel.Field.pdf_image.name(),
      imageElement.getAttribute( AttributeNames.Core.NAMESPACE, "field" ) );
  }

  @Test
  public void pdfImageColumnUsesDrawableType() {
    ReportSubjectLocation location =
      new ReportSubjectLocation( "sample.ktr", null, null, RepositoryObjectType.TRANSFORMATION );
    KettleFileTableModel model =
      new KettleFileTableModel( DefaultBowl.getInstance(), mock( LoggingObjectInterface.class ),
        Collections.singletonList( location ) );

    assertEquals( Drawable.class, KettleFileTableModel.Field.pdf_image.getClazz() );
    assertEquals( Drawable.class, model.getColumnClass( KettleFileTableModel.Field.pdf_image.ordinal() ) );
    assertTrue( model.getValueAt( 0, KettleFileTableModel.Field.pdf_image.ordinal() ) instanceof Drawable );
  }

  private KettleReportBuilder createReportBuilder( OutputType outputType ) {
    LoggingObjectInterface log = mock( LoggingObjectInterface.class );

    AutoDocOptionsInterface options = mock( AutoDocOptionsInterface.class );
    when( options.isIncludingImage() ).thenReturn( Boolean.TRUE );
    when( options.getOutputType() ).thenReturn( outputType );

    return new KettleReportBuilder( DefaultBowl.getInstance(), log,
      Collections.<ReportSubjectLocation>emptyList(), "", options );
  }

  private Element findImageElement( KettleReportBuilder builder ) {
    RelationalGroup group = (RelationalGroup) builder.getReport().getRootGroup();
    GroupDataBody groupDataBody = group.findGroupDataBody();
    ItemBand itemBand = groupDataBody.getItemBand();

    return findElementByName( itemBand, "image" );
  }

  private void assertNoGetImageExpression( KettleReportBuilder builder ) {
    for ( int i = 0; i < builder.getReport().getExpressions().size(); i++ ) {
      Expression expression = builder.getReport().getExpressions().getExpression( i );
      assertNotEquals( "Unexpected legacy getImage expression found", "getImage", expression.getName() );
    }
  }

  private Element findElementByName( Section section, String name ) {
    for ( int i = 0; i < section.getElementCount(); i++ ) {
      Element element = section.getElement( i );
      if ( name.equals( element.getName() ) ) {
        return element;
      }
      if ( element instanceof Section ) {
        Element childElement = findElementByName( (Section) element, name );
        if ( childElement != null ) {
          return childElement;
        }
      }
    }
    return null;
  }

}
