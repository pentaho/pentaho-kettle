package org.pentaho.di.ui.trans.steps.jsoninput.getfields;

import org.eclipse.swt.widgets.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.trans.steps.jsoninput.JsonInputMeta;
import org.pentaho.di.trans.steps.jsoninput.json.JsonSampler;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.TableView;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class GetFieldsDialogTest {
    private GetFieldsDialog mockGetFieldsDialog;
    private JsonInputMeta jsonInputMeta;

    private JsonSampler mockJsonSampler;
    private Shell mockShell;
    private TableView mockTableView;
    private Tree mockTree;
    private Field pathsField;

    @Before
    //mock UI elements
    public void setup() {
        jsonInputMeta = new JsonInputMeta();

        mockJsonSampler = mock( JsonSampler.class );
        mockShell = mock( Shell.class );
        mockTableView = mock( TableView.class );
        mockTree = mock( Tree.class );
        mockTableView.table = mock( Table.class );

        TableItem tableItem = mock( TableItem.class );
        GUIResource guiResource = mock( GUIResource.class );

        when( guiResource.getImageLogoSmall() ).thenReturn(null);
        when( mockTableView.table.getItem( anyInt() ) ).thenReturn( tableItem );

        try ( MockedStatic<GUIResource> mockedStatic = mockStatic( GUIResource.class ) ) {
            mockedStatic.when( GUIResource::getInstance ).thenReturn( guiResource );
            mockGetFieldsDialog = mock( GetFieldsDialog.class, CALLS_REAL_METHODS );
            mockGetFieldsDialog.meta = jsonInputMeta;

            pathsField = mockGetFieldsDialog.getClass().getDeclaredField("paths");
            pathsField.setAccessible( true );
        } catch ( NoSuchFieldException e ) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOnOkWithChangesMetaChanged() throws IllegalAccessException {
        java.util.List<String> prevPaths = Arrays.asList( "path1", "path2", "path3" );
        java.util.List<String> afterPaths = Arrays.asList( "details1:path3:details2", "details3:path1:details4" );

        when( mockJsonSampler.getChecked( mockTree ) ).thenReturn( afterPaths );

        pathsField.set( mockGetFieldsDialog, prevPaths );

        mockGetFieldsDialog.ok( mockJsonSampler, mockTree, mockShell, mockTableView );

        assertTrue( jsonInputMeta.hasChanged() );
    }

    @Test
    public void testOnOkNoChangesNoMetaChanged() throws IllegalAccessException {
        java.util.List<String> prevPaths = Arrays.asList( "path1", "path2", "path3" );
        List<String> afterPaths= Arrays.asList( "details1:path3:details2", "details3:path1:details4", "details5:path2:details6" );

        when( mockJsonSampler.getChecked( mockTree ) ).thenReturn( afterPaths );

        pathsField.set( mockGetFieldsDialog, prevPaths );

        mockGetFieldsDialog.ok( mockJsonSampler, mockTree, mockShell, mockTableView );

        assertFalse( jsonInputMeta.hasChanged() );
    }

    @Test
    public void testOnClearNoMetaChanged() throws IllegalAccessException {
        TreeItem treeItem = mock( TreeItem.class );
        java.util.List<String> prevPaths = Arrays.asList( "path1", "path2", "path3" );

        when( treeItem.getItems() ).thenReturn( new TreeItem[0] );
        pathsField.set( mockGetFieldsDialog, prevPaths );

        mockGetFieldsDialog.clearSelection( treeItem );

        assertTrue( jsonInputMeta.hasChanged() );
    }
}
