package org.enoeclipse.preferences;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.enoeclipse.Activator;
import org.enoeclipse.utils.EnoEclipseUtils;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class EnoEclipsePreference
extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

	public EnoEclipsePreference() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("For access over HTTP, use http://<server>:<port>/<context> as ENOVIA Host. \nSample: http://localhost:8080/enovia");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(
				new StringFieldEditor(PreferenceConstants.P_ENOVIA_HOST, "ENOVIA Host:", getFieldEditorParent())
				);

		addField(
				new StringFieldEditor(PreferenceConstants.P_ENOVIA_USER, "ENOVIA User:", getFieldEditorParent())
				);

		addField(
				new StringFieldEditor(PreferenceConstants.P_ENOVIA_USER_PASSWORD, "ENOVIA User Password:", getFieldEditorParent())
				);


		addField(
				new BooleanFieldEditor(
						PreferenceConstants.P_ENOVIA_USE_SPECIFIED_LOGIN,
						"Always login with the specified user credentials",
						getFieldEditorParent())
				);

		addField(
				new BooleanFieldEditor(
						PreferenceConstants.P_ENOVIA_AUTOMATIC_LOGIN,
						"Automatically login without any dialogs",
						getFieldEditorParent())
				);

		Button cmdClearCache = new Button(getFieldEditorParent(), 0);
		cmdClearCache.setText("Clear Cache");
		cmdClearCache.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				EnoEclipseUtils.clearCache();
				MessageDialog.openInformation(EnoEclipsePreference.this.getFieldEditorParent().getShell(), "Clear Cache", "MxEclipse cache has been cleared!");
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}