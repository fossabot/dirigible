package org.eclipse.dirigible.repository.datasource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.eclipse.dirigible.repository.ext.db.DBUtils;
import org.eclipse.dirigible.repository.ext.db.dialect.IDialectSpecifier;
import org.eclipse.dirigible.repository.logging.Logger;

/**
 * Convenience class for common DataSource operations. 
 * An instance represents a single DataSource.
 *
 */
@SuppressWarnings("javadoc")
public class DataSources {

	private static final Logger logger = Logger.getLogger(DataSources.class);

	private static final String PRCNT = "%"; //$NON-NLS-1$
	private static final String CBC = "] "; //$NON-NLS-1$
	private static final String CBO = " ["; //$NON-NLS-1$

	private static final String COLUMN_NAME = "COLUMN_NAME"; //$NON-NLS-1$
	private static final String TYPE_NAME = "TYPE_NAME"; //$NON-NLS-1$
	private static final String COLUMN_SIZE = "COLUMN_SIZE"; //$NON-NLS-1$
	private static final String EMPTY = ""; //$NON-NLS-1$
	private static final String PK = "PK"; //$NON-NLS-1$
	private static final String IS_NULLABLE = "IS_NULLABLE"; //$NON-NLS-1$

	private static final String INDEX_NAME = "INDEX_NAME"; //$NON-NLS-1$
	private static final String TYPE_INDEX = "TYPE"; //$NON-NLS-1$
	private static final String NON_UNIQUE = "NON_UNIQUE"; //$NON-NLS-1$
	private static final String INDEX_QUALIFIER = "INDEX_QUALIFIER"; //$NON-NLS-1$
	private static final String ORDINAL_POSITION = "ORDINAL_POSITION"; //$NON-NLS-1$
	private static final String ASC_OR_DESC = "ASC_OR_DESC"; //$NON-NLS-1$
	private static final String CARDINALITY = "CARDINALITY"; //$NON-NLS-1$
	private static final String PAGES_INDEX = "PAGES"; //$NON-NLS-1$
	private static final String FILTER_CONDITION = "FILTER_CONDITION"; //$NON-NLS-1$

	static final String DEFAULT_DATASOURCE_NAME = "Default";

	public enum Config {
		ShowTableContentScript, SchemaFilterScript;
	}

	String dsName;
	Connection conn;
	DatabaseMetaData dmd;
	IDialectSpecifier dialectSpecifier;
	Properties dsConfig;

	public DataSources(String dsName, Connection conn) throws SQLException {
		this.dsName = dsName;
		if(conn==null)
			throw new IllegalArgumentException("Connection is null");
		if(conn.isClosed())
			throw new IllegalStateException("Connection is in closed state");
		this.conn = conn;
		this.dmd = this.conn.getMetaData();
		String productName = dmd.getDatabaseProductName();
		this.dialectSpecifier = IDialectSpecifier.Factory.getInstance(productName);
		if (this.dialectSpecifier == null) {
			this.dialectSpecifier = IDialectSpecifier.Factory.getInstance(dsName);
		}
		this.dsConfig = org.eclipse.dirigible.repository.datasource.DataSourceFacade.getInstance().getNamedDataSourceConfig(dsName);
	}
	
	public DataSources(String dsName) throws SQLException {
		DataSource ds = DataSourceFacade.getInstance().getNamedDataSource(null, dsName);
		this.conn = ds.getConnection();
		this.dmd = this.conn.getMetaData();
		String productName = dmd.getDatabaseProductName();
		this.dialectSpecifier = IDialectSpecifier.Factory.getInstance(productName);
		if (this.dialectSpecifier == null) {
			this.dialectSpecifier = IDialectSpecifier.Factory.getInstance(dsName);
		}
		this.dsConfig = org.eclipse.dirigible.repository.datasource.DataSourceFacade.getInstance().getNamedDataSourceConfig(dsName);		
	}
	
	public DatabaseMetaData getDataBaseMetadata() {
		return this.dmd;
	}
	
	public IDialectSpecifier getDialect(){
		return this.dialectSpecifier;
	}

	public String getDataSourceLabel() throws SQLException {
		String productName = dmd.getDatabaseProductName()!=null?dmd.getDatabaseProductName():this.dsConfig.getProperty("ds.id");
		String productVersion = dmd.getDatabaseProductVersion()!=null? CBO + dmd.getDatabaseProductVersion() + CBC :"";
		String driverName = dmd.getDriverName()!=null?dmd.getDriverName():"";
		return  productName + productVersion + driverName;
	}

	public interface Filter<T> {
		boolean accepts(T t);
	}

	public List<String> listTableNames(String catalogName, String schemeName, Filter<String> tableNameFilter) throws SQLException {

		List<String> listOfTables = new ArrayList<String>();

		ResultSet rs = null;
		if (this.dialectSpecifier.isCatalogForSchema()) {
			rs = this.dmd.getTables(schemeName, null, PRCNT, DBUtils.TABLE_TYPES);
		} else {
			rs = this.dmd.getTables(catalogName, schemeName, PRCNT, DBUtils.TABLE_TYPES);
		}

		while (rs.next()) {
			String tableName = rs.getString(3);
			if ((tableNameFilter != null) && !tableNameFilter.accepts(tableName)) {
				continue;
			}
			listOfTables.add(tableName);
		}
		rs.close();

		return listOfTables;
	}

	public List<String> listSchemeNames(String catalogName, Filter<String> schemaNameFilter) throws SQLException {

		List<String> listOfSchemes = new ArrayList<String>();
		ResultSet rs = null;

		try {

			if (this.dialectSpecifier.isSchemaFilterSupported()) {
				try {
					// low level filtering for schema
					rs = this.conn.createStatement().executeQuery(this.dialectSpecifier.getSchemaFilterScript());
				} catch (Exception e) {
					// backup in case of wrong product recognition
					rs = this.dmd.getSchemas(catalogName, null);
				} finally {
					if (rs != null) {
						rs.close();
					}
				}
			} else if (dialectSpecifier.isCatalogForSchema()) {
				rs = this.dmd.getCatalogs();
			} else {
				rs = this.dmd.getSchemas(catalogName, null);
			}
			if (rs != null) {
				while (rs.next()) {
					String schemeName = rs.getString(1);
					// higher level filtering for schema if low level is not supported
					if ((schemaNameFilter != null) && !schemaNameFilter.accepts(schemeName)) {
						continue;
					}
					listOfSchemes.add(schemeName);
				}
			}

		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		return listOfSchemes;
	}

	public interface ColumnsIteratorCallback {
		void onColumn(String name, String type, String size, String isNullable, String isKey);
	}

	public interface IndicesIteratorCallback {
		void onIndex(String indexName, String indexType, String columnName, String isNonUnique, String indexQualifier, String ordinalPosition,
				String sortOrder, String cardinality, String pagesIndex, String filterCondition);
	}

	public void iterateTableDefinition(String tableName, String catalogName, String schemaName, ColumnsIteratorCallback columnsIteratorCallback,
			IndicesIteratorCallback indicesIteratorCallback) throws SQLException {

		ResultSet columns = this.dmd.getColumns(catalogName, schemaName, tableName, null);
		if(columns==null)
			throw new SQLException("DatabaseMetaData.getColumns returns null");
		ResultSet pks = this.dmd.getPrimaryKeys(catalogName, schemaName, tableName);
		if(pks==null)
			throw new SQLException("DatabaseMetaData.getPrimaryKeys returns null");
		ResultSet indexes = this.dmd.getIndexInfo(catalogName, schemaName, tableName, false, false);
		if(indexes==null)
			throw new SQLException("DatabaseMetaData.getIndexInfo returns null");

		try {

			List<String> pkList = new ArrayList<String>();
			while (pks.next()) {
				String pkName = pks.getString(COLUMN_NAME);
				pkList.add(pkName);
			}

			while (columns.next()) {
				if (columnsIteratorCallback != null) {
					String cname = columns.getString(COLUMN_NAME);
					columnsIteratorCallback.onColumn(cname, columns.getString(TYPE_NAME), columns.getInt(COLUMN_SIZE) + EMPTY,
							columns.getString(IS_NULLABLE), pkList.contains(cname) ? PK : EMPTY);
				}
			}
			while (indexes.next()) {
				if (indicesIteratorCallback != null) {
					indicesIteratorCallback.onIndex(indexes.getString(INDEX_NAME), indexes.getString(TYPE_INDEX), indexes.getString(COLUMN_NAME),
							indexes.getString(NON_UNIQUE), indexes.getString(INDEX_QUALIFIER), indexes.getShort(ORDINAL_POSITION) + EMPTY,
							indexes.getString(ASC_OR_DESC), indexes.getInt(CARDINALITY) + EMPTY, indexes.getInt(PAGES_INDEX) + EMPTY,
							indexes.getString(FILTER_CONDITION));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			columns.close();
			indexes.close();
			pks.close();
		}
	}
	
	public interface RequestExecutionCallback {
		void updateDone(int recordsCount);
		void queryDone(ResultSet rs);
		void error(Throwable t);
	}
	
	public void executeSingleStatement(String sql, boolean isQuery, RequestExecutionCallback callback) {
		ResultSet resultSet = null;
		PreparedStatement preparedStatement = null;
		try{
			preparedStatement = this.conn.prepareStatement(sql);
			preparedStatement.closeOnCompletion();
			if (isQuery) {
				preparedStatement.executeQuery();
				resultSet = preparedStatement.getResultSet();
				callback.queryDone(resultSet);
			} else {
				preparedStatement.executeUpdate();
				callback.updateDone(preparedStatement.getUpdateCount());
			}
		} catch (Exception e){
			callback.error(e);
		} finally {
			try {
				if(resultSet!=null)
					resultSet.close();
			} catch (SQLException e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

}