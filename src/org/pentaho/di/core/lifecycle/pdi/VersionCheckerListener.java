package org.pentaho.di.core.lifecycle.pdi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.VFS;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifeEventInfo;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.versionchecker.IVersionCheckErrorHandler;
import org.pentaho.versionchecker.IVersionCheckResultHandler;
import org.pentaho.versionchecker.VersionChecker;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A specialized listener calls the Pentaho server and checks the latest version
 * of PDI. If the user is running the latest version, nothing happens.
 * Otherwise, an information dialog box is displayed with the current version
 * information.
 * 
 * @author Alex Silva
 * 
 */
public class VersionCheckerListener implements LifecycleListener
{
	private LifecycleException root;

	private static LogWriter log = LogWriter.getInstance();
	
	public static final String VERSION_CHECKER = "Version Checker";

	/**
	 * Interface method: checks the version
	 */
	public void onStart(LifeEventHandler handler) throws LifecycleException
	{
		try
		{
			final KettleVersionCheckDataProvider dataProvider = new KettleVersionCheckDataProvider();

			final StringBuilder message = new StringBuilder();
			final StringBuilder version = new StringBuilder(), url = new StringBuilder();
			VersionChecker vc = new VersionChecker();
			vc.setDataProvider(dataProvider);
			vc.addResultHandler(new IVersionCheckResultHandler()
			{
				public void processResults(String result)
				{
					try
					{
						Document doc = XMLHandler.loadXMLString(result);
						Node vers = doc.getDocumentElement();
						Node product = XMLHandler.getNodeWithAttributeValue(vers, "product", "id",
								dataProvider.getApplicationID());

						if (product != null)
						{
							Node update = XMLHandler.getSubNode(product, "update");

							try
							{
								FileObject html = KettleVFS
										.getFileObject("docs/English/welcome/kettle_version_check.html"); // TODO:
								// other
								// langs

								String line = null;

								version.append(update.getAttributes().getNamedItem("version")
										.getTextContent());
								url.append(XMLHandler.getTagValue(update, "downloadurl"));

								BufferedReader myInput = new BufferedReader(new InputStreamReader(html
										.getURL().openStream()));
								while ((line = myInput.readLine()) != null)
									message.append(line);

							} catch (IOException e)
							{
								root = new LifecycleException(e, false);
							}

						} else
						{
							log.logBasic("VersionChecker", "OK", new Object[] {});
						}

					} catch (KettleXMLException e)
					{
						log.logDebug("XML ERROR", e.getMessage(), new Object[] {});
						root = new LifecycleException(e, false);
					}

				}
			});
			vc.addErrorHandler(new IVersionCheckErrorHandler()
			{
				public void handleException(Exception e)
				{
					root = new LifecycleException(e, false);
				}
			});

			vc.performCheck(false);

			if (root != null)
				throw root;

			if (message.length() > 0)
			{
				String smsg = message.toString().replace("$url$", url).replace("$version$", version);

				try
				{
					// the logo and css images/pentaho_logo.png
					FileObject root = KettleVFS.getFileObject("docs/English/welcome");
					FileObject logo = VFS.getManager().resolveFile(root,"images/pentaho_logo.png");
					FileObject css = VFS.getManager().resolveFile(root,"images/styles-new.css");
					smsg = smsg.replace("$pentaho_logo$",logo.getURL().toURI().toString()).replace("$css$",css.getURL().toURI().toString());
					
				} catch (Exception e)
				{
					e.printStackTrace();
				}

				LifeEventInfo info = new LifeEventInfo();
				info.setName(VERSION_CHECKER);
				info.setMessage(smsg);
				info.setHint(LifeEventInfo.Hint.DISPLAY_BROWSER);
				info.setState(LifeEventInfo.State.SUCCESS);
				handler.consume(info);
			}

		} catch (IOException e)
		{
			throw new LifecycleException(e, false);
		}

	}

	public void onExit(LifeEventHandler handler) throws LifecycleException
	{
		// nothing

	}

}
