package org.pentaho.di.trans.steps.scriptvalues_mod;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.steps.StepMockUtil;
import static org.mockito.Mockito.*;


/**
 * @author Andrea Torre
 */
public class ScriptValuesAddedFunctionsTest {
    ScriptValuesMod step;
    ScriptValuesMetaMod meta;
    ScriptValuesModData data;

    @BeforeClass
    public static void initKettle() throws Exception {
        KettleEnvironment.init();
    }

    @Before
    public void setUp() throws Exception {
        step = StepMockUtil.getStep( ScriptValuesMod.class, ScriptValuesMetaMod.class, "test" );
        meta = new ScriptValuesMetaMod();
        data = new ScriptValuesModData();

        RowMeta input = new RowMeta();
        input.addValueMeta(new ValueMetaString("variable_name"));
        input.addValueMeta(new ValueMetaString("variable_data"));
        step.setInputRowMeta(input);
        step = spy(step);

        doReturn(new Object[] {"var_1", "don't panic"}).when(step).getRow();
        meta.setCompatible(false);
        meta.allocate(0);
    }

    @Test
    public void shouldSetVariablesAtParentJobLevel() throws Exception{
        meta.setJSScripts( new ScriptValuesScript[] {
                new ScriptValuesScript( ScriptValuesScript.TRANSFORM_SCRIPT, "script",
                        "setVariable(variable_name, variable_data, 'p')" )
        } );

        step.init( meta, data );

        VariableSpace mock_transVariableSpace = mock(VariableSpace.class);
        VariableSpace mock_parentJobVariableSpace = mock(VariableSpace.class);

        mock_transVariableSpace.setParentVariableSpace(mock_parentJobVariableSpace);
        when(mock_transVariableSpace.getParentVariableSpace()).thenReturn(mock_parentJobVariableSpace);
        step.setParentVariableSpace(mock_transVariableSpace);

        step.processRow(meta, data);

        verify(mock_parentJobVariableSpace).setVariable("var_1", "don't panic");
    }

    @Test
    public void shouldSetVariablesAtGrandParentJobLevel() throws Exception{

        meta.setJSScripts( new ScriptValuesScript[] {
                new ScriptValuesScript( ScriptValuesScript.TRANSFORM_SCRIPT, "script",
                        "setVariable(variable_name, variable_data, 'g')" )
        } );


        step.init( meta, data );

        VariableSpace mock_transVariableSpace = mock(VariableSpace.class);
        VariableSpace mock_parentJobVariableSpace = mock(VariableSpace.class);
        VariableSpace mock_grandParentJobVariableSpace = mock(VariableSpace.class);

        when(mock_transVariableSpace.getParentVariableSpace()).thenReturn(mock_parentJobVariableSpace);
        when(mock_parentJobVariableSpace.getParentVariableSpace()).thenReturn(mock_grandParentJobVariableSpace);

        step.setParentVariableSpace(mock_transVariableSpace);

        step.processRow(meta, data);
        verify(mock_grandParentJobVariableSpace).setVariable("var_1", "don't panic");
    }
}
