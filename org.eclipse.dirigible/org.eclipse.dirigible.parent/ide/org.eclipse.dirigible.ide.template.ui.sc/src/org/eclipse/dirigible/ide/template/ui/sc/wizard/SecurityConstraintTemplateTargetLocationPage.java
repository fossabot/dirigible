/*******************************************************************************
 * Copyright (c) 2015 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * SAP - initial API and implementation
 *******************************************************************************/

package org.eclipse.dirigible.ide.template.ui.sc.wizard;

import org.eclipse.dirigible.ide.template.ui.common.GenerationModel;
import org.eclipse.dirigible.ide.template.ui.common.TemplateTargetLocationPage;
import org.eclipse.dirigible.ide.ui.common.validation.IValidationStatus;
import org.eclipse.dirigible.repository.api.ICommonConstants;
import org.eclipse.dirigible.repository.ext.utils.CommonUtils;

public class SecurityConstraintTemplateTargetLocationPage extends TemplateTargetLocationPage {

	private static final String SELECT_THE_TARGET_LOCATION_AND_THE_TARGET_FILE_NAME = Messages.SecurityConstraintTemplateTargetLocationPage_SELECT_THE_TARGET_LOCATION_AND_THE_TARGET_FILE_NAME;

	private static final String TARGET_LOCATION = Messages.SecurityConstraintTemplateTargetLocationPage_TARGET_LOCATION;

	private static final long serialVersionUID = 5413819137031452222L;

	private static final String PAGE_NAME = "org.eclipse.dirigible.ide.template.ui.sc.wizard.SecurityConstraintTemplateTargetLocationPage"; //$NON-NLS-1$

	private SecurityConstraintTemplateModel model;

	protected SecurityConstraintTemplateTargetLocationPage(SecurityConstraintTemplateModel model) {
		super(PAGE_NAME);
		this.model = model;
		setTitle(TARGET_LOCATION);
		setDescription(SELECT_THE_TARGET_LOCATION_AND_THE_TARGET_FILE_NAME);
	}

	@Override
	protected void checkPageStatus() {
		if ((getModel().getTargetLocation() == null) || "".equals(getModel().getTargetLocation())) { //$NON-NLS-1$
			setPageComplete(false);
			return;
		}
		if ((getModel().getFileName() == null) || "".equals(getModel().getFileName())) { //$NON-NLS-1$
			setPageComplete(false);
			return;
		}
		IValidationStatus status = model.validateLocation();
		if (status.hasErrors()) {
			setErrorMessage(status.getMessage());
			setPageComplete(false);
		} else if (status.hasWarnings()) {
			setErrorMessage(status.getMessage());
			setPageComplete(true);
		} else {
			setErrorMessage(null);
			setPageComplete(true);
		}
	}

	@Override
	protected String getArtifactContainerName() {
		return ICommonConstants.ARTIFACT_TYPE.SECURITY_CONSTRAINTS;
	}

	@Override
	protected GenerationModel getModel() {
		return model;
	}

	@Override
	protected String getDefaultFileName(String preset) {
		String defaultName = (preset == null) ? "main.access" : CommonUtils.getFileNameNoExtension(preset) + ".access"; //$NON-NLS-1$
		return defaultName;
	}

}
