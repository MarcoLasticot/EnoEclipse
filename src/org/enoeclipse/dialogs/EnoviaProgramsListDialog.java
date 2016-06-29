package org.enoeclipse.dialogs;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.enoeclipse.Activator;
import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.model.EnoTreeProgram;
import org.enoeclipse.preferences.PreferenceConstants;
import org.enoeclipse.utils.EnoEclipseLogger;
import org.enoeclipse.utils.EnoEclipseUtils;
import org.enoeclipse.utils.ProgramSorter;

import com.matrixone.apps.domain.util.MqlUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class EnoviaProgramsListDialog extends Dialog {

	private List list = null;
	private Text progNameField = null;
	private Label progNameLabel;
	private Label progListLabel;
	private ArrayList<String> javaPrograms = null;
	private ArrayList<String> hiddenPrograms = null;
	private ArrayList<String> programs = null;
	private Button showJavaProgCB = null;
	private Button showHidProgCB = null;
	private Button selectAllChkBox = null;
	private Logger logger = EnoEclipseLogger.getLogger();

	public EnoviaProgramsListDialog(Shell parent) {
		super(parent);
	}

	protected EnoviaProgramsListDialog(IShellProvider parentShell) {
		super(parentShell);
	}

	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite)super.createDialogArea(parent);
		GridLayout layout = (GridLayout)comp.getLayout();
		layout.marginLeft = 2;
		layout.marginRight = 2;
		layout.marginTop = 5;
		layout.marginBottom = 5;

		GridData labelData = new GridData(4, 16777216, true, false);
		this.progNameLabel = new Label(comp, 1);
		this.progNameLabel.setText(EnoEclipseUtils.getString("label.ProgramNameFilter"));
		this.progNameLabel.setLayoutData(labelData);

		GridData fieldData = new GridData(4, 16777216, true, false);
		this.progNameField = new Text(comp, 2048);
		this.progNameField.setLayoutData(fieldData);

		this.progListLabel = new Label(comp, 1);
		this.progListLabel.setText(EnoEclipseUtils.getString("label.MatchingPrograms"));
		this.progListLabel.setLayoutData(labelData);

		GridData data = new GridData(4, 4, true, true);
		this.list = new List(comp, 2562);
		this.list.setLayoutData(data);

		try {
			Context context = Activator.getDefault().getContext();
			String queryResult = MqlUtil.mqlCommand(context, "list program $1 select $2 $3 $4 dump $5", "*", "name", "hidden", "isjavaprogram", "|");

			StringTokenizer tkzr = new StringTokenizer(queryResult, "\n");
			StringTokenizer progTkzr = null;
			this.javaPrograms = new ArrayList<String>();
			this.hiddenPrograms = new ArrayList<String>();
			this.programs = new ArrayList<String>();

			while (tkzr.hasMoreTokens()) {
				String progDetails = tkzr.nextToken();
				progTkzr = new StringTokenizer(progDetails, "|");
				while (progTkzr.hasMoreTokens()) {
					String progName = progTkzr.nextToken();
					boolean progHidden = new Boolean(progTkzr.nextToken()).booleanValue();
					boolean progJava = new Boolean(progTkzr.nextToken()).booleanValue();
					if (progJava) {
						this.javaPrograms.add(progName);
					}
					if (progHidden) {
						this.hiddenPrograms.add(progName);
					}
					this.programs.add(progName);
				}
			}

			ProgramSorter progSort = new ProgramSorter();
			Collections.sort(this.programs, progSort);
			Collections.sort(this.javaPrograms, progSort);
			Collections.sort(this.hiddenPrograms, progSort);

			Iterator<String> iter = this.programs.listIterator();
			while (iter.hasNext()) {
				String progName = (String)iter.next();
				this.list.add(progName);
			}
		} catch (MatrixException e) {
			String message = e.getMessage();
			Status status = new Status(4, "EnoEclipse", 0, message, e);
			ErrorDialog.openError(getShell(), 
					EnoEclipseUtils.getString("EnoviaProgramsListDialog.error.header.ListProgramsFailed"), 
					EnoEclipseUtils.getString("EnoviaProgramsListDialog.error.message.ListProgramsFailed"), 
					status);
		}

		this.progNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				EnoviaProgramsListDialog.this.list.removeAll();
				EnoviaProgramsListDialog.this.showPrograms();
			}
		});
		GridData chkBoxData = new GridData(4, 16777216, true, false);
		this.showHidProgCB = new Button(comp, 32);
		this.showHidProgCB.setText(EnoEclipseUtils.getString("checkBox.ShowHiddenPrograms"));
		this.showHidProgCB.setLayoutData(chkBoxData);
		this.showHidProgCB.setSelection(true);
		this.showHidProgCB.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				EnoviaProgramsListDialog.this.list.removeAll();
				EnoviaProgramsListDialog.this.showPrograms();
			}
		});
		this.showJavaProgCB = new Button(comp, 32);
		this.showJavaProgCB.setText(EnoEclipseUtils.getString("checkBox.ShowJavaPrograms"));
		this.showJavaProgCB.setLayoutData(chkBoxData);
		this.showJavaProgCB.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				EnoviaProgramsListDialog.this.list.removeAll();
				EnoviaProgramsListDialog.this.showPrograms();
			}
		});
		this.selectAllChkBox = new Button(comp, 32);
		this.selectAllChkBox.setText(EnoEclipseUtils.getString("checkBox.SelectAll"));
		this.selectAllChkBox.setLayoutData(chkBoxData);
		this.selectAllChkBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button chkBx = (Button)e.getSource();
				if (chkBx.getSelection()) {
					EnoviaProgramsListDialog.this.list.selectAll();
				} else {
					EnoviaProgramsListDialog.this.list.deselectAll();
				}
			}
		});
		return comp;
	}

	private void showPrograms() {
		boolean showHidProgCBSelected = this.showHidProgCB.getSelection();
		boolean showJavaProgCBSelected = this.showJavaProgCB.getSelection();
		boolean selectAllCBSelected = this.selectAllChkBox.getSelection();
		String enteredText = this.progNameField.getText().toLowerCase();

		if (showJavaProgCBSelected) {
			if (showHidProgCBSelected) {
				for (int i = 0; i < this.javaPrograms.size(); i++) {
					String progName = (String)this.javaPrograms.get(i);
					if (progName.toLowerCase().startsWith(enteredText)) {
						this.list.add(progName);
					}
				}
			} else {
				for (int i = 0; i < this.javaPrograms.size(); i++) {
					String progName = (String)this.javaPrograms.get(i);
					if ((this.hiddenPrograms.contains(progName)) || (!progName.toLowerCase().startsWith(enteredText))) {
						continue;
					}
					this.list.add(progName);
				}

			}

		} else if (showHidProgCBSelected) {
			for (int i = 0; i < this.programs.size(); i++) {
				String progName = (String)this.programs.get(i);
				if (progName.toLowerCase().startsWith(enteredText)) {
					this.list.add(progName);
				}
			}
		} else {
			for (int i = 0; i < this.programs.size(); i++) {
				String progName = (String)this.programs.get(i);
				if ((this.hiddenPrograms.contains(progName)) || (!progName.toLowerCase().startsWith(enteredText))) {
					continue;
				}
				this.list.add(progName);
			}

		}

		if (selectAllCBSelected) {
			if (this.list.getItemCount() > 0) {
				this.list.selectAll();
			}
		} else {
			this.list.deselectAll();
		}
	}

	@Override
	protected void okPressed() {
		EclipseProjectsDialog dlg = new EclipseProjectsDialog(getShell());
		dlg.open();
		if (dlg.getReturnCode() == 0) {
			try {
				String projName = dlg.getProjectName();
				IProject selProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projName);
				selProject.open(null);

				IPreferenceStore store = Activator.getDefault().getPreferenceStore();
				String sProgFolder = store.getString(PreferenceConstants.P_ENOVIA_JPO_IMPORT_FOLDER).trim();
				IFolder progFolder = selProject.getFolder(new Path(sProgFolder));
				if (!progFolder.exists()) {
					progFolder.create(true, true, null);
				}

				String sOtherSubfolder = store.getString(PreferenceConstants.P_ENOVIA_NON_JAVA_IMPORT_FOLDER).trim();
				if (!sOtherSubfolder.equals("")) {
					sOtherSubfolder = sProgFolder + File.separator + sOtherSubfolder;
				} else {
					sOtherSubfolder = sProgFolder;
				}
				IFolder otherProgFolder = selProject.getFolder(new Path(sOtherSubfolder));
				if (!otherProgFolder.exists()) {
					otherProgFolder.create(true, true, null);
				}

				String sJavaSubfolder = store.getString(PreferenceConstants.P_ENOVIA_JAVA_IMPORT_FOLDER).trim();
				if (!sJavaSubfolder.equals("")) {
					sJavaSubfolder = sProgFolder + File.separator + sJavaSubfolder;
				} else {
					sJavaSubfolder = sProgFolder;
				}
				IFolder javaProgFolder = selProject.getFolder(sJavaSubfolder);
				IJavaProject javaProjObj = JavaCore.create(selProject);
				if (!javaProgFolder.exists()) {
					javaProgFolder.create(true, true, null);
				}

				boolean bFound = false;
				IClasspathEntry srcEntry = JavaCore.newSourceEntry(javaProgFolder.getFullPath());
				IClasspathEntry[] buildPath = javaProjObj.getRawClasspath();
				for (IClasspathEntry oneBuildPath : buildPath) {
					if (oneBuildPath.getPath().toString().equals(srcEntry.getPath().toString())) {
						bFound = true;
						break;
					}
				}
				if (!bFound) {
					IClasspathEntry[] newBuildPath = new IClasspathEntry[buildPath.length + 1];
					for (int i = 0; i < buildPath.length; i++) {
						newBuildPath[i] = buildPath[i];
					}
					newBuildPath[(newBuildPath.length - 1)] = srcEntry;
					javaProjObj.setRawClasspath(newBuildPath, null);
				}

				IPackageFragment javaFragments = extractSelectedPrograms(otherProgFolder, javaProgFolder, javaProjObj);

				javaProgFolder.refreshLocal(2, null);
				//if ((EnoEclipseUtils.getLibPrimaryMajorVersion() > 10) && (EnoEclipseUtils.getLibPrimaryMinorVersion() < 6)) {
					renamePrograms(javaFragments);
				//}
				javaFragments.close();

				selProject.build(10, null);
				super.okPressed();
			} catch (CoreException e) {
				ErrorDialog.openError(getShell(), 
						EnoEclipseUtils.getString("EnoviaProgramsListDialog.error.header.ImportProgramFailed"), 
						EnoEclipseUtils.getString("EnoviaProgramsListDialog.error.message.ImportProgramFailed"), 
						e.getStatus());
				this.logger.logp(Level.SEVERE, 
						"MatrixProgramsListDialog", 
						"okPressed()", 
						e.getMessage(), 
						e.getStackTrace());
			} catch (Exception e) {
				Status status = new Status(4, "EnoEclipse", 0, e.getMessage(), e);
				ErrorDialog.openError(getShell(), 
						EnoEclipseUtils.getString("EnoviaProgramsListDialog.error.header.ImportProgramFailed"), 
						EnoEclipseUtils.getString("EnoviaProgramsListDialog.error.message.ImportProgramFailed"), 
						status);
				this.logger.logp(Level.SEVERE, 
						"EnoviaProgramsListDialog", 
						"okPressed()", 
						"totot" + e.getMessage(), 
						e.getStackTrace());
			}
		}
	}

	private IPackageFragment extractSelectedPrograms(IFolder otherProgFolder, IFolder javaProgFolder, IJavaProject javaProjObj) throws CoreException, JavaModelException, MatrixException, InterruptedException, InvocationTargetException, EnoEclipseException, IOException {
		String[] selectedPrograms = this.list.getSelection();
		MQLCommand mql = new MQLCommand();
		javaProgFolder.refreshLocal(2, null);
		IPackageFragment javaFragments = javaProjObj.findPackageFragment(javaProgFolder.getFullPath());
		javaFragments.open(null);

		for (int i = 0; i < selectedPrograms.length; i++) {
			String selProgram = selectedPrograms[i];
			if (this.javaPrograms.contains(selProgram)) {
				extractJavaPrograms(javaProgFolder, mql, javaFragments, selProgram);
			} else {
				extractOtherPrograms(otherProgFolder, mql, selProgram);
			}
		}
		return javaFragments;
	}


	private void extractOtherPrograms(IFolder otherProgFolder, MQLCommand mql, String selProgram) throws MatrixException, CoreException {
		String strQuery = "print program " + selProgram + " select code dump |";
		boolean bln = mql.executeCommand(Activator.getDefault().getContext(), strQuery);
		if (bln) {
			String progCode = mql.getResult();
			InputStream stream = new BufferedInputStream(new ByteArrayInputStream(progCode.getBytes()));
			IFile progFile = otherProgFolder.getFile(selProgram);
			if (progFile.exists()) {
				progFile.delete(true, true, null);
			}
			progFile.create(stream, true, null);
		} else {
			String strError = mql.getError();
			Status status = new Status(4, "MxEclipse", 0, strError, new Exception(strError));
			ErrorDialog.openError(getShell(), 
					EnoEclipseUtils.getString("MatrixProgramsListDialog.error.header.ImportProgramFailed"), 
					EnoEclipseUtils.getString("MatrixProgramsListDialog.error.message.ImportProgramFailed"), 
					status);
		}
	}

	private void extractJavaPrograms(IFolder javaProgFolder, MQLCommand mql, IPackageFragment javaFragments, String selProgram) throws JavaModelException, MatrixException, CoreException, InterruptedException, InvocationTargetException, EnoEclipseException, IOException {
		String strPath = javaProgFolder.getLocation().toString();

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String hostName = store.getString(PreferenceConstants.P_ENOVIA_HOST).trim();
		if ((hostName.startsWith("http://")) || (hostName.startsWith("rmi://"))) {
			EnoTreeProgram.saveJpoToFile(strPath, selProgram);
			return;
		}

		ICompilationUnit compilationUnit = javaFragments.getCompilationUnit(selProgram + "_mxJPO.java");
		if (compilationUnit.exists()) {
			compilationUnit.delete(true, null);
		}

		String strQuery = "extract program " + selProgram + " source \"" + strPath + "\"";

		if ((EnoEclipseUtils.getLibPrimaryMajorVersion() >= 10) && (EnoEclipseUtils.getLibSecondaryMajorVersion() >= 6)) {
			strQuery = strQuery + " demangle";
		}
		this.logger.logp(Level.SEVERE, "EnoviaProgramsListDialog", "extractJavaPrograms()", strQuery, strQuery);

		boolean bln = mql.executeCommand(Activator.getDefault().getContext(), strQuery);
		if (!bln) {
			String strError = mql.getError();
			Status status = new Status(4, "EnoEclipse", 0, strError, new Exception(strError));
			ErrorDialog.openError(getShell(), 
					EnoEclipseUtils.getString("MatrixProgramsListDialog.error.header.ImportProgramFailed"), 
					EnoEclipseUtils.getString("MatrixProgramsListDialog.error.message.ImportProgramFailed"), 
					status);
		}

		javaProgFolder.refreshLocal(2, null);
		if ((EnoEclipseUtils.getLibPrimaryMajorVersion() >= 10) && (EnoEclipseUtils.getLibSecondaryMajorVersion() <= 5)) {
			renamePrograms(javaFragments);
		}
	}

	private void renamePrograms(IPackageFragment javaFragments) throws JavaModelException, CoreException, InterruptedException, InvocationTargetException {
		ICompilationUnit[] compUnits = javaFragments.getCompilationUnits();
		for (int j = 0; j < compUnits.length; j++) {
			ICompilationUnit unit = compUnits[j];
			unit.open(null);
			String elementName = unit.getElementName();
			int uspos = elementName.indexOf("_mxJPO");
			String progName = elementName.substring(0, uspos);
			String newEleName = progName + "_mxJPO";
			String eleSubStr = elementName.substring(uspos);

			if (!"_mxJPO.java".equalsIgnoreCase(eleSubStr)) {
				ICompilationUnit compilationUnit = javaFragments.getCompilationUnit(progName + "_mxJPO.java");
				if (compilationUnit.exists()) {
					compilationUnit.delete(true, null);
				}

				RenameSupport renSup = RenameSupport.create(unit, newEleName, 1);

				renSup.perform(getShell(), new ProgressMonitorDialog(getShell()));
			}
			unit.close();
		}
	}

	protected void cancelPressed() {
		super.cancelPressed();
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(EnoEclipseUtils.getString("MatrixProgramsListDialog.header.ListPrograms"));
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, 0, EnoEclipseUtils.getString("button.ImportSelected"), true);
		createButton(parent, 1, IDialogConstants.CANCEL_LABEL, false);
	}

}
