/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.hadoop.mapreduce.test;

import java.io.File;

import junit.framework.TestCase;

import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.ToolRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.util.FileUtil;
import org.pentaho.weblogs.WebLogs;

/**
 * @author sflatley
 */
public class WebLogsLocalTestFIXME extends TestCase {
        
    private static final String baseDir = "./";
    private static final String input = "junit/weblogs/input/access.log";
    private static final String outputFolder = "junit/weblogs/output";
    
    public static void test1() throws Exception {
        
        String args[] = {input, outputFolder};
        
        try { 
            beforeTest();  //  See comment by the annotation below.
            
            KettleEnvironment.init();
            JobConf conf = new JobConf();
            //TransExecutionConfiguration transExecConfig = new TransExecutionConfiguration();
            //TransMeta transMeta = new TransMeta(baseDir+"/test-res/weblogs-reducer.kjb");
            //TransConfiguration transConfig = new TransConfiguration(transMeta, transExecConfig);
            //conf.set("transformation-reduce-xml", transConfig.getXML());
            //conf.set("transformation-reduce-output-stepname", "Output");
            //conf.set("transformation-reduce-input-stepname", "Injector");
            
            ToolRunner.run(conf, new WebLogs(), args);
        }
        catch (KettleException ke) {
            fail(ke.getMessage());
            ke.printStackTrace();
        }
        catch (Exception e ) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }
    
    //  @Before - this does not seem to work.  This method
    //  does not get called before an @Test method.
    public static void beforeTest() { 
      FileUtil.deleteDir(new File(baseDir+outputFolder));
    } 
}