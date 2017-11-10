/*
 * Copyright (c) 2017 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * SAP - initial API and implementation
 */

package org.eclipse.dirigible.api.v3.utils;

import org.eclipse.dirigible.commons.utils.xml2json.Xml2Json;

/**
 * The Class Xml2JsonFacade.
 */
public class Xml2JsonFacade {

	/**
	 * Converts JSON to XML
	 *
	 * @param json
	 * @return the JSON as XML
	 * @throws Exception
	 */
	public static final String fromJson(String json) throws Exception {
		return Xml2Json.toXml(json);
	}

	/**
	 * Converts XML to JSON
	 *
	 * @param xml
	 * @return the XML as JSON
	 * @throws Exception
	 */
	public static final String toJson(String xml) throws Exception {
		return Xml2Json.toJson(xml);
	}
}
