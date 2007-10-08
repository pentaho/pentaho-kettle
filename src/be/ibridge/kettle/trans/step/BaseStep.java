/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

/*
 * Created on 9-apr-2003
 *
 */

package be.ibridge.kettle.trans.step;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.KettleVariables;
import be.ibridge.kettle.core.LocalVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.RowSet;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleRowException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.exception.KettleStepLoaderException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.StepPlugin;
import be.ibridge.kettle.trans.StepPluginMeta;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.XMLInputSax.XMLInputSaxMeta;
import be.ibridge.kettle.trans.step.abort.AbortMeta;
import be.ibridge.kettle.trans.step.accessoutput.AccessOutputMeta;
import be.ibridge.kettle.trans.step.addsequence.AddSequenceMeta;
import be.ibridge.kettle.trans.step.addxml.AddXMLMeta;
import be.ibridge.kettle.trans.step.aggregaterows.AggregateRowsMeta;
import be.ibridge.kettle.trans.step.blockingstep.BlockingStepMeta;
import be.ibridge.kettle.trans.step.calculator.CalculatorMeta;
import be.ibridge.kettle.trans.step.combinationlookup.CombinationLookupMeta;
import be.ibridge.kettle.trans.step.constant.ConstantMeta;
import be.ibridge.kettle.trans.step.cubeinput.CubeInputMeta;
import be.ibridge.kettle.trans.step.cubeoutput.CubeOutputMeta;
import be.ibridge.kettle.trans.step.databasejoin.DatabaseJoinMeta;
import be.ibridge.kettle.trans.step.databaselookup.DatabaseLookupMeta;
import be.ibridge.kettle.trans.step.dbproc.DBProcMeta;
import be.ibridge.kettle.trans.step.delete.DeleteMeta;
import be.ibridge.kettle.trans.step.denormaliser.DenormaliserMeta;
import be.ibridge.kettle.trans.step.dimensionlookup.DimensionLookupMeta;
import be.ibridge.kettle.trans.step.dummytrans.DummyTransMeta;
import be.ibridge.kettle.trans.step.excelinput.ExcelInputMeta;
import be.ibridge.kettle.trans.step.exceloutput.ExcelOutputMeta;
import be.ibridge.kettle.trans.step.fieldsplitter.FieldSplitterMeta;
import be.ibridge.kettle.trans.step.filesfromresult.FilesFromResultMeta;
import be.ibridge.kettle.trans.step.filestoresult.FilesToResultMeta;
import be.ibridge.kettle.trans.step.filterrows.FilterRowsMeta;
import be.ibridge.kettle.trans.step.flattener.FlattenerMeta;
import be.ibridge.kettle.trans.step.formula.FormulaMeta;
import be.ibridge.kettle.trans.step.getfilenames.GetFileNamesMeta;
import be.ibridge.kettle.trans.step.getvariable.GetVariableMeta;
import be.ibridge.kettle.trans.step.groupby.GroupByMeta;
import be.ibridge.kettle.trans.step.http.HTTPMeta;
import be.ibridge.kettle.trans.step.injector.InjectorMeta;
import be.ibridge.kettle.trans.step.insertupdate.InsertUpdateMeta;
import be.ibridge.kettle.trans.step.joinrows.JoinRowsMeta;
import be.ibridge.kettle.trans.step.mapping.MappingMeta;
import be.ibridge.kettle.trans.step.mappinginput.MappingInputMeta;
import be.ibridge.kettle.trans.step.mappingoutput.MappingOutputMeta;
import be.ibridge.kettle.trans.step.mergejoin.MergeJoinMeta;
import be.ibridge.kettle.trans.step.mergerows.MergeRowsMeta;
import be.ibridge.kettle.trans.step.normaliser.NormaliserMeta;
import be.ibridge.kettle.trans.step.nullif.NullIfMeta;
import be.ibridge.kettle.trans.step.rowgenerator.RowGeneratorMeta;
import be.ibridge.kettle.trans.step.rowsfromresult.RowsFromResultMeta;
import be.ibridge.kettle.trans.step.rowstoresult.RowsToResultMeta;
import be.ibridge.kettle.trans.step.scriptvalues.ScriptValuesMeta;
import be.ibridge.kettle.trans.step.scriptvalues_mod.ScriptValuesMetaMod;
import be.ibridge.kettle.trans.step.selectvalues.SelectValuesMeta;
import be.ibridge.kettle.trans.step.setvariable.SetVariableMeta;
import be.ibridge.kettle.trans.step.socketreader.SocketReaderMeta;
import be.ibridge.kettle.trans.step.socketwriter.SocketWriterMeta;
import be.ibridge.kettle.trans.step.sortedmerge.SortedMergeMeta;
import be.ibridge.kettle.trans.step.sortrows.SortRowsMeta;
import be.ibridge.kettle.trans.step.sql.ExecSQLMeta;
import be.ibridge.kettle.trans.step.streamlookup.StreamLookupMeta;
import be.ibridge.kettle.trans.step.systemdata.SystemDataMeta;
import be.ibridge.kettle.trans.step.tableinput.TableInputMeta;
import be.ibridge.kettle.trans.step.tableoutput.TableOutputMeta;
import be.ibridge.kettle.trans.step.textfileinput.TextFileInputMeta;
import be.ibridge.kettle.trans.step.textfileoutput.TextFileOutputMeta;
import be.ibridge.kettle.trans.step.uniquerows.UniqueRowsMeta;
import be.ibridge.kettle.trans.step.update.UpdateMeta;
import be.ibridge.kettle.trans.step.valuemapper.ValueMapperMeta;
import be.ibridge.kettle.trans.step.webservices.WebServiceMeta;
import be.ibridge.kettle.trans.step.xbaseinput.XBaseInputMeta;
import be.ibridge.kettle.trans.step.xmlinput.XMLInputMeta;
import be.ibridge.kettle.trans.step.xmloutput.XMLOutputMeta;
import be.ibridge.kettle.trans.step.orabulkloader.OraBulkLoaderMeta;
import be.ibridge.kettle.trans.step.accessinput.AccessInputMeta;
import be.ibridge.kettle.trans.step.regexeval.RegexEvalMeta;
import be.ibridge.kettle.trans.step.xsdvalidator.XsdValidatorMeta;
import be.ibridge.kettle.trans.step.xslt.XsltMeta;
import be.ibridge.kettle.trans.step.getfilesrowscount.GetFilesRowsCountMeta;
import be.ibridge.kettle.trans.step.sqlfileoutput.SQLFileOutputMeta;


public class BaseStep extends Thread
{
    public static final String CATEGORY_INPUT          = Messages.getString("BaseStep.Category.Input");
    public static final String CATEGORY_OUTPUT         = Messages.getString("BaseStep.Category.Output");
    public static final String CATEGORY_TRANSFORM      = Messages.getString("BaseStep.Category.Transform");
    public static final String CATEGORY_SCRIPTING      = Messages.getString("BaseStep.Category.Scripting");
    public static final String CATEGORY_LOOKUP         = Messages.getString("BaseStep.Category.Lookup");
    public static final String CATEGORY_JOINS          = Messages.getString("BaseStep.Category.Joins");
    public static final String CATEGORY_DATA_WAREHOUSE = Messages.getString("BaseStep.Category.DataWarehouse");
    public static final String CATEGORY_JOB            = Messages.getString("BaseStep.Category.Job");
    public static final String CATEGORY_MAPPING        = Messages.getString("BaseStep.Category.Mapping");
    public static final String CATEGORY_INLINE         = Messages.getString("BaseStep.Category.Inline");
    public static final String CATEGORY_EXPERIMENTAL   = Messages.getString("BaseStep.Category.Experimental");
    public static final String CATEGORY_DEPRECATED     = Messages.getString("BaseStep.Category.Deprecated");

    protected static LocalVariables localVariables = LocalVariables.getInstance();

    public static final StepPluginMeta[] steps =
        {
            new StepPluginMeta(TextFileInputMeta.class, "TextFileInput", Messages.getString("BaseStep.TypeLongDesc.TextFileInput"), Messages
                    .getString("BaseStep.TypeTooltipDesc.TextInputFile", Const.CR), "TFI.png", CATEGORY_INPUT),
            new StepPluginMeta(TextFileOutputMeta.class, "TextFileOutput", Messages.getString("BaseStep.TypeLongDesc.TextFileOutput"), Messages
                    .getString("BaseStep.TypeTooltipDesc.TextOutputFile"), "TFO.png", CATEGORY_OUTPUT),
            new StepPluginMeta(TableInputMeta.class, "TableInput", Messages.getString("BaseStep.TypeLongDesc.TableInput"), Messages
                    .getString("BaseStep.TypeTooltipDesc.TableInput"), "TIP.png", CATEGORY_INPUT),
            new StepPluginMeta(TableOutputMeta.class, "TableOutput", Messages.getString("BaseStep.TypeLongDesc.Output"), Messages
                    .getString("BaseStep.TypeTooltipDesc.TableOutput"), "TOP.png", CATEGORY_OUTPUT),
            new StepPluginMeta(SelectValuesMeta.class, "SelectValues", Messages.getString("BaseStep.TypeLongDesc.SelectValues"), Messages.getString(
                    "BaseStep.TypeTooltipDesc.SelectValues", Const.CR), "SEL.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(FilterRowsMeta.class, "FilterRows", Messages.getString("BaseStep.TypeLongDesc.FilterRows"), Messages
                    .getString("BaseStep.TypeTooltipDesc.FilterRows"), "FLT.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(DatabaseLookupMeta.class, "DBLookup", Messages.getString("BaseStep.TypeLongDesc.DatabaseLookup"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Databaselookup"), "DLU.png", CATEGORY_LOOKUP),
            new StepPluginMeta(SortRowsMeta.class, "SortRows", Messages.getString("BaseStep.TypeLongDesc.SortRows"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Sortrows"), "SRT.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(StreamLookupMeta.class, "StreamLookup", Messages.getString("BaseStep.TypeLongDesc.StreamLookup"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Streamlookup"), "SLU.png", CATEGORY_LOOKUP),
            new StepPluginMeta(AddSequenceMeta.class, "Sequence", Messages.getString("BaseStep.TypeLongDesc.AddSequence"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Addsequence"), "SEQ.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(DimensionLookupMeta.class, "DimensionLookup", Messages.getString("BaseStep.TypeLongDesc.DimensionUpdate"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Dimensionupdate", Const.CR), "DIM.png", CATEGORY_DATA_WAREHOUSE),
            new StepPluginMeta(CombinationLookupMeta.class, "CombinationLookup", Messages.getString("BaseStep.TypeLongDesc.CombinationUpdate"),
                    Messages.getString("BaseStep.TypeTooltipDesc.CombinationUpdate", Const.CR, Const.CR), "CMB.png", CATEGORY_DATA_WAREHOUSE),
            new StepPluginMeta(DummyTransMeta.class, "Dummy", Messages.getString("BaseStep.TypeLongDesc.Dummy"), Messages.getString(
                    "BaseStep.TypeTooltipDesc.Dummy", Const.CR), "DUM.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(JoinRowsMeta.class, "JoinRows", Messages.getString("BaseStep.TypeLongDesc.JoinRows"), Messages.getString(
                    "BaseStep.TypeTooltipDesc.JoinRows", Const.CR), "JRW.png", CATEGORY_JOINS),
            new StepPluginMeta(AggregateRowsMeta.class, "AggregateRows", Messages.getString("BaseStep.TypeLongDesc.AggregateRows"), Messages
                    .getString("BaseStep.TypeTooltipDesc.AggregateRows", Const.CR), "AGG.png", CATEGORY_DEPRECATED),
            new StepPluginMeta(SystemDataMeta.class, "SystemInfo", Messages.getString("BaseStep.TypeLongDesc.GetSystemInfo"), Messages
                    .getString("BaseStep.TypeTooltipDesc.GetSystemInfo"), "SYS.png", CATEGORY_INPUT),
            new StepPluginMeta(RowGeneratorMeta.class, "RowGenerator", Messages.getString("BaseStep.TypeLongDesc.GenerateRows"), Messages
                    .getString("BaseStep.TypeTooltipDesc.GenerateRows"), "GEN.png", CATEGORY_INPUT),
            new StepPluginMeta(ScriptValuesMeta.class, "ScriptValue", Messages.getString("BaseStep.TypeLongDesc.JavaScript"), Messages
                    .getString("BaseStep.TypeTooltipDesc.JavaScriptValue"), "SCR.png", CATEGORY_SCRIPTING),
            new StepPluginMeta(ScriptValuesMetaMod.class, "ScriptValueMod", Messages.getString("BaseStep.TypeLongDesc.JavaScriptMod"), Messages
                    .getString("BaseStep.TypeTooltipDesc.JavaScriptValueMod"), "SCR_mod.png", CATEGORY_SCRIPTING),
            new StepPluginMeta(DBProcMeta.class, "DBProc", Messages.getString("BaseStep.TypeLongDesc.CallDBProcedure"), Messages
                    .getString("BaseStep.TypeTooltipDesc.CallDBProcedure"), "PRC.png", CATEGORY_LOOKUP),
            new StepPluginMeta(InsertUpdateMeta.class, "InsertUpdate", Messages.getString("BaseStep.TypeLongDesc.InsertOrUpdate"), Messages
                    .getString("BaseStep.TypeTooltipDesc.InsertOrUpdate"), "INU.png", CATEGORY_OUTPUT),
            new StepPluginMeta(UpdateMeta.class, "Update", Messages.getString("BaseStep.TypeLongDesc.Update"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Update"), "UPD.png", CATEGORY_OUTPUT),
            new StepPluginMeta(DeleteMeta.class, "Delete", Messages.getString("BaseStep.TypeLongDesc.Delete"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Delete"), "Delete.png", CATEGORY_OUTPUT),
            new StepPluginMeta(NormaliserMeta.class, "Normaliser", Messages.getString("BaseStep.TypeLongDesc.RowNormaliser"), Messages
                    .getString("BaseStep.TypeTooltipDesc.RowNormaliser"), "NRM.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(FieldSplitterMeta.class, "FieldSplitter", Messages.getString("BaseStep.TypeLongDesc.SplitFields"), Messages
                    .getString("BaseStep.TypeTooltipDesc.SplitFields"), "SPL.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(UniqueRowsMeta.class, "Unique", Messages.getString("BaseStep.TypeLongDesc.UniqueRows"), Messages.getString(
                    "BaseStep.TypeTooltipDesc.Uniquerows", Const.CR, Const.CR), "UNQ.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(GroupByMeta.class, "GroupBy", Messages.getString("BaseStep.TypeLongDesc.GroupBy"), Messages.getString(
                    "BaseStep.TypeTooltipDesc.Groupby", Const.CR, Const.CR), "GRP.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(RowsFromResultMeta.class, "RowsFromResult", Messages.getString("BaseStep.TypeLongDesc.GetRows"), Messages
                    .getString("BaseStep.TypeTooltipDesc.GetRowsFromResult"), "FCH.png", CATEGORY_JOB),
            new StepPluginMeta(RowsToResultMeta.class, "RowsToResult", Messages.getString("BaseStep.TypeLongDesc.CopyRows"), Messages.getString(
                    "BaseStep.TypeTooltipDesc.CopyRowsToResult", Const.CR), "TCH.png", CATEGORY_JOB),
            new StepPluginMeta(CubeInputMeta.class, "CubeInput", Messages.getString("BaseStep.TypeLongDesc.CubeInput"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Cubeinput"), "CIP.png", CATEGORY_INPUT),
            new StepPluginMeta(CubeOutputMeta.class, "CubeOutput", Messages.getString("BaseStep.TypeLongDesc.CubeOutput"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Cubeoutput"), "COP.png", CATEGORY_OUTPUT),
            new StepPluginMeta(DatabaseJoinMeta.class, "DBJoin", Messages.getString("BaseStep.TypeLongDesc.DatabaseJoin"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Databasejoin"), "DBJ.png", CATEGORY_JOINS),
            new StepPluginMeta(XBaseInputMeta.class, "XBaseInput", Messages.getString("BaseStep.TypeLongDesc.XBaseInput"), Messages
                    .getString("BaseStep.TypeTooltipDesc.XBaseinput"), "XBI.png", CATEGORY_INPUT),
            new StepPluginMeta(ExcelInputMeta.class, "ExcelInput", Messages.getString("BaseStep.TypeLongDesc.ExcelInput"), Messages
                    .getString("BaseStep.TypeTooltipDesc.ExcelInput"), "XLI.png", CATEGORY_INPUT),
            new StepPluginMeta(NullIfMeta.class, "NullIf", Messages.getString("BaseStep.TypeLongDesc.NullIf"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Nullif"), "NUI.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(CalculatorMeta.class, "Calculator", Messages.getString("BaseStep.TypeLongDesc.Caculator"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Calculator"), "CLC.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(ExecSQLMeta.class, "ExecSQL", Messages.getString("BaseStep.TypeLongDesc.ExcuteSQL"), Messages
                    .getString("BaseStep.TypeTooltipDesc.ExecuteSQL"), "SQL.png", CATEGORY_SCRIPTING),
            new StepPluginMeta(MappingMeta.class, "Mapping", Messages.getString("BaseStep.TypeLongDesc.MappingSubTransformation"), Messages
                    .getString("BaseStep.TypeTooltipDesc.MappingSubTransformation"), "MAP.png", CATEGORY_MAPPING),
            new StepPluginMeta(MappingInputMeta.class, "MappingInput", Messages.getString("BaseStep.TypeLongDesc.MappingInput"), Messages
                    .getString("BaseStep.TypeTooltipDesc.MappingInputSpecification"), "MPI.png", CATEGORY_MAPPING),
            new StepPluginMeta(MappingOutputMeta.class, "MappingOutput", Messages.getString("BaseStep.TypeLongDesc.MappingOutput"), Messages
                    .getString("BaseStep.TypeTooltipDesc.MappingOutputSpecification"), "MPO.png", CATEGORY_MAPPING),
            new StepPluginMeta(XMLInputMeta.class, "XMLInput", Messages.getString("BaseStep.TypeLongDesc.XMLInput"), Messages
                    .getString("BaseStep.TypeTooltipDesc.XMLInput"), "XIN.png", CATEGORY_INPUT),
            new StepPluginMeta(XMLInputSaxMeta.class, "XMLInputSax", Messages.getString("BaseStep.TypeLongDesc.XMLInputSax"), Messages
                    .getString("BaseStep.TypeTooltipDesc.XMLInputSax"), "XIS.png", CATEGORY_INPUT),
            new StepPluginMeta(XMLOutputMeta.class, "XMLOutput", Messages.getString("BaseStep.TypeLongDesc.XMLOutput"), Messages
                    .getString("BaseStep.TypeTooltipDesc.XMLOutput"), "XOU.png", CATEGORY_OUTPUT),
            new StepPluginMeta(AddXMLMeta.class, "AddXML", Messages.getString("BaseStep.TypeLongDesc.AddXML"), Messages
                    .getString("BaseStep.TypeTooltipDesc.AddXML"), "XIN.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(MergeRowsMeta.class, "MergeRows", Messages.getString("BaseStep.TypeLongDesc.MergeRows"), Messages
                    .getString("BaseStep.TypeTooltipDesc.MergeRows"), "MRG.png", CATEGORY_JOINS),
            new StepPluginMeta(ConstantMeta.class, "Constant", Messages.getString("BaseStep.TypeLongDesc.AddConstants"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Addconstants"), "CST.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(DenormaliserMeta.class, "Denormaliser", Messages.getString("BaseStep.TypeLongDesc.RowDenormaliser"), Messages
                    .getString("BaseStep.TypeTooltipDesc.RowsDenormalises", Const.CR), "UNP.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(FlattenerMeta.class, new String[] { "Flattener", "Flatterner" }, Messages
                    .getString("BaseStep.TypeLongDesc.RowFalttener"), Messages.getString("BaseStep.TypeTooltipDesc.Rowflattener"), "FLA.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(ValueMapperMeta.class, "ValueMapper", Messages.getString("BaseStep.TypeLongDesc.ValueMapper"), Messages
                    .getString("BaseStep.TypeTooltipDesc.MapValues"), "VMP.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(SetVariableMeta.class, "SetVariable", Messages.getString("BaseStep.TypeLongDesc.SetVariable"), Messages
                    .getString("BaseStep.TypeTooltipDesc.SetVariable"), "SVA.png", CATEGORY_JOB),
            new StepPluginMeta(GetVariableMeta.class, "GetVariable", Messages.getString("BaseStep.TypeLongDesc.GetVariable"), Messages
                    .getString("BaseStep.TypeTooltipDesc.GetVariable"), "GVA.png", CATEGORY_JOB),
            new StepPluginMeta(GetFileNamesMeta.class, "GetFileNames", Messages.getString("BaseStep.TypeLongDesc.GetFileNames"), Messages
                    .getString("BaseStep.TypeTooltipDesc.GetFileNames"), "GFN.png", CATEGORY_INPUT),
            new StepPluginMeta(FilesFromResultMeta.class, "FilesFromResult", Messages.getString("BaseStep.TypeLongDesc.FilesFromResult"), Messages
                    .getString("BaseStep.TypeTooltipDesc.FilesFromResult"), "FFR.png", CATEGORY_JOB),
            new StepPluginMeta(FilesToResultMeta.class, "FilesToResult", Messages.getString("BaseStep.TypeLongDesc.FilesToResult"), Messages
                    .getString("BaseStep.TypeTooltipDesc.FilesToResult"), "FTR.png", CATEGORY_JOB),
            new StepPluginMeta(BlockingStepMeta.class, "BlockingStep", Messages.getString("BaseStep.TypeLongDesc.BlockingStep"), Messages
                    .getString("BaseStep.TypeTooltipDesc.BlockingStep"), "BLK.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(InjectorMeta.class, "Injector", Messages.getString("BaseStep.TypeLongDesc.Injector"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Injector"), "INJ.png", CATEGORY_INLINE),
            new StepPluginMeta(ExcelOutputMeta.class, "ExcelOutput", Messages.getString("BaseStep.TypeLongDesc.ExcelOutput"), Messages
                    .getString("BaseStep.TypeTooltipDesc.ExcelOutput"), "XLO.png", CATEGORY_OUTPUT),
            new StepPluginMeta(AccessOutputMeta.class, "AccessOutput", Messages.getString("BaseStep.TypeLongDesc.AccessOutput"), Messages
                    .getString("BaseStep.TypeTooltipDesc.AccessOutput"), "ACO.png", CATEGORY_OUTPUT),
            new StepPluginMeta(SortedMergeMeta.class, "SortedMerge", Messages.getString("BaseStep.TypeLongDesc.SortedMerge"), Messages
                    .getString("BaseStep.TypeTooltipDesc.SortedMerge"), "SMG.png", CATEGORY_JOINS),
            new StepPluginMeta(MergeJoinMeta.class, "MergeJoin", Messages.getString("BaseStep.TypeLongDesc.MergeJoin"), Messages
                    .getString("BaseStep.TypeTooltipDesc.MergeJoin"), "MJOIN.png", CATEGORY_JOINS),
            new StepPluginMeta(SocketReaderMeta.class, "SocketReader", Messages.getString("BaseStep.TypeLongDesc.SocketReader"), Messages
                    .getString("BaseStep.TypeTooltipDesc.SocketReader"), "SKR.png", CATEGORY_INLINE),
            new StepPluginMeta(SocketWriterMeta.class, "SocketWriter", Messages.getString("BaseStep.TypeLongDesc.SocketWriter"), Messages
                    .getString("BaseStep.TypeTooltipDesc.SocketWriter"), "SKW.png", CATEGORY_INLINE),
            new StepPluginMeta(HTTPMeta.class, "HTTP", Messages.getString("BaseStep.TypeLongDesc.HTTP"), Messages
                    .getString("BaseStep.TypeTooltipDesc.HTTP"), "WEB.png", CATEGORY_LOOKUP),
            new StepPluginMeta(WebServiceMeta.class, "WebServiceLookup", Messages.getString("BaseStep.TypeLongDesc.WebServiceLookup"), Messages
                    .getString("BaseStep.TypeTooltipDesc.WebServiceLookup"), "WSL.png", CATEGORY_LOOKUP),
            new StepPluginMeta(FormulaMeta.class, "Formula", Messages.getString("BaseStep.TypeLongDesc.Formula"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Formula"), "FRM.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(AbortMeta.class, "Abort", Messages.getString("BaseStep.TypeLongDesc.Abort"), Messages
                    .getString("BaseStep.TypeTooltipDesc.Abort"), "ABR.png", CATEGORY_TRANSFORM),
            new StepPluginMeta(OraBulkLoaderMeta.class, "OraBulkLoader", Messages.getString("BaseStep.TypeLongDesc.OraBulkLoader"), Messages
                    .getString("BaseStep.TypeTooltipDesc.OraBulkLoader"), "OBL.png", CATEGORY_TRANSFORM),        
	
			new StepPluginMeta(AccessInputMeta.class, "AccessInput", Messages.getString("BaseStep.TypeLongDesc.AccessInput"), Messages
					.getString("BaseStep.TypeTooltipDesc.AccessInput"), "ACI.png", CATEGORY_INPUT), 
	

			new StepPluginMeta(RegexEvalMeta.class, "RegexEval", Messages.getString("BaseStep.TypeLongDesc.RegexEval"), Messages
			.getString("BaseStep.TypeTooltipDesc.RegexEval"), "RGE.png", CATEGORY_SCRIPTING), 

			new StepPluginMeta(XsdValidatorMeta.class, "XsdValodator", Messages.getString("BaseStep.TypeLongDesc.XsdValidator"), Messages
			.getString("BaseStep.TypeTooltipDesc.XsdValidator"), "XSD.png", CATEGORY_TRANSFORM), 

			new StepPluginMeta(XsltMeta.class, "Xslt", Messages.getString("BaseStep.TypeLongDesc.Xslt"), Messages
			.getString("BaseStep.TypeTooltipDesc.Xslt"), "XSLT.png", CATEGORY_TRANSFORM), 
			
			new StepPluginMeta(GetFilesRowsCountMeta.class, "GetFilesRowsCount", Messages.getString("BaseStep.TypeLongDesc.GetFilesRowsCount"), Messages
					.getString("BaseStep.TypeTooltipDesc.GetFilesRowsCount"), "FRC.png", CATEGORY_INPUT), 
			new StepPluginMeta(SQLFileOutputMeta.class, "SQLFileOutput", Messages.getString("BaseStep.TypeLongDesc.SQLFileOutput"), Messages
					.getString("BaseStep.TypeTooltipDesc.SQLFileOutput"), "SFO.png", CATEGORY_EXPERIMENTAL),

        };

    public static final String category_order[] =
        {
            CATEGORY_INPUT,
            CATEGORY_OUTPUT,
            CATEGORY_LOOKUP,
            CATEGORY_TRANSFORM,
            CATEGORY_JOINS,
            CATEGORY_SCRIPTING,
            CATEGORY_DATA_WAREHOUSE,
            CATEGORY_MAPPING,
            CATEGORY_JOB,
            CATEGORY_INLINE,
            CATEGORY_EXPERIMENTAL,
            CATEGORY_DEPRECATED,
        };

    private static final int             MIN_PRIORITY            = 1;
    private static final int             LOW_PRIORITY            = 3;
    private static final int             NORMAL_PRIORITY         = 5;
    private static final int             HIGH_PRIORITY           = 7;
    private static final int             MAX_PRIORITY            = 10;

    public static final String[]         statusDesc              = { Messages.getString("BaseStep.status.Empty"),
            Messages.getString("BaseStep.status.Init"), Messages.getString("BaseStep.status.Running"), Messages.getString("BaseStep.status.Idle"),
            Messages.getString("BaseStep.status.Finished"), Messages.getString("BaseStep.status.Stopped"),
            Messages.getString("BaseStep.status.Disposed"), Messages.getString("BaseStep.status.Halted"), };

    private TransMeta                    transMeta;

    private StepMeta                     stepMeta;

    private String                       stepname;

    protected LogWriter                  log;

    private Trans                        trans;

    public ArrayList                     previewBuffer;

    public int                           previewSize;

    /**  nr of lines read from previous step(s) */
    public long                          linesRead;
    /** nr of lines written to next step(s) */
    public long                          linesWritten;
    /** nr of lines read from file or database */
    public long                          linesInput;
    /** nr of lines written to file or database */
    public long                          linesOutput;
    /** nr of updates in a database table or file */
    public long                          linesUpdated;
    /** nr of lines skipped */
    public long                          linesSkipped;
    /** total sleep time in ns caused by an empty input buffer (previous step is slow) */
    public long                          linesRejected;
    /** total sleep time in ns caused by an empty input buffer (previous step is slow) */


    private long                         nrGetSleeps;
    /** total sleep time in ns cause by a full output buffer (next step is slow) */
    private long                         nrPutSleeps;

    private boolean                      distributed;

    private long                         errors;

    private StepMeta                     nextSteps[];

    private StepMeta                     prevSteps[];

    private int                          in_handling, out_handling;

    public ArrayList                     thr;

    /** The rowsets on the input, size() == nr of source steps */
    public List inputRowSets;

    /** the rowsets on the output, size() == nr of target steps */
    public List outputRowSets;

    /** the rowset for the error rows */
    public RowSet errorRowSet;

    public boolean                       stopped;

    public boolean                       waiting;

    public boolean                       init;

    /** the copy number of this thread */
    private int                          stepcopy;
    /** the output rowset nr, for fixed input channels like Stream Lookup */
    private int                          output_rowset_nr;

    private Date                         start_time, stop_time;

    public boolean                       first;

    public boolean                       terminator;

    public ArrayList                     terminator_rows;

    private StepMetaInterface            stepMetaInterface;

    private StepDataInterface            stepDataInterface;

    /** The list of RowListener interfaces */
    private List                         rowListeners;

    /**
     * Map of files that are generated or used by this step. After execution, these can be added to result.
     * The entry to the map is the filename
     */
    private Map                          resultFiles;

    /**
     * Set this to true if you want to have extra checking enabled on the rows that are entering this step. All too
     * often people send in bugs when it is really the mixing of different types of rows that is causing the problem.
     */
    private boolean                      safeModeEnabled;

    /**
     * This contains the first row received and will be the reference row. We used it to perform extra checking: see if
     * we don't get rows with "mixed" contents.
     */
    private Row                          referenceRow;

    /**
     * This field tells the putRow() method that we are in partitioned mode
     */
    private boolean                      partitioned;

    /**
     * The partition ID at which this step copy runs, or null if this step is not running partitioned.
     */
    private String                       partitionID;

    /**
     * This field tells the putRow() method to re-partition the incoming data, See also StepPartitioningMeta.PARTITIONING_METHOD_*
     */
    private int                          repartitioning;

    /**
     * True if the step needs to perform a sorted merge on the incoming partitioned data
     */
    private boolean                      partitionMerging;

    /**
     * The index of the column to partition or -1 if not known yet (before first row)
     */
    private int                          partitionColumnIndex;

    /**
     * The partitionID to rowset mapping
     */
    private Map                          partitionTargets;

    /**
     * Cache for the partition IDs
     */
    private static String[]              partitionIDs;

    /**
     * step partitioning information of the NEXT step
     */
    private static StepPartitioningMeta  nextStepPartitioningMeta;

    /**
     * This is the base step that forms that basis for all steps. You can derive from this class to implement your own
     * steps.
     *
     * @param stepMeta The StepMeta object to run.
     * @param stepDataInterface the data object to store temporary data, database connections, caches, result sets,
     * hashtables etc.
     * @param copyNr The copynumber for this step.
     * @param transMeta The TransInfo of which the step stepMeta is part of.
     * @param trans The (running) transformation to obtain information shared among the steps.
     */
    public BaseStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        log = LogWriter.getInstance();
        this.stepMeta = stepMeta;
        this.stepDataInterface = stepDataInterface;
        this.stepcopy = copyNr;
        this.transMeta = transMeta;
        this.trans = trans;
        this.stepname = stepMeta.getName();

        // Set the name of the thread
        if (stepMeta.getName() != null)
        {
            setName(toString() + " (" + super.getName() + ")");
        }
        else
        {
            throw new RuntimeException("A step in transformation [" + transMeta.toString()
                    + "] doesn't have a name.  A step should always have a name to identify it by.");
        }

        first = true;

        stopped = false;
        init = false;

        linesRead = 0L; // Keep some statistics!
        linesWritten = 0L;
        linesUpdated = 0L;
        linesSkipped = 0L;

        nrGetSleeps = 0L;
        nrPutSleeps = 0L;

        inputRowSets = null;
        outputRowSets = null;
        nextSteps = null;

        terminator = stepMeta.hasTerminator();
        if (terminator)
        {
            terminator_rows = new ArrayList();
        }
        else
        {
            terminator_rows = null;
        }

        // debug="-"; //$NON-NLS-1$

        output_rowset_nr = -1;
        start_time = null;
        stop_time = null;

        distributed = stepMeta.isDistributes();

        if (distributed) if (log.isDetailed())
            logDetailed(Messages.getString("BaseStep.Log.DistributionActivated")); //$NON-NLS-1$
        else
            if (log.isDetailed()) logDetailed(Messages.getString("BaseStep.Log.DistributionDeactivated")); //$NON-NLS-1$

        rowListeners = new ArrayList();
        resultFiles = new Hashtable();

        repartitioning = StepPartitioningMeta.PARTITIONING_METHOD_NONE;
        partitionColumnIndex = -1;
        partitionTargets = new Hashtable();

        dispatch();
    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        sdi.setStatus(StepDataInterface.STATUS_INIT);

        return true;
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        sdi.setStatus(StepDataInterface.STATUS_DISPOSED);
    }

    public long getProcessed()
    {
        return linesRead;
    }

    public void setCopy(int cop)
    {
        stepcopy = cop;
    }

    /**
     * @return The steps copy number (default 0)
     */
    public int getCopy()
    {
        return stepcopy;
    }

    public long getErrors()
    {
        return errors;
    }

    public void setErrors(long e)
    {
        errors = e;
    }

    /**
     * @return Returns the linesInput.
     */
    public long getLinesInput()
    {
        return linesInput;
    }

    /**
     * @return Returns the linesOutput.
     */
    public long getLinesOutput()
    {
        return linesOutput;
    }

    /**
     * @return Returns the linesRead.
     */
    public long getLinesRead()
    {
        return linesRead;
    }

    /**
     * @return Returns the linesWritten.
     */
    public long getLinesWritten()
    {
        return linesWritten;
    }

    /**
     * @return Returns the linesUpdated.
     */
    public long getLinesUpdated()
    {
        return linesUpdated;
    }

    public String getStepname()
    {
        return stepname;
    }

    public void setStepname(String stepname)
    {
        this.stepname = stepname;
    }

    public Trans getDispatcher()
    {
        return trans;
    }

    public String getStatusDescription()
    {
        return statusDesc[getStatus()];
    }

    /**
     * @return Returns the stepMetaInterface.
     */
    public StepMetaInterface getStepMetaInterface()
    {
        return stepMetaInterface;
    }

    /**
     * @param stepMetaInterface The stepMetaInterface to set.
     */
    public void setStepMetaInterface(StepMetaInterface stepMetaInterface)
    {
        this.stepMetaInterface = stepMetaInterface;
    }

    /**
     * @return Returns the stepDataInterface.
     */
    public StepDataInterface getStepDataInterface()
    {
        return stepDataInterface;
    }

    /**
     * @param stepDataInterface The stepDataInterface to set.
     */
    public void setStepDataInterface(StepDataInterface stepDataInterface)
    {
        this.stepDataInterface = stepDataInterface;
    }

    /**
     * @return Returns the stepMeta.
     */
    public StepMeta getStepMeta()
    {
        return stepMeta;
    }

    /**
     * @param stepMeta The stepMeta to set.
     */
    public void setStepMeta(StepMeta stepMeta)
    {
        this.stepMeta = stepMeta;
    }

    /**
     * @return Returns the transMeta.
     */
    public TransMeta getTransMeta()
    {
        return transMeta;
    }

    /**
     * @param transMeta The transMeta to set.
     */
    public void setTransMeta(TransMeta transMeta)
    {
        this.transMeta = transMeta;
    }

    /**
     * @return Returns the trans.
     */
    public Trans getTrans()
    {
        return trans;
    }

    /**
     * putRow is used to copy a row, to the alternate rowset(s) This should get priority over everything else!
     * (synchronized) If distribute is true, a row is copied only once to the output rowsets, otherwise copies are sent
     * to each rowset!
     *
     * @param row The row to put to the destination rowset(s).
     * @throws KettleStepException
     */
    public synchronized void putRow(Row row) throws KettleStepException
    {
        // Have all threads started?
        // Are we running yet?  If not, wait a bit until all threads have been started.
        while (!trans.isRunning() && !stopped)
        {
            try { Thread.sleep(1); } catch (InterruptedException e) { }
        }

        if (previewSize > 0 && previewBuffer.size() < previewSize)
        {
            previewBuffer.add(new Row(row));
        }

        // call all rowlisteners...
        for (int i = 0; i < rowListeners.size(); i++)
        {
            RowListener rowListener = (RowListener) rowListeners.get(i);
            rowListener.rowWrittenEvent(row);
        }

        // Keep adding to terminator_rows buffer...
        if (terminator && terminator_rows != null)
        {
            terminator_rows.add(new Row(row));
        }

        if (outputRowSets.isEmpty())
        {
            // No more output rowsets!
            return; // we're done here!
        }

        // Before we copy this row to output, wait for room...
        for (int i = 0; i < outputRowSets.size(); i++) // Wait for all rowsets: keep synchronised!
        {
            int sleeptime = transMeta.getSleepTimeFull();
            RowSet rs = (RowSet) outputRowSets.get(i);

            // Set the priority every 128k rows only
            //
            if (transMeta.isUsingThreadPriorityManagment())
            {
                if (linesWritten>0 && (linesWritten & 0xFF) == 0)
                {
                    rs.setPriorityFrom(calcPutPriority(rs));
                }
            }

            while (rs.isFull() && !stopped)
            {
                try
                {
                    if (sleeptime > 0)
                    {
                        sleep(0, sleeptime);
                    }
                    else
                    {
                        super.notifyAll();
                    }
                }
                catch (Exception e)
                {
                    logError(Messages.getString("BaseStep.Log.ErrorInThreadSleeping") + e.toString()); //$NON-NLS-1$
                    setErrors(1);
                    stopAll();
                    return;
                }
                nrPutSleeps += sleeptime;
                if (sleeptime < 100)
                    sleeptime = ((int) (sleeptime * 1.2)) + 1;
                else
                    sleeptime = 100;
            }
        }

        if (stopped)
        {
            if (log.isDebug()) logDebug(Messages.getString("BaseStep.Log.StopPuttingARow")); //$NON-NLS-1$
            stopAll();
            return;
        }

        // Repartitioning happens when the current step is not partitioned, but the next one is.
        // That means we need to look up the partitioning information in the next step..
        // If there are multiple steps, we need to look at the first (they should be all the same)
        // TODO: make something smart later to allow splits etc.
        //
        switch(repartitioning)
        {
        case StepPartitioningMeta.PARTITIONING_METHOD_MOD:
            {
                // Do some pre-processing on the first row...
                // This is only done once and should cost very little in terms of processing time.
                //
                if (partitionColumnIndex < 0)
                {
                    StepMeta nextSteps[] = transMeta.getNextSteps(stepMeta);
                    if (nextSteps == null || nextSteps.length == 0) { throw new KettleStepException(
                            "Re-partitioning is enabled but no next steps could be found: developer error!"); }
    
                    // Take the partitioning logic from one of the next steps
                    nextStepPartitioningMeta = nextSteps[0].getStepPartitioningMeta();
    
                    // What's the column index of the partitioning fieldname?
                    partitionColumnIndex = row.searchValueIndex(nextStepPartitioningMeta.getFieldName());
                    if (partitionColumnIndex < 0) { throw new KettleStepException("Unable to find partitioning field name ["
                            + nextStepPartitioningMeta.getFieldName() + "] in the output row : " + row); }
    
                    // Cache the partition IDs as well...
                    partitionIDs = nextSteps[0].getStepPartitioningMeta().getPartitionSchema().getPartitionIDs();
    
                    // OK, we also want to cache the target rowset
                    //
                    // We know that we have to partition in N pieces
                    // We should also have N rowsets to the next step
                    // This is always the case, wheter the target is partitioned or not.
                    //
                    // So what we do is now count the number of rowsets
                    // And we take the steps copy nr to map.
                    // It's simple for the time being.
                    //
                    // P1 : MOD(field,N)==0
                    // P2 : MOD(field,N)==1
                    // ...
                    // PN : MOD(field,N)==N-1
                    //
    
                    for (int r = 0; r < outputRowSets.size(); r++)
                    {
                        RowSet rowSet = (RowSet) outputRowSets.get(r);
                        if (rowSet.getOriginStepName().equalsIgnoreCase(getStepname()) && rowSet.getOriginStepCopy() == getCopy())
                        {
                            // Find the target step metadata
                            StepMeta targetStep = transMeta.findStep(rowSet.getDestinationStepName());
    
                            // What are the target partition ID's
                            String targetPartitions[] = targetStep.getStepPartitioningMeta().getPartitionSchema().getPartitionIDs();
    
                            // The target partitionID:
                            String targetPartitionID = targetPartitions[rowSet.getDestinationStepCopy()];
    
                            // Save the mapping: if we want to know to which rowset belongs to a partition this is the place
                            // to be.
                            partitionTargets.put(targetPartitionID, rowSet);
                        }
                    }
                } // End of the one-time init code.
    
                // Here we go with the regular show
                int partitionNr = nextStepPartitioningMeta.getPartitionNr(row.getValue(partitionColumnIndex), partitionIDs.length);
                String targetPartition = partitionIDs[partitionNr];
    
                // Put the row forward to the next step according to the partition rule.
                RowSet rs = (RowSet) partitionTargets.get(targetPartition);
                rs.putRow(row);
                linesWritten++;
            }
            break;
        case StepPartitioningMeta.PARTITIONING_METHOD_MIRROR:
            {
                // Copy always to all target steps/copies.
                // 
                for (int r = 0; r < outputRowSets.size(); r++)
                {
                    RowSet rowSet = (RowSet) outputRowSets.get(r);
                    rowSet.putRow(row);
                }
            }
            break;
        case StepPartitioningMeta.PARTITIONING_METHOD_NONE:
            {
                if (distributed)
                {
                    // Copy the row to the "next" output rowset.
                    // We keep the next one in out_handling
                    RowSet rs = (RowSet) outputRowSets.get(out_handling);
                    rs.putRow(row);
                    linesWritten++;
    
                    // Now determine the next output rowset!
                    // Only if we have more then one output...
                    if (outputRowSets.size() > 1)
                    {
                        out_handling++;
                        if (out_handling >= outputRowSets.size()) out_handling = 0;
                    }
                }
                else
                // Copy the row to all output rowsets!
                {
                    // Copy to the row in the other output rowsets...
                    for (int i = 1; i < outputRowSets.size(); i++) // start at 1
                    {
                        RowSet rs = (RowSet) outputRowSets.get(i);
                        rs.putRow(new Row(row));
                    }
    
                    // set row in first output rowset
                    RowSet rs = (RowSet) outputRowSets.get(0);
                    rs.putRow(row);
                    linesWritten++;
                }
            }
            break;
        }
    }

    /**
     * This version of getRow() only takes data from certain rowsets We select these rowsets that have name = step
     * Otherwise it's the same as the other one.
     *
     * @param row the row to send to the destination step
     * @param to the name of the step to send the row to
     */
    public synchronized void putRowTo(Row row, String to) throws KettleStepException
    {
        output_rowset_nr = findOutputRowSetNumber(stepname, getCopy(), to, 0);
        if (output_rowset_nr < 0)
        {
            //
            // No rowset found: normally it can't happen:
            // we deleted the rowset because it was
            // finished
            //
            throw new KettleStepException(Messages.getString("BaseStep.Exception.UnableToFindRowset", to)); //$NON-NLS-1$ //$NON-NLS-2$
        }

        putRowTo(row, output_rowset_nr);
    }

    /**
     * putRow is used to copy a row, to the alternate rowset(s) This should get priority over everything else!
     * (synchronized) If distribute is true, a a row is copied only once to a single output rowset!
     *
     * @param row The row to put to the destination rowsets.
     * @param output_rowset_nr the number of the rowset to put the row to.
     */
    public synchronized void putRowTo(Row row, int output_rowset_nr)
    {
        int sleeptime;

        if (previewSize > 0 && previewBuffer.size() < previewSize)
        {
            previewBuffer.add(new Row(row));
        }

        // call all rowlisteners...
        for (int i = 0; i < rowListeners.size(); i++)
        {
            RowListener rowListener = (RowListener) rowListeners.get(i);
            rowListener.rowWrittenEvent(row);
        }

        // Keep adding to terminator_rows buffer...
        if (terminator && terminator_rows != null)
        {
            terminator_rows.add(new Row(row));
        }

        if (outputRowSets.isEmpty()) return; // nothing to do here!

        RowSet rs = (RowSet) outputRowSets.get(output_rowset_nr);
        sleeptime = transMeta.getSleepTimeFull();
        while (rs.isFull() && !stopped)
        {
            try
            {
                if (sleeptime > 0)
                {
                    sleep(0, sleeptime);
                }
                else
                {
                    super.notifyAll();
                }
            }
            catch (Exception e)
            {
                logError(Messages.getString("BaseStep.Log.ErrorInThreadSleeping") + e.toString()); //$NON-NLS-1$
                setErrors(1);
                stopAll();
                return;
            }
            nrPutSleeps += sleeptime;
            if (sleeptime < 100)
                sleeptime = ((int) (sleeptime * 1.2)) + 1;
            else
                sleeptime = 100;
        }
        if (stopped)
        {
            if (log.isDebug()) logDebug(Messages.getString("BaseStep.Log.StopPuttingARow")); //$NON-NLS-1$
            stopAll();
            return;
        }

        // Don't distribute or anything, only go to this rowset!
        rs.putRow(row);
        linesWritten++;
    }


    public synchronized void putError(Row row, long nrErrors, String errorDescriptions, String fieldNames, String errorCodes)
    {
        StepErrorMeta stepErrorMeta = stepMeta.getStepErrorMeta();
        Row add = stepErrorMeta.getErrorFields(nrErrors, errorDescriptions, fieldNames, errorCodes);
        row.addRow(add);

        // call all rowlisteners...
        for (int i = 0; i < rowListeners.size(); i++)
        {
            RowListener rowListener = (RowListener) rowListeners.get(i);
            rowListener.errorRowWrittenEvent(row);
        }

        linesRejected++;

        if (errorRowSet!=null) errorRowSet.putRow(row);

        verifyRejectionRates();
    }

    private void verifyRejectionRates()
    {
        StepErrorMeta stepErrorMeta = stepMeta.getStepErrorMeta();
        if (stepErrorMeta==null) return; // nothing to verify.

        // Was this one error too much?
        if (stepErrorMeta.getMaxErrors()>0 && linesRejected>stepErrorMeta.getMaxErrors())
        {
            logError(Messages.getString("BaseStep.Log.TooManyRejectedRows", Long.toString(stepErrorMeta.getMaxErrors()), Long.toString(linesRejected)));
            setErrors(1L);
            stopAll();
        }

        if ( stepErrorMeta.getMaxPercentErrors()>0 && linesRejected>0 &&
            ( stepErrorMeta.getMinPercentRows()<=0 || linesRead>=stepErrorMeta.getMinPercentRows())
            )
        {
            int pct = (int) (100 * linesRejected / linesRead );
            if (pct>stepErrorMeta.getMaxPercentErrors())
            {
                logError(Messages.getString("BaseStep.Log.MaxPercentageRejectedReached", Integer.toString(pct) ,Long.toString(linesRejected), Long.toString(linesRead)));
                setErrors(1L);
                stopAll();
            }
        }
    }

    private synchronized RowSet currentInputStream()
    {
        return (RowSet) inputRowSets.get(in_handling);
    }

    /**
     * Find the next not-finished input-stream... in_handling says which one...
     */
    private synchronized void nextInputStream()
    {
        int streams = inputRowSets.size();

        // No more streams left: exit!
        if (streams == 0) return;

        // If we have some left: take the next!
        in_handling++;
        if (in_handling >= inputRowSets.size()) in_handling = 0;
        // logDebug("nextInputStream advanced to in_handling="+in_handling);
    }

    /**
     * In case of getRow, we receive data from previous steps through the input rowset. In case we split the stream, we
     * have to copy the data to the alternate splits: rowsets 1 through n.
     */
    public synchronized Row getRow() throws KettleException
    {
        int sleeptime;
        int switches;

        // Have all threads started?
        // Are we running yet?  If not, wait a bit until all threads have been started.
        while (!trans.isRunning() && !stopped)
        {
            try { Thread.sleep(1); } catch (InterruptedException e) { }
        }

        // If everything is finished, we can stop immediately!
        if (inputRowSets.isEmpty())
        {
            return null;
        }

        // What's the current input stream?
        RowSet in = currentInputStream();
        switches = 0;
        sleeptime = transMeta.getSleepTimeEmpty();
        while (in.isEmpty() && !stopped)
        {
            // in : empty
            synchronized(in)
            {
                if (in.isEmpty() && in.isDone()) // nothing more here: remove it from input
                {
                    inputRowSets.remove(in_handling);
                    if (inputRowSets.isEmpty()) // nothing more to be found!
                    {
                        return null;
                    }
                }
            }
            nextInputStream();
            in = currentInputStream();
            switches++;
            if (switches >= inputRowSets.size()) // every n looks, wait a bit! Don't use too much CPU!
            {
                switches = 0;
                try
                {
                    if (sleeptime > 0)
                    {
                        sleep(0, sleeptime);
                    }
                    else
                    {
                        super.notifyAll();
                    }
                }
                catch (Exception e)
                {
                    logError(Messages.getString("BaseStep.Log.SleepInterupted") + e.toString()); //$NON-NLS-1$
                    setErrors(1);
                    stopAll();
                    return null;
                }
                if (sleeptime < 100)
                    sleeptime = ((int) (sleeptime * 1.2)) + 1;
                else
                    sleeptime = 100;
                nrGetSleeps += sleeptime;
            }
        }
        if (stopped)
        {
            if (log.isDebug()) logDebug(Messages.getString("BaseStep.Log.StopLookingForMoreRows")); //$NON-NLS-1$
            stopAll();
            return null;
        }

        // Set the appropriate priority depending on the amount of data in the rowset:
        // Only do this every 4096 rows...
        // Mmm, the less we do it, the faster the tests run, let's leave this out for now ;-)
        if (transMeta.isUsingThreadPriorityManagment())
        {
            if (linesRead>0 && (linesRead & 0xFF) == 0)
            {
                in.setPriorityTo(calcGetPriority(in));
            }
        }

        // Get this row!
        Row row = in.getRow();
        linesRead++;

        // Notify all rowlisteners...
        for (int i = 0; i < rowListeners.size(); i++)
        {
            RowListener rowListener = (RowListener) rowListeners.get(i);
            rowListener.rowReadEvent(row);
        }

        nextInputStream(); // Look for the next input stream to get row from.

        // OK, before we return the row, let's see if we need to check on mixing row compositions...
        if (safeModeEnabled)
        {
            safeModeChecking(row);
        } // Extra checking

        // Check the rejection rates etc. as well.
        verifyRejectionRates();

        return row;
    }

    protected synchronized void safeModeChecking(Row row) throws KettleRowException
    {
        // String saveDebug=debug;
        // debug="Safe mode checking";
        if (referenceRow == null)
        {
            referenceRow = new Row(row); // copy it!
            
            // Check for double fieldnames.
            // 
            String[] fieldnames = row.getFieldNames();
            Arrays.sort(fieldnames);
            for (int i=0;i<fieldnames.length-1;i++)
            {
                if (fieldnames[i].equals(fieldnames[i+1]))
                {
                    throw new KettleRowException(Messages.getString("BaseStep.SafeMode.Exception.DoubleFieldnames", fieldnames[i]));
                }
            }
        }
        else
        {
            safeModeChecking(referenceRow, row);
        }
        // debug=saveDebug;
    }

    public static void safeModeChecking(Row referenceRow, Row row) throws KettleRowException
    {
        // See if the row we got has the same layout as the reference row.
        // First check the number of fields
        if (referenceRow.size() != row.size())
        {
            throw new KettleRowException(Messages.getString("BaseStep.SafeMode.Exception.VaryingSize", ""+referenceRow.size(), ""+row.size(), row.toString()));
        }
        else
        {
            // Check field by field for the position of the names...
            for (int i = 0; i < referenceRow.size(); i++)
            {
                Value referenceValue = referenceRow.getValue(i);
                Value compareValue = row.getValue(i);

                if (!referenceValue.getName().equalsIgnoreCase(compareValue.getName()))
                {
                    throw new KettleRowException(Messages.getString("BaseStep.SafeMode.Exception.MixingLayout", ""+(i+1), referenceValue.getName()+" "+referenceValue.toStringMeta(), compareValue.getName()+" "+compareValue.toStringMeta()));
                }

                if (referenceValue.getType()!=compareValue.getType())
                {
                    throw new KettleRowException(Messages.getString("BaseStep.SafeMode.Exception.MixingTypes", ""+(i+1), referenceValue.getName()+" "+referenceValue.toStringMeta(), compareValue.getName()+" "+compareValue.toStringMeta()));               
                }
            }
        }
    }

    /**
     * This version of getRow() only takes data from certain rowsets We select these rowsets that have name = step
     * Otherwise it's the same as the other one.
     */
    public synchronized Row getRowFrom(String from)
    {
        output_rowset_nr = findInputRowSetNumber(from, 0, stepname, 0);
        if (output_rowset_nr < 0) // No rowset found: normally it can't happen: we deleted the rowset because it was
                                    // finished
        { return null; }

        return getRowFrom(output_rowset_nr);
    }

    public synchronized Row getRowFrom(int input_rowset_nr)
    {
        // Read from one specific rowset
        //
        int sleeptime = transMeta.getSleepTimeEmpty();

        RowSet in = (RowSet) inputRowSets.get(input_rowset_nr);
        while (in.isEmpty() && !in.isDone() && !stopped)
        {
            try
            {
                if (sleeptime > 0)
                {
                    sleep(0, sleeptime);
                }
                else
                {
                    super.notifyAll();
                }
            }
            catch (Exception e)
            {
                logError(Messages.getString("BaseStep.Log.SleepInterupted2", in.getOriginStepName()) + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
                setErrors(1);
                stopAll();
                return null;
            }
            nrGetSleeps += sleeptime;
        }

        if (stopped)
        {
            logError(Messages.getString("BaseStep.Log.SleepInterupted3", in.getOriginStepName())); //$NON-NLS-1$ //$NON-NLS-2$
            stopAll();
            return null;
        }

        if (in.isEmpty() && in.isDone())
        {
            inputRowSets.remove(input_rowset_nr);
            return null;
        }

        Row row = in.getRow(); // Get this row!
        linesRead++;

        // call all rowlisteners...
        for (int i = 0; i < rowListeners.size(); i++)
        {
            RowListener rowListener = (RowListener) rowListeners.get(i);
            rowListener.rowWrittenEvent(row);
        }

        return row;
    }

    private synchronized int findInputRowSetNumber(String from, int fromcopy, String to, int tocopy)
    {
        int i;
        for (i = 0; i < inputRowSets.size(); i++)
        {
            RowSet rs = (RowSet) inputRowSets.get(i);
            if (rs.getOriginStepName().equalsIgnoreCase(from) && rs.getDestinationStepName().equalsIgnoreCase(to)
                    && rs.getOriginStepCopy() == fromcopy && rs.getDestinationStepCopy() == tocopy) return i;
        }
        return -1;
    }

    private synchronized int findOutputRowSetNumber(String from, int fromcopy, String to, int tocopy)
    {
        int i;
        for (i = 0; i < outputRowSets.size(); i++)
        {
            RowSet rs = (RowSet) outputRowSets.get(i);
            if (rs.getOriginStepName().equalsIgnoreCase(from) && rs.getDestinationStepName().equalsIgnoreCase(to)
                    && rs.getOriginStepCopy() == fromcopy && rs.getDestinationStepCopy() == tocopy) return i;
        }
        return -1;
    }

    //
    // We have to tell the next step we're finished with
    // writing to output rowset(s)!
    //
    public synchronized void setOutputDone()
    {
        if (log.isDebug()) logDebug(Messages.getString("BaseStep.Log.OutputDone", String.valueOf(outputRowSets.size()))); //$NON-NLS-1$ //$NON-NLS-2$
        synchronized(outputRowSets)
        {
            for (int i = 0; i < outputRowSets.size(); i++)
            {
                RowSet rs = (RowSet) outputRowSets.get(i);
                rs.setDone();
            }
            if (errorRowSet!=null) errorRowSet.setDone();
        }
    }

    /**
     * This method finds the surrounding steps and rowsets for this base step. This steps keeps it's own list of rowsets
     * (etc.) to prevent it from having to search every time.
     */
    public void dispatch()
    {
        if (transMeta == null) // for preview reasons, no dispatching is done!
        { return; }

        StepMeta stepMeta = transMeta.findStep(stepname);

        if (log.isDetailed()) logDetailed(Messages.getString("BaseStep.Log.StartingBuffersAllocation")); //$NON-NLS-1$

        // How many next steps are there? 0, 1 or more??
        // How many steps do we send output to?
        int nrInput = transMeta.findNrPrevSteps(stepMeta, true);
        int nrOutput = transMeta.findNrNextSteps(stepMeta);

        inputRowSets = Collections.synchronizedList( new ArrayList() ); // new RowSet[nrinput];
        outputRowSets = Collections.synchronizedList( new ArrayList() ); // new RowSet[nroutput+out_copies];
        errorRowSet = null;
        prevSteps = new StepMeta[nrInput];
        nextSteps = new StepMeta[nrOutput];

        in_handling = 0; // we start with input[0];

        logDetailed(Messages.getString("BaseStep.Log.StepInfo", String.valueOf(nrInput), String.valueOf(nrOutput))); //$NON-NLS-1$ //$NON-NLS-2$

        for (int i = 0; i < nrInput; i++)
        {
            prevSteps[i] = transMeta.findPrevStep(stepMeta, i, true); // sir.getHopFromWithTo(stepname, i);
            logDetailed(Messages.getString("BaseStep.Log.GotPreviousStep", stepname, String.valueOf(i), prevSteps[i].getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            // Looking at the previous step, you can have either 1 rowset to look at or more then one.
            int prevCopies = prevSteps[i].getCopies();
            int nextCopies = stepMeta.getCopies();
            logDetailed(Messages.getString("BaseStep.Log.InputRowInfo", String.valueOf(prevCopies), String.valueOf(nextCopies))); //$NON-NLS-1$ //$NON-NLS-2$

            int nrCopies;
            int dispatchType;

            if (prevCopies == 1 && nextCopies == 1)
            {
                dispatchType = Trans.TYPE_DISP_1_1;
                nrCopies = 1;
            }
            else
            {
                if (prevCopies == 1 && nextCopies > 1)
                {
                    dispatchType = Trans.TYPE_DISP_1_N;
                    nrCopies = 1;
                }
                else
                {
                    if (prevCopies > 1 && nextCopies == 1)
                    {
                        dispatchType = Trans.TYPE_DISP_N_1;
                        nrCopies = prevCopies;
                    }
                    else
                    {
                        if (prevCopies == nextCopies)
                        {
                            dispatchType = Trans.TYPE_DISP_N_N;
                            nrCopies = 1;
                        } // > 1!
                        else
                        {
                            dispatchType = Trans.TYPE_DISP_N_M;
                            nrCopies = prevCopies;
                        }
                    }
                }
            }

            for (int c = 0; c < nrCopies; c++)
            {
                RowSet rowSet = null;
                switch (dispatchType)
                {
                case Trans.TYPE_DISP_1_1:
                    rowSet = trans.findRowSet(prevSteps[i].getName(), 0, stepname, 0);
                    break;
                case Trans.TYPE_DISP_1_N:
                    rowSet = trans.findRowSet(prevSteps[i].getName(), 0, stepname, getCopy());
                    break;
                case Trans.TYPE_DISP_N_1:
                    rowSet = trans.findRowSet(prevSteps[i].getName(), c, stepname, 0);
                    break;
                case Trans.TYPE_DISP_N_N:
                    rowSet = trans.findRowSet(prevSteps[i].getName(), getCopy(), stepname, getCopy());
                    break;
                case Trans.TYPE_DISP_N_M:
                    rowSet = trans.findRowSet(prevSteps[i].getName(), c, stepname, getCopy());
                    break;
                }
                if (rowSet != null)
                {
                    inputRowSets.add(rowSet);
                    logDetailed(Messages.getString("BaseStep.Log.FoundInputRowset", rowSet.getName())); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else
                {
                    logError(Messages.getString("BaseStep.Log.UnableToFindInputRowset")); //$NON-NLS-1$
                    setErrors(1);
                    stopAll();
                    return;
                }
            }
        }
        // And now the output part!
        for (int i = 0; i < nrOutput; i++)
        {
            nextSteps[i] = transMeta.findNextStep(stepMeta, i);

            int prevCopies = stepMeta.getCopies();
            int nextCopies = nextSteps[i].getCopies();

            logDetailed(Messages.getString("BaseStep.Log.OutputRowInfo", String.valueOf(prevCopies), String.valueOf(nextCopies))); //$NON-NLS-1$ //$NON-NLS-2$

            int nrCopies;
            int dispatchType;

            if (prevCopies == 1 && nextCopies == 1)
            {
                dispatchType = Trans.TYPE_DISP_1_1;
                nrCopies = 1;
            }
            else
            {
                if (prevCopies == 1 && nextCopies > 1)
                {
                    dispatchType = Trans.TYPE_DISP_1_N;
                    nrCopies = nextCopies;
                }
                else
                {
                    if (prevCopies > 1 && nextCopies == 1)
                    {
                        dispatchType = Trans.TYPE_DISP_N_1;
                        nrCopies = 1;
                    }
                    else
                    {
                        if (prevCopies == nextCopies)
                        {
                            dispatchType = Trans.TYPE_DISP_N_N;
                            nrCopies = 1;
                        } // > 1!
                        else
                        {
                            dispatchType = Trans.TYPE_DISP_N_M;
                            nrCopies = nextCopies;
                        }
                    }
                }
            }

            for (int c = 0; c < nrCopies; c++)
            {
                RowSet rowSet = null;
                switch (dispatchType)
                {
                case Trans.TYPE_DISP_1_1:
                    rowSet = trans.findRowSet(stepname, 0, nextSteps[i].getName(), 0);
                    break;
                case Trans.TYPE_DISP_1_N:
                    rowSet = trans.findRowSet(stepname, 0, nextSteps[i].getName(), c);
                    break;
                case Trans.TYPE_DISP_N_1:
                    rowSet = trans.findRowSet(stepname, getCopy(), nextSteps[i].getName(), 0);
                    break;
                case Trans.TYPE_DISP_N_N:
                    rowSet = trans.findRowSet(stepname, getCopy(), nextSteps[i].getName(), getCopy());
                    break;
                case Trans.TYPE_DISP_N_M:
                    rowSet = trans.findRowSet(stepname, getCopy(), nextSteps[i].getName(), c);
                    break;
                }
                if (rowSet != null)
                {
                    outputRowSets.add(rowSet);
                    logDetailed(Messages.getString("BaseStep.Log.FoundOutputRowset", rowSet.getName())); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else
                {
                    logError(Messages.getString("BaseStep.Log.UnableToFindOutputRowset")); //$NON-NLS-1$
                    setErrors(1);
                    stopAll();
                    return;
                }
            }
        }

        logDetailed(Messages.getString("BaseStep.Log.FinishedDispatching")); //$NON-NLS-1$
    }

    public void logMinimal(String s)
    {
        log.println(LogWriter.LOG_LEVEL_MINIMAL, stepname + "." + stepcopy, s); //$NON-NLS-1$
    }

    public void logBasic(String s)
    {
        log.println(LogWriter.LOG_LEVEL_BASIC, stepname + "." + stepcopy, s); //$NON-NLS-1$
    }

    public void logError(String s)
    {
        log.println(LogWriter.LOG_LEVEL_ERROR, stepname + "." + stepcopy, s); //$NON-NLS-1$
    }

    public void logDetailed(String s)
    {
        log.println(LogWriter.LOG_LEVEL_DETAILED, stepname + "." + stepcopy, s); //$NON-NLS-1$
    }

    public void logDebug(String s)
    {
        log.println(LogWriter.LOG_LEVEL_DEBUG, stepname + "." + stepcopy, s); //$NON-NLS-1$
    }

    public void logRowlevel(String s)
    {
        log.println(LogWriter.LOG_LEVEL_ROWLEVEL, stepname + "." + stepcopy, s); //$NON-NLS-1$
    }

    public int getNextClassNr()
    {
        int ret = trans.class_nr;
        trans.class_nr++;

        return ret;
    }

    public boolean outputIsDone()
    {
        int nrstopped = 0;
        RowSet rs;
        int i;

        for (i = 0; i < outputRowSets.size(); i++)
        {
            rs = (RowSet) outputRowSets.get(i);
            if (rs.isDone()) nrstopped++;
        }
        return nrstopped >= outputRowSets.size();
    }

    public void stopAll()
    {
        stopped = true;
        trans.stopAll();
    }

    public boolean isStopped()
    {
        return stopped;
    }

    public boolean isInitialising()
    {
        return init;
    }

    public void markStart()
    {
        Calendar cal = Calendar.getInstance();
        start_time = cal.getTime();
                
        setInternalVariables();
    }

    public void setInternalVariables()
    {
        KettleVariables kettleVariables = KettleVariables.getNamedInstance(getName());

        kettleVariables.setVariable(Const.INTERNAL_VARIABLE_STEP_NAME, stepname);
        kettleVariables.setVariable(Const.INTERNAL_VARIABLE_STEP_COPYNR, Integer.toString(getCopy()));

        // Also set the internal variable for the partition
        if (!Const.isEmpty(partitionID))
        {
            kettleVariables.setVariable(Const.INTERNAL_VARIABLE_STEP_PARTITION_ID, partitionID);
        }
    }

    public void markStop()
    {
        Calendar cal = Calendar.getInstance();
        stop_time = cal.getTime();
    }

    public long getRuntime()
    {
        long lapsed;
        if (start_time != null && stop_time == null)
        {
            Calendar cal = Calendar.getInstance();
            long now = cal.getTimeInMillis();
            long st = start_time.getTime();
            lapsed = now - st;
        }
        else
            if (start_time != null && stop_time != null)
            {
                lapsed = stop_time.getTime() - start_time.getTime();
            }
            else
            {
                lapsed = 0;
            }

        return lapsed;
    }

    public Row buildLog(String sname, int copynr, long lines_read, long lines_written, long lines_updated, long lines_skipped, long errors,
            Value start_date, Value end_date)
    {
        Row r = new Row();

        r.addValue(new Value(Messages.getString("BaseStep.ColumnName.Stepname"), sname)); //$NON-NLS-1$
        r.addValue(new Value(Messages.getString("BaseStep.ColumnName.Copy"), (double) copynr)); //$NON-NLS-1$
        r.addValue(new Value(Messages.getString("BaseStep.ColumnName.LinesReaded"), (double) lines_read)); //$NON-NLS-1$
        r.addValue(new Value(Messages.getString("BaseStep.ColumnName.LinesWritten"), (double) lines_written)); //$NON-NLS-1$
        r.addValue(new Value(Messages.getString("BaseStep.ColumnName.LinesUpdated"), (double) lines_updated)); //$NON-NLS-1$
        r.addValue(new Value(Messages.getString("BaseStep.ColumnName.LinesSkipped"), (double) lines_skipped)); //$NON-NLS-1$
        r.addValue(new Value(Messages.getString("BaseStep.ColumnName.Errors"), (double) errors)); //$NON-NLS-1$
        r.addValue(start_date);
        r.addValue(end_date);

        return r;
    }

    public static final Row getLogFields(String comm)
    {
        Row r = new Row();
        int i;
        Value sname = new Value(Messages.getString("BaseStep.ColumnName.Stepname"), ""); //$NON-NLS-1$ //$NON-NLS-2$
        sname.setLength(256);
        r.addValue(sname);

        r.addValue(new Value(Messages.getString("BaseStep.ColumnName.Copy"), 0.0)); //$NON-NLS-1$
        r.addValue(new Value(Messages.getString("BaseStep.ColumnName.LinesReaded"), 0.0)); //$NON-NLS-1$
        r.addValue(new Value(Messages.getString("BaseStep.ColumnName.LinesWritten"), 0.0)); //$NON-NLS-1$
        r.addValue(new Value(Messages.getString("BaseStep.ColumnName.LinesUpdated"), 0.0)); //$NON-NLS-1$
        r.addValue(new Value(Messages.getString("BaseStep.ColumnName.LinesSkipped"), 0.0)); //$NON-NLS-1$
        r.addValue(new Value(Messages.getString("BaseStep.ColumnName.Errors"), 0.0)); //$NON-NLS-1$
        r.addValue(new Value(Messages.getString("BaseStep.ColumnName.StartDate"), Const.MIN_DATE)); //$NON-NLS-1$
        r.addValue(new Value(Messages.getString("BaseStep.ColumnName.EndDate"), Const.MAX_DATE)); //$NON-NLS-1$

        for (i = 0; i < r.size(); i++)
        {
            r.getValue(i).setOrigin(comm);
        }

        return r;
    }

    public String toString()
    {
        return stepname + "." + getCopy(); //$NON-NLS-1$
    }

    public Thread getThread()
    {
        return this;
    }

    private int calcPutPriority(RowSet rs)
    {
        if (rs.size() > transMeta.getSizeRowset() * 0.95) return MIN_PRIORITY;
        if (rs.size() > transMeta.getSizeRowset() * 0.75) return LOW_PRIORITY;
        if (rs.size() > transMeta.getSizeRowset() * 0.50) return NORMAL_PRIORITY;
        if (rs.size() > transMeta.getSizeRowset() * 0.25) return HIGH_PRIORITY;
        return MAX_PRIORITY;
    }

    private int calcGetPriority(RowSet rs)
    {
        if (rs.size() > transMeta.getSizeRowset() * 0.95) return MAX_PRIORITY;
        if (rs.size() > transMeta.getSizeRowset() * 0.75) return HIGH_PRIORITY;
        if (rs.size() > transMeta.getSizeRowset() * 0.50) return NORMAL_PRIORITY;
        if (rs.size() > transMeta.getSizeRowset() * 0.25) return LOW_PRIORITY;
        return MIN_PRIORITY;
    }

    public int rowsetOutputSize()
    {
        int size = 0;
        int i;
        for (i = 0; i < outputRowSets.size(); i++)
        {
            size += ((RowSet) outputRowSets.get(i)).size();
        }

        return size;
    }

    public int rowsetInputSize()
    {
        int size = 0;
        int i;
        for (i = 0; i < inputRowSets.size(); i++)
        {
            size += ((RowSet) inputRowSets.get(i)).size();
        }

        return size;
    }

    /**
     * Create a new empty StepMeta class from the steploader
     *
     * @param stepplugin The step/plugin to use
     * @param steploader The StepLoader to load from
     * @return The requested class.
     */
    public static final StepMetaInterface getStepInfo(StepPlugin stepplugin, StepLoader steploader) throws KettleStepLoaderException
    {
        return steploader.getStepClass(stepplugin);
    }

    public static final String getIconFilename(int steptype)
    {
        return steps[steptype].getImageFileName();
    }

    /**
     * Perform actions to stop a running step. This can be stopping running SQL queries (cancel), etc. Default it
     * doesn't do anything.
     *
     * @param stepDataInterface The interface to the step data containing the connections, resultsets, open files, etc.
     * @throws KettleException in case something goes wrong
     *
     */
    public void stopRunning(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) throws KettleException
    {
    }

    /**
     * Stops running operations This method is deprecated, please use the method specifying the metadata and data
     * interfaces.
     *
     * @deprecated
     */
    public void stopRunning()
    {
    }

    public void logSummary()
    {
        logBasic(Messages.getString("BaseStep.Log.SummaryInfo", String.valueOf(linesInput), String.valueOf(linesOutput), String.valueOf(linesRead), String.valueOf(linesWritten), String.valueOf(linesUpdated), String.valueOf(errors+linesRejected))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    }

    public String getStepID()
    {
        if (stepMeta != null) return stepMeta.getStepID();
        return null;
    }

    /**
     * @return Returns the inputRowSets.
     */
    public List getInputRowSets()
    {
        return inputRowSets;
    }

    /**
     * @param inputRowSets The inputRowSets to set.
     */
    public void setInputRowSets(ArrayList inputRowSets)
    {
        this.inputRowSets = inputRowSets;
    }

    /**
     * @return Returns the outputRowSets.
     */
    public List getOutputRowSets()
    {
        return outputRowSets;
    }

    /**
     * @param outputRowSets The outputRowSets to set.
     */
    public void setOutputRowSets(ArrayList outputRowSets)
    {
        this.outputRowSets = outputRowSets;
    }

    /**
     * @return Returns the distributed.
     */
    public boolean isDistributed()
    {
        return distributed;
    }

    /**
     * @param distributed The distributed to set.
     */
    public void setDistributed(boolean distributed)
    {
        this.distributed = distributed;
    }

    public void addRowListener(RowListener rowListener)
    {
        rowListeners.add(rowListener);
    }

    public void removeRowListener(RowListener rowListener)
    {
        rowListeners.remove(rowListener);
    }

    public List getRowListeners()
    {
        return rowListeners;
    }

    public void addResultFile(ResultFile resultFile)
    {
        resultFiles.put(resultFile.getFile().toString(), resultFile);
    }

    public Map getResultFiles()
    {
        return resultFiles;
    }

    /**
     * @return Returns the total sleep time in ns in case nothing was found in an input buffer for this step.
     */
    public long getNrGetSleeps()
    {
        return nrGetSleeps;
    }

    /**
     * @param nrGetSleeps the total sleep time in ns in case nothing was found in an input buffer for this step.
     */
    public void setNrGetSleeps(long nrGetSleeps)
    {
        this.nrGetSleeps = nrGetSleeps;
    }

    /**
     * @return Returns the total sleep time in ns in case the output buffer was full for this step.
     */
    public long getNrPutSleeps()
    {
        return nrPutSleeps;
    }

    /**
     * @param nrPutSleeps the total sleep time in ns in case the output buffer was full for this step.
     */
    public void setNrPutSleeps(long nrPutSleeps)
    {
        this.nrPutSleeps = nrPutSleeps;
    }

    /**
     * @return Returns true is this step is running in safe mode, with extra checking enabled...
     */
    public boolean isSafeModeEnabled()
    {
        return safeModeEnabled;
    }

    /**
     * @param safeModeEnabled set to true is this step has to be running in safe mode, with extra checking enabled...
     */
    public void setSafeModeEnabled(boolean safeModeEnabled)
    {
        this.safeModeEnabled = safeModeEnabled;
    }

    public int getStatus()
    {
        if (isAlive()) return StepDataInterface.STATUS_RUNNING;
        if (isStopped()) return StepDataInterface.STATUS_STOPPED;

        // Get the rest in StepDataInterface object:
        StepDataInterface sdi = trans.getStepDataInterface(stepname, stepcopy);
        if (sdi != null)
        {
            if (sdi.getStatus() == StepDataInterface.STATUS_DISPOSED && !isAlive()) return StepDataInterface.STATUS_FINISHED;
            return sdi.getStatus();
        }
        return StepDataInterface.STATUS_EMPTY;
    }

    /**
     * @return the partitionID
     */
    public String getPartitionID()
    {
        return partitionID;
    }

    /**
     * @param partitionID the partitionID to set
     */
    public void setPartitionID(String partitionID)
    {
        this.partitionID = partitionID;
    }

    /**
     * @return the partitionTargets
     */
    public Map getPartitionTargets()
    {
        return partitionTargets;
    }

    /**
     * @param partitionTargets the partitionTargets to set
     */
    public void setPartitionTargets(Map partitionTargets)
    {
        this.partitionTargets = partitionTargets;
    }

    /**
     * @return the partitionColumnIndex
     */
    public int getPartitionColumnIndex()
    {
        return partitionColumnIndex;
    }

    /**
     * @param partitionColumnIndex the partitionColumnIndex to set
     */
    public void setPartitionColumnIndex(int partitionColumnIndex)
    {
        this.partitionColumnIndex = partitionColumnIndex;
    }

    /**
     * @return the repartitioning
     */
    public int getRepartitioning()
    {
        return repartitioning;
    }

    /**
     * @param repartitioning the repartitioning to set
     */
    public void setRepartitioning(int repartitioning)
    {
        this.repartitioning = repartitioning;
    }

    /**
     * @return the partitioned
     */
    public boolean isPartitioned()
    {
        return partitioned;
    }

    /**
     * @param partitioned the partitioned to set
     */
    public void setPartitioned(boolean partitioned)
    {
        this.partitioned = partitioned;
    }

    /**
     * @return the partitionMerging
     */
    public boolean isPartitionMerging()
    {
        return partitionMerging;
    }

    /**
     * @param partitionMerging the partitionMerging to set
     */
    public void setPartitionMerging(boolean partitionMerging)
    {
        this.partitionMerging = partitionMerging;
    }

    protected boolean checkFeedback(long lines)
    {
        return getTransMeta().isFeedbackShown() && (lines > 0) && (getTransMeta().getFeedbackSize() > 0)
                && (lines % getTransMeta().getFeedbackSize()) == 0;
    }

    /**
     * @return the linesRejected
     */
    public long getLinesRejected()
    {
        return linesRejected;
    }

    /**
     * @param linesRejected the linesRejected to set
     */
    public void setLinesRejected(long linesRejected)
    {
        this.linesRejected = linesRejected;
    }    
}