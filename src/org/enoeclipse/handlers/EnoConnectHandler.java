package org.enoeclipse.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.enoeclipse.Activator;
import org.enoeclipse.enovia.EnoviaOperations;
import org.enoeclipse.preferences.PreferenceConstants;
import org.enoeclipse.utils.EnoEclipseUtils;

import matrix.db.Context;
import matrix.util.MatrixException;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class EnoConnectHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Context context = Activator.getDefault().getContext();
			
			if (context!=null && context.isConnected()) {
				MessageDialog.openInformation(HandlerUtil.getActiveShell(event).getShell(), 
						EnoEclipseUtils.getString("enoEclipseAction.info.header.ConnectEnovia"), 
						EnoEclipseUtils.getString("enoEclipseAction.info.message.ConnectEnovia") + " " + context.getUser());
			} else {
				IPreferenceStore store = Activator.getDefault().getPreferenceStore();
				boolean defLogin = store.getBoolean(PreferenceConstants.P_ENOVIA_USE_SPECIFIED_LOGIN);
				if (!defLogin) {
					
				} else {
					loginDirect(store, event);
				}
			}
		} catch (MatrixException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void loginDirect(IPreferenceStore store, ExecutionEvent event) throws MatrixException {
		ProgressMonitorDialog pmd = null;
		Shell shell = HandlerUtil.getActiveShell(event).getShell();
		try {
			EnoviaOperations enops = new EnoviaOperations();
			enops.setHost(store.getString(PreferenceConstants.P_ENOVIA_HOST));
			enops.setUser(store.getString(PreferenceConstants.P_ENOVIA_USER));
			enops.setPassword(store.getString(PreferenceConstants.P_ENOVIA_USER_PASSWORD));
			
			pmd = new ProgressMonitorDialog(shell);
			pmd.open();
			pmd.run(true, true, enops);
			
			Context context = Activator.getDefault().getContext();
			if (context.isConnected()) {
				MessageDialog.openInformation(shell, 
						EnoEclipseUtils.getString("enoEclipseAction.info.header.EnoviaConnectSuccess"), 
						EnoEclipseUtils.getString("enoEclipseAction.info.message.EnoviaConnectSuccess"));
				pmd.close();				
			}
		} catch (InvocationTargetException e) {
			if (pmd != null) {
				pmd.close();
			}
			String message = e.getCause().getMessage();
			Status status = new Status(4, "EnoEclipse", 0, message, e);
			ErrorDialog.openError(shell, 
					EnoEclipseUtils.getString("enoEclipseAction.error.header.ConnectionFailed"), 
					EnoEclipseUtils.getString("enoEclipseAction.error.message.ConnectionFailed"), 
					status);
		} catch (InterruptedException e) {
			if (pmd != null) {
				pmd.close();
			}
			String message = e.getCause().getMessage();
			Status status = new Status(4, "EnoEclipse", 0, message, e);
			ErrorDialog.openError(shell, 
					EnoEclipseUtils.getString("enoEclipseAction.error.header.ConnectionFailed"), 
					EnoEclipseUtils.getString("enoEclipseAction.error.message.ConnectionFailed"), 
					status);
		}
	}
}
