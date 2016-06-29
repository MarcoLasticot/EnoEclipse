package org.enoeclipse.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import matrix.db.Context;
import matrix.db.Group;
import matrix.db.MQLCommand;
import matrix.db.Role;
import matrix.db.User;
import matrix.util.MatrixException;


public abstract class EnoTreeAssignment extends EnoTreeUser {
	User assignment;
	protected static String MQL_INFO = "print {0} \"{1}\" select name description hidden dump |;";
	protected static String MQL_INFO_SITE = "print {0} \"{1}\" select site dump |;";
	protected static String MQL_INFO_PARENT = "print {0} \"{1}\" select parent dump |;";
	protected static String MQL_INFO_CHILD = "print {0} \"{1}\" select child dump |;";
	protected static String MQL_INFO_PERSONS = "print {0} \"{1}\" select person dump |;";
	protected static String MQL_ADD_PERSON = "modify {0} \"{1}\" assign person \"{2}\";";
	protected static String MQL_REMOVE_PERSON = "modify {0} \"{1}\" remove assign person \"{2}\";";
	protected String description;
	protected String comment;
	protected boolean hidden;
	protected EnoTreeSite site;
	protected ArrayList<EnoTreeAssignment> parentAssignments;
	protected ArrayList<EnoTreeAssignment> childrenAssignments;
	protected ArrayList<EnoTreePerson> persons;
	protected String realName;
	protected static final int INFO_NAME = 0;
	protected static final int INFO_DESCRIPTION = 1;
	protected static final int INFO_HIDDEN = 2;
	protected static final int INFO_SITE = 3;

	public EnoTreeAssignment(String type, String name) throws EnoEclipseException, MatrixException {
		super(type, name);
		this.realName = type.toLowerCase();
		if (type.equals("Role")) {
			this.assignment = new Role(name);
		} else {
			this.assignment = new Group(name);
		}
	}

	public static EnoTreeAssignment createAssignment(String name) throws EnoEclipseException, MatrixException {
		String assignmentType = getAssignmentType(name);
		if (assignmentType.equals("Role")) {
			return new EnoTreeRole(name);
		}
		return new EnoTreeGroup(name);
	}

	public static String getAssignmentType(String name) throws EnoEclipseException, MatrixException {
		ArrayList<EnoTreeAssignment> allAssignments = getAllAssignments(false, null);
		for (Iterator<EnoTreeAssignment> iterator = allAssignments.iterator(); iterator.hasNext();) {
			EnoTreeAssignment a = iterator.next();
			if(a.getName().equals(name)) {
				return a.getType();
			}
		}
		throw new EnoEclipseException("Assignment not found in the list of all assignments in Matrix");
	}

	public void addChildAssignment(String assignmentType) throws EnoEclipseException, MatrixException {
		EnoTreeAssignment newAssignment = null;
		if (assignmentType.equals("Role")) {
			newAssignment = new EnoTreeRole("");
		} else {
			newAssignment = new EnoTreeGroup("");
		}
		addChildAssignment(newAssignment);
	}
	
	public void addChildAssignment(EnoTreeAssignment newAssignment) {
		getChildrenAssignments(false).add(newAssignment);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			IEnoBusinessViewer contentProvider = (IEnoBusinessViewer)iterator.next();
			//TODO
			//if((contentProvider instanceof org.mxeclipse.business.table.assignment.MxAssignmentComposite.MxAssignmentContentProvider) && !((org.mxeclipse.business.table.assignment.MxAssignmentComposite.MxAssignmentContentProvider)contentProvider).isParentAssignment()) {
			//	contentProvider.addProperty(newAssignment);
			//}
		}
	}

	public void removeChildAssignment(EnoTreeAssignment assignment) {
		if (this.childrenAssignments == null) {
			getChildrenAssignments(false);
		}
		this.childrenAssignments.remove(assignment);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			IEnoBusinessViewer contentProvider = (IEnoBusinessViewer)iterator.next();
			//TODO
//			if((contentProvider instanceof org.mxeclipse.business.table.assignment.MxAssignmentComposite.MxAssignmentContentProvider) && !((org.mxeclipse.business.table.assignment.MxAssignmentComposite.MxAssignmentContentProvider)contentProvider).isParentAssignment()) {
//				contentProvider.removeProperty(assignment);
//			}
		}
	}

	public void addParentAssignment(String assignmentType) throws EnoEclipseException, MatrixException {
		EnoTreeAssignment newAssignment = null;
		if (assignmentType.equals("Role")) {
			newAssignment = new EnoTreeRole("");
		} else {
			newAssignment = new EnoTreeGroup("");
		}
		addParentAssignment(newAssignment);
	}
	public void addParentAssignment(EnoTreeAssignment newAssignment) {
		getParentAssignments(false).add(newAssignment);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			IEnoBusinessViewer contentProvider = (IEnoBusinessViewer)iterator.next();
			//TODO
//			if((contentProvider instanceof org.mxeclipse.business.table.assignment.MxAssignmentComposite.MxAssignmentContentProvider) && ((org.mxeclipse.business.table.assignment.MxAssignmentComposite.MxAssignmentContentProvider)contentProvider).isParentAssignment()) {
//				contentProvider.addProperty(newAssignment);
//			}
		}
	}

	public void removeParentAssignment(EnoTreeAssignment assignment) {
		if (this.childrenAssignments == null) {
			getParentAssignments(false);
		}
		this.parentAssignments.remove(assignment);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			IEnoBusinessViewer contentProvider = (IEnoBusinessViewer)iterator.next();
			//TODO
//			if((contentProvider instanceof org.mxeclipse.business.table.assignment.MxAssignmentComposite.MxAssignmentContentProvider) && ((org.mxeclipse.business.table.assignment.MxAssignmentComposite.MxAssignmentContentProvider)contentProvider).isParentAssignment()) {
//				contentProvider.removeProperty(assignment);
//			}
		}
	}

	public void addPerson() throws EnoEclipseException, MatrixException {
		addPerson(new EnoTreePerson(""));
	}

	public void addPerson(EnoTreePerson newPerson) {
		getPersons(false).add(newPerson);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			IEnoBusinessViewer contentProvider = (IEnoBusinessViewer)iterator.next();
			contentProvider.addProperty(newPerson);
		}
	}

	public void removePerson(EnoTreePerson person) {
		if (this.persons == null) {
			getPersons(false);
		}
		this.persons.remove(person);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			IEnoBusinessViewer contentProvider = (IEnoBusinessViewer)iterator.next();
			contentProvider.removeProperty(person);
		}
	}

	public void refresh() throws EnoEclipseException, MatrixException {
		super.refresh();

		fillBasics();
		this.parentAssignments = null;
		this.childrenAssignments = null;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isHidden() {
		return this.hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public void fillBasics() {
		try {
			Context context = getContext();
			MQLCommand command = new MQLCommand();
			command.executeCommand(context, MessageFormat.format(MQL_INFO, new Object[] { this.realName, this.name }));

			String[] info = command.getResult().trim().split("\\|");
			this.name = info[0];
			if (info.length > 1) {
				this.description = info[1];
			} else {
				this.description = "";
			}
			if (info.length > 2) {
				this.hidden = info[2].equalsIgnoreCase("true");
			} else {
				this.hidden = false;
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public void setSite(EnoTreeSite site) {
		this.site = site;
	}

	public static EnoTreeSite getSite(EnoTreeAssignment assignment) {
		try {
			String realName = assignment.getType().toLowerCase();
			Context context = getContext();
			MQLCommand command = new MQLCommand();
			command.executeCommand(context, MessageFormat.format(MQL_INFO_SITE, new Object[] { realName, assignment.getName() }));
			String siteName = command.getResult().trim();
			if ((siteName != null) && (!siteName.equals(""))) {
				return new EnoTreeSite(siteName);
			}
			return null;
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return null;
	}

	public EnoTreeSite getSite(boolean forceRefresh) {
		if (forceRefresh) {
			this.site = getSite(this);
		}
		return this.site;
	}

	public static ArrayList<EnoTreeAssignment> getAllAssignments(boolean refresh, String assignmentType) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeAssignment> retList = new ArrayList<EnoTreeAssignment>();
		if ((assignmentType == null) || (assignmentType.equals("Role"))) {
			ArrayList<EnoTreeRole> lstRoles = EnoTreeRole.getAllRoles(refresh);
			retList.addAll(lstRoles);
			if (assignmentType != null) {
				return retList;
			}
		}

		ArrayList<EnoTreeGroup> lstGroups = EnoTreeGroup.getAllGroups(refresh);
		retList.addAll(lstGroups);
		return retList;
	}

	public static ArrayList<EnoTreeAssignment> getAllAssignments(boolean refresh) throws MatrixException, EnoEclipseException {
		return getAllAssignments(refresh, null);
	}

	public static ArrayList<EnoTreeAssignment> getParentAssignments(EnoTreeAssignment assignment) {
		ArrayList<EnoTreeAssignment> retAssignments = new ArrayList<EnoTreeAssignment>();
		try {
			String realName = assignment.getType().toLowerCase();
			Context context = getContext();
			MQLCommand command = new MQLCommand();
			command.executeCommand(context, MessageFormat.format(MQL_INFO_PARENT, new Object[] { realName, assignment.getName() }));
			String[] t = command.getResult().split("\\|");
			for (int i = 0; i < t.length; i++) {
				String tname = t[i].trim();
				if (!tname.equals("")) {
					EnoTreeAssignment a = assignment.getType().equals("Role") ? new EnoTreeRole(tname) : new EnoTreeGroup(tname);
					a.setFrom(false);
					a.setRelType("inherits");
					a.setParent(assignment);
					retAssignments.add(a);
				}
			}
			Collections.sort(retAssignments);
			return retAssignments;
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return null;
	}

	public ArrayList<EnoTreeAssignment> getParentAssignments(boolean forceRefresh) {
		if ((forceRefresh) || (this.parentAssignments == null)) {
			this.parentAssignments = getParentAssignments(this);
		}
		return this.parentAssignments;
	}

	public static ArrayList<EnoTreeAssignment> getChildrenAssignments(EnoTreeAssignment assignment) {
		ArrayList<EnoTreeAssignment> retAssignments = new ArrayList<EnoTreeAssignment>();
		try {
			String realName = assignment.getType().toLowerCase();
			Context context = getContext();
			MQLCommand command = new MQLCommand();
			command.executeCommand(context, MessageFormat.format(MQL_INFO_CHILD, new Object[] { realName, assignment.getName() }));
			String[] t = command.getResult().split("\\|");
			for (int i = 0; i < t.length; i++) {
				String tname = t[i].trim();
				if (!tname.equals("")) {
					EnoTreeAssignment a = assignment.getType().equals("Role") ? new EnoTreeRole(tname) : new EnoTreeGroup(tname);
					a.setFrom(true);
					a.setRelType("inherits");
					a.setParent(assignment);
					retAssignments.add(a);
				}
			}
			Collections.sort(retAssignments);
			return retAssignments;
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return null;
	}

	public ArrayList<EnoTreeAssignment> getChildrenAssignments(boolean forceRefresh) {
		if ((forceRefresh) || (this.childrenAssignments == null)) {
			this.childrenAssignments = getChildrenAssignments(this);
		}
		return this.childrenAssignments;
	}

	public static ArrayList<EnoTreePerson> getPersons(EnoTreeAssignment assignment) {
		ArrayList<EnoTreePerson> retPersons = new ArrayList<EnoTreePerson>();
		try {
			String realTypeName = assignment.getType().toLowerCase();
			Context context = getContext();
			MQLCommand command = new MQLCommand();
			command.executeCommand(context, MessageFormat.format(MQL_INFO_PERSONS, new Object[] { realTypeName, assignment.getName() }));
			String[] t = command.getResult().split("\\|");
			for (int i = 0; i < t.length; i++) {
				String tname = t[i].trim();
				if (!tname.equals("")) {
					EnoTreePerson a = new EnoTreePerson(tname);
					a.setFrom(true);
					a.setRelType("contains");
					a.setParent(assignment);
					retPersons.add(a);
				}
			}
			Collections.sort(retPersons);
			return retPersons;
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return null;
	}

	public ArrayList<EnoTreePerson> getPersons(boolean forceRefresh) {
		if ((forceRefresh) || (this.persons == null)) {
			this.persons = getPersons(this);
		}
		return this.persons;
	}

	public String prepareSaveAssignments(Context context, MQLCommand command, boolean parentAssignment) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeAssignment> assignments = parentAssignment ? getParentAssignments(false) : getChildrenAssignments(false);
		String sAdded = "";

		for (Iterator<EnoTreeAssignment> iterator = assignments.iterator(); iterator.hasNext();) {
			EnoTreeAssignment assignment = (EnoTreeAssignment)iterator.next();
			if (!sAdded.equals("")) {
				sAdded = (new StringBuilder(String.valueOf(sAdded))).append(",").toString();
			}
			sAdded = (new StringBuilder(String.valueOf(sAdded))).append("\"").append(assignment.getName()).append("\"").toString();
		}
		return sAdded;
	}

	public void savePersons(Context context, MQLCommand command) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreePerson> oldPersons = getPersons(this);
		ArrayList<EnoTreePerson> persons = getPersons(false);
		String realTypeName = getType().toLowerCase();

		for (Iterator<EnoTreePerson> iterator = persons.iterator(); iterator.hasNext();) {
			EnoTreePerson person = (EnoTreePerson)iterator.next();
			if(person.getOldName().equals("")) {
				command.executeCommand(context, MessageFormat.format(MQL_ADD_PERSON, new Object[] {realTypeName, getName(), person.getName()}));
			}
		}

		if (oldPersons != null) {
			for (Iterator<EnoTreePerson> iterator1 = oldPersons.iterator(); iterator1.hasNext();) {
				EnoTreePerson oldPerson = (EnoTreePerson)iterator1.next();
				boolean bFound = false;
				for (Iterator<EnoTreePerson> iterator2 = persons.iterator(); iterator2.hasNext();) {
					EnoTreePerson person = (EnoTreePerson)iterator2.next();
					if (oldPerson.getName().equals(person.getName())) {
						bFound = true;
						break;
					}
					if (oldPerson.getName().equals(person.getOldName())) {
						command.executeCommand(context, MessageFormat.format(MQL_ADD_PERSON, new Object[] {realTypeName, getName(), person.getName()}));
						command.executeCommand(context, MessageFormat.format(MQL_REMOVE_PERSON, new Object[] {realTypeName, getName(), oldPerson.getOldName()}));
						bFound = true;
						break;
					}
				}

				if (!bFound) {
					command.executeCommand(context, MessageFormat.format(MQL_REMOVE_PERSON, new Object[] {realTypeName, getName(), oldPerson.getOldName()}));
				}
			}
		}
	}

	public void save() {
		try {
			MQLCommand command = new MQLCommand();
			Context context = getContext();
			this.assignment.open(context);
			try {
				String modString = "";
				String assignmentName = this.assignment.getName();
				boolean changedName = !assignmentName.equals(getName());
				if (changedName) {
					modString = modString + " name \"" + getName() + "\"";
				}

				command.executeCommand(context, MessageFormat.format(MQL_INFO, new Object[] { this.realName, assignmentName }));
				String[] info = command.getResult().trim().split("\\|");
				if (!info[1].equals(getDescription())) {
					modString = modString + " description \"" + getDescription() + "\"";
				}
				boolean oldIsHidden = info[2].equalsIgnoreCase("true");
				if (oldIsHidden != isHidden()) {
					modString = modString + (isHidden() ? " hidden" : " nothidden");
				}
				command.executeCommand(context, MessageFormat.format(MQL_INFO_SITE, new Object[] { this.realName, assignmentName }));
				String sSite = getSite(false) != null ? getSite(false).getName() : "";
				if (!command.getResult().trim().equals(sSite)) {
					modString = modString + " site \"" + sSite + "\"";
				}

				String sParentAssignments = prepareSaveAssignments(context, command, true);
				if (!sParentAssignments.equals("")) {
					modString = modString + " parent " + sParentAssignments;
				} else {
					modString = modString + " remove parent all";
				}

				String sChildAssignments = prepareSaveAssignments(context, command, false);
				if (!sChildAssignments.equals("")) {
					modString = modString + " child " + sChildAssignments;
				} else {
					modString = modString + " remove child all";
				}

				savePersons(context, command);

				if (!modString.equals("")) {
					command.executeCommand(context, "modify " + this.realName + " \"" + assignmentName + "\" " + modString + ";");
				}

				if (changedName) {
					if (this.type.equals("Role")) {
						this.assignment = new Role(this.name);
					} else {
						this.assignment = new Group(this.name);
					}
				}

				this.parentAssignments = null;
				this.childrenAssignments = null;
				this.persons = null;
				EnoTreeGroup.clearCache();
				EnoTreeRole.clearCache();
				EnoTreePerson.clearCache();
				refresh();
			} finally {
				this.assignment.close(context);
			}
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
			this.children.addAll(getChildrenAssignments(false));
			this.children.addAll(getParentAssignments(false));
			this.children.addAll(getPersons(false));
		}
		return (EnoTreeBusiness[])this.children.toArray(new EnoTreeBusiness[this.children.size()]);
	}
}