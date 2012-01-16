/*******************************************************************************
 *
 * Pentaho Data Integration
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

package org.pentaho.di.ui.trans.steps.googleanalytics;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.googleanalytics.GaInputStepMeta;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.AccountEntry;
import com.google.gdata.data.analytics.AccountFeed;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.data.analytics.Dimension;
import com.google.gdata.data.analytics.Metric;
import com.google.gdata.data.analytics.Property;
import com.google.gdata.data.analytics.Segment;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class GaInputStepDialog extends BaseStepDialog implements StepDialogInterface {

	private static Class<?> PKG = GaInputStepMeta.class; // for i18n purposes

	private GaInputStepMeta input;

	private HashMap<String, String> profileTableIds = new HashMap<String, String>();
	private HashMap<String, String> segmentIds = new HashMap<String, String>();

	// connection settings widgets
	private Label wlGaEmail;
	private TextVar wGaEmail;
	private Label wlGaPassword;
	private TextVar wGaPassword;

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

	private Label wlQuCustomSegment;

	private TextVar wQuCustomSegment;

	private Link wQuCustomSegmentReference;

	private Button wCustomSegmentEnabled;

	private Label wlGaCustomProfile;

	private Button wCustomProfileEnabled;

	private TextVar wGaCustomProfile;

	private Link wGaCustomProfileReference;

	private Label wlGaAppName;

	private TextVar wGaAppName;

	private Label wlLimit;

	private Text wLimit;

	final static String REFERENCE_SORT_URI = "http://code.google.com/apis/analytics/docs/gdata/gdataReferenceDataFeed.html#sort";
	final static String REFERENCE_METRICS_URI = "http://code.google.com/apis/analytics/docs/gdata/gdataReferenceDataFeed.html#metrics";
	final static String REFERENCE_DIMENSIONS_URI = "http://code.google.com/apis/analytics/docs/gdata/gdataReferenceDataFeed.html#dimensions";
	final static String REFERENCE_SEGMENT_URI = "http://code.google.com/apis/analytics/docs/gdata/gdataReferenceDataFeed.html#segment";
	final static String REFERENCE_FILTERS_URI = "http://code.google.com/apis/analytics/docs/gdata/gdataReferenceDataFeed.html#filters";
	final static String REFERENCE_DIMENSION_AND_METRIC_URI = "http://code.google.com/apis/analytics/docs/gdata/gdataReferenceDimensionsMetrics.html";
	final static String REFERENCE_TABLE_ID_URI = "http://code.google.com/apis/analytics/docs/gdata/gdataReferenceDataFeed.html#ids";
	final static String WEBSITE_URL = "http://type-exit.org/adventures-with-open-source-bi/google-analytics-plugin-for-kettle/";

	// constructor
	public GaInputStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (GaInputStepMeta) in;
	}

	// builds and shows the dialog
	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		backupChanged = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Shell.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		/*************************************************
		 * // STEP NAME ENTRY
		 *************************************************/

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);

		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		/*************************************************
		 * // GOOGLE ANALYTICS CONNECTION GROUP
		 *************************************************/

		Group gConnect = new Group(shell, SWT.SHADOW_ETCHED_IN);
		gConnect.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.ConnectGroup.Label"));
		FormLayout gConnectLayout = new FormLayout();
		gConnectLayout.marginWidth = 3;
		gConnectLayout.marginHeight = 3;
		gConnect.setLayout(gConnectLayout);
		props.setLook(gConnect);
		
		// Google Analytics app name
		wlGaAppName = new Label(gConnect, SWT.RIGHT);
		wlGaAppName.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.AppName.Label"));
		props.setLook(wlGaAppName);
		FormData fdlGaAppName = new FormData();
		fdlGaAppName.top = new FormAttachment(0, margin);
		fdlGaAppName.left = new FormAttachment(0, 0);
		fdlGaAppName.right = new FormAttachment(middle, -margin);
		wlGaAppName.setLayoutData(fdlGaAppName);
		wGaAppName = new TextVar(transMeta, gConnect, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wGaAppName.addModifyListener(lsMod);
		wGaAppName.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.AppName.Tooltip"));
		props.setLook(wGaAppName);
		FormData fdGaAppName = new FormData();
		fdGaAppName.top = new FormAttachment(0, margin);
		fdGaAppName.left = new FormAttachment(middle, 0);
		fdGaAppName.right = new FormAttachment(100, 0);
		wGaAppName.setLayoutData(fdGaAppName);		

		// Google Analytics Email
		wlGaEmail = new Label(gConnect, SWT.RIGHT);
		wlGaEmail.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Email.Label"));
		props.setLook(wlGaEmail);
		FormData fdlGaEmail = new FormData();
		fdlGaEmail.top = new FormAttachment(wGaAppName, margin);
		fdlGaEmail.left = new FormAttachment(0, 0);
		fdlGaEmail.right = new FormAttachment(middle, -margin);
		wlGaEmail.setLayoutData(fdlGaEmail);
		wGaEmail = new TextVar(transMeta, gConnect, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wGaEmail.addModifyListener(lsMod);
		wGaEmail.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Email.Tooltip"));
		props.setLook(wGaEmail);
		FormData fdGaEmail = new FormData();
		fdGaEmail.top = new FormAttachment(wGaAppName, margin);
		fdGaEmail.left = new FormAttachment(middle, 0);
		fdGaEmail.right = new FormAttachment(100, 0);
		wGaEmail.setLayoutData(fdGaEmail);

		// Google Analytics Password
		wlGaPassword = new Label(gConnect, SWT.RIGHT);
		wlGaPassword.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Password.Label"));
		props.setLook(wlGaPassword);
		FormData fdlGaPassword = new FormData();
		fdlGaPassword.top = new FormAttachment(wGaEmail, margin);
		fdlGaPassword.left = new FormAttachment(0, 0);
		fdlGaPassword.right = new FormAttachment(middle, -margin);
		wlGaPassword.setLayoutData(fdlGaPassword);
		wGaPassword = new TextVar(transMeta, gConnect, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.PASSWORD);
		wGaPassword.addModifyListener(lsMod);
		wGaPassword.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Password.Tooltip"));
		props.setLook(wGaPassword);
		FormData fdGaPassword = new FormData();
		fdGaPassword.top = new FormAttachment(wGaEmail, margin);
		fdGaPassword.left = new FormAttachment(middle, 0);
		fdGaPassword.right = new FormAttachment(100, 0);
		wGaPassword.setLayoutData(fdGaPassword);

		
		// custom profile definition
		wlGaCustomProfile = new Label(gConnect, SWT.RIGHT);
		wlGaCustomProfile.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Profile.CustomProfileEnabled.Label"));
		props.setLook(wlGaCustomProfile);
		FormData fdlGaCustomProfile = new FormData();
		fdlGaCustomProfile.top = new FormAttachment(wGaPassword, margin);
		fdlGaCustomProfile.left = new FormAttachment(0, 0);
		fdlGaCustomProfile.right = new FormAttachment(middle, -margin);
		wlGaCustomProfile.setLayoutData(fdlGaCustomProfile);

		wCustomProfileEnabled = new Button(gConnect, SWT.CHECK);
		props.setLook(wCustomProfileEnabled);
		wCustomProfileEnabled.pack(true);

		FormData fdCustomProfileEnabled = new FormData();
		fdCustomProfileEnabled.left = new FormAttachment(middle, 0);
		fdCustomProfileEnabled.top = new FormAttachment(wGaPassword, margin);
		wCustomProfileEnabled.setLayoutData(fdCustomProfileEnabled);

		wCustomProfileEnabled.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setActive();
				if (wCustomProfileEnabled.getSelection()) {
					wGaCustomProfile.setFocus();
				} else {
					wGaProfile.setFocus();
				}
			}

		});

		wGaCustomProfile = new TextVar(transMeta, gConnect, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wGaCustomProfile.addModifyListener(lsMod);
		wGaCustomProfile.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Profile.CustomProfile.Tooltip"));
		props.setLook(wGaCustomProfile);

		wGaCustomProfileReference = new Link(gConnect, SWT.SINGLE);
		wGaCustomProfileReference.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Reference.Label"));
		props.setLook(wGaCustomProfileReference);
		wGaCustomProfileReference.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event ev) {
				BareBonesBrowserLaunch.openURL(REFERENCE_TABLE_ID_URI);
			}
		});

		wGaCustomProfileReference.pack(true);

		FormData fdGaCustomProfile = new FormData();
		fdGaCustomProfile.top = new FormAttachment(wGaPassword, margin);
		fdGaCustomProfile.left = new FormAttachment(wCustomProfileEnabled, margin);
		fdGaCustomProfile.right = new FormAttachment(100, -wGaCustomProfileReference.getBounds().width - margin);
		wGaCustomProfile.setLayoutData(fdGaCustomProfile);

		FormData fdGaCustomProfileReference = new FormData();
		fdGaCustomProfileReference.top = new FormAttachment(wGaPassword, margin);
		fdGaCustomProfileReference.left = new FormAttachment(wGaCustomProfile, 0);
		fdGaCustomProfileReference.right = new FormAttachment(100, 0);
		wGaCustomProfileReference.setLayoutData(fdGaCustomProfileReference);		
		
		
		// Google analytics profile

		wlGaProfile = new Label(gConnect, SWT.RIGHT);
		wlGaProfile.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Profile.Label"));
		props.setLook(wlGaProfile);

		FormData fdlGaProfile = new FormData();
		fdlGaProfile.top = new FormAttachment(wGaCustomProfile, margin);
		fdlGaProfile.left = new FormAttachment(0, 0);
		fdlGaProfile.right = new FormAttachment(middle, -margin);
		wlGaProfile.setLayoutData(fdlGaProfile);

		wGaProfile = new CCombo(gConnect, SWT.LEFT | SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);

		props.setLook(wGaProfile);
		wGaProfile.addModifyListener(lsMod);
		wGaProfile.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Profile.Tooltip"));

		wGetProfiles = new Button(gConnect, SWT.PUSH);
		wGetProfiles.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Profile.GetProfilesButton.Label"));
		wGetProfiles.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Profile.GetProfilesButton.Tooltip"));
		props.setLook(wGetProfiles);
		wGetProfiles.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event ev) {
				shell.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						readGaProfiles(wGaEmail.getText(), wGaPassword.getText(), wGaAppName.getText());
					}
				});
			}
		});

		wGetProfiles.pack(true);

		FormData fdGaProfile = new FormData();
		fdGaProfile.left = new FormAttachment(middle, 0);
		fdGaProfile.top = new FormAttachment(wGaCustomProfile, margin);
		fdGaProfile.right = new FormAttachment(100, -wGetProfiles.getBounds().width - margin);
		wGaProfile.setLayoutData(fdGaProfile);

		FormData fdGetProfiles = new FormData();
		fdGetProfiles.left = new FormAttachment(wGaProfile, 0);
		fdGetProfiles.top = new FormAttachment(wGaCustomProfile, margin);
		fdGetProfiles.right = new FormAttachment(100, 0);
		wGetProfiles.setLayoutData(fdGetProfiles);
		
		
		

		FormData fdConnect = new FormData();
		fdConnect.left = new FormAttachment(0, 0);
		fdConnect.right = new FormAttachment(100, 0);
		fdConnect.top = new FormAttachment(wStepname, margin);
		gConnect.setLayoutData(fdConnect);
		
		gConnect.setTabList(new Control[] {wGaAppName, wGaEmail, wGaPassword, wCustomProfileEnabled, wGaCustomProfile, wGaProfile, wGetProfiles});

		/*************************************************
		 * // GOOGLE ANALYTICS QUERY GROUP
		 *************************************************/

		Group gQuery = new Group(shell, SWT.SHADOW_ETCHED_IN);
		gQuery.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.QueryGroup.Label"));
		FormLayout gQueryLayout = new FormLayout();
		gQueryLayout.marginWidth = 3;
		gQueryLayout.marginHeight = 3;
		gQuery.setLayout(gQueryLayout);
		props.setLook(gQuery);

		// query start date
		wlQuStartDate = new Label(gQuery, SWT.RIGHT);
		wlQuStartDate.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.StartDate.Label"));
		props.setLook(wlQuStartDate);
		FormData fdlQuStartDate = new FormData();
		fdlQuStartDate.top = new FormAttachment(0, margin);
		fdlQuStartDate.left = new FormAttachment(0, 0);
		fdlQuStartDate.right = new FormAttachment(middle, -margin);
		wlQuStartDate.setLayoutData(fdlQuStartDate);
		wQuStartDate = new TextVar(transMeta, gQuery, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wQuStartDate.addModifyListener(lsMod);
		wQuStartDate.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.StartDate.Tooltip"));
		props.setLook(wQuStartDate);
		FormData fdQuStartDate = new FormData();
		fdQuStartDate.top = new FormAttachment(0, margin);
		fdQuStartDate.left = new FormAttachment(middle, 0);
		fdQuStartDate.right = new FormAttachment(100, 0);
		wQuStartDate.setLayoutData(fdQuStartDate);

		// query end date
		wlQuEndDate = new Label(gQuery, SWT.RIGHT);
		wlQuEndDate.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.EndDate.Label"));
		props.setLook(wlQuEndDate);
		FormData fdlQuEndDate = new FormData();
		fdlQuEndDate.top = new FormAttachment(wQuStartDate, margin);
		fdlQuEndDate.left = new FormAttachment(0, 0);
		fdlQuEndDate.right = new FormAttachment(middle, -margin);
		wlQuEndDate.setLayoutData(fdlQuEndDate);
		wQuEndDate = new TextVar(transMeta, gQuery, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wQuEndDate.addModifyListener(lsMod);
		wQuEndDate.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.EndDate.Tooltip"));
		props.setLook(wQuEndDate);
		FormData fdQuEndDate = new FormData();
		fdQuEndDate.top = new FormAttachment(wQuStartDate, margin);
		fdQuEndDate.left = new FormAttachment(middle, 0);
		fdQuEndDate.right = new FormAttachment(100, 0);
		wQuEndDate.setLayoutData(fdQuEndDate);

		// query dimensions
		wlQuDimensions = new Label(gQuery, SWT.RIGHT);
		wlQuDimensions.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Dimensions.Label"));
		props.setLook(wlQuDimensions);
		FormData fdlQuDimensions = new FormData();
		fdlQuDimensions.top = new FormAttachment(wQuEndDate, margin);
		fdlQuDimensions.left = new FormAttachment(0, 0);
		fdlQuDimensions.right = new FormAttachment(middle, -margin);
		wlQuDimensions.setLayoutData(fdlQuDimensions);
		wQuDimensions = new TextVar(transMeta, gQuery, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wQuDimensions.addModifyListener(lsMod);
		wQuDimensions.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Dimensions.Tooltip"));
		props.setLook(wQuDimensions);

		wQuDimensionsReference = new Link(gQuery, SWT.SINGLE);

		wQuDimensionsReference.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Reference.Label"));
		props.setLook(wQuDimensionsReference);
		wQuDimensionsReference.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event ev) {
				BareBonesBrowserLaunch.openURL(REFERENCE_DIMENSIONS_URI);
			}
		});

		wQuDimensionsReference.pack(true);

		FormData fdQuDimensions = new FormData();
		fdQuDimensions.top = new FormAttachment(wQuEndDate, margin);
		fdQuDimensions.left = new FormAttachment(middle, 0);
		fdQuDimensions.right = new FormAttachment(100, -wQuDimensionsReference.getBounds().width - margin);
		wQuDimensions.setLayoutData(fdQuDimensions);

		FormData fdQuDimensionsReference = new FormData();
		fdQuDimensionsReference.top = new FormAttachment(wQuEndDate, margin);
		fdQuDimensionsReference.left = new FormAttachment(wQuDimensions, 0);
		fdQuDimensionsReference.right = new FormAttachment(100, 0);
		wQuDimensionsReference.setLayoutData(fdQuDimensionsReference);

		// query Metrics
		wlQuMetrics = new Label(gQuery, SWT.RIGHT);
		wlQuMetrics.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Metrics.Label"));
		props.setLook(wlQuMetrics);
		FormData fdlQuMetrics = new FormData();
		fdlQuMetrics.top = new FormAttachment(wQuDimensions, margin);
		fdlQuMetrics.left = new FormAttachment(0, 0);
		fdlQuMetrics.right = new FormAttachment(middle, -margin);
		wlQuMetrics.setLayoutData(fdlQuMetrics);
		wQuMetrics = new TextVar(transMeta, gQuery, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wQuMetrics.addModifyListener(lsMod);
		wQuMetrics.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Metrics.Tooltip"));
		props.setLook(wQuMetrics);

		wQuMetricsReference = new Link(gQuery, SWT.SINGLE);
		wQuMetricsReference.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Reference.Label"));
		props.setLook(wQuMetricsReference);
		wQuMetricsReference.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event ev) {
				BareBonesBrowserLaunch.openURL(REFERENCE_METRICS_URI);
			}
		});

		wQuMetricsReference.pack(true);

		FormData fdQuMetrics = new FormData();
		fdQuMetrics.top = new FormAttachment(wQuDimensions, margin);
		fdQuMetrics.left = new FormAttachment(middle, 0);
		fdQuMetrics.right = new FormAttachment(100, -wQuMetricsReference.getBounds().width - margin);
		wQuMetrics.setLayoutData(fdQuMetrics);

		FormData fdQuMetricsReference = new FormData();
		fdQuMetricsReference.top = new FormAttachment(wQuDimensions, margin);
		fdQuMetricsReference.left = new FormAttachment(wQuMetrics, 0);
		fdQuMetricsReference.right = new FormAttachment(100, 0);
		wQuMetricsReference.setLayoutData(fdQuMetricsReference);

		// query filters
		wlQuFilters = new Label(gQuery, SWT.RIGHT);
		wlQuFilters.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Filters.Label"));
		props.setLook(wlQuFilters);
		FormData fdlQuFilters = new FormData();
		fdlQuFilters.top = new FormAttachment(wQuMetrics, margin);
		fdlQuFilters.left = new FormAttachment(0, 0);
		fdlQuFilters.right = new FormAttachment(middle, -margin);
		wlQuFilters.setLayoutData(fdlQuFilters);
		wQuFilters = new TextVar(transMeta, gQuery, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wQuFilters.addModifyListener(lsMod);
		wQuFilters.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Filters.Tooltip"));
		props.setLook(wQuFilters);

		wQuFiltersReference = new Link(gQuery, SWT.SINGLE);
		wQuFiltersReference.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Reference.Label"));
		props.setLook(wQuFiltersReference);
		wQuFiltersReference.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event ev) {
				BareBonesBrowserLaunch.openURL(REFERENCE_FILTERS_URI);
			}
		});

		wQuFiltersReference.pack(true);

		FormData fdQuFilters = new FormData();
		fdQuFilters.top = new FormAttachment(wQuMetrics, margin);
		fdQuFilters.left = new FormAttachment(middle, 0);
		fdQuFilters.right = new FormAttachment(100, -wQuFiltersReference.getBounds().width - margin);
		wQuFilters.setLayoutData(fdQuFilters);

		FormData fdQuFiltersReference = new FormData();
		fdQuFiltersReference.top = new FormAttachment(wQuMetrics, margin);
		fdQuFiltersReference.left = new FormAttachment(wQuFilters, 0);
		fdQuFiltersReference.right = new FormAttachment(100, 0);
		wQuFiltersReference.setLayoutData(fdQuFiltersReference);

		// query Sort
		wlQuSort = new Label(gQuery, SWT.RIGHT);
		wlQuSort.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Sort.Label"));
		props.setLook(wlQuSort);
		FormData fdlQuSort = new FormData();
		fdlQuSort.top = new FormAttachment(wQuFilters, margin);
		fdlQuSort.left = new FormAttachment(0, 0);
		fdlQuSort.right = new FormAttachment(middle, -margin);
		wlQuSort.setLayoutData(fdlQuSort);
		wQuSort = new TextVar(transMeta, gQuery, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wQuSort.addModifyListener(lsMod);
		wQuSort.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Sort.Tooltip"));
		props.setLook(wQuSort);

		wQuSortReference = new Link(gQuery, SWT.SINGLE);
		wQuSortReference.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Reference.Label"));
		props.setLook(wQuSortReference);
		wQuSortReference.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event ev) {
				BareBonesBrowserLaunch.openURL(REFERENCE_SORT_URI);
			}
		});

		wQuSortReference.pack(true);

		FormData fdQuSort = new FormData();
		fdQuSort.top = new FormAttachment(wQuFilters, margin);
		fdQuSort.left = new FormAttachment(middle, 0);
		fdQuSort.right = new FormAttachment(100, -wQuSortReference.getBounds().width - margin);
		wQuSort.setLayoutData(fdQuSort);

		FormData fdQuSortReference = new FormData();
		fdQuSortReference.top = new FormAttachment(wQuFilters, margin);
		fdQuSortReference.left = new FormAttachment(wQuSort, 0);
		fdQuSortReference.right = new FormAttachment(100, 0);
		wQuSortReference.setLayoutData(fdQuSortReference);

		// custom segment definition
		wlQuCustomSegment = new Label(gQuery, SWT.RIGHT);
		wlQuCustomSegment.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.CustomSegment.Label"));
		props.setLook(wlQuCustomSegment);
		FormData fdlQuCustomSegment = new FormData();
		fdlQuCustomSegment.top = new FormAttachment(wQuSort, margin);
		fdlQuCustomSegment.left = new FormAttachment(0, 0);
		fdlQuCustomSegment.right = new FormAttachment(middle, -margin);
		wlQuCustomSegment.setLayoutData(fdlQuCustomSegment);

		wCustomSegmentEnabled = new Button(gQuery, SWT.CHECK);
		props.setLook(wCustomSegmentEnabled);
		wCustomSegmentEnabled.pack(true);

		FormData fdCustomSegmentEnabled = new FormData();
		fdCustomSegmentEnabled.left = new FormAttachment(middle, 0);
		fdCustomSegmentEnabled.top = new FormAttachment(wQuSort, margin);
		wCustomSegmentEnabled.setLayoutData(fdCustomSegmentEnabled);

		wCustomSegmentEnabled.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setActive();
				if (wCustomSegmentEnabled.getSelection()) {
					wQuCustomSegment.setFocus();
				} else {
					wQuSegment.setFocus();
				}
			}

		});

		wQuCustomSegment = new TextVar(transMeta, gQuery, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wQuCustomSegment.addModifyListener(lsMod);
		wQuCustomSegment.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.CustomSegment.Tooltip"));
		props.setLook(wQuCustomSegment);

		wQuCustomSegmentReference = new Link(gQuery, SWT.SINGLE);
		wQuCustomSegmentReference.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Reference.Label"));
		props.setLook(wQuCustomSegmentReference);
		wQuCustomSegmentReference.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event ev) {
				BareBonesBrowserLaunch.openURL(REFERENCE_SEGMENT_URI);
			}
		});

		wQuCustomSegmentReference.pack(true);

		FormData fdQuCustomSegment = new FormData();
		fdQuCustomSegment.top = new FormAttachment(wQuSort, margin);
		fdQuCustomSegment.left = new FormAttachment(wCustomSegmentEnabled, margin);
		fdQuCustomSegment.right = new FormAttachment(100, -wQuCustomSegmentReference.getBounds().width - margin);
		wQuCustomSegment.setLayoutData(fdQuCustomSegment);

		FormData fdQuCustomSegmentReference = new FormData();
		fdQuCustomSegmentReference.top = new FormAttachment(wQuSort, margin);
		fdQuCustomSegmentReference.left = new FormAttachment(wQuCustomSegment, 0);
		fdQuCustomSegmentReference.right = new FormAttachment(100, 0);
		wQuCustomSegmentReference.setLayoutData(fdQuCustomSegmentReference);

		// segment selection

		wlQuSegment = new Label(gQuery, SWT.RIGHT);
		wlQuSegment.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Segment.Label"));
		props.setLook(wlQuSegment);

		FormData fdlQuSegment = new FormData();
		fdlQuSegment.top = new FormAttachment(wQuCustomSegment, margin);
		fdlQuSegment.left = new FormAttachment(0, 0);
		fdlQuSegment.right = new FormAttachment(middle, -margin);
		wlQuSegment.setLayoutData(fdlQuSegment);

		wQuSegment = new CCombo(gQuery, SWT.LEFT | SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);

		props.setLook(wQuSegment);
		wQuSegment.addModifyListener(lsMod);
		wQuSegment.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.Segment.Tooltip"));

		wGetSegments = new Button(gQuery, SWT.PUSH);
		wGetSegments.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.GetSegmentsButton.Label"));
		wGetSegments.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Query.GetSegmentsButton.Tooltip"));
		props.setLook(wGetSegments);
		wGetSegments.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event ev) {
				shell.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						readGaSegments(wGaEmail.getText(), wGaPassword.getText(), wGaProfile.getText(), wGaAppName.getText());
					}
				});
			}
		});

		wGetSegments.pack(true);

		FormData fdQuSegment = new FormData();
		fdQuSegment.left = new FormAttachment(middle, 0);
		fdQuSegment.top = new FormAttachment(wQuCustomSegment, margin);
		fdQuSegment.right = new FormAttachment(100, -wGetSegments.getBounds().width - margin);
		wQuSegment.setLayoutData(fdQuSegment);

		FormData fdGetSegments = new FormData();
		fdGetSegments.left = new FormAttachment(wQuSegment, 0);
		fdGetSegments.top = new FormAttachment(wQuCustomSegment, margin);
		fdGetSegments.right = new FormAttachment(100, 0);
		wGetSegments.setLayoutData(fdGetSegments);

		FormData fdQueryGroup = new FormData();
		fdQueryGroup.left = new FormAttachment(0, 0);
		fdQueryGroup.right = new FormAttachment(100, 0);
		fdQueryGroup.top = new FormAttachment(gConnect, margin);
		gQuery.setLayoutData(fdQueryGroup);

		gQuery.setTabList(new Control[] { wQuStartDate, wQuEndDate, wQuDimensions, wQuMetrics, wQuFilters, wQuSort, wCustomSegmentEnabled, wQuCustomSegment, wQuSegment, wGetSegments });

		// Limit input ...
		wlLimit=new Label(shell, SWT.RIGHT);
		wlLimit.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.LimitSize.Label"));
 		props.setLook(wlLimit);
		FormData fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.right= new FormAttachment(middle, -margin);
		fdlLimit.bottom = new FormAttachment(100, -50);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wLimit.setToolTipText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.LimitSize.Tooltip"));
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		FormData fdLimit=new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.right= new FormAttachment(100, 0);
		fdLimit.bottom = new FormAttachment(100, -50);
		
		wLimit.setLayoutData(fdLimit);		
		
		/*************************************************
		 * // KEY / LOOKUP TABLE
		 *************************************************/

		wlFields = new Link(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.Return.Label"));
		props.setLook(wlFields);
		wlFields.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event ev) {
				BareBonesBrowserLaunch.openURL(REFERENCE_DIMENSION_AND_METRIC_URI);
			}
		});
		
		FormData fdlReturn = new FormData();
		fdlReturn.left = new FormAttachment(0, 0);
		fdlReturn.top = new FormAttachment(gQuery, margin);
		wlFields.setLayoutData(fdlReturn);

		int fieldWidgetCols = 5;
		int fieldWidgetRows = (input.getFeedField() != null ? input.getFeedField().length : 1);

		ColumnInfo[] ciKeys = new ColumnInfo[fieldWidgetCols];
		ciKeys[0] = new ColumnInfo(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.ColumnInfo.FeedFieldType"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[]{GaInputStepMeta.FIELD_TYPE_DIMENSION, GaInputStepMeta.FIELD_TYPE_METRIC, GaInputStepMeta.FIELD_TYPE_CONFIDENCE_INTERVAL, GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_PROPERTY, GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_FIELD}, true);
		ciKeys[1] = new ColumnInfo(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.ColumnInfo.FeedField"), ColumnInfo.COLUMN_TYPE_TEXT, false, false);
		ciKeys[2] = new ColumnInfo(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.ColumnInfo.RenameTo"), ColumnInfo.COLUMN_TYPE_TEXT, false, false);
		ciKeys[3] = new ColumnInfo(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.ColumnInfo.Type"), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes());
        ciKeys[4] = new ColumnInfo(BaseMessages.getString(PKG, "GoogleAnalyticsDialog.ColumnInfo.Format"), ColumnInfo.COLUMN_TYPE_FORMAT, 4);

		wFields = new TableView(transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciKeys, fieldWidgetRows, lsMod, props);

		FormData fdReturn = new FormData();
		fdReturn.left = new FormAttachment(0, 0);
		fdReturn.top = new FormAttachment(wlFields, margin);
		fdReturn.right = new FormAttachment(100, 0);
		fdReturn.bottom = new FormAttachment(wLimit, -margin);
		wFields.setLayoutData(fdReturn);

		/*************************************************
		 * // OK AND CANCEL BUTTONS
		 *************************************************/

		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
		
		wGet = new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields"));
		
		wGet.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {

				DataQuery query = getPreviewQuery();
				query.setMaxResults(1);

				String email = transMeta.environmentSubstitute(wGaEmail.getText());
				String pass = transMeta.environmentSubstitute(wGaPassword.getText());

				AnalyticsService analyticsService = new AnalyticsService(transMeta.environmentSubstitute(wGaAppName.getText()));
				
				try{
				
					analyticsService.setUserCredentials(email, pass);
					DataFeed dataFeed = analyticsService.getFeed(query.getUrl(), DataFeed.class);
					
					if (dataFeed.getEntries().size() < 1){
						
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
						mb.setText("Query yields empty feed");
						mb.setMessage("The feed did not give any results. Please specify a query that returns data.");
						mb.open();
						
						return;
					}
					
					DataEntry entry = dataFeed.getEntries().get(0);
					
					List<Dimension> dims = entry.getDimensions();
					List<Metric> metrics = entry.getMetrics();
					List<Property> dsprops = new ArrayList<Property>();
					int dataSourceFields = 0;
					if (dataFeed.getDataSources().size() > 0){
						dsprops = dataFeed.getDataSources().get(0).getProperties();
						dataSourceFields=2;
					}
					
					wFields.table.setItemCount(dims.size()+metrics.size()*2+dsprops.size()+dataSourceFields);
					
					int i = 0;
					
					// Fill Dimension Fields
					for (Dimension d : dims){
						
						TableItem item = wFields.table.getItem(i);
						item.setText(1, GaInputStepMeta.FIELD_TYPE_DIMENSION);
						item.setText(2, d.getName());
						item.setText(3, d.getName());

						// recognize date dimension
						if (d.getName().equalsIgnoreCase("ga:date")){
							item.setText(4, ValueMeta.getTypeDesc(ValueMeta.TYPE_DATE));
							item.setText(5, "yyyyMMdd");
						}
						else if (d.getName().equalsIgnoreCase("ga:daysSinceLastVisit") || d.getName().equalsIgnoreCase("ga:visitLength") || d.getName().equalsIgnoreCase("ga:visitCount")){
							item.setText(4, ValueMeta.getTypeDesc(ValueMeta.TYPE_INTEGER));
							item.setText(5, "#;-#");
						}						
						else if (d.getName().equalsIgnoreCase("ga:latitude") || d.getName().equalsIgnoreCase("ga:longitude")){
							item.setText(4, ValueMeta.getTypeDesc(ValueMeta.TYPE_NUMBER));
							item.setText(5, "#.#;-#.#");
						} 
						else{
							item.setText(4, ValueMeta.getTypeDesc(ValueMeta.TYPE_STRING));
							item.setText(5, "");
						}
						i++;
					}
					
					// Fill Metric fields
					for (Metric m: metrics){
						
						TableItem item = wFields.table.getItem(i);
						
						item.setText(1, GaInputStepMeta.FIELD_TYPE_METRIC);
						item.setText(2, m.getName());
						item.setText(3, m.getName());

						// depending on type
						if (m.getType().compareToIgnoreCase("currency") == 0 || m.getType().compareToIgnoreCase("float") == 0 || m.getType().compareToIgnoreCase("percent") == 0 || m.getType().compareToIgnoreCase("us_currency") == 0){
							item.setText(4, ValueMeta.getTypeDesc(ValueMeta.TYPE_NUMBER));
							item.setText(5, "#.#;-#.#");
						}
						else if(m.getType().compareToIgnoreCase("time") == 0 || m.getType().compareToIgnoreCase("integer") == 0){
							item.setText(4, ValueMeta.getTypeDesc(ValueMeta.TYPE_INTEGER));
							item.setText(5, "#;-#");
						}
						else{
							item.setText(4, ValueMeta.getTypeDesc(ValueMeta.TYPE_STRING));
							item.setText(5, "");
						}
						
						i++;
						item = wFields.table.getItem(i);
						item.setText(1, GaInputStepMeta.FIELD_TYPE_CONFIDENCE_INTERVAL);
						item.setText(2, m.getName());
						item.setText(3, m.getName()+"#confidenceInterval");
						item.setText(4, ValueMeta.getTypeDesc(ValueMeta.TYPE_NUMBER));	
						item.setText(5, "#.#;-#.#");
						
						i++;
						
					}
					
					// Fill ds property fields
					for (Property prop:dsprops){
						
						TableItem item = wFields.table.getItem(i);
						item.setText(1, GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_PROPERTY);
						item.setText(2, prop.getName());
						item.setText(3, prop.getName());
						item.setText(4, ValueMeta.getTypeDesc(ValueMeta.TYPE_STRING));
						item.setText(5, "");
						i++;
						
					}
					// Fill ds field fields
					if (dataFeed.getDataSources().size() > 0){
						TableItem item = wFields.table.getItem(i);
						item.setText(1, GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_FIELD);
						item.setText(2, GaInputStepMeta.FIELD_DATA_SOURCE_TABLE_ID);
						item.setText(3, GaInputStepMeta.FIELD_DATA_SOURCE_TABLE_ID);
						item.setText(4, ValueMeta.getTypeDesc(ValueMeta.TYPE_STRING));
						item.setText(5, "");
						i++;

						item = wFields.table.getItem(i);
						item.setText(1, GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_FIELD);
						item.setText(2, GaInputStepMeta.FIELD_DATA_SOURCE_TABLE_NAME);
						item.setText(3, GaInputStepMeta.FIELD_DATA_SOURCE_TABLE_NAME);
						item.setText(4, ValueMeta.getTypeDesc(ValueMeta.TYPE_STRING));
						item.setText(5, "");
						i++;
						
					}
					
					
					
										
		            wFields.removeEmptyRows();
		            wFields.setRowNums();
		            wFields.optWidth(true);
		            input.setChanged();

				} catch (AuthenticationException e1) {
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
					mb.setText("Authentication Error");
					mb.setMessage("Could not authenticate. Please check the credentials and ensure that there's no network connectivity problem.\n\n"+e1.getMessage());
					mb.open();
				
					e1.printStackTrace();
					return;

				} catch (IOException e2) {
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
					mb.setText("IO Error");
					mb.setMessage("Could not contact Google Analytics service. Please make sure that there's no network connectivity problem.");
					mb.open();
					e2.printStackTrace();
					return;
					
				} catch (ServiceException e3) {
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
					mb.setText("Service Error");
					mb.setMessage("Google Service Error\n\n"+e3.getMessage());
					mb.open();
					e3.printStackTrace();
					return;
				}

			}

		});		

		wPreview = new Button(shell, SWT.PUSH);
		wPreview.setText(BaseMessages.getString(PKG, "System.Button.Preview"));		
		wPreview.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event ev) {
				preview();
			}
		});
		
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wGet, wPreview, wCancel }, margin, wLimit);

		// Add listeners
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);

		/*************************************************
		 * // DEFAULT ACTION LISTENERS
		 *************************************************/

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);
		wGaEmail.addSelectionListener(lsDef);
		wGaPassword.addSelectionListener(lsDef);
		wGaCustomProfile.addSelectionListener(lsDef);
		wQuStartDate.addSelectionListener(lsDef);
		wQuEndDate.addSelectionListener(lsDef);
		wQuDimensions.addSelectionListener(lsDef);
		wQuMetrics.addSelectionListener(lsDef);
		wQuFilters.addSelectionListener(lsDef);
		wQuSort.addSelectionListener(lsDef);
		wQuCustomSegment.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		// Set the shell size, based upon previous time...
		setSize();

		/*************************************************
		 * // POPULATE AND OPEN DIALOG
		 *************************************************/

		getData();

		input.setChanged(backupChanged);
		wStepname.setFocus();

		shell.setTabList(new Control[]{wStepname, gConnect, gQuery, wFields});
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}
	
	
	private void getInfo(GaInputStepMeta meta){
		
		stepname = wStepname.getText(); // return value

		meta.setGaEmail(wGaEmail.getText());
		meta.setGaPassword(wGaPassword.getText());
		meta.setGaProfileName(wGaProfile.getText());
		meta.setGaAppName(wGaAppName.getText());
		
		if (!Const.isEmpty(wGaProfile.getText())) {
			meta.setGaProfileTableId(profileTableIds.get(wGaProfile.getText()));
		} else {
			meta.setGaProfileTableId(null);
		}
		
		meta.setUseCustomTableId(wCustomProfileEnabled.getSelection());
		meta.setGaCustomTableId(wGaCustomProfile.getText());

		meta.setSegmentName(Const.isEmpty(wQuSegment.getText())?"All Visits":wQuSegment.getText());
		if (!Const.isEmpty(wQuSegment.getText())) {
			meta.setSegmentId(segmentIds.get(wQuSegment.getText()));
		}
		else{
			// all visits is default
			meta.setSegmentId("gaid::-1");
		}

		meta.setStartDate(wQuStartDate.getText());
		meta.setEndDate(wQuEndDate.getText());

		meta.setDimensions(wQuDimensions.getText());
		meta.setMetrics(wQuMetrics.getText());
		meta.setFilters(wQuFilters.getText());
		meta.setSort(wQuSort.getText());

		meta.setUseCustomSegment(wCustomSegmentEnabled.getSelection());
		meta.setCustomSegment(wQuCustomSegment.getText());

		int nrFields = wFields.nrNonEmpty();

		meta.allocate(nrFields);

		for (int i = 0; i < nrFields; i++) {
			TableItem item = wFields.getNonEmpty(i);
			meta.getFeedFieldType()[i] = item.getText(1);
			meta.getFeedField()[i] = item.getText(2);
			meta.getOutputField()[i] = item.getText(3);

			meta.getOutputType()[i] = ValueMeta.getType(item.getText(4));
			meta.getConversionMask()[i] = item.getText(5);

			// fix unknowns
			if (meta.getOutputType()[i] < 0) {
				meta.getOutputType()[i] = ValueMetaInterface.TYPE_STRING;
			}

		}		
		
		meta.setRowLimit(Const.toInt(wLimit.getText(), 0));
		
	}
	
    // Preview the data
    private void preview()
    {
        // Create the XML input step
        GaInputStepMeta oneMeta = new GaInputStepMeta();
        getInfo(oneMeta);

        TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
        
        EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), BaseMessages.getString(PKG, "GoogleAnalyticsDialog.PreviewSize.DialogTitle"), BaseMessages.getString(PKG, "GoogleAnalyticsDialog.PreviewSize.DialogMessage"));
        int previewSize = numberDialog.open();
        if (previewSize>0)
        {
            TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
            progressDialog.open();

            Trans trans = progressDialog.getTrans();
            String loggingText = progressDialog.getLoggingText();

            if (!progressDialog.isCancelled())
            {
                if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
                {
                	EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"),  
                			BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"), loggingText, true );
                	etd.setReadOnly();
                	etd.open();
                }
            }
            
            PreviewRowsDialog prd =new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
            prd.open();
        }
    }		
	
	protected DataQuery getPreviewQuery(){
		
		DataQuery query = null;
		try {
			query = new DataQuery(new URL(GaInputStepMeta.GA_DATA_URL));
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			return null;
		}
		query.setIds(wCustomProfileEnabled.getSelection()? transMeta.environmentSubstitute(wGaCustomProfile.getText()): profileTableIds.get(wGaProfile.getText()));
		query.setStartDate(transMeta.environmentSubstitute(wQuStartDate.getText()));
		query.setEndDate(transMeta.environmentSubstitute(wQuEndDate.getText()));
		query.setDimensions(transMeta.environmentSubstitute(wQuDimensions.getText()));
		query.setMetrics(transMeta.environmentSubstitute(wQuMetrics.getText()));

		if (wCustomSegmentEnabled.getSelection()) {
			query.setSegment(transMeta.environmentSubstitute(wQuCustomSegment.getText()));
		} else {
			query.setSegment(segmentIds.get(wQuSegment.getText()));
		}

		if (!Const.isEmpty(wQuFilters.getText())) {
			query.setFilters(transMeta.environmentSubstitute(wQuFilters.getText()));
		}
		if (!Const.isEmpty(wQuSort.getText())) {
			query.setSort(transMeta.environmentSubstitute(wQuSort.getText()));
		}

		return query;
		
	}

	protected void setActive() {
		boolean custom = wCustomSegmentEnabled.getSelection();

		wQuCustomSegment.setEnabled(custom);
		wQuCustomSegmentReference.setEnabled(custom);
		wQuSegment.setEnabled(!custom);
		wGetSegments.setEnabled(!custom);
		
		boolean directTableId = wCustomProfileEnabled.getSelection();
		
		wGaProfile.setEnabled(!directTableId);
		wGetProfiles.setEnabled(!directTableId);
		wGaCustomProfile.setEnabled(directTableId);
		wGaCustomProfileReference.setEnabled(directTableId);

	}

	// Collect profile list from the GA service for the given authentication
	// information
	public void readGaProfiles(String emailText, String passwordText, String appName) {

		String email = transMeta.environmentSubstitute(emailText);
		String pass = transMeta.environmentSubstitute(passwordText);

		AnalyticsService analyticsService = new AnalyticsService(transMeta.environmentSubstitute(appName));
		try {
			analyticsService.setUserCredentials(email, pass);
			URL q = new URL(GaInputStepMeta.GA_ACCOUNTS_URL);
			AccountFeed accountFeed = analyticsService.getFeed(q, AccountFeed.class);

			ArrayList<String> profileNames = new ArrayList<String>(5);
			profileTableIds.clear();

			for (AccountEntry entry : accountFeed.getEntries()) {
				String profileName = entry.getTableId().getValue() + " - profile: "+ entry.getTitle().getPlainText() + " [" + entry.getProperty("ga:accountName") + "]";
				profileNames.add(profileName);
				profileTableIds.put(profileName, entry.getTableId().getValue());
			}

			// put the profiles to the combo box and select first one
			wGaProfile.setItems(profileNames.toArray(new String[0]));
			if (profileNames.size() > 0) {
				wGaProfile.select(0);
			}

		} catch (AuthenticationException e) {
			e.printStackTrace();
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setText("Authentication Failure");
			mb.setMessage("Authentication failure occured when contacting Google Analytics.\nPlease verify the credentials in the email and password fields as well as your network connectivity.");
			mb.open();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setText("IO Exception");
			mb.setMessage("IO exception occured when contacting Google Analytics. Is your network connection working and allowing HTTPS connections?\n\n" + e.getMessage());
			mb.open();
		} catch (ServiceException e) {
			e.printStackTrace();
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setText("Service Exception");
			mb.setMessage("Service exception occured when contacting Google Analytics.\n\n" + e.getMessage());
			mb.open();
		}

	}

	// Collect segment list from the GA service for the given authentication
	// information
	public void readGaSegments(String emailText, String passwordText, String profileText, String appName) {

		String email = transMeta.environmentSubstitute(emailText);
		String pass = transMeta.environmentSubstitute(passwordText);

		AnalyticsService analyticsService = new AnalyticsService(transMeta.environmentSubstitute(appName));
		try {
			analyticsService.setUserCredentials(email, pass);
			URL q = new URL(GaInputStepMeta.GA_ACCOUNTS_URL);
			AccountFeed accountFeed = analyticsService.getFeed(q, AccountFeed.class);

			ArrayList<String> segmentNames = new ArrayList<String>(20);
			segmentIds.clear();

			if (!accountFeed.hasSegments()) {

			} else {
				for (Segment segment : accountFeed.getSegments()) {
					segmentNames.add(segment.getName());
					segmentIds.put(segment.getName(), segment.getId());
				}
			}

			// put the segments to the combo box and select first one
			wQuSegment.setItems(segmentNames.toArray(new String[0]));
			if (segmentNames.size() > 0) {
				wQuSegment.select(0);
			}

		} catch (AuthenticationException e) {
			e.printStackTrace();
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setText("Authentication Failure");
			mb.setMessage("Authentication failure occured when contacting Google Analytics.\nPlease verify the credentials in the email and password fields as well as your network connectivity.");
			mb.open();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setText("IO Exception");
			mb.setMessage("IO exception occured when contacting Google Analytics. Is your network connection working and allowing HTTPS connections?\n\n" + e.getMessage());
			mb.open();
		} catch (ServiceException e) {
			e.printStackTrace();
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setText("Service Exception");
			mb.setMessage("Service exception occured when contacting Google Analytics.\n\n" + e.getMessage());
			mb.open();

		}

	}

	// Collect data from the meta and place it in the dialog
	public void getData() {

		wStepname.selectAll();

		if (input.getGaAppName() != null){
			wGaAppName.setText(input.getGaAppName());
		}
		
		if (input.getGaEmail() != null) {
			wGaEmail.setText(input.getGaEmail());
		}

		if (input.getGaPassword() != null) {
			wGaPassword.setText(input.getGaPassword());
		}

		if (input.getGaProfileName() != null) {
			wGaProfile.setText(input.getGaProfileName());
			profileTableIds.clear();
			profileTableIds.put(input.getGaProfileName(), input.getGaProfileTableId());
		}

		if (input.isUseCustomTableId()){
			wCustomProfileEnabled.setSelection(true);
		}
		else{
			wCustomProfileEnabled.setSelection(false);
		}
		
		if (input.getGaCustomTableId() != null){
			wGaCustomProfile.setText(input.getGaCustomTableId());
		}
		
		if (input.getStartDate() != null) {
			wQuStartDate.setText(input.getStartDate());
		}

		if (input.getEndDate() != null) {
			wQuEndDate.setText(input.getEndDate());
		}

		if (input.getDimensions() != null) {
			wQuDimensions.setText(input.getDimensions());
		}

		if (input.getMetrics() != null) {
			wQuMetrics.setText(input.getMetrics());
		}

		if (input.getFilters() != null) {
			wQuFilters.setText(input.getFilters());
		}

		if (input.getSort() != null) {
			wQuSort.setText(input.getSort());
		}

		if (input.isUseCustomSegment()) {
			wCustomSegmentEnabled.setSelection(true);
		} else {
			wCustomSegmentEnabled.setSelection(false);
		}

		if (input.getCustomSegment() != null) {
			wQuCustomSegment.setText(input.getCustomSegment());
		}

		if (input.getSegmentName() != null) {
			wQuSegment.setText(input.getSegmentName());
			segmentIds.clear();
			segmentIds.put(input.getSegmentName(), input.getSegmentId());
		}

		if (input.getFeedField() != null) {

			for (int i = 0; i < input.getFeedField().length; i++) {

				TableItem item = wFields.table.getItem(i);

				if (input.getFeedFieldType()[i] != null) {
					item.setText(1, input.getFeedFieldType()[i]);
				}
				
				if (input.getFeedField()[i] != null) {
					item.setText(2, input.getFeedField()[i]);
				}

				if (input.getOutputField()[i] != null) {
					item.setText(3, input.getOutputField()[i]);
				}

				item.setText(4, ValueMeta.getTypeDesc(input.getOutputType()[i]));
				
				if (input.getConversionMask()[i] != null) {
					item.setText(5, input.getConversionMask()[i]);
				}
				

			}
		}

		wFields.setRowNums();
		wFields.optWidth(true);

		wLimit.setText(input.getRowLimit()+"");

		setActive();
	}

	private void cancel() {
		stepname = null;
		input.setChanged(backupChanged);
		dispose();
	}

	// let the meta know about the entered data
	private void ok() {
	
		getInfo(input);
		dispose();
	}
}
