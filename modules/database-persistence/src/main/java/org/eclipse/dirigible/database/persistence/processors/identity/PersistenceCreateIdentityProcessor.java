/*
 * Copyright (c) 2017 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * SAP - initial API and implementation
 */

package org.eclipse.dirigible.database.persistence.processors.identity;

import java.sql.Connection;

import org.eclipse.dirigible.database.persistence.IEntityManagerInterceptor;
import org.eclipse.dirigible.database.persistence.PersistenceException;
import org.eclipse.dirigible.database.persistence.PersistenceManager;
import org.eclipse.dirigible.database.persistence.model.PersistenceTableModel;
import org.eclipse.dirigible.database.persistence.processors.AbstractPersistenceProcessor;

/**
 * The Persistence Create Identity Processor.
 */
public class PersistenceCreateIdentityProcessor extends AbstractPersistenceProcessor {

	/**
	 * Instantiates a new persistence create identity processor.
	 *
	 * @param entityManagerInterceptor
	 *            the entity manager interceptor
	 */
	public PersistenceCreateIdentityProcessor(IEntityManagerInterceptor entityManagerInterceptor) {
		super(entityManagerInterceptor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.dirigible.database.persistence.processors.AbstractPersistenceProcessor#generateScript(java.sql.
	 * Connection, org.eclipse.dirigible.database.persistence.model.PersistenceTableModel)
	 */
	@Override
	protected String generateScript(Connection connection, PersistenceTableModel tableModel) {
		return null;
	}

	/**
	 * Creates the.
	 *
	 * @param connection
	 *            the connection
	 * @param tableModel
	 *            the table model
	 * @return the int
	 * @throws PersistenceException
	 *             the persistence exception
	 */
	public int create(Connection connection, PersistenceTableModel tableModel) throws PersistenceException {
		PersistenceManager<Identity> persistenceManager = new PersistenceManager<Identity>();
		if (!persistenceManager.tableExists(connection, Identity.class)) {
			persistenceManager.tableCreate(connection, Identity.class);
		}
		return 0;
	}

}
