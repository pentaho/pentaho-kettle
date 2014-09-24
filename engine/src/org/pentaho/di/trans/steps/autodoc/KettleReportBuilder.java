/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.autodoc;

import java.awt.Point;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.GroupDataBody;
import org.pentaho.reporting.engine.classic.core.ItemBand;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.RelationalGroup;
import org.pentaho.reporting.engine.classic.core.ReportFooter;
import org.pentaho.reporting.engine.classic.core.ReportHeader;
import org.pentaho.reporting.engine.classic.core.SimplePageDefinition;
import org.pentaho.reporting.engine.classic.core.TableDataFactory;
import org.pentaho.reporting.engine.classic.core.elementfactory.ContentElementFactory;
import org.pentaho.reporting.engine.classic.core.elementfactory.LabelElementFactory;
import org.pentaho.reporting.engine.classic.core.elementfactory.TextFieldElementFactory;
import org.pentaho.reporting.engine.classic.core.modules.misc.beanshell.BSHExpression;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.csv.CSVReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.rtf.RTFReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xls.ExcelReportUtil;
import org.pentaho.reporting.engine.classic.core.style.BandStyleKeys;
import org.pentaho.reporting.engine.classic.core.style.TextStyleKeys;

public class KettleReportBuilder {

  public enum OutputType {
    PDF, HTML, DOC, XLS, CSV, METADATA
  }

  private static final float DEF_FONT_HEIGHT = 12f;
  private static final int DEF_LABEL_WIDTH = 120;
  private static final int DEF_TEXT_WIDTH = 630;

  private static final boolean DEF_LABEL_UNDERLINE = false;
  private static final boolean DEF_LABEL_BOLD = false;
  private static final boolean DEF_LABEL_ITALIC = true;

  private List<ReportSubjectLocation> filenames;

  private MasterReport report;

  private String targetFilename;

  private AutoDocOptionsInterface options;

  private LoggingObjectInterface parentObject;

  public KettleReportBuilder( LoggingObjectInterface parentObject, List<ReportSubjectLocation> locations,
    String targetFilename, AutoDocOptionsInterface options ) {
    this.parentObject = parentObject;
    this.filenames = locations;
    this.targetFilename = targetFilename;
    this.options = options;
  }

  private static int createTextField( ItemBand details,
    String labelText, String fieldName,
    int labelWidth, int textWidth,
    int pagePosition, float fontHeight,
    boolean labelUnderline, boolean labelBold, boolean labelItalic ) {

    ItemBand rowBand = new ItemBand();
    rowBand.setLayout( BandStyleKeys.LAYOUT_ROW );

    LabelElementFactory labelElementFactory = new LabelElementFactory();

    labelElementFactory.setText( labelText );
    labelElementFactory.setAbsolutePosition( new Point( 0, pagePosition ) );
    labelElementFactory.setMinimumWidth( (float) labelWidth );
    labelElementFactory.setMinimumHeight( fontHeight );
    labelElementFactory.setUnderline( labelUnderline );
    labelElementFactory.setBold( labelBold );
    labelElementFactory.setItalic( labelItalic );
    labelElementFactory.setDynamicHeight( true );
    Element label = labelElementFactory.createElement();
    rowBand.addElement( label );

    TextFieldElementFactory textFactory = new TextFieldElementFactory();
    textFactory.setFieldname( fieldName );
    textFactory.setAbsolutePosition( new Point( labelWidth, pagePosition ) );
    textFactory.setMinimumWidth( (float) textWidth );
    textFactory.setMinimumHeight( fontHeight );
    textFactory.setOverflowY( false );
    textFactory.setDynamicHeight( true );
    Element element = textFactory.createElement();
    element.setDynamicContent( true );
    rowBand.addElement( element );

    details.addElement( rowBand );

    return (int) ( pagePosition + fontHeight );
  }

  private static int createTextField( ItemBand details, String labelText, String fieldName, int pagePosition ) {
    return createTextField( details, labelText, fieldName, DEF_LABEL_WIDTH, DEF_TEXT_WIDTH, pagePosition,
      DEF_FONT_HEIGHT, DEF_LABEL_UNDERLINE, DEF_LABEL_BOLD, DEF_LABEL_ITALIC );
  }

  public void createReport() throws Exception {
    // Create a new report
    //
    report = new MasterReport();

    // Define where which transformation and step to read from, explain it to the reporting engine
    //
    KettleFileTableModel transMetaTableModel = new KettleFileTableModel( parentObject, filenames );

    TableDataFactory dataFactory = new TableDataFactory( "default", transMetaTableModel );

    // Give the data to the report at runtime!
    //
    report.setDataFactory( dataFactory );

    // Add a report header and footer
    //
    ReportHeader reportHeader = new ReportHeader();
    report.setReportHeader( reportHeader );
    ReportFooter reportFooter = new ReportFooter();
    report.setReportFooter( reportFooter );

    // Now we need to define an area on which we can draw report elements, called groups and bands...
    //
    RelationalGroup group = new RelationalGroup();
    group.addField( "filename" );
    GroupDataBody groupData = new GroupDataBody();
    ItemBand itemBand = new ItemBand();
    itemBand.setVisible( true );
    itemBand.setLayout( BandStyleKeys.LAYOUT_AUTO );
    groupData.setItemBand( itemBand );
    group.setBody( groupData );
    report.setRootGroup( group );

    // Put a title at the top of the report
    //
    /*
     * LabelElementFactory labelElementFactory = new LabelElementFactory();
     * labelElementFactory.setText("Kettle documentation"); labelElementFactory.setMinimumWidth(500f);
     * labelElementFactory.setMinimumHeight(20f); labelElementFactory.setUnderline(true);
     * labelElementFactory.setBold(true); Element label = labelElementFactory.createElement();
     *
     * // Add the label to the header... // reportHeader.addElement(label);
     */
    int pagePosition = 0;

    // Set the header to bold...
    //
    reportHeader.getStyle().setStyleProperty( TextStyleKeys.BOLD, true );

    // Now add the filename to the report
    //
    pagePosition = createTextField( itemBand, "Filename: ", "filename", pagePosition );

    // The name of the transformation
    //
    if ( options.isIncludingName() ) {
      pagePosition = createTextField( itemBand, "Name: ", "name", pagePosition );
    }

    // The description of the transformation...
    //
    if ( options.isIncludingDescription() ) {
      pagePosition = createTextField( itemBand, "Description: ", "description", pagePosition );
    }

    // The description of the transformation...
    //
    if ( options.isIncludingExtendedDescription() ) {
      pagePosition = createTextField( itemBand, "Long description: ", "extended_description", pagePosition );
    }

    // Include a line with logging information
    //
    if ( options.isIncludingLoggingConfiguration() ) {
      pagePosition = createTextField( itemBand, "Logging: ", "logging", pagePosition );
    }

    // Include a line with the creation date and user
    //
    if ( options.isIncludingCreated() ) {
      pagePosition = createTextField( itemBand, "Creation: ", "creation", pagePosition );
    }

    // Include a line with the modification date and user
    //
    if ( options.isIncludingModified() ) {
      pagePosition = createTextField( itemBand, "Modification: ", "modification", pagePosition );
    }

    // The last execution result
    //
    if ( options.isIncludingLastExecutionResult() ) {
      pagePosition = createTextField( itemBand, "Last execution result: : ", "last_exec_result", pagePosition );
    }

    // Optionally include an image of the transformation...
    //
    if ( options.isIncludingImage() ) {
      String packName = KettleReportBuilder.class.getPackage().getName();
      String bshCode =
        "Object getValue() { "
          + Const.CR + "  return new " + packName + ".TransJobDrawable(dataRow, "
          + ( options.getOutputType() == OutputType.PDF ? "true" : "false" ) + ");" + Const.CR + "}";
      BSHExpression bshExpression = new BSHExpression();
      bshExpression.setExpression( bshCode );
      bshExpression.setName( "getImage" );
      report.addExpression( bshExpression );

      ContentElementFactory contentElementFactory = new ContentElementFactory();
      contentElementFactory.setName( "image" );
      contentElementFactory.setAbsolutePosition( new Point( 0, pagePosition ) );
      contentElementFactory.setMinimumWidth( 750f );
      contentElementFactory.setMaximumWidth( 750f );
      contentElementFactory.setMinimumHeight( 400f );
      contentElementFactory.setMaximumHeight( 750f );
      contentElementFactory.setScale( true );
      contentElementFactory.setDynamicHeight( true );
      Element imageElement = contentElementFactory.createElement();

      imageElement
        .setAttributeExpression( AttributeNames.Core.NAMESPACE, AttributeNames.Core.VALUE, bshExpression );
      imageElement.setAttribute( AttributeNames.Core.NAMESPACE, AttributeNames.Core.IMAGE_ENCODING_QUALITY, "9" );
      imageElement.setAttribute( AttributeNames.Core.NAMESPACE, AttributeNames.Core.IMAGE_ENCODING_TYPE, "PNG" );

      ItemBand imageBand = new ItemBand();
      imageBand.setLayout( BandStyleKeys.LAYOUT_ROW );
      imageBand.addElement( imageElement );
      itemBand.addElement( imageBand );
    }

    Paper a4Paper = new Paper();

    double paperWidth = 8.26;
    double paperHeight = 11.69;
    a4Paper.setSize( paperWidth * 72.0, paperHeight * 72.0 );

    /*
     * set the margins respectively the imageable area
     */
    double leftMargin = 0.78; /* should be about 2cm */
    double rightMargin = 0.78;
    double topMargin = 0.08; // this is a very small topMargin
    double bottomMargin = 0.78;

    a4Paper.setImageableArea(
      leftMargin * 72.0, topMargin * 72.0, ( paperWidth - leftMargin - rightMargin ) * 72.0, ( paperHeight
        - topMargin - bottomMargin ) * 72.0 );

    /*
     * create a PageFormat and associate the Paper with it.
     */
    PageFormat pageFormat = new PageFormat();
    pageFormat.setOrientation( PageFormat.LANDSCAPE );
    pageFormat.setPaper( a4Paper );

    SimplePageDefinition pageDefinition = new SimplePageDefinition( pageFormat );
    report.setPageDefinition( pageDefinition );
  }

  public void render() throws Exception {

    createReport();

    switch ( options.getOutputType() ) {
      case PDF:
        PdfReportUtil.createPDF( report, targetFilename );
        break;
      case DOC:
        RTFReportUtil.createRTF( report, targetFilename );
        break;
      case XLS:
        ExcelReportUtil.createXLS( report, targetFilename );
        break;
      case HTML:
        HtmlReportUtil.createDirectoryHTML( report, targetFilename );
        break;
      case CSV:
        CSVReportUtil.createCSV( report, targetFilename );
        break;
      default:
        break;
    }
  }

  @VisibleForTesting
  MasterReport getReport() {
    return report;
  }
}
