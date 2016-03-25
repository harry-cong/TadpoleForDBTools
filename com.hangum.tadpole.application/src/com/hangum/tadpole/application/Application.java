/*******************************************************************************
 * Copyright (c) 2013 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.application;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

import com.hangum.tadpole.application.initialize.wizard.SystemInitializeWizard;
import com.hangum.tadpole.application.start.ApplicationWorkbenchAdvisor;
import com.hangum.tadpole.commons.exception.dialog.ExceptionDetailsErrorDialog;
import com.hangum.tadpole.engine.initialize.TadpoleSystemInitializer;
import com.hangum.tadpole.rdb.core.Activator;

/**
 * This class controls all aspects of the application's execution
 * and is contributed through the plugin.xml.
 */
public class Application implements EntryPoint {
	private static final Logger logger = Logger.getLogger(Application.class);

	public int createUI() {
		Display display = PlatformUI.createDisplay();

		Locale locale = RWT.getLocale();
		Locale.setDefault(locale);
		RWT.getUISession().setLocale(locale);
		RWT.setLocale(locale);
		
		systemInitialize();
	
		WorkbenchAdvisor advisor = new ApplicationWorkbenchAdvisor();		
		return PlatformUI.createAndRunWorkbench( display, advisor );
	}
	
	/**
	 * System initialize
	 * If the system table does not exist, create a table.
	 */
	private void systemInitialize() {
		try {
			boolean isInitialize = TadpoleSystemInitializer.initSystem();
			if(!isInitialize) {
				if(logger.isInfoEnabled()) logger.info("Initialize System default setting.");
				
				WizardDialog dialog = new WizardDialog(null, new SystemInitializeWizard());
				if(Dialog.OK != dialog.open()) {
					throw new Exception("System initialize fail.\n");
				}
			}
		} catch(Exception e) {
			logger.error("System initialize error", e); //$NON-NLS-1$
			Status errStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e); //$NON-NLS-1$
			ExceptionDetailsErrorDialog.openError(null, Messages.get().Error, com.hangum.tadpole.application.start.Messages.get().ApplicationWorkbenchWindowAdvisor_2, errStatus); //$NON-NLS-1$
			
			System.exit(0);
		}
	}
}
