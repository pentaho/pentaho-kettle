package org.pentaho.di.core.plugins;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.core.exception.KettleFileException;
import org.scannotation.AnnotationDB;

public class JarFileCache {

  private static JarFileCache cache;

  private final Map<PluginFolderInterface, FileObject[]> folderMap;

  private final Map<FileObject, AnnotationDB> annotationMap;

  private JarFileCache() {
    annotationMap = new HashMap<FileObject, AnnotationDB>();
    folderMap = new HashMap<PluginFolderInterface, FileObject[]>();
  }

  public static JarFileCache getInstance() {
    if (cache == null) {
      cache = new JarFileCache();
    }
    return cache;
  }

  public AnnotationDB getAnnotationDB(FileObject fileObject) throws FileSystemException, IOException {
    AnnotationDB result = annotationMap.get(fileObject);
    if (result == null) {
      result = new AnnotationDB();
      result.scanArchives(fileObject.getURL());
      annotationMap.put(fileObject, result);
    }
    return result;
  }

  public FileObject[] getFileObjects(PluginFolderInterface pluginFolderInterface) throws KettleFileException {
    FileObject[] result = folderMap.get(pluginFolderInterface);
    if (result == null) {
      result = pluginFolderInterface.findJarFiles();
      folderMap.put(pluginFolderInterface, result);
    }
    return result;
  }

  public void clear() {
    annotationMap.clear();
    folderMap.clear();
  }
}
