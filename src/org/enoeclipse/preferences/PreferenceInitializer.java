package org.enoeclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.enoeclipse.Activator;

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
		store.setDefault(PreferenceConstants.P_BOOLEAN, true);
		store.setDefault(PreferenceConstants.P_CHOICE, "choice2");
		store.setDefault(PreferenceConstants.P_STRING, "Default value");
		
		store.setDefault(PreferenceConstants.P_ENOVIA_HOST, "http://localhost:8080/enovia");
		store.setDefault(PreferenceConstants.P_ENOVIA_USER, "creator");
		store.setDefault(PreferenceConstants.P_ENOVIA_USER_PASSWORD, "");
		store.setDefault(PreferenceConstants.P_ENOVIA_JPO_IMPORT_FOLDER, "programs");
		store.setDefault(PreferenceConstants.P_ENOVIA_JAVA_IMPORT_FOLDER, "java");
		store.setDefault(PreferenceConstants.P_ENOVIA_NON_JAVA_IMPORT_FOLDER, "others");
		store.setDefault(PreferenceConstants.P_ENOVIA_WARN_JPO_UPDATE, true);
		store.setDefault(PreferenceConstants.P_ENOVIA_CODE_STYLE, true);
		
		store.setDefault(PreferenceConstants.P_TRIGGER_OFF, false);
	}

}
