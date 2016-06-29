package org.enoeclipse.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.enoeclipse.Activator;
import org.enoeclipse.preferences.PreferenceConstants;

import com.matrixone.apps.domain.util.MqlUtil;

import matrix.db.Context;
import matrix.util.MatrixException;

public class EnoEclipseUtils {

	public static final String BUNDLE_NAME = "org.enoeclipse.EnoEclipseStringResource";
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("org.enoeclipse.EnoEclipseStringResource");
	
	private static List<String> appVersion = new ArrayList<String>();
	private static List<String> libVersion = new ArrayList<String>();
	
	static {
		generateMatrixLibVersions();
	}

	private static void generateMatrixLibVersions() {
		StringTokenizer tkzr = new StringTokenizer("V6R2012x", ".");
		while (tkzr.hasMoreTokens()) {
			libVersion.add(tkzr.nextToken());
		}
	}
	
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
		}
		return '!' + key + '!';
	}
	
	public static void triggerOnOff() throws MatrixException {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean triggerOff = store.getBoolean(PreferenceConstants.P_TRIGGER_OFF);
		Context context = Activator.getDefault().getContext();
		if (context != null) {
			MqlUtil.mqlCommand(context, "trigger $1", (triggerOff ? "off" : "on"));
		}
	}
	
	public static void clearCache() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
//		try {
//			MxEclipseObjectView view = (MxEclipseObjectView)page.showView("org.mxeclipse.views.MxEclipseObjectView");
//			view.clearAll();
//
//			MxEclipseBusinessView bview = (MxEclipseBusinessView)page.showView("org.mxeclipse.views.MxEclipseBusinessView");
//			bview.clearAll();
//		} catch (PartInitException e) {
//			MessageDialog.openError(null, "Clear Cache", "Error when trying to clear views! " + e.getMessage());
//		}
	}
	
	public static int getLibPrimaryMajorVersion() {
		return Integer.parseInt((String)libVersion.get(0));
	}

	public static int getLibSecondaryMajorVersion() {
		return Integer.parseInt((String)libVersion.get(1));
	}

	public static int getLibPrimaryMinorVersion() {
		return Integer.parseInt((String)libVersion.get(2));
	}

	public static int getLibSecondaryMinorVersion() {
		return Integer.parseInt((String)libVersion.get(3));
	}

	public static int getAppPrimaryMajorVersion() {
		return Integer.parseInt((String)appVersion.get(0));
	}

	public static int getAppSecondaryMajorVersion() {
		return Integer.parseInt((String)appVersion.get(1));
	}

	public static int getAppPrimaryMinorVersion() {
		return Integer.parseInt((String)appVersion.get(2));
	}


}
