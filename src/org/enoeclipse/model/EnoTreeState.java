package org.enoeclipse.model;

import java.util.ArrayList;
import java.util.Iterator;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import com.matrixone.apps.domain.util.MqlUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;


public class EnoTreeState extends EnoTreeBusiness
implements ITriggerable
{
	private EnoTreePolicy policy;
	protected boolean versionable;
	protected boolean revisionable;
	protected boolean promote;
	protected boolean checkoutHistory;
	protected ArrayList<EnoTreeStateUserAccess> userAccess = new ArrayList<EnoTreeStateUserAccess>();
	public static final String STATE = "state";
	public static final String PROPERTY = "property";
	public static final String NOTHIDDEN = "nothidden";
	public static final String HIDDEN = "hidden";
	public static final String VERSIONABLE = "versionable";
	public static final String REVISIONABLE = "revisionable";
	public static final String PROMOTE = "promote";
	public static final String CHECKOUT_HISTORY = "checkout history";
	public static final String FILTER = "filter";

	public EnoTreeState(String name, EnoTreePolicy policy)
			throws EnoEclipseException, MatrixException
	{
		super("State", name);
		this.policy = policy;
	}

	public void refresh()
			throws EnoEclipseException, MatrixException
	{
		super.refresh();
		fillBasics();
	}

	public boolean isVersionable()
	{
		return this.versionable;
	}

	public void setVersionable(boolean versionable) {
		this.versionable = versionable;
	}

	public boolean isRevisionable() {
		return this.revisionable;
	}

	public void setRevisionable(boolean revisionable) {
		this.revisionable = revisionable;
	}

	public boolean isPromote() {
		return this.promote;
	}

	public void setPromote(boolean promote) {
		this.promote = promote;
	}

	public boolean isCheckoutHistory() {
		return this.checkoutHistory;
	}

	public void setCheckoutHistory(boolean checkoutHistory) {
		this.checkoutHistory = checkoutHistory;
	}

	public void splitUserAccess(String lines)
	{
	}

	public EnoTreePolicy getPolicy() {
		return this.policy;
	}

	public void fillBasics() {
		try {
			this.userAccess.clear();
			Context context = getContext();

//			MQLCommand command = new MQLCommand();
//			command.executeCommand(context, MessageFormat.format(MQL_SIMPLE_PRINT, new Object[] { "Policy", this.policy.getName() }));

			String[] lines = MqlUtil.mqlCommand(context, MQL_SIMPLE_PRINT_NEW, "Policy", this.policy.getName()).split("\n");
			String myLineBeginning = "state " + this.name;
			boolean bProcessing = false;
			for (int i = 0; i < lines.length; i++) {
				lines[i] = lines[i].trim();
				if (lines[i].startsWith(myLineBeginning)) {
					bProcessing = true;
				} else if ((lines[i].startsWith("state")) || (lines[i].startsWith("property")) || (lines[i].startsWith("nothidden")) || (lines[i].startsWith("hidden"))) {
					if (bProcessing) {
						break;
					}
				} else {
					if (!bProcessing) {
						continue;
					}
					if (lines[i].startsWith("versionable")) {
						this.versionable = lines[i].endsWith("true");
					} else if (lines[i].startsWith("revisionable")) {
						this.revisionable = lines[i].endsWith("true");
					} else if (lines[i].startsWith("promote")) {
						this.promote = lines[i].endsWith("true");
					} else if (lines[i].startsWith("checkout history")) {
						this.checkoutHistory = lines[i].endsWith("true");
					} else {
						if ((!lines[i].startsWith("owner")) && (!lines[i].startsWith("public")) && (!lines[i].startsWith("user"))) {
							continue;
						}
						int indexOfBlank = lines[i].indexOf(" ");
						if (indexOfBlank > 0) {
							String userBasicType = lines[i].substring(0, indexOfBlank).trim();
							String userName = "";
							String withoutCommand = lines[i].substring(indexOfBlank).trim();
							if (userBasicType.equals("user")) {
								indexOfBlank = withoutCommand.lastIndexOf(" ");
								if (indexOfBlank > 0) {
									userName = withoutCommand.substring(0, indexOfBlank);
									withoutCommand = withoutCommand.substring(indexOfBlank).trim();
								}
							} else {
								userName = userBasicType;
							}
							EnoTreeStateUserAccess sua = new EnoTreeStateUserAccess(userBasicType, userName);
							sua.setAccessRights(withoutCommand);
							if ((i < lines.length) && (lines[(i + 1)].trim().startsWith("filter"))) {
								withoutCommand = lines[(i + 1)].trim().substring("filter".length()).trim();
								sua.setFilter(withoutCommand);
							}
							this.userAccess.add(sua);
						}
					}
				}
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public ArrayList<EnoTreeStateUserAccess> getUserAccess() {
		return this.userAccess;
	}

	public void setUserAccess(ArrayList<EnoTreeStateUserAccess> userAccess) {
		this.userAccess = userAccess;
	}

	public void addUserAccess() throws EnoEclipseException, MatrixException {
		addUserAccess(new EnoTreeStateUserAccess("user", ""));
	}
	public void addUserAccess(EnoTreeStateUserAccess newUserAccess) {
		this.userAccess.add(newUserAccess);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext())
			((IEnoBusinessViewer)iterator.next()).addProperty(newUserAccess); 
	}

	public void removeUserAccess(EnoTreeStateUserAccess oldUserAccess) {
		if (this.userAccess == null) {
			this.userAccess = new ArrayList<EnoTreeStateUserAccess>();
		}
		this.userAccess.remove(oldUserAccess);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext())
			((IEnoBusinessViewer)iterator.next()).removeProperty(oldUserAccess);
	}

	public String prepareSaveUserAccess(EnoTreeState oldState) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeStateUserAccess> oldUserAccesses = oldState.getUserAccess();
		ArrayList<EnoTreeStateUserAccess> userAccesses = getUserAccess();

		String sAdded = "";
		String sRemoved = "";

		for (int i = 0; i < userAccesses.size(); i++) {
			if (!((EnoTreeStateUserAccess)userAccesses.get(i)).getOldName().equals(""))
				continue;
			EnoTreeStateUserAccess newUserAccess = (EnoTreeStateUserAccess)userAccesses.get(i);
			String ar = newUserAccess.getAccessRightCommaSeparated();
			sAdded = sAdded + " " + newUserAccess.getUserBasicType() + (newUserAccess.getUserBasicType().equals("user") ? " \"" + newUserAccess.getName() + "\" " : " ") + ar + "\n";
			if ((newUserAccess.getFilter() != null) && (!newUserAccess.getFilter().equals(""))) {
				sAdded = sAdded + " filter \"" + newUserAccess.getFilter() + "\"\n";
			}
		}

		if (oldUserAccesses != null) {
			for (int i = 0; i < oldUserAccesses.size(); i++) {
				boolean bFound = false;
				EnoTreeStateUserAccess oldUserAccess = (EnoTreeStateUserAccess)oldUserAccesses.get(i);
				for (int j = 0; j < userAccesses.size(); j++) {
					EnoTreeStateUserAccess userAccess = (EnoTreeStateUserAccess)userAccesses.get(j);
					if (oldUserAccess.getUserBasicType().equals(userAccess.getUserBasicType())) {
						if (oldUserAccess.getName().equals(userAccess.getName())) {
							bFound = true;
							String diff = userAccess.getDifferenceCommaSeparated(oldUserAccess);
							if (diff.equals("")) break;
							String ar = userAccess.getAccessRightCommaSeparated();
							sAdded = sAdded + " " + userAccess.getUserBasicType() + (userAccess.getUserBasicType().equals("user") ? " \"" + userAccess.getName() + "\" " : " ") + ar + "\n";
							if ((userAccess.getFilter() == null) || (userAccess.getFilter().equals(""))) break;
							sAdded = sAdded + " filter \"" + userAccess.getFilter() + "\"\n";

							break;
						}if (oldUserAccess.getName().equals(userAccess.getOldName())) {
							String ar = userAccess.getAccessRightCommaSeparated();
							if (ar.equals("")) break;
							sAdded = sAdded + " " + userAccess.getUserBasicType() + (userAccess.getUserBasicType().equals("user") ? " \"" + userAccess.getName() + "\" " : " ") + ar + "\n";
							if ((userAccess.getFilter() == null) || (userAccess.getFilter().equals(""))) break;
							sAdded = sAdded + " filter \"" + userAccess.getFilter() + "\"\n";

							break;
						}
					}
				}
				if (bFound)
					continue;
				sRemoved = sRemoved + " remove " + oldUserAccess.getUserBasicType() + (oldUserAccess.getUserBasicType().equals("user") ? " \"" + oldUserAccess.getName() + "\" all" : " all") + "\n";
			}

		}

		return sAdded + sRemoved;
	}

	public void save()
	{
		try {
			MQLCommand command = new MQLCommand();
			Context context = getContext();
			EnoTreeState oldState = new EnoTreeState(this.oldName, this.policy);
			oldState.fillBasics();

			String modString = "";
			if ((oldState.getName() == null) || (!oldState.getName().equals(getName()))) {
				modString = modString + " name \"" + getName() + "\"";
			}
			if (oldState.isVersionable() != isVersionable()) {
				modString = modString + " version " + (isVersionable() ? "true" : "false");
			}
			if (oldState.isRevisionable() != isRevisionable()) {
				modString = modString + " revision " + (isRevisionable() ? "true" : "false");
			}
			if (oldState.isPromote() != isPromote()) {
				modString = modString + " promote " + (isPromote() ? "true" : "false");
			}
			if (oldState.isCheckoutHistory() != isCheckoutHistory()) {
				modString = modString + " checkouthistory " + (isCheckoutHistory() ? "true" : "false");
			}

			modString = modString + prepareSaveUserAccess(oldState);

			if (!modString.equals("")) {
//				command.executeCommand(context, "modify policy \"" + this.policy.getName() + "\" state \"" + this.oldName + "\" " + modString + ";");
				MqlUtil.mqlCommand(context, "modify policy $1 state $2 " + modString, this.policy.getName(), this.oldName);
			}

			saveTriggers(context, command);
			this.userAccess.clear();
			refresh();
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}
}