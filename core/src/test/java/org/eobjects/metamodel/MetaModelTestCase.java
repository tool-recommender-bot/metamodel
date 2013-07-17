/**
 * eobjects.org MetaModel
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.metamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.DefaultRow;
import org.eobjects.metamodel.data.EmptyDataSet;
import org.eobjects.metamodel.data.InMemoryDataSet;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.data.SimpleDataSetHeader;
import org.eobjects.metamodel.query.SelectItem;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.ColumnType;
import org.eobjects.metamodel.schema.MutableColumn;
import org.eobjects.metamodel.schema.MutableRelationship;
import org.eobjects.metamodel.schema.MutableSchema;
import org.eobjects.metamodel.schema.MutableTable;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.TableType;

/**
 * Convenient super-class to use for unittesting
 */
public abstract class MetaModelTestCase extends TestCase {

    public static final String COLUMN_CONTRIBUTOR_COUNTRY = "country";
    public static final String COLUMN_CONTRIBUTOR_NAME = "name";
    public static final String COLUMN_CONTRIBUTOR_CONTRIBUTOR_ID = "contributor_id";

    public static final String COLUMN_PROJECT_PROJECT_ID = "project_id";
    public static final String COLUMN_PROJECT_NAME = "name";
    public static final String COLUMN_PROJECT_LINES_OF_CODE = "lines_of_code";
    public static final String COLUMN_PROJECT_PARENT_PROJECT_ID = "parent_project_id";

    public static final String COLUMN_ROLE_PROJECT_ID = "project_id";
    public static final String COLUMN_ROLE_CONTRIBUTOR_ID = "contributor_id";
    public static final String COLUMN_ROLE_ROLE_NAME = "name";

    public static final String COLUMN_PROJECT_CONTRIBUTOR_CONTRIBUTOR = "contributor";
    public static final String COLUMN_PROJECT_CONTRIBUTOR_ROLE = "role";
    public static final String COLUMN_PROJECT_CONTRIBUTOR_PROJECT = "project";

    public static final String TABLE_PROJECT_CONTRIBUTOR = "project_contributor";
    public static final String TABLE_ROLE = "role";
    public static final String TABLE_PROJECT = "project";
    public static final String TABLE_CONTRIBUTOR = "contributor";

    /**
     * Creates an example schema with three tables and a view:
     * <ul>
     * <li>contributor[contributor_id,name,country] (TABLE)</li>
     * <li>project[project_id,name,lines_of_code,parent_project_id] (TABLE)</li>
     * <li>role[contributor_id,project_id,role_name] (TABLE)</li>
     * <li>project_contributor[contributor,project,role] (VIEW)</li>
     * </ul>
     * The example schema is good for testing purposes and possess various
     * features of the schema model:
     * <ul>
     * <li>Relations between tables: one-Contributor-to-many-Role's and
     * many-Role's-to-one-Project</li>
     * <li>Recursive relations: A project can have a parent project</li>
     * <li>Views: The ProjectContributor view</li>
     * </ul>
     */
    protected Schema getExampleSchema() {
        MutableSchema schema = new MutableSchema("MetaModelSchema");

        MutableTable table1 = new MutableTable(TABLE_CONTRIBUTOR, TableType.TABLE, schema);
        Column column1 = new MutableColumn(COLUMN_CONTRIBUTOR_CONTRIBUTOR_ID, ColumnType.INTEGER, table1, 0, false)
                .setIndexed(true).setPrimaryKey(true);
        Column column2 = new MutableColumn(COLUMN_CONTRIBUTOR_NAME, ColumnType.VARCHAR, table1, 1, false);
        Column column3 = new MutableColumn(COLUMN_CONTRIBUTOR_COUNTRY, ColumnType.VARCHAR, table1, 2, true);
        table1.setColumns(column1, column2, column3);

        MutableTable table2 = new MutableTable(TABLE_PROJECT, TableType.TABLE, schema);
        Column column4 = new MutableColumn(COLUMN_PROJECT_PROJECT_ID, ColumnType.INTEGER, table2, 0, false)
                .setPrimaryKey(true);
        Column column5 = new MutableColumn(COLUMN_PROJECT_NAME, ColumnType.VARCHAR, table2, 1, false);
        Column column6 = new MutableColumn(COLUMN_PROJECT_LINES_OF_CODE, ColumnType.BIGINT, table2, 2, true);
        Column column7 = new MutableColumn(COLUMN_PROJECT_PARENT_PROJECT_ID, ColumnType.INTEGER, table2, 3, true);
        table2.setColumns(column4, column5, column6, column7);

        MutableTable table3 = new MutableTable(TABLE_ROLE, TableType.TABLE, schema);
        Column column8 = new MutableColumn(COLUMN_ROLE_CONTRIBUTOR_ID, ColumnType.INTEGER, table3, 0, false)
                .setPrimaryKey(true);
        Column column9 = new MutableColumn(COLUMN_ROLE_PROJECT_ID, ColumnType.INTEGER, table3, 1, false)
                .setPrimaryKey(true);
        Column column10 = new MutableColumn(COLUMN_ROLE_ROLE_NAME, ColumnType.VARCHAR, table3, 2, false);
        table3.setColumns(column8, column9, column10);

        MutableTable table4 = new MutableTable(TABLE_PROJECT_CONTRIBUTOR, TableType.VIEW, schema);
        Column column11 = new MutableColumn(COLUMN_PROJECT_CONTRIBUTOR_CONTRIBUTOR, ColumnType.VARCHAR, table4, 0,
                false);
        Column column12 = new MutableColumn(COLUMN_PROJECT_CONTRIBUTOR_PROJECT, ColumnType.VARCHAR, table4, 1, false);
        Column column13 = new MutableColumn(COLUMN_PROJECT_CONTRIBUTOR_ROLE, ColumnType.VARCHAR, table4, 2, false);
        ArrayList<Column> columnList = new ArrayList<Column>();
        columnList.add(column11);
        columnList.add(column12);
        columnList.add(column13);
        table4.setColumns(columnList);

        // one-Contributor-to-many-Role's
        MutableRelationship.createRelationship(new Column[] { column1 }, new Column[] { column8 });

        // one-Project-to-many-Role's
        MutableRelationship.createRelationship(new Column[] { column4 }, new Column[] { column9 });

        // view relation [contributor -> contributor_name]
        MutableRelationship.createRelationship(new Column[] { column2 }, new Column[] { column11 });

        // view relation [project -> project_name]
        MutableRelationship.createRelationship(new Column[] { column5 }, new Column[] { column12 });

        // view relation [role -> role_name]
        MutableRelationship.createRelationship(new Column[] { column10 }, new Column[] { column13 });

        schema.setTables(table1, table2, table3, table4);
        return schema;
    }

    protected static DataSet createDataSet(SelectItem[] selectItems, List<Object[]> data) {
        if (data.isEmpty()) {
            return new EmptyDataSet(selectItems);
        }

        SimpleDataSetHeader header = new SimpleDataSetHeader(selectItems);

        List<Row> rows = new ArrayList<Row>();
        for (Object[] objects : data) {
            rows.add(new DefaultRow(header, objects));
        }
        return new InMemoryDataSet(header, rows);
    }

    private List<Object> _mocks = new ArrayList<Object>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _mocks.clear();
    }

    public <T extends Object> T createMock(Class<T> clazz) {
        T mock = EasyMock.createMock(clazz);
        _mocks.add(mock);
        return mock;
    }

    public void verifyMocks() {
        EasyMock.verify(_mocks.toArray());
    }

    public void replayMocks() {
        EasyMock.replay(_mocks.toArray());
    }

    public void assertEquals(DataSet ds1, DataSet ds2) {
        assertEquals(Arrays.toString(ds1.getSelectItems()), Arrays.toString(ds2.getSelectItems()));
        boolean ds1next = true;
        while (ds1next) {
            ds1next = ds1.next();
            boolean ds2next = ds2.next();
            assertEquals("DataSet 1 next=" + ds1next, ds1next, ds2next);
            if (ds1next) {
                Row row1 = ds1.getRow();
                Row row2 = ds2.getRow();
                assertEquals(row1, row2);
            }
        }
    }

    protected File getTestResourceAsFile(String filename) {
        return new File("src/test/resources/" + filename);
    }
}