package org.enoeclipse.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;
import org.enoeclipse.Activator;
import org.enoeclipse.model.EnoTreeProgram;
import org.enoeclipse.preferences.PreferenceConstants;
import org.enoeclipse.utils.EnoEclipseLogger;
import org.enoeclipse.utils.EnoEclipseUtils;
import org.enoeclipse.utils.EnoEclispeMqlUtils;

import com.matrixone.apps.domain.util.MqlUtil;

import matrix.db.Context;
import matrix.util.MatrixException;

public class EnoUpdateHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell parent = HandlerUtil.getActiveShell(event).getShell();
			Logger logger = EnoEclipseLogger.getLogger();
			Context context = Activator.getDefault().getContext();
			//TODO if context is null propose to login automatically
			if ((context != null) && (context.isConnected())) {
				IEditorPart part = HandlerUtil.getActiveEditor(event);
				if (part != null) {
					IEditorInput input = part.getEditorInput();
					if ((input instanceof IFileEditorInput)) {
						IFile file = ((IFileEditorInput)input).getFile();
						try {
							IPath filePath = file.getLocation();
							String fileName = file.getName();
							int dotpos = fileName.indexOf(".");
							String fileExtn = fileName.substring(dotpos + 1);
							
							//Check if exists
							String jpoName = null;
							if (fileName.endsWith("java")) {
								String className = fileName.substring(0, fileName.indexOf("."));
								jpoName = className.substring(0, className.length() - EnoTreeProgram.JPO_SUFFIX.length());
							} else if (fileName.endsWith("mql")) {
								jpoName = fileName.substring(0, fileName.indexOf("."));
							} else {
								jpoName = fileName;
							}
							
							if (!EnoTreeProgram.exists(jpoName)) {
								//Propose to create JPO
								if (MessageDialog.openQuestion(parent, "Remote program update", "Program doesn't exists, do you want to create it")) {
									MqlUtil.mqlCommand(context, "add program $1 $2 execute immediate", jpoName, "java".equalsIgnoreCase(fileExtn) ? "Java" : "Mql");
									EnoTreeProgram.getAllPrograms(true);
								} else {
									Status status = new Status(4, "EnoEclipse", 0, "Cannot update Program as it doesn't exist", new Exception("Cannot update JPO as it doesn't exist"));
									ErrorDialog.openError(parent, 
											EnoEclipseUtils.getString("MxEclipseAction.error.header.UpdateProgramFailed"), 
											EnoEclipseUtils.getString("MxEclipseAction.error.message.UpdateProgramFailed"), 
											status);
									return null;
								}
							}

							IPreferenceStore store = Activator.getDefault().getPreferenceStore();
							String hostName = store.getString(PreferenceConstants.P_ENOVIA_HOST).trim();

							boolean bAlreadyStored = false;
							String packageName = "";
							if ((hostName.startsWith("http://")) || (hostName.startsWith("rmi://"))) {
								boolean remoteDialog = store.getBoolean(PreferenceConstants.P_ENOVIA_WARN_JPO_UPDATE);
								if ((remoteDialog) && (!MessageDialog.openQuestion(parent, "Remote program update", "Are you sure that you want to update the program on a remote server?"))) {
									return null;
								}
								packageName = EnoTreeProgram.readJpoFromFileAndStoreToMatrix(filePath.toString(), fileName, true);

								bAlreadyStored = true;
							} else {
								packageName = EnoTreeProgram.readJpoFromFileAndStoreToMatrix(filePath.toString(), fileName, false);
							}

							if ("java".equalsIgnoreCase(fileExtn)) {
								if (!bAlreadyStored) {
									String insertQuery = "insert program " + filePath.toString();
									EnoEclispeMqlUtils.mqlCommand(context, insertQuery);
								}

								int mxJPOStrPos = fileName.indexOf("_mxJPO");
								if (mxJPOStrPos != 1) {
									fileName = fileName.substring(0, mxJPOStrPos);
								}

								if (!packageName.equals("")) {
									fileName = packageName + "." + fileName;
								}
								String strQuery = "compile program " + fileName + " force update";
								logger.logp(Level.FINE, "EnoEclipseAction", "updateProgram", strQuery, strQuery);
								EnoEclispeMqlUtils.mqlCommand(context, strQuery);
								String strSuccess = "Program " + fileName + " saved to database.\n" + "Program " + fileName + " compiled with no errors. ";
								MessageDialog.openInformation(parent, "Success", strSuccess);
							} else {
								String existsQuery = "list program \"" + fileName + "\"";
								String strResult = EnoEclispeMqlUtils.mqlCommand(context, existsQuery);
								String strUpdateQuery = "";

								if (!bAlreadyStored) {
									if ((strResult != null) && (strResult.trim().length() > 0)) {
										strUpdateQuery = "modify program \"" + fileName + "\" file " + filePath.toString();
									} else {
										strUpdateQuery = "add program \"" + fileName + "\" description \"" + fileName + "\" mql file " + filePath.toString();
									}
									EnoEclispeMqlUtils.mqlCommand(context, strUpdateQuery);
								}
								String strSuccess = "Program " + fileName + " saved to database.";
								MessageDialog.openInformation(parent, "Success", strSuccess);
							}
						} catch (Exception e1) {
							String strError = e1.getMessage();
							Status status = new Status(4, "MxEclipse", 0, strError, new Exception(strError));
							ErrorDialog.openError(parent, 
									EnoEclipseUtils.getString("MxEclipseAction.error.header.UpdateProgramFailed"), 
									EnoEclipseUtils.getString("MxEclipseAction.error.message.UpdateProgramFailed"), 
									status);
							logger.logp(Level.SEVERE, 
									"MxEclipseAction", 
									"updateProgram()", 
									e1.getMessage(), 
									e1.getStackTrace());
						}
					}
				}
			}
		} catch (MatrixException e) {
			e.printStackTrace();
		}
		return null;
	}

}
