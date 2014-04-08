package com.rohde_schwarz.cmake4cdt.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.rohde_schwarz.cmake4cdt.Activator;

import org.eclipse.core.variables.IStringVariable;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_AVAIL_TARGET_ARCHS, "arm;ppc;x86;native");
		store.setDefault(PreferenceConstants.P_AVAIL_TARGET_DEVICES, "localhost;rasPi");
		store.setDefault(PreferenceConstants.P_NOT_INST_SPECIFIC, "");

		store.setDefault(PreferenceConstants.P_BUILDDIR, "${CMake_ProjectPath}/${ConfigName}_${CMake_TargetArch}");
		store.setDefault(PreferenceConstants.P_DESTDIR, "${env_var:HOME}/${CMake_TargetDevice}/${ConfigName}_${CMake_TargetArch}/opt/");
	}

}