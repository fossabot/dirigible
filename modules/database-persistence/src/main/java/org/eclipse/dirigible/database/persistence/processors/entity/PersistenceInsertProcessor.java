/*
 * Copyright (c) 2017 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * SAP - initial API and implementation
 */

package org.eclipse.dirigible.database.persistence.processors.entity;

import static java.text.MessageFormat.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.GenerationType;

import org.eclipse.dirigible.database.persistence.IEntityManagerInterceptor;
import org.eclipse.dirigible.database.persistence.PersistenceException;
import org.eclipse.dirigible.database.persistence.model.PersistenceTableColumnModel;
import org.eclipse.dirigible.database.persistence.model.PersistenceTableModel;
import org.eclipse.dirigible.database.persistence.processors.AbstractPersistenceProcessor;
import org.eclipse.dirigible.database.persistence.processors.identity.PersistenceNextValueIdentityProcessor;
import org.eclipse.dirigible.database.persistence.processors.sequence.PersistenceNextValueSequenceProcessor;
import org.eclipse.dirigible.database.sql.SqlFactory;
import org.eclipse.dirigible.database.sql.builders.records.InsertBuilder;
import org.eclipse.dirigible.database.sql.builders.sequence.LastValueIdentityBuilder;

/**
 * The Persistence Insert Processor.
 */
public class PersistenceInsertProcessor extends AbstractPersistenceProcessor {

	/**
	 * Instantiates a new persistence insert processor.
	 *
	 * @param entityManagerInterceptor
	 *            the entity manager interceptor
	 */
	public PersistenceInsertProcessor(IEntityManagerInterceptor entityManagerInterceptor) {
		super(entityManagerInterceptor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.dirigible.database.persistence.processors.AbstractPersistenceProcessor#generateScript(java.sql.
	 * Connection, org.eclipse.dirigible.database.persistence.model.PersistenceTableModel)
	 */
	@Override
	protected String generateScript(Connection connection, PersistenceTableModel tableModel) {
		InsertBuilder insertBuilder = SqlFactory.getNative(SqlFactory.deriveDialect(connection)).insert().into(tableModel.getTableName());
		for (PersistenceTableColumnModel columnModel : tableModel.getColumns()) {
			if (columnModel.isIdentity()) {
				continue;
			}
			insertBuilder.column(columnModel.getName());
		}
		String sql = insertBuilder.build();
		return sql;
	}

	/**
	 * Insert.
	 *
	 * @param connection
	 *            the connection
	 * @param tableModel
	 *            the table model
	 * @param pojo
	 *            the pojo
	 * @return the object
	 * @throws PersistenceException
	 *             the persistence exception
	 */
	public Object insert(Connection connection, PersistenceTableModel tableModel, Object pojo) throws PersistenceException {
		Object result = 0;
		String sql = null;
		PreparedStatement preparedStatement = null;
		try {
			boolean identified = setGeneratedValues(connection, tableModel, pojo);
			sql = generateScript(connection, tableModel);
			preparedStatement = openPreparedStatement(connection, sql);
			setValuesFromPojo(tableModel, pojo, preparedStatement);
			preparedStatement.executeUpdate();
			if (identified) {
				result = getPrimaryKeyValue(tableModel, pojo);
			} else {
				result = getLastInserted(connection, tableModel, pojo);
				for (PersistenceTableColumnModel column : tableModel.getColumns()) {
					if (column.isPrimaryKey() && column.isIdentity()) {
						setValueToPojo(pojo, result, column);
						break;
					}
				}
			}
		} catch (Exception e) {
			throw new PersistenceException(sql, e);
		} finally {
			closePreparedStatement(preparedStatement);
		}
		return result;
	}

	private Object getLastInserted(Connection connection, PersistenceTableModel tableModel, Object pojo) throws SQLException {
		LastValueIdentityBuilder identityBuilder = SqlFactory.getNative(SqlFactory.deriveDialect(connection)).lastval(tableModel.getTableName());
		String sql = identityBuilder.build();
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = openPreparedStatement(connection, sql);
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next()) {
				long id = rs.getLong(1);
				return id;
			}
		} finally {
			closePreparedStatement(preparedStatement);
		}
		return -1;
	}

	/**
	 * Sets the generated values.
	 *
	 * @param connection
	 *            the connection
	 * @param tableModel
	 *            the table model
	 * @param pojo
	 *            the pojo
	 * @throws NoSuchFieldException
	 *             the no such field exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws SQLException
	 *             the SQL exception
	 */
	private boolean setGeneratedValues(Connection connection, PersistenceTableModel tableModel, Object pojo)
			throws NoSuchFieldException, IllegalAccessException, SQLException {
		for (PersistenceTableColumnModel columnModel : tableModel.getColumns()) {
			if (columnModel.isPrimaryKey() && (columnModel.getGenerated() != null)) {
				long id = -1;
				if (GenerationType.SEQUENCE.name().equals(columnModel.getGenerated())) {
					PersistenceNextValueSequenceProcessor persistenceNextValueSequenceProcessor = new PersistenceNextValueSequenceProcessor(
							getEntityManagerInterceptor());
					id = persistenceNextValueSequenceProcessor.nextval(connection, tableModel);
				} else if (GenerationType.TABLE.name().equals(columnModel.getGenerated())) {
					PersistenceNextValueIdentityProcessor persistenceNextValueIdentityProcessor = new PersistenceNextValueIdentityProcessor(
							getEntityManagerInterceptor());
					id = persistenceNextValueIdentityProcessor.nextval(connection, tableModel);
				} else if (GenerationType.IDENTITY.name().equals(columnModel.getGenerated())) {
					return false;
				} else {
					throw new IllegalArgumentException(format("Generation Type: [{0}] not supported.", columnModel.getGenerated()));
				}
				setValueToPojo(pojo, id, columnModel);
			}
		}
		return true;
	}

	/**
	 * Gets the primary key value.
	 *
	 * @param tableModel
	 *            the table model
	 * @param pojo
	 *            the pojo
	 * @return the primary key value
	 * @throws NoSuchFieldException
	 *             the no such field exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws SQLException
	 *             the SQL exception
	 */
	private Object getPrimaryKeyValue(PersistenceTableModel tableModel, Object pojo)
			throws NoSuchFieldException, IllegalAccessException, SQLException {
		for (PersistenceTableColumnModel columnModel : tableModel.getColumns()) {
			if (columnModel.isPrimaryKey()) {
				return getValueFromPojo(pojo, columnModel);
			}
		}
		return null;
	}

}
