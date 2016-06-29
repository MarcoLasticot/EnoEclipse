package org.enoeclipse;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.enoeclipse.enovia.EnoviaOperations;
import org.enoeclipse.preferences.PreferenceConstants;
import org.enoeclipse.utils.EnoEclipseLogger;
import org.osgi.framework.BundleContext;

import matrix.db.Context;
import matrix.util.MatrixException;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "enoEclipse"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private Context context;
	private String host;
	private String user;
	private ResourceBundle resourceBundle;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		this.resourceBundle = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static String getResourceString(String key) {
		ResourceBundle bundle = getDefault().getResourceBundle();
		try {
			return bundle != null ? bundle.getString(key) : key; } catch (MissingResourceException e) {
			}
		return key;
	}

	public ResourceBundle getResourceBundle() {
		try {
			if (this.resourceBundle == null) {
				this.resourceBundle = ResourceBundle.getBundle("org.enoeclipse.EnoEclipsePluginResources");
			}
		} catch (MissingResourceException x) {
			this.resourceBundle = null;
		}
		return this.resourceBundle;
	}
	
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public Context getContext() throws MatrixException {
		try {
			if (this.context == null) {
				IPreferenceStore store = getDefault().getPreferenceStore();
				loginDirect(store);

			}
			return this.context;
		} catch (MatrixException e) {
			EnoEclipseLogger.getLogger().severe("Error when tried to login " + e.getMessage());
			throw e;
		}
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void loginDirect(IPreferenceStore store) throws MatrixException {
		EnoviaOperations enops = new EnoviaOperations();
		enops.setHost(store.getString(PreferenceConstants.P_ENOVIA_HOST));
		enops.setHost(store.getString(PreferenceConstants.P_ENOVIA_USER));
		enops.setHost(store.getString(PreferenceConstants.P_ENOVIA_USER_PASSWORD));

		//		enops.setHost("http://192.168.127.75:8080/enovia");
		//		enops.setHost("creator");
		//		enops.setHost("");
		//		enops.login();

		this.host = store.getString(PreferenceConstants.P_ENOVIA_HOST);
		this.user = store.getString(PreferenceConstants.P_ENOVIA_USER);
		//EnoEclipseObjectView.refreshViewStatusBar(null);

	}


}
