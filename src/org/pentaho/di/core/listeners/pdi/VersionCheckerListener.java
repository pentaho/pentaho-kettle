package org.pentaho.di.core.listeners.pdi;

import java.io.IOException;

import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.listeners.LifecycleException;
import org.pentaho.di.core.listeners.LifecycleListener;
import org.pentaho.di.core.logging.LogWriter;
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

	private LifecycleException root = null;

	private static LogWriter log = LogWriter.getInstance();

	/**
	 * Interface method: Does nothing
	 */
	public void onExit() throws LifecycleException
	{
		// Nothing Required

	}

	/**
	 * Interface method: checks the version
	 */
	public void onStart() throws LifecycleException
	{
		try
		{
			final KettleVersionCheckDataProvider dataProvider = new KettleVersionCheckDataProvider();
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
							StringBuilder sb = new StringBuilder();
							Node update = XMLHandler.getSubNode(product, "update");
							sb.append(Messages.getString("VersionListener.new.version")).append("\n\n");
							sb.append(update.getAttributes().getNamedItem("title").getTextContent())
									.append(
											"  v. "
													+ update.getAttributes().getNamedItem("version")
															.getTextContent()).append("\n\n");
							sb.append(Messages.getString("VersionListener.get.header")).append("\n");
							sb.append(XMLHandler.getTagValue(update, "downloadurl"));

							root = new LifecycleException(sb.toString(), false);
						} else
							log.logBasic("VersionChecker", "OK", new Object[] {});

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

		} catch (IOException e)
		{
			throw new LifecycleException(e, false);
		}

	}

}
