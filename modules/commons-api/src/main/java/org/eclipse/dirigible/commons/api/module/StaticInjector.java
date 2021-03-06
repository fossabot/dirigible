/*
 * Copyright (c) 2017 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * SAP - initial API and implementation
 */

package org.eclipse.dirigible.commons.api.module;

import com.google.inject.Injector;

/**
 * The StaticInjector holds a static instance of a Guice injector in cases when there is no execution context (e.g.
 * HttpRequest/HttpResponse lacks in asynchronous tasks).
 */
public class StaticInjector {

	private static Injector injector;

	/**
	 * Gets the injector.
	 *
	 * @return returns injector
	 */
	public static Injector getInjector() {
		return injector;
	}

	/**
	 * Sets the injector.
	 *
	 * @param injector
	 *            the new injector
	 */
	public static void setInjector(Injector injector) {
		StaticInjector.injector = injector;
	}

}
