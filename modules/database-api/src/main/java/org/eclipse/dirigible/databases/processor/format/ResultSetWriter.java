/*
 * Copyright (c) 2017 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * SAP - initial API and implementation
 */

package org.eclipse.dirigible.databases.processor.format;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The ResultSet Writer.
 *
 * @param <T>
 *            the generic type
 */
public interface ResultSetWriter<T> {

	/**
	 * Write the provided ResultSet.
	 *
	 * @param rs
	 *            the rs
	 * @return the t
	 * @throws SQLException
	 *             the SQL exception
	 */
	T write(ResultSet rs) throws SQLException;

}
