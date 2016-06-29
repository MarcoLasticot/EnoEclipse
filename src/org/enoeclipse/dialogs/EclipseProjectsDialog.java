package org.enoeclipse.dialogs;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.enoeclipse.utils.EnoEclipseUtils;

public class EclipseProjectsDialog extends Dialog {
	
	protected Label messageLabel;
	protected Label imageLabel;
	protected Table table;
	private String projectPath;
	private String projectName;

	public EclipseProjectsDialog(Shell parentShell) {
		super(parentShell);
	}
	
	protected EclipseProjectsDialog(IShellProvider parentShell) {
		super(parentShell);
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite)super.createDialogArea(parent);
		GridLayout layout = (GridLayout)comp.getLayout();
		layout.marginLeft = 2;
		layout.marginRight = 2;
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.numColumns = 2;

		Image image = getSWTImage(2);
		if (image != null) {
			this.imageLabel = new Label(comp, 0);
			image.setBackground(this.imageLabel.getBackground());
			this.imageLabel.setImage(image);
			this.imageLabel.setLayoutData(new GridData(66));
		}

		this.messageLabel = new Label(comp, 16448);
		this.messageLabel.setText(EnoEclipseUtils.getString("EclipseProjectsDialog.MainLabel.Message"));
		GridData labData = new GridData(770);

		labData.widthHint = convertHorizontalDLUsToPixels(300);
		this.messageLabel.setLayoutData(labData);

		this.table = new Table(comp, 68132);
		this.table.setToolTipText(EnoEclipseUtils.getString("EclipseProjectsDialog.Table.ToolTip.Message"));
		GridData gd = new GridData(4, 4, true, true);
		gd.horizontalSpan = 2;
		this.table.setLayoutData(gd);
		this.table.setHeaderVisible(true);
		TableColumn name = new TableColumn(this.table, 16384);
		name.setText(EnoEclipseUtils.getString("EclipseProjectsDialog.table.column.ProjectName"));
		TableColumn path = new TableColumn(this.table, 16384);
		path.setText(EnoEclipseUtils.getString("EclipseProjectsDialog.table.column.Path"));
		TableColumn location = new TableColumn(this.table, 16384);
		location.setText(EnoEclipseUtils.getString("EclipseProjectsDialog.table.column.Location"));

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot wsRoot = workspace.getRoot();
		IJavaModel model = JavaCore.create(wsRoot);
		try {
			model.open(null);
			IJavaProject[] projects = model.getJavaProjects();
			TableItem item = null;
			for (int i = 0; i < projects.length; i++) {
				IJavaProject proj = projects[i];
				IPath projLoc = proj.getResource().getLocation();
				IPath projPath = proj.getPath();
				proj.open(null);
				item = new TableItem(this.table, 0);
				item.setText(0, proj.getElementName());
				item.setText(1, projPath.toString());
				item.setText(2, projLoc.toString());
				item.setChecked(false);
				proj.close();
			}
			model.close();
		} catch (JavaModelException e) {
			ErrorDialog.openError(getShell(), 
					EnoEclipseUtils.getString("EclipseProjectsDialog.error.header.ListProjectsFailed"), 
					EnoEclipseUtils.getString("EclipseProjectsDialog.error.message.ListProjectsFailed"), 
					e.getStatus());
		}

		int columnCount = this.table.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			TableColumn tableColumn = this.table.getColumn(i);
			tableColumn.pack();
		}

		this.table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				TableItem item = (TableItem)event.item;
				TableItem[] totalItems = EclipseProjectsDialog.this.table.getItems();
				for (int x = 0; x < totalItems.length; x++) {
					totalItems[x].setChecked(false);
				}

				item.setChecked(true);
			}
		});
		return comp;
	}

	protected void okPressed() {
		TableItem[] totalItems = this.table.getItems();
		TableItem[] chkdItems = new TableItem[totalItems.length];
		boolean[] chkItems = new boolean[totalItems.length];
		int chkItemCount = 0;
		boolean moreChkd = false;
		for(int x = 0; x < totalItems.length; x++) {
			chkItems[x] = totalItems[x].getChecked();
			if (!chkItems[x]) {
				continue;
			}
			if (chkItemCount > 0) {
				moreChkd = true;
				break;
			}
			chkdItems[chkItemCount] = totalItems[x];
			chkItemCount++;
		}

		if (chkItemCount == 0) {
			MessageDialog.openWarning(getShell(), 
					EnoEclipseUtils.getString("EclipseProjectsDialog.warning.header.SelectedProjects"), 
					EnoEclipseUtils.getString("EclipseProjectsDialog.warning.message.PleaseSelectProject"));
		} else {
			if (moreChkd) {
				MessageDialog.openWarning(getShell(), 
						EnoEclipseUtils.getString("EclipseProjectsDialog.warning.header.SelectedProjects"), 
						EnoEclipseUtils.getString("EclipseProjectsDialog.warning.message.PleaseSelectOnlyOneProject"));
				for (int x = 0; x < totalItems.length; x++) {
					totalItems[x].setChecked(false);
				}
			}

			TableItem firstItem = chkdItems[0];
			this.projectName = firstItem.getText(0);
			this.projectPath = firstItem.getText(1);
			super.okPressed();
		}
	}

	protected void cancelPressed() {
		super.cancelPressed();
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(EnoEclipseUtils.getString("EclipseProjectsDialog.header.ListProjects"));
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, 0, EnoEclipseUtils.getString("button.Select"), true);
		createButton(parent, 1, IDialogConstants.CANCEL_LABEL, false);
	}

	private Image getSWTImage(final int imageID) {
		Shell shell = getShell();

		if (shell == null) {
			shell = getParentShell();
		}
		final Display display;
		if (shell == null) {
			display = Display.getCurrent();
		}
		else {
			display = shell.getDisplay();
		}

		final Image[] image = new Image[1];
		display.syncExec(new Runnable() {
			public void run() {
				image[0] = display.getSystemImage(imageID);
			}
		});
		return image[0];
	}

	public String getProjectName() {
		return this.projectName;
	}

	public String getProjectPath() {
		return this.projectPath;
	}

}
