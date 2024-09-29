/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.googleanalytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.googleanalytics.GaInputStepMeta;
import org.pentaho.di.trans.steps.googleanalytics.GoogleAnalyticsApiFacade;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.ProviderFilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.GaData;
import com.google.api.services.analytics.model.Profile;
import com.google.api.services.analytics.model.Profiles;
import com.google.api.services.analytics.model.Segment;
import com.google.api.services.analytics.model.Segments;

public class GaInputStepDialog extends BaseStepDialog implements StepDialogInterface {

  private static Class<?> PKG = GaInputStepMeta.class; // for i18n purposes

  private GaInputStepMeta input;

  private HashMap<String, String> profileTableIds = new HashMap<String, String>();
  private HashMap<String, String> segmentIds = new HashMap<String, String>();

  private Label wlOauthAccount;
  private TextVar wOauthAccount;

  private Button fileChooser;
  private TextVar keyFilename;

  // lookup fields settings widgets
  private Link wlFields;
  private TableView wFields;

  private CCombo wGaProfile;

  private Label wlGaProfile;

  private Button wGetProfiles;

  private Label wlQuSegment;

  private CCombo wQuSegment;

  private Button wGetSegments;

  private Label wlQuStartDate;

  private TextVar wQuStartDate;

  private Label wlQuEndDate;

  private TextVar wQuEndDate;

  private Label wlQuDimensions;

  private TextVar wQuDimensions;

  private Label wlQuMetrics;

  private TextVar wQuMetrics;

  private Label wlQuFilters;

  private TextVar wQuFilters;

  private Label wlQuSort;

  private TextVar wQuSort;

  private Link wQuSortReference;

  private Link wQuFiltersReference;

  private Link wQuMetricsReference;

  private Link wQuDimensionsReference;

  private Link wQuSamplingLevelReference;

  private Label wlQuCustomSegment;

  private TextVar wQuCustomSegment;

  private Link wQuCustomSegmentReference;

  private Button wCustomSegmentEnabled;

  private Label wlGaCustomProfile;

  private Button wCustomProfileEnabled;

  private TextVar wGaCustomProfile;

  private Link wGaCustomProfileReference;

  private Group gConnect;

  private Label wlGaAppName;

  private TextVar wGaAppName;

  private Label wlLimit;

  private Text wLimit;

  private Label wlQuUseSegment;

  private Button wUseSegmentEnabled;

  private Label wlQuSamplingLevel;

  private CCombo wQuSamplingLevel;

  private int middle;
  private int margin;

  private ModifyListener lsMod;

  static final String REFERENCE_SORT_URI =
    "https://developers.google.com/analytics/devguides/reporting/core/v3/reference#sort";
  static final String REFERENCE_METRICS_URI =
    "https://developers.google.com/analytics/devguides/reporting/core/v3/reference#metrics";
  static final String REFERENCE_DIMENSIONS_URI =
    "https://developers.google.com/analytics/devguides/reporting/core/v3/reference#dimensions";
  static final String REFERENCE_SEGMENT_URI =
    "https://developers.google.com/analytics/devguides/reporting/core/v3/reference#segment";
  static final String REFERENCE_FILTERS_URI =
    "https://developers.google.com/analytics/devguides/reporting/core/v3/reference#filters";
  static final String REFERENCE_DIMENSION_AND_METRIC_URI =
    "https://developers.google.com/analytics/devguides/reporting/core/v3/";
  static final String REFERENCE_TABLE_ID_URI =
    "https://developers.google.com/analytics/devguides/reporting/core/v3/reference#ids";
  static final String REFERENCE_SAMPLING_LEVEL_URI =
    "https://developers.google.com/analytics/devguides/reporting/core/v3/reference#samplingLevel";

  // constructor
  public GaInputStepDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    setInput( (GaInputStepMeta) in );
  }

  // builds and shows the dialog
  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, getInput() );

    lsMod = new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        getInput().setChanged();
      }
    };
    backupChanged = getInput().hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Shell.Title" ) );

    middle = props.getMiddlePct();
    margin = Const.MARGIN;

    /*************************************************
     * // STEP NAME ENTRY
     *************************************************/

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );

    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    /*************************************************
     * // GOOGLE ANALYTICS CONNECTION GROUP
     *************************************************/

    gConnect = new Group( shell, SWT.SHADOW_ETCHED_IN );
    gConnect.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.ConnectGroup.Label" ) );
    FormLayout gConnectLayout = new FormLayout();
    gConnectLayout.marginWidth = 3;
    gConnectLayout.marginHeight = 3;
    gConnect.setLayout( gConnectLayout );
    props.setLook( gConnect );

    FormData fdConnect = new FormData();
    fdConnect.left = new FormAttachment( 0, 0 );
    fdConnect.right = new FormAttachment( 100, 0 );
    fdConnect.top = new FormAttachment( wStepname, margin );
    gConnect.setLayoutData( fdConnect );

    // Google Analytics app name
    wlGaAppName = new Label( gConnect, SWT.RIGHT );
    wlGaAppName.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.AppName.Label" ) );
    props.setLook( wlGaAppName );
    FormData fdlGaAppName = new FormData();
    fdlGaAppName.top = new FormAttachment( 0, margin );
    fdlGaAppName.left = new FormAttachment( 0, 0 );
    fdlGaAppName.right = new FormAttachment( middle, -margin );
    wlGaAppName.setLayoutData( fdlGaAppName );
    wGaAppName = new TextVar( transMeta, gConnect, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wGaAppName.addModifyListener( lsMod );
    wGaAppName.setToolTipText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.AppName.Tooltip" ) );
    props.setLook( wGaAppName );
    FormData fdGaAppName = new FormData();
    fdGaAppName.top = new FormAttachment( wStepname, margin );
    fdGaAppName.left = new FormAttachment( middle, 0 );
    fdGaAppName.right = new FormAttachment( 100, 0 );
    wGaAppName.setLayoutData( fdGaAppName );

    createOauthServiceCredentialsControls();


    // custom profile definition
    wlGaCustomProfile = new Label( gConnect, SWT.RIGHT );
    wlGaCustomProfile.setText( BaseMessages.getString(
      PKG, "GoogleAnalyticsDialog.Profile.CustomProfileEnabled.Label" ) );
    props.setLook( wlGaCustomProfile );
    FormData fdlGaCustomProfile = new FormData();
    fdlGaCustomProfile.top = new FormAttachment( keyFilename, margin );
    fdlGaCustomProfile.left = new FormAttachment( 0, 0 );
    fdlGaCustomProfile.right = new FormAttachment( middle, -margin );
    wlGaCustomProfile.setLayoutData( fdlGaCustomProfile );

    wCustomProfileEnabled = new Button( gConnect, SWT.CHECK );
    wCustomProfileEnabled.setToolTipText(
      BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Profile.CustomProfileEnabled.Tooltip" ) );
    props.setLook( wCustomProfileEnabled );
    wCustomProfileEnabled.pack( true );

    FormData fdCustomProfileEnabled = new FormData();
    fdCustomProfileEnabled.left = new FormAttachment( middle, 0 );
    fdCustomProfileEnabled.top = new FormAttachment( keyFilename, margin );
    wCustomProfileEnabled.setLayoutData( fdCustomProfileEnabled );

    wCustomProfileEnabled.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        getInput().setChanged();
        setActive();
        if ( wCustomProfileEnabled.getSelection() ) {
          wGaCustomProfile.setFocus();
        } else {
          wGaProfile.setFocus();
        }
      }

    } );

    wGaCustomProfile = new TextVar( transMeta, gConnect, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wGaCustomProfile.addModifyListener( lsMod );
    wGaCustomProfile.setToolTipText( BaseMessages.getString(
      PKG, "GoogleAnalyticsDialog.Profile.CustomProfile.Tooltip" ) );
    props.setLook( wGaCustomProfile );

    wGaCustomProfileReference = new Link( gConnect, SWT.SINGLE );
    wGaCustomProfileReference
      .setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Reference.Label" ) );
    props.setLook( wGaCustomProfileReference );
    wGaCustomProfileReference.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event ev ) {
        BareBonesBrowserLaunch.openURL( REFERENCE_TABLE_ID_URI );
      }
    } );

    wGaCustomProfileReference.pack( true );

    FormData fdGaCustomProfile = new FormData();
    fdGaCustomProfile.top = new FormAttachment( keyFilename, margin );
    fdGaCustomProfile.left = new FormAttachment( wCustomProfileEnabled, margin );
    fdGaCustomProfile.right = new FormAttachment( 100, -wGaCustomProfileReference.getBounds().width - margin );
    wGaCustomProfile.setLayoutData( fdGaCustomProfile );

    FormData fdGaCustomProfileReference = new FormData();
    fdGaCustomProfileReference.top = new FormAttachment( keyFilename, margin );
    fdGaCustomProfileReference.left = new FormAttachment( wGaCustomProfile, 0 );
    fdGaCustomProfileReference.right = new FormAttachment( 100, 0 );
    wGaCustomProfileReference.setLayoutData( fdGaCustomProfileReference );

    // Google analytics profile

    wlGaProfile = new Label( gConnect, SWT.RIGHT );
    wlGaProfile.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Profile.Label" ) );
    props.setLook( wlGaProfile );

    FormData fdlGaProfile = new FormData();
    fdlGaProfile.top = new FormAttachment( wGaCustomProfile, margin );
    fdlGaProfile.left = new FormAttachment( 0, 0 );
    fdlGaProfile.right = new FormAttachment( middle, -margin );
    wlGaProfile.setLayoutData( fdlGaProfile );

    wGaProfile = new CCombo( gConnect, SWT.LEFT | SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY );

    props.setLook( wGaProfile );
    wGaProfile.addModifyListener( lsMod );
    wGaProfile.setToolTipText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Profile.Tooltip" ) );

    wGetProfiles = new Button( gConnect, SWT.PUSH );
    wGetProfiles.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Profile.GetProfilesButton.Label" ) );
    wGetProfiles.setToolTipText( BaseMessages.getString(
      PKG, "GoogleAnalyticsDialog.Profile.GetProfilesButton.Tooltip" ) );
    props.setLook( wGetProfiles );
    wGetProfiles.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event ev ) {
        shell.getDisplay().asyncExec( new Runnable() {
          @Override
          public void run() {
            readGaProfiles();
          }
        } );
      }
    } );

    wGetProfiles.pack( true );

    FormData fdGaProfile = new FormData();
    fdGaProfile.left = new FormAttachment( middle, 0 );
    fdGaProfile.top = new FormAttachment( wGaCustomProfile, margin );
    fdGaProfile.right = new FormAttachment( 100, -wGetProfiles.getBounds().width - margin );
    wGaProfile.setLayoutData( fdGaProfile );

    FormData fdGetProfiles = new FormData();
    fdGetProfiles.left = new FormAttachment( wGaProfile, 0 );
    fdGetProfiles.top = new FormAttachment( wGaCustomProfile, margin );
    fdGetProfiles.right = new FormAttachment( 100, 0 );
    wGetProfiles.setLayoutData( fdGetProfiles );

    /*************************************************
     * // GOOGLE ANALYTICS QUERY GROUP
     *************************************************/

    Group gQuery = new Group( shell, SWT.SHADOW_ETCHED_IN );
    gQuery.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.QueryGroup.Label" ) );
    FormLayout gQueryLayout = new FormLayout();
    gQueryLayout.marginWidth = 3;
    gQueryLayout.marginHeight = 3;
    gQuery.setLayout( gQueryLayout );
    props.setLook( gQuery );

    // query start date
    wlQuStartDate = new Label( gQuery, SWT.RIGHT );
    wlQuStartDate.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.StartDate.Label" ) );
    props.setLook( wlQuStartDate );
    FormData fdlQuStartDate = new FormData();
    fdlQuStartDate.top = new FormAttachment( 0, margin );
    fdlQuStartDate.left = new FormAttachment( 0, 0 );
    fdlQuStartDate.right = new FormAttachment( middle, -margin );
    wlQuStartDate.setLayoutData( fdlQuStartDate );
    wQuStartDate = new TextVar( transMeta, gQuery, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wQuStartDate.addModifyListener( lsMod );
    wQuStartDate.setToolTipText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.StartDate.Tooltip" ) );
    props.setLook( wQuStartDate );
    FormData fdQuStartDate = new FormData();
    fdQuStartDate.top = new FormAttachment( 0, margin );
    fdQuStartDate.left = new FormAttachment( middle, 0 );
    fdQuStartDate.right = new FormAttachment( 100, 0 );
    wQuStartDate.setLayoutData( fdQuStartDate );

    // query end date
    wlQuEndDate = new Label( gQuery, SWT.RIGHT );
    wlQuEndDate.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.EndDate.Label" ) );
    props.setLook( wlQuEndDate );
    FormData fdlQuEndDate = new FormData();
    fdlQuEndDate.top = new FormAttachment( wQuStartDate, margin );
    fdlQuEndDate.left = new FormAttachment( 0, 0 );
    fdlQuEndDate.right = new FormAttachment( middle, -margin );
    wlQuEndDate.setLayoutData( fdlQuEndDate );
    wQuEndDate = new TextVar( transMeta, gQuery, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wQuEndDate.addModifyListener( lsMod );
    wQuEndDate.setToolTipText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.EndDate.Tooltip" ) );
    props.setLook( wQuEndDate );
    FormData fdQuEndDate = new FormData();
    fdQuEndDate.top = new FormAttachment( wQuStartDate, margin );
    fdQuEndDate.left = new FormAttachment( middle, 0 );
    fdQuEndDate.right = new FormAttachment( 100, 0 );
    wQuEndDate.setLayoutData( fdQuEndDate );

    // query dimensions
    wlQuDimensions = new Label( gQuery, SWT.RIGHT );
    wlQuDimensions.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Dimensions.Label" ) );
    props.setLook( wlQuDimensions );
    FormData fdlQuDimensions = new FormData();
    fdlQuDimensions.top = new FormAttachment( wQuEndDate, margin );
    fdlQuDimensions.left = new FormAttachment( 0, 0 );
    fdlQuDimensions.right = new FormAttachment( middle, -margin );
    wlQuDimensions.setLayoutData( fdlQuDimensions );
    wQuDimensions = new TextVar( transMeta, gQuery, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wQuDimensions.addModifyListener( lsMod );
    wQuDimensions.setToolTipText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Dimensions.Tooltip" ) );
    props.setLook( wQuDimensions );

    wQuDimensionsReference = new Link( gQuery, SWT.SINGLE );

    wQuDimensionsReference.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Reference.Label" ) );
    props.setLook( wQuDimensionsReference );
    wQuDimensionsReference.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event ev ) {
        BareBonesBrowserLaunch.openURL( REFERENCE_DIMENSIONS_URI );
      }
    } );

    wQuDimensionsReference.pack( true );

    FormData fdQuDimensions = new FormData();
    fdQuDimensions.top = new FormAttachment( wQuEndDate, margin );
    fdQuDimensions.left = new FormAttachment( middle, 0 );
    fdQuDimensions.right = new FormAttachment( 100, -wQuDimensionsReference.getBounds().width - margin );
    wQuDimensions.setLayoutData( fdQuDimensions );

    FormData fdQuDimensionsReference = new FormData();
    fdQuDimensionsReference.top = new FormAttachment( wQuEndDate, margin );
    fdQuDimensionsReference.left = new FormAttachment( wQuDimensions, 0 );
    fdQuDimensionsReference.right = new FormAttachment( 100, 0 );
    wQuDimensionsReference.setLayoutData( fdQuDimensionsReference );

    // query Metrics
    wlQuMetrics = new Label( gQuery, SWT.RIGHT );
    wlQuMetrics.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Metrics.Label" ) );
    props.setLook( wlQuMetrics );
    FormData fdlQuMetrics = new FormData();
    fdlQuMetrics.top = new FormAttachment( wQuDimensions, margin );
    fdlQuMetrics.left = new FormAttachment( 0, 0 );
    fdlQuMetrics.right = new FormAttachment( middle, -margin );
    wlQuMetrics.setLayoutData( fdlQuMetrics );
    wQuMetrics = new TextVar( transMeta, gQuery, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wQuMetrics.addModifyListener( lsMod );
    wQuMetrics.setToolTipText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Metrics.Tooltip" ) );
    props.setLook( wQuMetrics );

    wQuMetricsReference = new Link( gQuery, SWT.SINGLE );
    wQuMetricsReference.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Reference.Label" ) );
    props.setLook( wQuMetricsReference );
    wQuMetricsReference.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event ev ) {
        BareBonesBrowserLaunch.openURL( REFERENCE_METRICS_URI );
      }
    } );

    wQuMetricsReference.pack( true );

    FormData fdQuMetrics = new FormData();
    fdQuMetrics.top = new FormAttachment( wQuDimensions, margin );
    fdQuMetrics.left = new FormAttachment( middle, 0 );
    fdQuMetrics.right = new FormAttachment( 100, -wQuMetricsReference.getBounds().width - margin );
    wQuMetrics.setLayoutData( fdQuMetrics );

    FormData fdQuMetricsReference = new FormData();
    fdQuMetricsReference.top = new FormAttachment( wQuDimensions, margin );
    fdQuMetricsReference.left = new FormAttachment( wQuMetrics, 0 );
    fdQuMetricsReference.right = new FormAttachment( 100, 0 );
    wQuMetricsReference.setLayoutData( fdQuMetricsReference );

    // query filters
    wlQuFilters = new Label( gQuery, SWT.RIGHT );
    wlQuFilters.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Filters.Label" ) );
    props.setLook( wlQuFilters );
    FormData fdlQuFilters = new FormData();
    fdlQuFilters.top = new FormAttachment( wQuMetrics, margin );
    fdlQuFilters.left = new FormAttachment( 0, 0 );
    fdlQuFilters.right = new FormAttachment( middle, -margin );
    wlQuFilters.setLayoutData( fdlQuFilters );
    wQuFilters = new TextVar( transMeta, gQuery, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wQuFilters.addModifyListener( lsMod );
    wQuFilters.setToolTipText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Filters.Tooltip" ) );
    props.setLook( wQuFilters );

    wQuFiltersReference = new Link( gQuery, SWT.SINGLE );
    wQuFiltersReference.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Reference.Label" ) );
    props.setLook( wQuFiltersReference );
    wQuFiltersReference.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event ev ) {
        BareBonesBrowserLaunch.openURL( REFERENCE_FILTERS_URI );
      }
    } );

    wQuFiltersReference.pack( true );

    FormData fdQuFilters = new FormData();
    fdQuFilters.top = new FormAttachment( wQuMetrics, margin );
    fdQuFilters.left = new FormAttachment( middle, 0 );
    fdQuFilters.right = new FormAttachment( 100, -wQuFiltersReference.getBounds().width - margin );
    wQuFilters.setLayoutData( fdQuFilters );

    FormData fdQuFiltersReference = new FormData();
    fdQuFiltersReference.top = new FormAttachment( wQuMetrics, margin );
    fdQuFiltersReference.left = new FormAttachment( wQuFilters, 0 );
    fdQuFiltersReference.right = new FormAttachment( 100, 0 );
    wQuFiltersReference.setLayoutData( fdQuFiltersReference );

    // query Sort
    wlQuSort = new Label( gQuery, SWT.RIGHT );
    wlQuSort.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Sort.Label" ) );
    props.setLook( wlQuSort );
    FormData fdlQuSort = new FormData();
    fdlQuSort.top = new FormAttachment( wQuFilters, margin );
    fdlQuSort.left = new FormAttachment( 0, 0 );
    fdlQuSort.right = new FormAttachment( middle, -margin );
    wlQuSort.setLayoutData( fdlQuSort );
    wQuSort = new TextVar( transMeta, gQuery, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wQuSort.addModifyListener( lsMod );
    wQuSort.setToolTipText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Sort.Tooltip" ) );
    props.setLook( wQuSort );

    wQuSortReference = new Link( gQuery, SWT.SINGLE );
    wQuSortReference.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Reference.Label" ) );
    props.setLook( wQuSortReference );
    wQuSortReference.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event ev ) {
        BareBonesBrowserLaunch.openURL( REFERENCE_SORT_URI );
      }
    } );

    wQuSortReference.pack( true );

    FormData fdQuSort = new FormData();
    fdQuSort.top = new FormAttachment( wQuFilters, margin );
    fdQuSort.left = new FormAttachment( middle, 0 );
    fdQuSort.right = new FormAttachment( 100, -wQuSortReference.getBounds().width - margin );
    wQuSort.setLayoutData( fdQuSort );

    FormData fdQuSortReference = new FormData();
    fdQuSortReference.top = new FormAttachment( wQuFilters, margin );
    fdQuSortReference.left = new FormAttachment( wQuSort, 0 );
    fdQuSortReference.right = new FormAttachment( 100, 0 );
    wQuSortReference.setLayoutData( fdQuSortReference );

    // custom segment definition
    wlQuUseSegment = new Label( gQuery, SWT.RIGHT );
    wlQuUseSegment.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.UseSegment.Label" ) );
    props.setLook( wlQuUseSegment );
    FormData fdlQuUseSegment = new FormData();
    fdlQuUseSegment.top = new FormAttachment( wQuSort, margin );
    fdlQuUseSegment.left = new FormAttachment( 0, 0 );
    fdlQuUseSegment.right = new FormAttachment( middle, -margin );
    wlQuUseSegment.setLayoutData( fdlQuUseSegment );

    wUseSegmentEnabled = new Button( gQuery, SWT.CHECK );
    props.setLook( wUseSegmentEnabled );
    wUseSegmentEnabled.pack( true );

    FormData fdUseSegmentEnabled = new FormData();
    fdUseSegmentEnabled.left = new FormAttachment( middle, 0 );
    fdUseSegmentEnabled.top = new FormAttachment( wQuSort, margin );
    wUseSegmentEnabled.setLayoutData( fdUseSegmentEnabled );

    wUseSegmentEnabled.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        getInput().setChanged();
        setActive();
        if ( wUseSegmentEnabled.getSelection() ) {
          if ( wCustomSegmentEnabled.getSelection() ) {
            wQuCustomSegment.setFocus();
          } else {
            wQuSegment.setFocus();
          }
        }
      }
    } );

    // custom segment definition
    wlQuCustomSegment = new Label( gQuery, SWT.RIGHT );
    wlQuCustomSegment.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.CustomSegment.Label" ) );
    props.setLook( wlQuCustomSegment );
    FormData fdlQuCustomSegment = new FormData();
    fdlQuCustomSegment.top = new FormAttachment( wUseSegmentEnabled, margin );
    fdlQuCustomSegment.left = new FormAttachment( 0, 0 );
    fdlQuCustomSegment.right = new FormAttachment( middle, -margin );
    wlQuCustomSegment.setLayoutData( fdlQuCustomSegment );

    wCustomSegmentEnabled = new Button( gQuery, SWT.CHECK );
    props.setLook( wCustomSegmentEnabled );
    wCustomSegmentEnabled.pack( true );

    FormData fdCustomSegmentEnabled = new FormData();
    fdCustomSegmentEnabled.left = new FormAttachment( middle, 0 );
    fdCustomSegmentEnabled.top = new FormAttachment( wUseSegmentEnabled, margin );
    wCustomSegmentEnabled.setLayoutData( fdCustomSegmentEnabled );

    wCustomSegmentEnabled.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        getInput().setChanged();
        setActive();
        if ( wCustomSegmentEnabled.getSelection() ) {
          wQuCustomSegment.setFocus();
        } else {
          wQuSegment.setFocus();
        }
      }

    } );

    wQuCustomSegment = new TextVar( transMeta, gQuery, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wQuCustomSegment.addModifyListener( lsMod );
    wQuCustomSegment.setToolTipText( BaseMessages.getString(
      PKG, "GoogleAnalyticsDialog.Query.CustomSegment.Tooltip" ) );
    props.setLook( wQuCustomSegment );

    wQuCustomSegmentReference = new Link( gQuery, SWT.SINGLE );
    wQuCustomSegmentReference
      .setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Reference.Label" ) );
    props.setLook( wQuCustomSegmentReference );
    wQuCustomSegmentReference.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event ev ) {
        BareBonesBrowserLaunch.openURL( REFERENCE_SEGMENT_URI );
      }
    } );

    wQuCustomSegmentReference.pack( true );

    FormData fdQuCustomSegment = new FormData();
    fdQuCustomSegment.top = new FormAttachment( wUseSegmentEnabled, margin );
    fdQuCustomSegment.left = new FormAttachment( wCustomSegmentEnabled, margin );
    fdQuCustomSegment.right = new FormAttachment( 100, -wQuCustomSegmentReference.getBounds().width - margin );
    wQuCustomSegment.setLayoutData( fdQuCustomSegment );

    FormData fdQuCustomSegmentReference = new FormData();
    fdQuCustomSegmentReference.top = new FormAttachment( wUseSegmentEnabled, margin );
    fdQuCustomSegmentReference.left = new FormAttachment( wQuCustomSegment, 0 );
    fdQuCustomSegmentReference.right = new FormAttachment( 100, 0 );
    wQuCustomSegmentReference.setLayoutData( fdQuCustomSegmentReference );

    // segment selection

    wlQuSegment = new Label( gQuery, SWT.RIGHT );
    wlQuSegment.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Segment.Label" ) );
    props.setLook( wlQuSegment );

    FormData fdlQuSegment = new FormData();
    fdlQuSegment.top = new FormAttachment( wQuCustomSegment, margin );
    fdlQuSegment.left = new FormAttachment( 0, 0 );
    fdlQuSegment.right = new FormAttachment( middle, -margin );
    wlQuSegment.setLayoutData( fdlQuSegment );

    wQuSegment = new CCombo( gQuery, SWT.LEFT | SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY );

    props.setLook( wQuSegment );
    wQuSegment.addModifyListener( lsMod );
    wQuSegment.setToolTipText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Segment.Tooltip" ) );

    wGetSegments = new Button( gQuery, SWT.PUSH );
    wGetSegments.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.GetSegmentsButton.Label" ) );
    wGetSegments.setToolTipText( BaseMessages.getString(
      PKG, "GoogleAnalyticsDialog.Query.GetSegmentsButton.Tooltip" ) );
    props.setLook( wGetSegments );
    wGetSegments.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event ev ) {
        shell.getDisplay().asyncExec( new Runnable() {
          @Override
          public void run() {
            readGaSegments();
          }
        } );
      }
    } );

    wGetSegments.pack( true );

    FormData fdQuSegment = new FormData();
    fdQuSegment.left = new FormAttachment( middle, 0 );
    fdQuSegment.top = new FormAttachment( wQuCustomSegment, margin );
    fdQuSegment.right = new FormAttachment( 100, -wGetSegments.getBounds().width - margin );
    wQuSegment.setLayoutData( fdQuSegment );

    FormData fdGetSegments = new FormData();
    fdGetSegments.left = new FormAttachment( wQuSegment, 0 );
    fdGetSegments.top = new FormAttachment( wQuCustomSegment, margin );
    fdGetSegments.right = new FormAttachment( 100, 0 );
    wGetSegments.setLayoutData( fdGetSegments );

    // samplingLevel selection

    wlQuSamplingLevel = new Label( gQuery, SWT.RIGHT );
    wlQuSamplingLevel.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.SamplingLevel.Label" ) );
    props.setLook( wlQuSamplingLevel );

    FormData fdlQuSamplingLevel = new FormData();
    fdlQuSamplingLevel.top = new FormAttachment( wQuSegment, margin );
    fdlQuSamplingLevel.left = new FormAttachment( 0, 0 );
    fdlQuSamplingLevel.right = new FormAttachment( middle, -margin );
    wlQuSamplingLevel.setLayoutData( fdlQuSamplingLevel );

    wQuSamplingLevel = new CCombo( gQuery, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wQuSamplingLevel );

    wQuSamplingLevelReference = new Link( gQuery, SWT.SINGLE );

    wQuSamplingLevelReference.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.Reference.Label" ) );
    props.setLook( wQuSamplingLevelReference );
    wQuSamplingLevelReference.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event ev ) {
        BareBonesBrowserLaunch.openURL( REFERENCE_SAMPLING_LEVEL_URI );
      }
    } );

    wQuSamplingLevelReference.pack( true );

    wQuSamplingLevel.addModifyListener( lsMod );
    wQuSamplingLevel.setToolTipText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Query.SamplingLevel.Tooltip" ) );

    FormData fdQuSamplingLevel = new FormData();
    fdQuSamplingLevel.left = new FormAttachment( middle, 0 );
    fdQuSamplingLevel.top = new FormAttachment( wQuSegment, margin );
    fdQuSamplingLevel.right = new FormAttachment( 100, -wQuSamplingLevelReference.getBounds().width - margin );

    FormData fdQuSamplingLevelReference = new FormData();
    fdQuSamplingLevelReference.top = new FormAttachment( wQuSegment, margin );
    fdQuSamplingLevelReference.left = new FormAttachment( wQuDimensions, 0 );
    fdQuSamplingLevelReference.right = new FormAttachment( 100, 0 );
    wQuSamplingLevelReference.setLayoutData( fdQuSamplingLevelReference );

    wQuSamplingLevel.setLayoutData( fdQuSamplingLevel );
    wQuSamplingLevel.setItems( GaInputStepMeta.TYPE_SAMPLING_LEVEL_CODE );

    wQuSamplingLevel.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        getInput().setChanged();
      }
    } );

    FormData fdQueryGroup = new FormData();
    fdQueryGroup.left = new FormAttachment( 0, 0 );
    fdQueryGroup.right = new FormAttachment( 100, 0 );
    fdQueryGroup.top = new FormAttachment( gConnect, margin );
    gQuery.setLayoutData( fdQueryGroup );

    gQuery.setTabList( new Control[] {
      wQuStartDate, wQuEndDate, wQuDimensions, wQuMetrics, wQuFilters, wQuSort, wUseSegmentEnabled,
      wCustomSegmentEnabled, wQuCustomSegment, wQuSegment, wGetSegments } );

    // Limit input ...
    wlLimit = new Label( shell, SWT.RIGHT );
    wlLimit.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.LimitSize.Label" ) );
    props.setLook( wlLimit );
    FormData fdlLimit = new FormData();
    fdlLimit.left = new FormAttachment( 0, 0 );
    fdlLimit.right = new FormAttachment( middle, -margin );
    fdlLimit.bottom = new FormAttachment( 100, -50 );
    wlLimit.setLayoutData( fdlLimit );
    wLimit = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wLimit.setToolTipText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.LimitSize.Tooltip" ) );
    props.setLook( wLimit );
    wLimit.addModifyListener( lsMod );
    FormData fdLimit = new FormData();
    fdLimit.left = new FormAttachment( middle, 0 );
    fdLimit.right = new FormAttachment( 100, 0 );
    fdLimit.bottom = new FormAttachment( 100, -50 );

    wLimit.setLayoutData( fdLimit );

    /*************************************************
     * // KEY / LOOKUP TABLE
     *************************************************/

    wlFields = new Link( shell, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.Return.Label" ) );
    props.setLook( wlFields );
    wlFields.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event ev ) {
        BareBonesBrowserLaunch.openURL( REFERENCE_DIMENSION_AND_METRIC_URI );
      }
    } );

    FormData fdlReturn = new FormData();
    fdlReturn.left = new FormAttachment( 0, 0 );
    fdlReturn.top = new FormAttachment( gQuery, margin );
    wlFields.setLayoutData( fdlReturn );

    int fieldWidgetCols = 5;
    int fieldWidgetRows = ( getInput().getFeedField() != null ? getInput().getFeedField().length : 1 );

    ColumnInfo[] ciKeys = new ColumnInfo[ fieldWidgetCols ];
    ciKeys[ 0 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "GoogleAnalyticsDialog.ColumnInfo.FeedFieldType" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {
          GaInputStepMeta.FIELD_TYPE_DIMENSION, GaInputStepMeta.FIELD_TYPE_METRIC,
          GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_PROPERTY, GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_FIELD },
        true );
    ciKeys[ 1 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "GoogleAnalyticsDialog.ColumnInfo.FeedField" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false, false );
    ciKeys[ 1 ].setUsingVariables( true );
    ciKeys[ 2 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "GoogleAnalyticsDialog.ColumnInfo.RenameTo" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false, false );
    ciKeys[ 3 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "GoogleAnalyticsDialog.ColumnInfo.Type" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
        ValueMetaBase.getTypes() );
    ciKeys[ 4 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "GoogleAnalyticsDialog.ColumnInfo.Format" ),
        ColumnInfo.COLUMN_TYPE_FORMAT, 4 );

    setTableView( new TableView(
        transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciKeys,
        fieldWidgetRows, lsMod, props ) );

    FormData fdReturn = new FormData();
    fdReturn.left = new FormAttachment( 0, 0 );
    fdReturn.top = new FormAttachment( wlFields, margin );
    fdReturn.right = new FormAttachment( 100, 0 );
    fdReturn.bottom = new FormAttachment( wLimit, -margin );
    getTableView().setLayoutData( fdReturn );

    /*************************************************
     * // OK AND CANCEL BUTTONS
     *************************************************/

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    wGet = new Button( shell, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "System.Button.GetFields" ) );

    wGet.addListener( SWT.Selection, new Listener() {
        @Override
        public void handleEvent( Event e ) {
          getFields();
        }
      }
    );

    wPreview = new Button( shell, SWT.PUSH );

    wPreview.setText( BaseMessages.getString( PKG, "System.Button.Preview" ) );
    wPreview.addListener( SWT.Selection, new Listener() {
        @Override
        public void handleEvent( Event ev ) {
          preview();
        }
      }
    );
    BaseStepDialog.positionBottomButtons( shell, new Button[] { wOK, wGet, wPreview, wCancel }, margin, wLimit );

    // Add listeners
    lsCancel = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsOK = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );

    /*************************************************
     * // DEFAULT ACTION LISTENERS
     *************************************************/

    lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wGaCustomProfile.addSelectionListener( lsDef );
    wQuStartDate.addSelectionListener( lsDef );
    wQuEndDate.addSelectionListener( lsDef );
    wQuDimensions.addSelectionListener( lsDef );
    wQuMetrics.addSelectionListener( lsDef );
    wQuFilters.addSelectionListener( lsDef );
    wQuSort.addSelectionListener( lsDef );
    wQuCustomSegment.addSelectionListener( lsDef );
    wQuSamplingLevel.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(
      new ShellAdapter() {
        @Override
        public void shellClosed( ShellEvent e ) {
          cancel();
        }
      }
    );

    fileChooser.addSelectionListener( new SelectionAdapterFileDialogTextVar( log, keyFilename, transMeta,
      new SelectionAdapterOptions( SelectionOperation.FILE, new FilterType[] { FilterType.P12, FilterType.ALL },
        FilterType.P12, new ProviderFilterType[] { ProviderFilterType.LOCAL } ) ) );

    // Set the shell size, based upon previous time...
    setSize();

    /*************************************************
     * // POPULATE AND OPEN DIALOG
     *************************************************/

    getData();

    getInput().setChanged( backupChanged );
    wStepname.setFocus();

    shell.setTabList( new Control[] { wStepname, gConnect, gQuery, getTableView() } );
    shell.open();

    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }

    return stepname;
  }

  // Visible for testing
  void getFields() {
    Analytics.Data.Ga.Get query = getPreviewQuery();
    if ( query == null ) {
      return;
    }
    query.setMaxResults( 1 );

    try {
      GaData dataFeed = query.execute();

      if ( dataFeed == null || dataFeed.getRows() == null || dataFeed.getRows().size() < 1 ) {

        MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
        mb.setText( "Query yields empty feed" );
        mb.setMessage( "The feed did not give any results. Please specify a query that returns data." );
        mb.open();

        return;
      }


      int i = 0;
      List<GaData.ColumnHeaders> colHeaders = dataFeed.getColumnHeaders();
      getTableView().table.setItemCount( colHeaders.size() + dataFeed.getProfileInfo().size() );
      for ( GaData.ColumnHeaders colHeader : colHeaders ) {
        String name = colHeader.getName();
        String dataType = colHeader.getDataType();
        String columnType = colHeader.getColumnType();

        TableItem item = getTableView().table.getItem( i );

        if ( columnType.equals( "DIMENSION" ) ) {
          item.setText( 1, GaInputStepMeta.FIELD_TYPE_DIMENSION );
          item.setText( 2, name );
          item.setText( 3, name );

          // recognize date dimension
          if ( name.equalsIgnoreCase( "ga:date" ) ) {
            item.setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_DATE ) );
            item.setText( 5, "yyyyMMdd" );
          } else if ( name.equalsIgnoreCase( "ga:daysSinceLastVisit" )
              || name.equalsIgnoreCase( "ga:visitLength" )
              || name.equalsIgnoreCase( "ga:visitCount" ) ) {
            item.setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_INTEGER ) );
            item.setText( 5, "#;-#" );
          } else if ( name.equalsIgnoreCase( "ga:latitude" )
              || name.equalsIgnoreCase( "ga:longitude" ) ) {
            item.setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_NUMBER ) );
            item.setText( 5, "#.#;-#.#" );
          } else {
            item.setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_STRING ) );
            item.setText( 5, "" );
          }
          i++;
        } else if ( columnType.equals( "METRIC" ) ) {

          item.setText( 1, GaInputStepMeta.FIELD_TYPE_METRIC );
          item.setText( 2, name );
          item.setText( 3, name );

          // depending on type
          if ( dataType.compareToIgnoreCase( "currency" ) == 0
              || dataType.compareToIgnoreCase( "float" ) == 0
              || dataType.compareToIgnoreCase( "percent" ) == 0
              || dataType.compareToIgnoreCase( "us_currency" ) == 0 ) {
            item.setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_NUMBER ) );
            item.setText( 5, "#.#;-#.#" );
          } else if ( dataType.compareToIgnoreCase( "time" ) == 0
              || dataType.compareToIgnoreCase( "integer" ) == 0 ) {
            item.setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_INTEGER ) );
            item.setText( 5, "#;-#" );
          } else {
            item.setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_STRING ) );
            item.setText( 5, "" );
          }
          i++;
        }
      }
      // Fill ds property and ds fields
      TableItem item = getTableView().table.getItem( i );
      item.setText( 1, GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_PROPERTY );
      item.setText( 2, GaInputStepMeta.PROPERTY_DATA_SOURCE_PROFILE_ID );
      item.setText( 3, GaInputStepMeta.PROPERTY_DATA_SOURCE_PROFILE_ID );
      item.setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_STRING ) );
      item.setText( 5, "" );
      i++;

      item = getTableView().table.getItem( i );
      item.setText( 1, GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_PROPERTY );
      item.setText( 2, GaInputStepMeta.PROPERTY_DATA_SOURCE_WEBPROP_ID );
      item.setText( 3, GaInputStepMeta.PROPERTY_DATA_SOURCE_WEBPROP_ID );
      item.setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_STRING ) );
      item.setText( 5, "" );
      i++;

      item = getTableView().table.getItem( i );
      item.setText( 1, GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_PROPERTY );
      item.setText( 2, GaInputStepMeta.PROPERTY_DATA_SOURCE_ACCOUNT_NAME );
      item.setText( 3, GaInputStepMeta.PROPERTY_DATA_SOURCE_ACCOUNT_NAME );
      item.setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_STRING ) );
      item.setText( 5, "" );
      i++;

      item = getTableView().table.getItem( i );
      item.setText( 1, GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_FIELD );
      item.setText( 2, GaInputStepMeta.FIELD_DATA_SOURCE_TABLE_ID );
      item.setText( 3, GaInputStepMeta.FIELD_DATA_SOURCE_TABLE_ID );
      item.setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_STRING ) );
      item.setText( 5, "" );
      i++;

      item = getTableView().table.getItem( i );
      item.setText( 1, GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_FIELD );
      item.setText( 2, GaInputStepMeta.FIELD_DATA_SOURCE_TABLE_NAME );
      item.setText( 3, GaInputStepMeta.FIELD_DATA_SOURCE_TABLE_NAME );
      item.setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_STRING ) );
      item.setText( 5, "" );

      getTableView().removeEmptyRows();
      getTableView().setRowNums();
      getTableView().optWidth( true );
      getInput().setChanged();
    } catch ( IOException ioe ) {
      Exception exceptionToDisplay = ioe;
      // Try to display something more user friendly than plain JSON
      if ( ioe instanceof GoogleJsonResponseException ) {
        GoogleJsonResponseException gjre = (GoogleJsonResponseException) ioe;
        if ( gjre.getDetails() != null && gjre.getDetails().getMessage() != null ) {
          exceptionToDisplay = new IOException( gjre.getDetails().getMessage(), gjre );
        }
      }
      new ErrorDialog( shell, BaseMessages.getString( PKG, "GoogleAnalyticsDialog.RequestError.DialogTitle" ),
          BaseMessages.getString( PKG, "GoogleAnalyticsDialog.RequestError.DialogMessage" ), exceptionToDisplay );
    }
  }

  private void getInfo( GaInputStepMeta meta ) {

    stepname = wStepname.getText(); // return value

    meta.setGaProfileName( wGaProfile.getText() );
    meta.setGaAppName( wGaAppName.getText() );
    meta.setOauthServiceAccount( wOauthAccount.getText() );
    meta.setOAuthKeyFile( keyFilename.getText() );

    if ( !Utils.isEmpty( wGaProfile.getText() ) ) {
      meta.setGaProfileTableId( profileTableIds.get( wGaProfile.getText() ) );
    } else {
      meta.setGaProfileTableId( null );
    }

    meta.setUseCustomTableId( wCustomProfileEnabled.getSelection() );
    meta.setGaCustomTableId( wGaCustomProfile.getText() );

    meta.setSegmentName( Utils.isEmpty( wQuSegment.getText() ) ? "All Visits" : wQuSegment.getText() );
    if ( !Utils.isEmpty( wQuSegment.getText() ) ) {
      meta.setSegmentId( segmentIds.get( wQuSegment.getText() ) );
    } else {
      // all visits is default
      meta.setSegmentId( "gaid::-1" );
    }

    meta.setStartDate( wQuStartDate.getText() );
    meta.setEndDate( wQuEndDate.getText() );

    meta.setDimensions( wQuDimensions.getText() );
    meta.setMetrics( wQuMetrics.getText() );
    meta.setFilters( wQuFilters.getText() );
    meta.setSort( wQuSort.getText() );

    meta.setUseSegment( wUseSegmentEnabled.getSelection() );
    meta.setUseCustomSegment( wCustomSegmentEnabled.getSelection() );
    meta.setCustomSegment( wQuCustomSegment.getText() );
    meta.setSamplingLevel( wQuSamplingLevel.getText() );

    int nrFields = getTableView().nrNonEmpty();

    meta.allocate( nrFields );

    for ( int i = 0; i < nrFields; i++ ) {
      TableItem item = getTableView().getNonEmpty( i );
      meta.getFeedFieldType()[ i ] = item.getText( 1 );
      meta.getFeedField()[ i ] = item.getText( 2 );
      meta.getOutputField()[ i ] = item.getText( 3 );

      meta.getOutputType()[ i ] = ValueMetaBase.getType( item.getText( 4 ) );
      meta.getConversionMask()[ i ] = item.getText( 5 );

      // fix unknowns
      if ( meta.getOutputType()[ i ] < 0 ) {
        meta.getOutputType()[ i ] = ValueMetaInterface.TYPE_STRING;
      }
    }
    meta.setRowLimit( Const.toInt( wLimit.getText(), 0 ) );
  }

  // Preview the data
  private void preview() {
    // Create the XML input step
    GaInputStepMeta oneMeta = new GaInputStepMeta();
    getInfo( oneMeta );
    TransMeta previewMeta =
      TransPreviewFactory.generatePreviewTransformation( transMeta, oneMeta, wStepname.getText() );

    EnterNumberDialog numberDialog =
      new EnterNumberDialog( shell, props.getDefaultPreviewSize(),
        BaseMessages.getString( PKG, "GoogleAnalyticsDialog.PreviewSize.DialogTitle" ),
        BaseMessages.getString( PKG, "GoogleAnalyticsDialog.PreviewSize.DialogMessage" ) );
    int previewSize = numberDialog.open();
    if ( previewSize > 0 ) {
      TransPreviewProgressDialog progressDialog =
        new TransPreviewProgressDialog(
          shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
      progressDialog.open();

      Trans trans = progressDialog.getTrans();
      String loggingText = progressDialog.getLoggingText();

      if ( !progressDialog.isCancelled() ) {
        if ( trans.getResult() != null && trans.getResult().getNrErrors() > 0 ) {
          EnterTextDialog etd =
            new EnterTextDialog(
              shell, BaseMessages.getString( PKG, "System.Dialog.PreviewError.Title" ), BaseMessages
              .getString( PKG, "System.Dialog.PreviewError.Message" ), loggingText, true );
          etd.setReadOnly();
          etd.open();
        }
      }

      PreviewRowsDialog prd =
        new PreviewRowsDialog(
          shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(
            wStepname.getText() ), progressDialog.getPreviewRows( wStepname.getText() ), loggingText );
      prd.open();
    }
  }

  protected Analytics.Data.Ga.Get getPreviewQuery() {
    try {
      String ids = wCustomProfileEnabled.getSelection()
        ? transMeta.environmentSubstitute( wGaCustomProfile.getText() )
        : profileTableIds.get( wGaProfile.getText() );

      String metrics = transMeta.environmentSubstitute( wQuMetrics.getText() );
      if ( Utils.isEmpty( metrics ) ) {
        MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
        mb.setText( BaseMessages.getString( PKG, "GoogleAnalytics.Error.NoMetricsSpecified.Title" ) );
        mb.setMessage( BaseMessages.getString( PKG, "GoogleAnalytics.Error.NoMetricsSpecified.Message" ) );
        mb.open();
        return null;
      }

      Analytics analytics = getAnalytics();
      if ( analytics == null ) {
        return null;
      }
      Analytics.Data.Ga.Get query = analytics.data().ga().get(
        ids,
        transMeta.environmentSubstitute( wQuStartDate.getText() ),
        transMeta.environmentSubstitute( wQuEndDate.getText() ),
        metrics
      );

      String dimensions = transMeta.environmentSubstitute( wQuDimensions.getText() );
      if ( !Utils.isEmpty( dimensions ) ) {
        query.setDimensions( dimensions );
      }

      if ( wUseSegmentEnabled.getSelection() ) {
        if ( wCustomSegmentEnabled.getSelection() ) {
          query.setSegment( transMeta.environmentSubstitute( wQuCustomSegment.getText() ) );
        } else {
          query.setSegment( segmentIds.get( wQuSegment.getText() ) );
        }
      }

      if ( !Utils.isEmpty( wQuSamplingLevel.getText() ) ) {
        query.setSamplingLevel( transMeta.environmentSubstitute( wQuSamplingLevel.getText() ) );
      }

      if ( !Utils.isEmpty( wQuFilters.getText() ) ) {
        query.setFilters( transMeta.environmentSubstitute( wQuFilters.getText() ) );
      }
      if ( !Utils.isEmpty( wQuSort.getText() ) ) {
        query.setSort( transMeta.environmentSubstitute( wQuSort.getText() ) );
      }

      return query;
    } catch ( Exception e ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.AuthenticationFailure.DialogTitle" ) );
      mb.setMessage( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.AuthenticationFailure.DialogMessage" ) );
      mb.open();

    }
    return null;
  }

  protected void setActive() {
    boolean segment = wUseSegmentEnabled.getSelection();
    wCustomSegmentEnabled.setEnabled( segment );

    if ( !segment ) {
      wQuCustomSegment.setEnabled( false );
      wQuCustomSegmentReference.setEnabled( false );
      wQuSegment.setEnabled( false );
      wGetSegments.setEnabled( false );
    } else {

      boolean custom = wCustomSegmentEnabled.getSelection();

      wQuCustomSegment.setEnabled( custom );
      wQuCustomSegmentReference.setEnabled( custom );
      wQuSegment.setEnabled( !custom );
      wGetSegments.setEnabled( !custom );
    }

    boolean directTableId = wCustomProfileEnabled.getSelection();

    wGaProfile.setEnabled( !directTableId );
    wGetProfiles.setEnabled( !directTableId );
    wGaCustomProfile.setEnabled( directTableId );
    wGaCustomProfileReference.setEnabled( directTableId );
  }

  // Collect profile list from the GA service for the given authentication
  // information
  public void readGaProfiles() {
    try {
      Analytics analytics = getAnalytics();
      if ( analytics == null ) {
        return;
      }
      Analytics.Management.Profiles.List profiles = analytics.management().profiles().list( "~all", "~all" );

      Profiles profileList = profiles.execute();

      profileTableIds.clear();
      List<String> profileNames = new ArrayList<String>();
      for ( Profile profile : profileList.getItems() ) {
        String tableId = "ga:" + profile.getId();
        String profileName = tableId + " - profile: " + profile.getName();
        profileNames.add( profileName );
        profileTableIds.put( profileName, tableId );
      }

      // put the profiles to the combo box and select first one
      wGaProfile.setItems( profileNames.toArray( new String[ profileNames.size() ] ) );
      if ( profileNames.size() > 0 ) {
        wGaProfile.select( 0 );
      }
    } catch ( Exception e ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.AuthenticationFailure.DialogTitle" ) );
      mb.setMessage( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.AuthenticationFailure.DialogMessage" ) );
      mb.open();
    }
  }

  // Collect segment list from the GA service for the given authentication information
  public void readGaSegments() {
    try {
      Analytics analytics = getAnalytics();
      if ( analytics == null ) {
        return;
      }
      Segments segments = analytics.management().segments().list().execute();

      ArrayList<String> segmentNames = new ArrayList<String>( 20 );
      segmentIds.clear();

      for ( Segment segmentEntry : segments.getItems() ) {
        segmentNames.add( segmentEntry.getName() );
        segmentIds.put( segmentEntry.getName(), "gaid::" + segmentEntry.getId() );
      }

      // put the segments to the combo box and select first one
      wQuSegment.setItems( segmentNames.toArray( new String[ segmentNames.size() ] ) );
      if ( segmentNames.size() > 0 ) {
        wQuSegment.select( 0 );
      }

    } catch ( Exception e ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.AuthenticationFailure.DialogTitle" ) );
      mb.setMessage( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.AuthenticationFailure.DialogMessage" ) );
      mb.open();
    }
  }

  /**
   * Collect data from the meta and place it in the dialog
   */
  public void getData() {

    if ( getInput().getGaAppName() != null ) {
      wGaAppName.setText( getInput().getGaAppName() );
    }

    wOauthAccount.setText( Const.NVL( getInput().getOAuthServiceAccount(), "" ) );
    keyFilename.setText( Const.NVL( getInput().getOAuthKeyFile(), "" ) );

    if ( getInput().getGaProfileName() != null ) {
      wGaProfile.setText( getInput().getGaProfileName() );
      profileTableIds.clear();
      profileTableIds.put( getInput().getGaProfileName(), getInput().getGaProfileTableId() );
    }

    if ( getInput().isUseCustomTableId() ) {
      wCustomProfileEnabled.setSelection( true );
    } else {
      wCustomProfileEnabled.setSelection( false );
    }

    if ( getInput().getGaCustomTableId() != null ) {
      wGaCustomProfile.setText( getInput().getGaCustomTableId() );
    }

    if ( getInput().getStartDate() != null ) {
      wQuStartDate.setText( getInput().getStartDate() );
    }

    if ( getInput().getEndDate() != null ) {
      wQuEndDate.setText( getInput().getEndDate() );
    }

    if ( getInput().getDimensions() != null ) {
      wQuDimensions.setText( getInput().getDimensions() );
    }

    if ( getInput().getMetrics() != null ) {
      wQuMetrics.setText( getInput().getMetrics() );
    }

    if ( getInput().getFilters() != null ) {
      wQuFilters.setText( getInput().getFilters() );
    }

    if ( getInput().getSort() != null ) {
      wQuSort.setText( getInput().getSort() );
    }

    if ( getInput().isUseSegment() ) {
      wUseSegmentEnabled.setSelection( true );
    } else {
      wUseSegmentEnabled.setSelection( false );
    }

    if ( getInput().isUseCustomSegment() ) {
      wCustomSegmentEnabled.setSelection( true );
    } else {
      wCustomSegmentEnabled.setSelection( false );
    }

    if ( getInput().getCustomSegment() != null ) {
      wQuCustomSegment.setText( getInput().getCustomSegment() );
    }

    if ( getInput().getSegmentName() != null ) {
      wQuSegment.setText( getInput().getSegmentName() );
      segmentIds.clear();
      segmentIds.put( getInput().getSegmentName(), getInput().getSegmentId() );
    }

    if ( getInput().getSamplingLevel() != null ) {
      wQuSamplingLevel.setText( getInput().getSamplingLevel() );
    }

    if ( getInput().getFeedField() != null ) {

      for ( int i = 0; i < getInput().getFeedField().length; i++ ) {

        TableItem item = getTableView().table.getItem( i );

        if ( getInput().getFeedFieldType()[ i ] != null ) {
          item.setText( 1, getInput().getFeedFieldType()[ i ] );
        }

        if ( getInput().getFeedField()[ i ] != null ) {
          item.setText( 2, getInput().getFeedField()[ i ] );
        }

        if ( getInput().getOutputField()[ i ] != null ) {
          item.setText( 3, getInput().getOutputField()[ i ] );
        }

        item.setText( 4, ValueMetaBase.getTypeDesc( getInput().getOutputType()[ i ] ) );

        if ( getInput().getConversionMask()[ i ] != null ) {
          item.setText( 5, getInput().getConversionMask()[ i ] );
        }

      }
    }

    getTableView().setRowNums();
    getTableView().optWidth( true );

    wLimit.setText( getInput().getRowLimit() + "" );

    setActive();

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    getInput().setChanged( backupChanged );
    dispose();
  }

  // let the meta know about the entered data
  private void ok() {
    getInfo( getInput() );
    dispose();
  }

  private void createOauthServiceCredentialsControls() {
    // OathAccount line
    wlOauthAccount = new Label( gConnect, SWT.RIGHT );
    wlOauthAccount.setText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.OauthAccount.Label" ) );
    props.setLook( wlOauthAccount );

    FormData fdlOathAccount = new FormData();
    fdlOathAccount.left = new FormAttachment( 0, 0 );
    fdlOathAccount.top = new FormAttachment( wGaAppName, margin );
    fdlOathAccount.right = new FormAttachment( middle, -margin );

    wlOauthAccount.setLayoutData( fdlOathAccount );
    wOauthAccount = new TextVar( transMeta, gConnect, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wOauthAccount.setToolTipText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.OauthAccount.Tooltip" ) );
    props.setLook( wOauthAccount );

    wOauthAccount.addModifyListener( lsMod );
    FormData fdOathAccount = new FormData();
    fdOathAccount.left = new FormAttachment( middle, 0 );
    fdOathAccount.top = new FormAttachment( wGaAppName, margin );
    fdOathAccount.right = new FormAttachment( 100, -margin );
    wOauthAccount.setLayoutData( fdOathAccount );


    fileChooser = new Button( gConnect, SWT.PUSH | SWT.CENTER );
    fileChooser.setText( BaseMessages.getString( PKG, ( "System.Button.Browse" ) ) );
    props.setLook( fileChooser );

    FormData fdbFilename = new FormData();
    fdbFilename.right = new FormAttachment( 100, 0 );
    fdbFilename.top = new FormAttachment( wOauthAccount, margin );
    fileChooser.setLayoutData( fdbFilename );

    Label wlFilename = new Label( gConnect, SWT.RIGHT );
    wlFilename.setText( BaseMessages.getString( PKG, ( "GoogleAnalyticsDialog.KeyFile.Label" ) ) );
    props.setLook( wlFilename );
    FormData fdlFilename = new FormData();
    fdlFilename.top = new FormAttachment( wOauthAccount, margin );
    fdlFilename.left = new FormAttachment( 0, 0 );
    fdlFilename.right = new FormAttachment( middle, -margin );
    wlFilename.setLayoutData( fdlFilename );

    keyFilename = new TextVar( transMeta, gConnect, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    keyFilename.setToolTipText( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.KeyFilename.Tooltip" ) );
    keyFilename.addModifyListener( lsMod );
    props.setLook( keyFilename );

    FormData fdFilename = new FormData();
    fdFilename.top = new FormAttachment( wOauthAccount, margin );
    fdFilename.left = new FormAttachment( middle, 0 );
    fdFilename.right = new FormAttachment( fileChooser, -margin );
    keyFilename.setLayoutData( fdFilename );
  }

  private Analytics getAnalytics() {
    try {
      getInfo( getInput() );
      return GoogleAnalyticsApiFacade.createFor(
          transMeta.environmentSubstitute( wGaAppName.getText() ),
          transMeta.environmentSubstitute( wOauthAccount.getText() ),
          transMeta.environmentSubstitute( keyFilename.getText() )
      ).getAnalytics();
    } catch ( Exception e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "GoogleAnalyticsDialog.RequestError.DialogTitle" ),
          BaseMessages.getString( PKG, "GoogleAnalyticsDialog.RequestError.DialogMessage" ), e );
    }

    return null;
  }

  TableView getTableView() {
    return wFields;
  }

  void setTableView( TableView wFields ) {
    this.wFields = wFields;
  }

  GaInputStepMeta getInput() {
    return input;
  }

  void setInput( GaInputStepMeta input ) {
    this.input = input;
  }
}
