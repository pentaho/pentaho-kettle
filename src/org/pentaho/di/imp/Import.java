/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

/**
 *   Kettle was (re-)started in March 2003
 */

package org.pentaho.di.imp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.imp.rule.ImportRuleInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.pan.CommandLineOption;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryImportFeedbackInterface;
import org.pentaho.di.repository.RepositoryImporter;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.version.BuildVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Import {
  private static Class<?>    PKG           = Import.class; // i18n

  public static final String STRING_IMPORT = "Import";

  public static void main(String[] a) throws KettleException {
    KettleEnvironment.init();

    List<String> args = new ArrayList<String>();
    for (int i = 0; i < a.length; i++) {
      if (a[i].length() > 0)
        args.add(a[i]);
    }

    StringBuffer optionRepname, optionUsername, optionPassword, optionDirname, optionLimitDir, optionFilename, optionRules, optionComment;
    StringBuffer optionReplace, optionContinueOnError, optionVersion, optionFileDir, optionNoRules;

    CommandLineOption options[] = new CommandLineOption[] {
        // Basic options
        //
        new CommandLineOption("rep", BaseMessages.getString(PKG, "Import.CmdLine.RepName"), optionRepname = new StringBuffer()),
        new CommandLineOption("user", BaseMessages.getString(PKG, "Import.CmdLine.RepUsername"), optionUsername = new StringBuffer()),
        new CommandLineOption("pass", BaseMessages.getString(PKG, "Import.CmdLine.RepPassword"), optionPassword = new StringBuffer()),
        new CommandLineOption("dir", BaseMessages.getString(PKG, "Import.CmdLine.RepDir"), optionDirname = new StringBuffer()), 
        new CommandLineOption("limitdir", BaseMessages.getString(PKG, "Import.CmdLine.LimitDir"), optionLimitDir = new StringBuffer()), 
        new CommandLineOption("file", BaseMessages.getString(PKG, "Import.CmdLine.File"), optionFilename = new StringBuffer()),
        new CommandLineOption("filedir", BaseMessages.getString(PKG, "Import.CmdLine.FileDir"), optionFileDir = new StringBuffer()),
        new CommandLineOption("rules", BaseMessages.getString(PKG, "Import.CmdLine.RulesFile"), optionRules = new StringBuffer()),
        new CommandLineOption("norules", BaseMessages.getString(PKG, "Import.CmdLine.NoRules"), optionNoRules = new StringBuffer(), true, false),
        new CommandLineOption("comment", BaseMessages.getString(PKG, "Import.CmdLine.Comment"), optionComment = new StringBuffer(), true, false),
        new CommandLineOption("replace", BaseMessages.getString(PKG, "Import.CmdLine.Replace"), optionReplace = new StringBuffer(), true, false),
        new CommandLineOption("coe", BaseMessages.getString(PKG, "Import.CmdLine.ContinueOnError"), optionContinueOnError = new StringBuffer(), true, false),
        new CommandLineOption("version", BaseMessages.getString(PKG, "Import.CmdLine.Version"), optionVersion = new StringBuffer(), true, false), 
        
        new CommandLineOption("", BaseMessages.getString(PKG, "Import.CmdLine.ExtraFiles"), new StringBuffer(), false, true, true), 
      };

    if (args.size() == 0) {
      CommandLineOption.printUsage(options);
      exitJVM(9);
    }

    final LogChannelInterface log = new LogChannel(STRING_IMPORT);

    CommandLineOption.parseArguments(args, options, log);
    
    // The arguments that are still left in args are in fact filenames that need to be imported.
    // This list is otherwise empty.
    // To that we add the normal filename option
    //
    List<String> filenames = new ArrayList<String>(args);
    if (!Const.isEmpty(optionFilename)) {
      filenames.add(optionFilename.toString());
    }

    String kettleRepname = Const.getEnvironmentVariable("KETTLE_REPOSITORY", null);
    String kettleUsername = Const.getEnvironmentVariable("KETTLE_USER", null);
    String kettlePassword = Const.getEnvironmentVariable("KETTLE_PASSWORD", null);

    if (!Const.isEmpty(kettleRepname))
      optionRepname = new StringBuffer(kettleRepname);
    if (!Const.isEmpty(kettleUsername))
      optionUsername = new StringBuffer(kettleUsername);
    if (!Const.isEmpty(kettlePassword))
      optionPassword = new StringBuffer(kettlePassword);

    if (!Const.isEmpty(optionVersion)) {
      BuildVersion buildVersion = BuildVersion.getInstance();
      log.logBasic(BaseMessages.getString(PKG, "Import.Log.KettleVersion", buildVersion.getVersion(), buildVersion.getRevision(), buildVersion.getBuildDate()));
      if (a.length == 1)
        exitJVM(6);
    }

    // Verify repository options...
    //
    if (Const.isEmpty(optionRepname)) {
      log.logError(BaseMessages.getString(PKG, "Import.Error.NoRepProvided"));
      exitJVM(1);
    }

    if (Const.isEmpty(filenames)) {
      log.logError(BaseMessages.getString(PKG, "Import.Error.NoExportFileProvided"));
      exitJVM(1);
    }

    if (Const.isEmpty(optionDirname)) {
      log.logError(BaseMessages.getString(PKG, "Import.Error.NoRepositoryDirectoryProvided"));
      exitJVM(1);
    }

    if (Const.isEmpty(optionRules) && Const.isEmpty(optionNoRules) && !"Y".equalsIgnoreCase(optionNoRules.toString())) {
      log.logError(BaseMessages.getString(PKG, "Import.Error.NoRulesFileProvided"));
      exitJVM(1);
    }


    // Load the rules file!
    //
    ImportRules importRules = new ImportRules();
    String rulesFile = optionRules.toString();

    if (!Const.isEmpty(rulesFile)) {
      try {
        Document document = XMLHandler.loadXMLFile(rulesFile);
        Node rulesNode = XMLHandler.getSubNode(document, ImportRules.XML_TAG);
        importRules.loadXML(rulesNode);
        log.logMinimal(BaseMessages.getString(PKG, "Import.Log.RulesLoaded", rulesFile, ""+importRules.getRules().size()));
        for (ImportRuleInterface rule : importRules.getRules()) {
          log.logBasic(" - "+rule.toString());
        }
      } catch (KettleException e) {
        log.logError(BaseMessages.getString(PKG, "Import.Log.ExceptionLoadingRules", rulesFile), e);
        exitJVM(7);
      }
    }
    
    // Get the list of limiting source directories
    //
    List<String> limitDirs = new ArrayList<String>();
    if (!Const.isEmpty(optionLimitDir)) {
      String[] directories = optionLimitDir.toString().split(",");
      for (String directory : directories) {
        limitDirs.add(directory);
      }
    }
    
    // Find the repository metadata...
    //
    RepositoriesMeta repsinfo = new RepositoriesMeta();
    try {
      repsinfo.readData();
    } catch (Exception e) {
      log.logError(BaseMessages.getString(PKG, "Import.Error.UnableToLoadRepositoryInformation"), e);
      exitJVM(1);
    }

    RepositoryMeta repositoryMeta = repsinfo.findRepository(optionRepname.toString());
    if (repositoryMeta == null) {
      log.logError(BaseMessages.getString(PKG, "Import.Error.RepositoryCouldNotBeFound", optionRepname.toString()));
      exitJVM(1);
    }

    if (Const.isEmpty(optionRepname)) {
      log.logError(BaseMessages.getString(PKG, "Import.Error.NoRepProvided"));
      exitJVM(1);
    }

    // Load the repository object as a plugin...
    //
    Repository repository = null;
    try {
      repository = PluginRegistry.getInstance().loadClass(RepositoryPluginType.class, repositoryMeta, Repository.class);
      repository.init(repositoryMeta);
    } catch (Exception e) {
      log.logError(BaseMessages.getString(PKG, "Import.Error.UnableToLoadOrInitializeRepository"));
      exitJVM(1);
    }
    try {
      repository.connect(optionUsername != null ? optionUsername.toString() : null, optionPassword != null ? optionPassword.toString() : null);
    } catch (Exception e) {
      log.logError(BaseMessages.getString(PKG, "Import.Error.UnableToConnectToRepository"));
      exitJVM(1);
    }

    final boolean replace = Const.isEmpty(optionReplace) ? false : ValueMeta.convertStringToBoolean(optionReplace.toString());
    final boolean continueOnError = Const.isEmpty(optionContinueOnError) ? false : ValueMeta.convertStringToBoolean(optionContinueOnError.toString());

    // Start the import!
    //
    log.logMinimal(BaseMessages.getString(PKG, "Import.Log.Starting"));

    Date start, stop;
    Calendar cal;
    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
    cal = Calendar.getInstance();
    start = cal.getTime();
    int returnCode = 0;

    try {
      RepositoryDirectoryInterface tree = repository.loadRepositoryDirectoryTree();

      RepositoryDirectoryInterface targetDirectory = tree.findDirectory(optionDirname.toString());
      if (targetDirectory == null) {
        log.logError(BaseMessages.getString(PKG, "Import.Error.UnableToFindTargetDirectoryInRepository", optionDirname.toString()));
        exitJVM(1);
      }

      // Perform the actual import
      //
      RepositoryImporter importer = new RepositoryImporter(repository, importRules, limitDirs);
      RepositoryImportFeedbackInterface feedbackInterface = new RepositoryImportFeedbackInterface() {

        @Override
        public void updateDisplay() {
        }

        @Override
        public boolean transOverwritePrompt(TransMeta transMeta) {
          return replace;
        }

        @Override
        public void showError(String title, String message, Exception e) {
          log.logError(title + " : " + message, e);
        }

        @Override
        public void setLabel(String labelText) {
          log.logBasic(labelText);
        }

        @Override
        public boolean jobOverwritePrompt(JobMeta jobMeta) {
          return replace;
        }

        @Override
        public boolean askContinueOnErrorQuestion(String title, String message) {
          return continueOnError;
        }

        @Override
        public void addLog(String line) {
          log.logBasic(line);
        }
        
        @Override
        public boolean isAskingOverwriteConfirmation() {
          return false;
        }
      };
      
      // Import files in a certain directory
      //
      importer.importAll(feedbackInterface, optionFileDir.toString(), 
          filenames.toArray(new String[filenames.size()]), 
          targetDirectory, replace, continueOnError, optionComment.toString()
         );

    } catch (Exception e) {
      log.logError(BaseMessages.getString(PKG, "Import.Error.UnexpectedErrorDuringImport"), e);
      exitJVM(2);
    }
    log.logMinimal(BaseMessages.getString(PKG, "Import.Log.Finished"));

    cal = Calendar.getInstance();
    stop = cal.getTime();
    String begin = df.format(start).toString();
    String end = df.format(stop).toString();

    log.logMinimal(BaseMessages.getString(PKG, "Import.Log.StartStop", begin, end));

    long seconds = (stop.getTime() - start.getTime()) / 1000;
    if (seconds <= 60) {
      log.logMinimal(BaseMessages.getString(PKG, "Import.Log.ProcessEndAfter", String.valueOf(seconds)));
    } else if (seconds <= 60 * 60) {
      int min = (int) (seconds / 60);
      int rem = (int) (seconds % 60);
      log.logMinimal(BaseMessages.getString(PKG, "Import.Log.ProcessEndAfterLong", String.valueOf(min), String.valueOf(rem), String.valueOf(seconds)));
    } else if (seconds <= 60 * 60 * 24) {
      int rem;
      int hour = (int) (seconds / (60 * 60));
      rem = (int) (seconds % (60 * 60));
      int min = rem / 60;
      rem = rem % 60;
      log.logMinimal(BaseMessages.getString(PKG, "Import.Log.ProcessEndAfterLonger", String.valueOf(hour), String.valueOf(min), String.valueOf(rem), String.valueOf(seconds)));
    } else {
      int rem;
      int days = (int) (seconds / (60 * 60 * 24));
      rem = (int) (seconds % (60 * 60 * 24));
      int hour = rem / (60 * 60);
      rem = rem % (60 * 60);
      int min = rem / 60;
      rem = rem % 60;
      log.logMinimal(BaseMessages.getString(PKG, "Import.Log.ProcessEndAfterLongest", String.valueOf(days), String.valueOf(hour), String.valueOf(min), String.valueOf(rem), String.valueOf(seconds)));
    }

    exitJVM(returnCode);

  }

  /**
   * Parse an argument as an integer.
   * 
   * @param option
   *          Command Line Option to parse argument of
   * @param def
   *          Default if the argument is not set
   * @return The parsed argument or the default if the argument was not
   *         specified
   * @throws KettleException
   *           Error parsing provided argument as an integer
   */
  protected static int parseIntArgument(final CommandLineOption option, final int def) throws KettleException {
    if (!Const.isEmpty(option.getArgument())) {
      try {
        return Integer.parseInt(option.getArgument().toString());
      } catch (NumberFormatException ex) {
        throw new KettleException(BaseMessages.getString(PKG, "Import.Error.InvalidNumberArgument", option.getOption(), option.getArgument())); //$NON-NLS-1$
      }
    }
    return def;
  }

  private static final void exitJVM(int status) {
    // Close the open appenders...
    //
    LogWriter.getInstance().close();

    System.exit(status);
  }
}