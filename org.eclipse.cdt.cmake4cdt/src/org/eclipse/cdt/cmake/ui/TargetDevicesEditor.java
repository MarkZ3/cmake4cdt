/*******************************************************************************
 * Copyright (c) 2014 Rohde & Schwarz GmbH & Co. KG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Martin Runge - initial implementation of cmake support
 *******************************************************************************/
package org.eclipse.cdt.cmake.ui;


import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.window.Window;

public class TargetDevicesEditor extends ListEditor {

	public TargetDevicesEditor(String pAvailTargetDevices, String string,
			Composite fieldEditorParent) {
			super(pAvailTargetDevices, string, fieldEditorParent );
	}

	@Override
	protected String createList(String[] items) {
		StringBuilder result = new StringBuilder();
		
		for (int i=0; i<items.length; i++) {
	       	result.append(items[i]);
	       	result.append(';');
	    }
		
		return result.toString();
	}

	@Override
	protected String getNewInputObject() {

		IInputValidator validator = new IInputValidator() {
			public String isValid(String newText) {
				if(newText.contains("\t \n\r"))
					return "No whitespace characters allowed in architecture name";
				else
					return null;
			}
		};
		InputDialog dialog = new InputDialog(getShell(), 
				"Name of new instrument", 
				"Please specify the name of the new instrument. \n" +
				"This instrument name will be used in directory \n" +
				"names for projects that have been marked as \n" +
				"instrument specific in projects properties and when \n" +
				"assembling the installation path.", 
				null, 
				validator);
		if(dialog.open() == Window.OK) {
			return dialog.getValue();
		}else{
			return null;
		}
	}


	@Override
	protected String[] parseString(String stringList) {
		String[] resultItems = stringList.split(";");
		return resultItems;
	}
}
