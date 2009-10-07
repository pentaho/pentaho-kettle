package org.pentaho.di.ui.spoon.trans;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.HasLogChannelInterface;
import org.pentaho.di.core.logging.Log4jKettleLayout;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogParentProvidedInterface;

public class LogBrowser {
	private StyledText	text;
	private LogParentProvidedInterface	logProvider;

	public LogBrowser(final StyledText text, final LogParentProvidedInterface logProvider) {
		this.text = text;
		this.logProvider = logProvider;
	}
	
	public void installLogSniffer() {
		
		// Create a new buffer appender to the log and capture that directly...
		//
		final AtomicInteger lastLogId = new AtomicInteger(-1);
		final AtomicBoolean busy = new AtomicBoolean(false);
		final Log4jKettleLayout logLayout = new Log4jKettleLayout(true);
		
		// Refresh the log every second or so
		//
		final Timer logRefreshTimer = new Timer();
		TimerTask timerTask = new TimerTask() {
			public void run() {
				text.getDisplay().asyncExec(new Runnable() {
					public void run() {
						HasLogChannelInterface provider = logProvider.getLogChannelProvider();
						
						if (provider!=null && !text.isDisposed() && !busy.get()) {
							busy.set(true);

							// See if we need to log any lines...
							//
							LogChannelInterface logChannel = provider.getLogChannel();
							int lastNr = CentralLogStore.getLastBufferLineNr();
							if (lastNr>lastLogId.get()) {
								String parentLogChannelId = logChannel.getLogChannelId();
								List<LoggingEvent> logLines = CentralLogStore.getLogBufferFromTo(parentLogChannelId, true, lastLogId.get(), lastNr);

								// The maximum size of the log buffer
								//
								int maxSize = Props.getInstance().getMaxNrLinesInLog()*150;

								int position = text.getSelection().x;
								StringBuffer buffer = new StringBuffer(text.getText());

								for (LoggingEvent event : logLines) {
									String line = logLayout.format(event);
									
									buffer.append(line).append(Const.CR);
									
									// Allow 25 lines overshoot each time
									// This makes it a bit more efficient
									//
									int size = buffer.length();
									if (maxSize>0 && size>maxSize) {
										int nextCr = buffer.indexOf(Const.CR, size-maxSize)+Const.CR.length();
										buffer.delete(0, nextCr);
									}
								}

								text.setText(buffer.toString());
								// int count = text.getLineCount();
								
								text.setSelection(position);
								
								lastLogId.set(lastNr);
							}

							busy.set(false);
						}
					}
				});
			}
		};
		
		// Refresh every couple of seconds!
		//
		logRefreshTimer.schedule(timerTask, 2000, 2000);
		
		// Make sure the timer goes down when the widget is disposed
		//
		text.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				logRefreshTimer.cancel();
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

	
	
}
