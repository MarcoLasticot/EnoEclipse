package org.enoeclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.enoeclipse.Activator;
import org.enoeclipse.utils.EnoEclipseUtils;

import matrix.db.Context;
import matrix.util.MatrixException;

public class EnoDisconnectHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell parent = HandlerUtil.getActiveShell(event).getShell();
			Context context = Activator.getDefault().getContext();
			if ((context != null) && (context.isConnected())) {
				try {
					String user = context.getUser();
					context.closeContext();
					context.disconnect();
					MessageDialog.openInformation(parent, 
							EnoEclipseUtils.getString("enoEclipseAction.info.header.Disconnect"), 
							EnoEclipseUtils.getString("enoEclipseAction.info.message.DisconnectedUser") + 
							user);
				} catch (MatrixException e) {
					String message = e.getMessage();
					Status status = new Status(4, "EnoEclipse", 0, message, e);
					ErrorDialog.openError(parent, 
							EnoEclipseUtils.getString("enoEclipseAction.error.header.DisconnectFailed"), 
							EnoEclipseUtils.getString("enoEclipseAction.error.message.DisconnectFailed") +  " " + context.getUser(), 
							status);
				} finally {
					context = null;
					Activator.getDefault().setContext(context);
					Activator.getDefault().setHost(null);
					Activator.getDefault().setUser(null);
				}
			} else
				MessageDialog.openInformation(parent, 
						EnoEclipseUtils.getString("enoEclipseAction.info.header.Disconnect"), 
						EnoEclipseUtils.getString("enoEclipseAction.info.message.Disconnect"));
		} catch (MatrixException e) {
			e.printStackTrace();
		}
		return null;
	}

}
