package org.enoeclipse.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.enoeclipse.Activator;

public class EnoJPOPreference extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public EnoJPOPreference() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void createFieldEditors() {
		addField(
				new StringFieldEditor(PreferenceConstants.P_ENOVIA_JPO_IMPORT_FOLDER, "Default JPO import folder:", getFieldEditorParent())
				);
		addField(
				new StringFieldEditor(PreferenceConstants.P_ENOVIA_JAVA_IMPORT_FOLDER, "Default subfolder for java programs:", getFieldEditorParent())
				);
		addField(
				new StringFieldEditor(PreferenceConstants.P_ENOVIA_NON_JAVA_IMPORT_FOLDER, "Default subfolder for non-java programs:", getFieldEditorParent())
				);
		
	}

}
