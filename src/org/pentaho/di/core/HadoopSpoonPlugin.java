package org.pentaho.di.core;

import java.util.Arrays;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.gui.GUIOption;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.hdfs.vfs.HDFSFileProvider;

@LifecyclePlugin (id = "HadoopSpoonPlugin", name="Hadoop Spoon Plugin")
public class HadoopSpoonPlugin implements LifecycleListener, GUIOption {
	private static Class<?> PKG = HadoopSpoonPlugin.class;
	private LogChannelInterface log = new LogChannel(HadoopSpoonPlugin.class.getName());

	@Override
	public void onStart(LifeEventHandler arg0) throws LifecycleException {
		try {
			// Register HDFS as a file system type with VFS
			FileSystemManager fsm = KettleVFS.getInstance().getFileSystemManager();
			if(fsm instanceof DefaultFileSystemManager) {
				if(!Arrays.asList(fsm.getSchemes()).contains("hdfs")) {
					((DefaultFileSystemManager)fsm).addProvider("hdfs",  new HDFSFileProvider());
				}
			}
		} catch (FileSystemException e) {
			log.logError(BaseMessages.getString(PKG, "HadoopSpoonPlugin.StartupError.FailedToLoadHdfsDriver"));
		}
	}
	
	@Override
	public void onExit(LifeEventHandler arg0) throws LifecycleException {
	}
	
	public String getLabelText() {
        return null;
    }

    public Object getLastValue() {
        return null;
    }

    public DisplayType getType() {
        return null;
    }

    public void setValue(Object value) {
    }
}
