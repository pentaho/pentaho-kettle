package org.pentaho.di.core.fileinput;

import org.apache.commons.vfs2.FileObject;
import org.junit.Test;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Created by ccaspanello on 6/5/17.
 */
public class FileInputListTest {

  @Test
  public void testGetUrlStrings() throws Exception {

    String sFileA = "hdfs://myfolderA/myfileA.txt";
    String sFileB = "${schema}/myfolderB/myfileB.txt";



    VariableSpace space = new Variables();
    space.setVariable( "schema", "file://" );

    FileObject fileA = KettleVFS.getFileObject( sFileA, space );
    FileObject fileB = KettleVFS.getFileObject( sFileB, space );

    FileInputList fileInputList = new FileInputList();
    fileInputList.addFile(fileA);
    fileInputList.addFile(fileB);
    String[] result = fileInputList.getUrlStrings();
    assertEquals(2, result.length);
    assertEquals("", result[0]);
    assertEquals("", result[1]);
  }
}
