package org.enoeclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.enoeclipse.Activator;
import org.enoeclipse.dialogs.EnoviaProgramsListDialog;

import matrix.db.Context;
import matrix.util.MatrixException;

public class EnoImportHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell parent = HandlerUtil.getActiveShell(event).getShell();
			Context context = Activator.getDefault().getContext();
			//TODO if context is null propose to login automatically
			if ((context != null) && (context.isConnected())) {
				EnoviaProgramsListDialog programsDialog = new EnoviaProgramsListDialog(parent);
				programsDialog.open();
			}
		} catch (MatrixException e) {
			e.printStackTrace();
		}
		return null;
	}

}
