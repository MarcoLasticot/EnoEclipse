package org.enoeclipse.model;

import java.util.ArrayList;
import java.util.Collections;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import com.matrixone.apps.domain.util.MqlUtil;

import matrix.db.Context;
import matrix.util.MatrixException;


public class EnoTreeWebCommand extends EnoTreeWebNavigation {
	protected ArrayList<EnoTreeWebCommand> parentMenus;
	private static ArrayList<EnoTreeWebCommand> allCommands;

	public EnoTreeWebCommand(String name) throws EnoEclipseException, MatrixException {
		super("Command", name);
	}

	public void refresh() throws EnoEclipseException, MatrixException {
		super.refresh();
		fillBasics();
	}

	public void fillBasics() {
		super.fillBasics();
	}

	public static ArrayList<EnoTreeWebCommand> getAllCommands(boolean refresh) throws MatrixException, EnoEclipseException {
		if (refresh || allCommands == null) {
			allCommands = new ArrayList<EnoTreeWebCommand>();
			matrix.db.Context context = getContext();
			//MQLCommand command = new MQLCommand();
			//command.executeCommand(context, MessageFormat.format(MQL_LIST_ALL, new Object[] {"Command".toLowerCase()}));
			String lines[] = MqlUtil.mqlCommand(context, MQL_LIST_ALL_NEW, "Command").split("\n");
			for (int i = 0; i < lines.length; i++) {
				String name = lines[i] = lines[i].trim();
				allCommands.add(new EnoTreeWebCommand(name));
			}

			Collections.sort(allCommands);
		}
		return allCommands;
	}


	public static String[] getAllCommandNames(boolean refresh) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeWebCommand> allCommands = getAllCommands(refresh);

		String[] retVal = new String[allCommands.size()];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = ((EnoTreeWebCommand)allCommands.get(i)).getName();
		}
		return retVal;
	}

	public void save() {
		try {
			//MQLCommand command = new MQLCommand();
			Context context = getContext();

			String modString = "";
			EnoTreeWebCommand oldCommand = new EnoTreeWebCommand(this.oldName);
			oldCommand.fillBasics();

			if ((oldCommand.getName() == null) || (!oldCommand.getName().equals(getName()))) {
				modString = modString + " name \"" + getName() + "\"";
			}
			if (oldCommand.isHidden() != isHidden()) {
				modString = modString + (isHidden() ? " hidden" : "!hidden");
			}
			if ((oldCommand.getDescription() == null) || (!oldCommand.getDescription().equals(getDescription()))) {
				modString = modString + " description \"" + getDescription() + "\"";
			}
			if ((oldCommand.getLabel() == null) || (!oldCommand.getLabel().equals(getLabel()))) {
				modString = modString + " label \"" + getLabel() + "\"";
			}
			if ((oldCommand.getHref() == null) || (!oldCommand.getHref().equals(getHref()))) {
				modString = modString + " href \"" + getHref() + "\"";
			}
			if ((oldCommand.getAlt() == null) || (!oldCommand.getAlt().equals(getAlt()))) {
				modString = modString + " alt \"" + getAlt() + "\"";
			}

			modString = modString + prepareSaveSettings(oldCommand);
			modString = modString + prepareSaveUsers(oldCommand);

			if (!modString.equals("")) {
				//command.executeCommand(context, "modify " + this.realType + " \"" + oldCommand.getName() + "\" " + modString + ";");
				MqlUtil.mqlCommand(context, "modify $1 $2 " + modString, this.realType, oldCommand.getName());
			}

			this.parentMenus = null;
			clearCache();
			refresh();
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public EnoTreeBusiness[] getChildren(boolean forceUpdate) throws EnoEclipseException, MatrixException {
		if (forceUpdate) {
			this.children = null;
		}
		if (this.children == null) {
			this.children = new ArrayList<EnoTreeBusiness>();
			this.children.addAll(getParentMenus());
			this.children.addAll(getUsers(false));
		}
		return (EnoTreeBusiness[])this.children.toArray(new EnoTreeBusiness[this.children.size()]);
	}

	public static void clearCache() {
		allCommands = null;
		EnoTreeWebNavigation.clearCache();
	}
}