/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.ui.spoon.trans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.HasLogChannelInterface;
import org.pentaho.di.core.logging.Log4jKettleLayout;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogParentProvidedInterface;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;

public class LogBrowser {
	private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private StyledText	text;
	private LogParentProvidedInterface	logProvider;
	private List<String> childIds = new ArrayList<String>();
	private Date lastLogRegistryChange;
	private AtomicBoolean	paused;

	public LogBrowser(final StyledText text, final LogParentProvidedInterface logProvider) {
		this.text = text;
		this.logProvider = logProvider;
		this.paused = new AtomicBoolean(false);
	}
	
	public void installLogSniffer() {
		
		// Create a new buffer appender to the log and capture that directly...
		//
		final AtomicInteger lastLogId = new AtomicInteger(-1);
		final AtomicBoolean busy = new AtomicBoolean(false);
		final Log4jKettleLayout logLayout = new Log4jKettleLayout(true);
		
		final StyleRange normalLogLineStyle = new StyleRange();
		normalLogLineStyle.foreground = GUIResource.getInstance().getColorBlue();
		final StyleRange errorLogLineStyle = new StyleRange();
		errorLogLineStyle.foreground = GUIResource.getInstance().getColorRed();
		
		// Refresh the log every second or so
		//
		final Timer logRefreshTimer = new Timer();
		TimerTask timerTask = new TimerTask() {
			public void run() {
			  if (text.isDisposed()) return;
        
				text.getDisplay().asyncExec(new Runnable() {
					public void run() {
						HasLogChannelInterface provider = logProvider.getLogChannelProvider();
						
						if (provider!=null && !text.isDisposed() && !busy.get() && !paused.get() && text.isVisible()) {
							busy.set(true);

							LogChannelInterface logChannel = provider.getLogChannel();
							String parentLogChannelId = logChannel.getLogChannelId();
							LoggingRegistry registry = LoggingRegistry.getInstance();
							Date registryModDate = registry.getLastModificationTime();

							if (childIds==null || lastLogRegistryChange==null || registryModDate.compareTo(lastLogRegistryChange)>0) {
								lastLogRegistryChange = registry.getLastModificationTime();
								childIds = LoggingRegistry.getInstance().getLogChannelChildren(parentLogChannelId);
							}
							
							// See if we need to log any lines...
							//
							int lastNr = CentralLogStore.getLastBufferLineNr();
							if (lastNr>lastLogId.get()) {
								List<LoggingEvent> logLines = CentralLogStore.getLogBufferFromTo(childIds, true, lastLogId.get(), lastNr);

								// The maximum size of the log buffer
								//
								int maxSize = Props.getInstance().getMaxNrLinesInLog()*150;

								// int position = text.getSelection().x;
								// StringBuffer buffer = new StringBuffer(text.getText());

								synchronized(text) {
									
									for (int i=0;i<logLines.size();i++) {
										LoggingEvent event = logLines.get(i);
										String line = logLayout.format(event);

										int start = text.getText().length();
										int length = line.length();
										
										text.append(line);
										text.append(Const.CR);
	
										if (event.getLevel()==Level.ERROR) {
											StyleRange styleRange = new StyleRange();
											styleRange.foreground = GUIResource.getInstance().getColorRed();
											styleRange.start = start;
											styleRange.length = length;
											text.setStyleRange(styleRange);
										} else {
											StyleRange styleRange = new StyleRange();
											styleRange.foreground = GUIResource.getInstance().getColorBlue();
											styleRange.start = start;
											styleRange.length = 20;
											text.setStyleRange(styleRange);
										}
									}
								}
								
								// Erase it all in one go
								// This makes it a bit more efficient
								//
								int size = text.getText().length();
								if (maxSize>0 && size>maxSize) {
									
									int dropIndex = (text.getText().indexOf(Const.CR, size-maxSize))+Const.CR.length();
									text.replaceTextRange(0, dropIndex, "");
								}
								
								text.setSelection(text.getText().length());
																
								lastLogId.set(lastNr);
							}

							busy.set(false);
						}
					}
				});
			}
		};
		
		// Refresh every often enough
		//
		logRefreshTimer.schedule(timerTask, 1000, 1000);
		
		// Make sure the timer goes down when the widget is disposed
		//
		text.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				logRefreshTimer.cancel();
			}
		});
		
		final Menu menu = new Menu(text);
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText(BaseMessages.getString(PKG, "LogBrowser.CopySelectionToClipboard.MenuItem"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String selection = text.getSelectionText();
				if (!Const.isEmpty(selection)) {
					GUIResource.getInstance().toClipboard(selection);
				}
			}
		});
		text.setMenu(menu);

		text.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent event) {
				if (event.button==3) {
					ConstUI.displayMenu(menu, text);
				}
			}
		});
	}
	
	

	/**
	 * @return the text
	 */
	public StyledText getText() {
		return text;
	}

	public LogParentProvidedInterface getLogProvider() {
		return logProvider;
	}

	public boolean isPaused() {
		return paused.get();
	}

	public void setPaused(boolean paused) {
		this.paused.set(paused);
	}	
}
