/*
 * Copyright (c) 2017 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * SAP - initial API and implementation
 */

exports.getMenu = function() {
	var menu = {
			"name":"File",
			"link":"#",
			"order":"100",
			"items":[
				{
					"name":"Save All",
					"link":"#",
					"order":"105",
					"event":"workbench.editor.save",
					"data": ""
				},
				{
					"name":"Exit",
					"link":"/logout",
					"order":"199",
					"event":"workbench.editor.save",
					"data": ""
				}
			]
		};
	return menu;
}
