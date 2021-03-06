/*
 * Copyright (c) 2017 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * SAP - initial API and implementation
 */

package org.eclipse.dirigible.api.v3.test;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class Process.
 */
public class Process {

	/** The name. */
	private String name;

	/** The tasks. */
	private List<Task> tasks = new ArrayList<Task>();

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the tasks.
	 *
	 * @return the tasks
	 */
	public List<Task> getTasks() {
		return tasks;
	}

	/**
	 * Creates the task.
	 *
	 * @param taskName the task name
	 * @return the task
	 */
	public Task createTask(String taskName) {
		Task task = new Task();
		task.setName(taskName);
		tasks.add(task);
		return task;
	}

	/**
	 * Exists task.
	 *
	 * @param task the task
	 * @return true, if successful
	 */
	public boolean existsTask(Task task) {
		return tasks.contains(task);
	}

}
