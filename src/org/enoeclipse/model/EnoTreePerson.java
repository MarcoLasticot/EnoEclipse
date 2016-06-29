package org.enoeclipse.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import com.matrixone.apps.domain.util.MqlUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.db.Person;
import matrix.db.PersonItr;
import matrix.db.PersonList;
import matrix.db.User;
import matrix.db.UserList;
import matrix.util.MatrixException;


public class EnoTreePerson extends EnoTreeUser {
	Person person;
	private static ArrayList<EnoTreePerson> allPersons;
	protected static String MQL_INFO = "print person \"{0}\" select name fullname description hidden address phone fax email password passwordexpired neverexpires dump |;";
	protected static String MQL_INFO_SITE = "print person \"{0}\" select site dump |;";
	protected static String MQL_INFO_VAULT = "print person \"{0}\" select vault dump |;";
	protected static String MQL_ADD_ASSIGNMENT = "modify person \"{0}\" assign {1} \"{2}\";";
	protected static String MQL_REMOVE_ASSIGNMENT = "modify person \"{0}\" remove assign {1} \"{2}\";";
	protected static String MQL_MODIFY_PERSON_TYPES = "modify person \"{0}\" type {1}";
	protected static String MQL_MODIFY_ACCESS_RIGHTS = "modify person \"{0}\" access {1}";
	protected static String MQL_MODIFY_ADMIN_RIGHTS = "modify person \"{0}\" admin {1}";
	protected String fullName;
	protected String password;
	protected String passwordOption;
	protected String comment;
	protected boolean hidden;
	protected String address;
	protected String phone;
	protected String fax;
	protected String email;
	protected EnoTreeVault defaultVault;
	protected EnoTreeSite defaultSite;
	protected Set<String> accessRights = new TreeSet<String>();
	protected Set<String> adminRights = new TreeSet<String>();
	protected ArrayList<EnoTreeAssignment> roles;
	protected ArrayList<EnoTreeAssignment> groups;
	protected Set<String> personTypes;
	protected static final int INFO_NAME = 0;
	protected static final int INFO_FULL_NAME = 1;
	protected static final int INFO_COMMENT = 2;
	protected static final int INFO_HIDDEN = 3;
	protected static final int INFO_ADDRESS = 4;
	protected static final int INFO_PHONE = 5;
	protected static final int INFO_FAX = 6;
	protected static final int INFO_EMAIL = 7;
	protected static final int INFO_PASSWORD = 8;
	protected static final int INFO_PASSWORD_EXPIRED = 9;
	protected static final int INFO_NEVER_EXPIRES = 10;
	protected static final String TYPE = "type";
	protected static final String ACCESS = "access";
	protected static final String ADMIN = "admin";
	public static final String PERSON_TYPE_APPLICATION = "application";
	public static final String PERSON_TYPE_FULL = "full";
	public static final String PERSON_TYPE_BUSINESS = "business";
	public static final String PERSON_TYPE_SYSTEM = "system";
	public static final String PERSON_TYPE_INACTIVE = "inactive";
	public static final String PERSON_TYPE_TRUSTED = "trusted";
	public static final String[] PERSON_TYPES = {
		"application", 
		"full", 
		"business", 
		"system", 
		"inactive", 
		"trusted"
	};
	public static final String PASSWORD_OPTION_NORMAL = "";
	public static final String PASSWORD_OPTION_NO_PASSWORD = "No Password";
	public static final String PASSWORD_OPTION_DISABLE_PASSWORD = "Disabled";
	public static final String PASSWORD_OPTION_PASSWORD_EXPIRED = "Change Required";
	public static final String PASSWORD_OPTION_NEVER_EXPIRE = "Never Expires";
	public static final String[] PASSWORD_OPTIONS = {
		"", 
		"No Password", 
		"Disabled", 
		"Change Required", 
		"Never Expires"
	};
	public static final String ACCESS_READ = "read";
	public static final String ACCESS_MODIFY = "modify";
	public static final String ACCESS_DELETE = "delete";
	public static final String ACCESS_CHECKOUT = "checkout";
	public static final String ACCESS_CHECKIN = "checkin";
	public static final String ACCESS_SCHEDULE = "schedule";
	public static final String ACCESS_LOCK = "lock";
	public static final String ACCESS_EXECUTE = "execute";
	public static final String ACCESS_UNLOCK = "unlock";
	public static final String ACCESS_FREEZE = "freeze";
	public static final String ACCESS_THAW = "thaw";
	public static final String ACCESS_CREATE = "create";
	public static final String ACCESS_REVISE = "revise";
	public static final String ACCESS_PROMOTE = "promote";
	public static final String ACCESS_DEMOTE = "demote";
	public static final String ACCESS_GRANT = "grant";
	public static final String ACCESS_ENABLE = "enable";
	public static final String ACCESS_DISABLE = "disable";
	public static final String ACCESS_OVERRIDE = "override";
	public static final String ACCESS_CHANGENAME = "changename";
	public static final String ACCESS_CHANGETYPE = "changetype";
	public static final String ACCESS_CHANGEOWNER = "changeowner";
	public static final String ACCESS_CHANGEPOLICY = "changepolicy";
	public static final String ACCESS_REVOKE = "revoke";
	public static final String ACCESS_CHANGEVAULT = "changevault";
	public static final String ACCESS_FROMCONNECT = "fromconnect";
	public static final String ACCESS_TOCONNECT = "toconnect";
	public static final String ACCESS_FROMDISCONNECT = "fromdisconnect";
	public static final String ACCESS_TODISCONNECT = "todisconnect";
	public static final String ACCESS_VIEWFORM = "viewform";
	public static final String ACCESS_MODIFYFORM = "modifyform";
	public static final String ACCESS_SHOW = "show";
	public static final String[] ACCESS_OPTIONS = {
		"read", "modify", "delete", "checkout", 
		"checkin", "schedule", "lock", "execute", "unlock", "freeze", "thaw", 
		"create", "revise", "promote", "demote", "grant", "enable", "disable", 
		"override", "changename", "changetype", "changeowner", "changepolicy", "revoke", 
		"changevault", "fromconnect", "toconnect", "fromdisconnect", "todisconnect", 
		"viewform", "modifyform", "show" 
	};
	public static final String ACCESS_ALL = "all";
	public static final String ACCESS_NONE = "none";
	public static final String ADMIN_ATTRIBUTE = "attribute";
	public static final String ADMIN_TYPE = "type";
	public static final String ADMIN_RELATIONSHIP = "relationship";
	public static final String ADMIN_FORMAT = "format";
	public static final String ADMIN_PERSON = "person";
	public static final String ADMIN_GROUP = "group";
	public static final String ADMIN_ROLE = "role";
	public static final String ADMIN_ASSOCIATION = "association";
	public static final String ADMIN_POLICY = "policy";
	public static final String ADMIN_PROGRAM = "program";
	public static final String ADMIN_WIZARD = "wizard";
	public static final String ADMIN_REPORT = "report";
	public static final String ADMIN_FORM = "form";
	public static final String ADMIN_RULE = "rule";
	public static final String ADMIN_PROPERTY = "property";
	public static final String ADMIN_SITE = "site";
	public static final String ADMIN_STORE = "store";
	public static final String ADMIN_VAULT = "vault";
	public static final String ADMIN_SERVER = "server";
	public static final String ADMIN_LOCATION = "location";
	public static final String ADMIN_PROCESS = "process";
	public static final String ADMIN_MENU = "menu";
	public static final String ADMIN_INQUIRY = "inquiry";
	public static final String ADMIN_TABLE = "table";
	public static final String ADMIN_PORTAL = "portal";
	public static final String ADMIN_EXPRESSION = "expression";
	public static final String[] ADMIN_OPTIONS = {
		"attribute", "type", "relationship", "format", 
		"person", "group", "role", "association", "policy", "program", "wizard", "report", 
		"form", "rule", "property", "site", "store", "vault", "server", "location", 
		"process", "menu", "inquiry", "table", "portal", "expression"
	};
	public static final String ADMIN_ALL = "all";
	public static final String ADMIN_NONE = "none";

	public EnoTreePerson(String name) throws EnoEclipseException, MatrixException {
		super("Person", name);
		this.person = new Person(name);
	}

	public void refresh() throws EnoEclipseException, MatrixException {
		super.refresh();
		this.person = new Person(getName());
		fillBasics();
		this.defaultSite = null;
		this.defaultVault = null;
		this.roles = null;
		this.groups = null;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setVault(EnoTreeVault defaultVault) {
		this.defaultVault = defaultVault;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFax() {
		return this.fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getFullName() {
		return this.fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public boolean isHidden() {
		return this.hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordOption() {
		return this.passwordOption;
	}

	public void setPasswordOption(String passwordOption) {
		this.passwordOption = passwordOption;
	}

	public void setAccessRights(String sAccess) {
		String[] arrAccessRights = sAccess.split(",");
		setAccessRights(arrAccessRights);
	}

	public void setAccessRights(String[] arrAccessRights) {
		this.accessRights = splitAccessRights(arrAccessRights);
	}

	public static Set<String> splitAccessRights(String sAccess) {
		String[] arrAccessRights = sAccess.split(",");
		return splitAccessRights(arrAccessRights);
	}

	public static Set<String> splitAccessRights(String[] arrAccessRights) {
		Set<String> retSet = new TreeSet<String>();
		for (int i = 0; i < arrAccessRights.length; i++) {
			if (arrAccessRights[i].equals("all")) {
				retSet.clear();
				for (int j = 0; j < ACCESS_OPTIONS.length; j++) {
					retSet.add(ACCESS_OPTIONS[j]);
				}
				return retSet;
			}
			if (!arrAccessRights[i].equals("none")) {
				retSet.add(arrAccessRights[i]);
			}
		}
		return retSet;
	}

	public void setAdminRights(String sAdmin) {
		String[] arrAdminRights = sAdmin.split(",");
		setAdminRights(arrAdminRights);
	}

	public void setAdminRights(String[] arrAdminRights) {
		this.adminRights = splitAdminRights(arrAdminRights);
	}

	public static Set<String> splitAdminRights(String sAdmin) {
		String[] arrAdminRights = sAdmin.split(",");
		return splitAdminRights(arrAdminRights);
	}

	public static Set<String> splitAdminRights(String[] arrAdminRights) {
		Set<String> retSet = new TreeSet<String>();
		for (int i = 0; i < arrAdminRights.length; i++) {
			if (arrAdminRights[i].equals("all")) {
				retSet.clear();
				for (int j = 0; j < ADMIN_OPTIONS.length; j++) {
					retSet.add(ADMIN_OPTIONS[j]);
				}
				return retSet;
			}
			if (!arrAdminRights[i].equals("none")) {
				retSet.add(arrAdminRights[i]);
			}
		}
		return retSet;
	}

	public void fillBasics() {
		try {
			Context context = getContext();
			MQLCommand command = new MQLCommand();
			command.executeCommand(context, MessageFormat.format(MQL_INFO, new Object[] { this.name }));

			String[] info = command.getResult().trim().split("\\|");
			this.name = info[0];
			this.fullName = info[1];
			this.comment = info[2];
			this.hidden = info[3].equalsIgnoreCase("true");
			if (info.length > 4) {
				this.address = info[4];
			} else {
				this.address = "";
			}
			if (info.length > 5) {
				this.phone = info[5];
			} else {
				this.phone = "";
			}
			if (info.length > 6) {
				this.fax = info[6];
			} else {
				this.fax = "";
			}
			if (info.length > 7) {
				this.email = info[7];
			} else {
				this.email = "";
			}
			if (info.length > 8) {
				this.password = info[8];
			} else {
				this.password = "";
			}
			this.passwordOption = "";
			if (this.password.equals("<NONE>")) {
				this.passwordOption = "No Password";
			} else if (this.password.equals("<DISABLED>")) {
				this.passwordOption = "Disabled";
			} else if ((info.length > 9) && (info[9].equalsIgnoreCase("true"))) {
				this.passwordOption = "Change Required";
			}
			else if ((info.length > 10) && (info[10].equalsIgnoreCase("true"))) {
				this.passwordOption = "Never Expires";
			}
			//command.executeCommand(context, MessageFormat.format(EnoTreeState.MQL_SIMPLE_PRINT, new Object[] { "Person", getName() }));
			String[] lines = MqlUtil.mqlCommand(context, MQL_SIMPLE_PRINT_NEW, "Person", getName()).split("\n");

			for (int i = 0; i < lines.length; i++) {
				lines[i] = lines[i].trim();
				if (lines[i].startsWith("access")) {
					setAccessRights(lines[i].substring("access".length()).trim());
				} else if (lines[i].startsWith("admin"))  {
					setAdminRights(lines[i].substring("admin".length()).trim());
				}
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public void setSite(EnoTreeSite site) {
		this.defaultSite = site;
	}

	public static EnoTreeSite getSite(EnoTreePerson person) {
		try {
			Context context = getContext();
			MQLCommand command = new MQLCommand();
			command.executeCommand(context, MessageFormat.format(MQL_INFO_SITE, new Object[] { person.getName() }));
			String siteName = command.getResult().trim();
			if ((siteName != null) && (!siteName.equals(""))) {
				return new EnoTreeSite(siteName);
			}
			return null;
		}
		catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}return null;
	}

	public EnoTreeSite getSite(boolean forceRefresh) {
		if (forceRefresh) {
			this.defaultSite = getSite(this);
		}
		return this.defaultSite;
	}

	public static EnoTreeVault getVault(EnoTreePerson person) {
		try {
			Context context = getContext();
			MQLCommand command = new MQLCommand();
			command.executeCommand(context, MessageFormat.format(MQL_INFO_VAULT, new Object[] { person.getName() }));
			String vaultName = command.getResult().trim();
			if ((vaultName != null) && (!vaultName.equals(""))) {
				return new EnoTreeVault(vaultName);
			}
			return null;
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}return null;
	}

	public EnoTreeVault getVault(boolean forceRefresh) {
		if (forceRefresh) {
			this.defaultVault = getVault(this);
		}
		return this.defaultVault;
	}

	public static ArrayList<EnoTreePerson> getAllPersons(boolean refresh) throws MatrixException, EnoEclipseException {
		if ((refresh) || (allPersons == null)) {
			PersonList pl = Person.getPersons(getContext());
			allPersons = new ArrayList<EnoTreePerson>();
			PersonItr pi = new PersonItr(pl);
			while (pi.next()) {
				Person p = pi.obj();
				EnoTreePerson person = new EnoTreePerson(p.getName());
				allPersons.add(person);
			}
			Collections.sort(allPersons);
		}
		return allPersons;
	}

	public static ArrayList<EnoTreeAssignment> getAssignments(EnoTreePerson person, String assignmentType) {
		ArrayList<EnoTreeAssignment> retAssignments = new ArrayList<EnoTreeAssignment>();
		try {
			Context context = getContext();
			person.person.open(context);
			try {
				UserList ul = person.person.getAssignments(context);
				for (Iterator<Object> localIterator = ul.iterator(); localIterator.hasNext(); ) { 
					Object oUser = localIterator.next();
					User u = (User)oUser;
					String an = u.getName();
					String at = EnoTreeAssignment.getAssignmentType(an);
					if ((assignmentType == null) || (assignmentType.equals(at))) {
						EnoTreeAssignment child = EnoTreeAssignment.createAssignment(an);
						child.setParent(person);
						child.setFrom(false);
						child.setRelType("contains");
						retAssignments.add(child);
					}
				}
			} finally {
				person.person.close(context);
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return retAssignments;
	}

	public ArrayList<EnoTreeAssignment> getRoles(boolean forceRefresh) {
		if ((forceRefresh) || (this.roles == null)) {
			this.roles = getAssignments(this, "Role");
		}
		return this.roles;
	}

	public ArrayList<EnoTreeAssignment> getGroups(boolean forceRefresh) {
		if ((forceRefresh) || (this.groups == null)) {
			this.groups = getAssignments(this, "Group");
		}
		return this.groups;
	}

	public ArrayList<EnoTreeAssignment> getAssignments(boolean forceRefresh) {
		ArrayList<EnoTreeAssignment> assignments = getRoles(forceRefresh);
		assignments.addAll(getGroups(forceRefresh));
		return assignments;
	}

	public static Set<String> getPersonTypes(EnoTreePerson person) {
		Set<String> retTypes = new HashSet<String>();
		try {
			//MQLCommand command = new MQLCommand();
			Context context = getContext();
			//command.executeCommand(context, MessageFormat.format(MQL_SIMPLE_PRINT, new Object[] { person.getType().toLowerCase(), person.getName() }));
			String[] lines = MqlUtil.mqlCommand(context, MQL_SIMPLE_PRINT_NEW, person.getType().toLowerCase(), person.getName()).split("\n");
			for (int i = 0; i < lines.length; i++) {
				lines[i] = lines[i].trim();
				if (lines[i].startsWith("type")) {
					String sTypes = lines[i].replaceFirst("type", "").trim();
					String[] types = sTypes.split(",");
					for (String personType : types) {
						retTypes.add(personType.trim());
					}
				}
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return retTypes;
	}

	public Set<String> getPersonTypes(boolean forceRefresh) {
		if ((forceRefresh) || (this.personTypes == null)) {
			this.personTypes = getPersonTypes(this);
		}
		return this.personTypes;
	}

	public static Set<String> getAccessRights(EnoTreePerson person) {
		Set<String> retRights = new HashSet<String>();
		try {
			//MQLCommand command = new MQLCommand();
			Context context = getContext();
			//command.executeCommand(context, MessageFormat.format(MQL_SIMPLE_PRINT, new Object[] { person.getType().toLowerCase(), person.getName() }));
			String[] lines = MqlUtil.mqlCommand(context, MQL_SIMPLE_PRINT_NEW, person.getType().toLowerCase(), person.getName()).split("\n");
			for (int i = 0; i < lines.length; i++) {
				lines[i] = lines[i].trim();

				if (lines[i].startsWith("access")) {
					return splitAccessRights(lines[i].substring("access".length()).trim());
				}
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return retRights;
	}

	public Set<String> getAccessRights(boolean forceRefresh) {
		if ((forceRefresh) || (this.accessRights == null)) {
			this.accessRights = getAccessRights(this);
		}
		return this.accessRights;
	}

	public static Set<String> getAdminRights(EnoTreePerson person) {
		Set<String> retRights = new HashSet<String>();
		try {
			//MQLCommand command = new MQLCommand();
			Context context = getContext();
			//command.executeCommand(context, MessageFormat.format(MQL_SIMPLE_PRINT, new Object[] { person.getType().toLowerCase(), person.getName() }));
			String[] lines = MqlUtil.mqlCommand(context, MQL_SIMPLE_PRINT_NEW, person.getType().toLowerCase(), person.getName()).split("\n");
			for (int i = 0; i < lines.length; i++) {
				lines[i] = lines[i].trim();

				if (lines[i].startsWith("admin")) {
					return splitAdminRights(lines[i].substring("admin".length()).trim());
				}
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return retRights;
	}

	public Set<String> getAdminRights(boolean forceRefresh) {
		if ((forceRefresh) || (this.adminRights == null)) {
			this.adminRights = getAdminRights(this);
		}
		return this.adminRights;
	}

	public void addAssignment(String assignmentType) throws EnoEclipseException, MatrixException {
		EnoTreeAssignment newAssignment = null;
		if (assignmentType.equals("Role")) {
			newAssignment = new EnoTreeRole("");
		} else {
			newAssignment = new EnoTreeGroup("");
		}
		addAssignment(newAssignment);
	}

	public void addAssignment(EnoTreeAssignment newType) {
		if (newType instanceof EnoTreeRole) {
			roles.add(newType);
		} else {
			groups.add(newType);
		}
		for (Iterator<IEnoBusinessViewer> iterator = changeListeners.iterator(); iterator.hasNext();) {
			IEnoBusinessViewer contentProvider = (IEnoBusinessViewer)iterator.next();
//			if((contentProvider instanceof org.mxeclipse.business.table.assignment.MxAssignmentComposite.MxAssignmentContentProvider) && newType.getType().equals(((org.mxeclipse.business.table.assignment.MxAssignmentComposite.MxAssignmentContentProvider)contentProvider).getAssignmentType())) {
//				contentProvider.addProperty(newType);
//			}
		}

	}

	public void removeAssignment(EnoTreeAssignment assignment) {
		if ((assignment instanceof EnoTreeRole)) {
			if (this.roles == null) {
				getRoles(false);
			}
			this.roles.remove(assignment);
		} else {
			if (this.groups == null) {
				getGroups(false);
			}
			this.groups.remove(assignment);
		}
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoBusinessViewer)iterator.next()).removeProperty(assignment);
		}
	}

	public void saveAssignments(Context context, MQLCommand command, String assignmentType) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeAssignment> oldAssignments = getAssignments(this, assignmentType);
		ArrayList<EnoTreeAssignment> assignments = assignmentType.equals("Role") ? getRoles(false) : getGroups(false);
		for (Iterator<EnoTreeAssignment> iterator = assignments.iterator(); iterator.hasNext();) {
			EnoTreeAssignment assignment = (EnoTreeAssignment)iterator.next();
			if (assignment.getOldName().equals("")){				
				command.executeCommand(context, MessageFormat.format(MQL_ADD_ASSIGNMENT, new Object[] {getName(), assignmentType.toLowerCase(), assignment.getName()}));
			}
		}

		if (oldAssignments != null) {
			for (Iterator<EnoTreeAssignment> iterator1 = oldAssignments.iterator(); iterator1.hasNext();) {
				EnoTreeAssignment oldAssignment = (EnoTreeAssignment)iterator1.next();
				boolean bFound = false;
				for (Iterator<EnoTreeAssignment> iterator2 = assignments.iterator(); iterator2.hasNext();) {
					EnoTreeAssignment assignment = (EnoTreeAssignment)iterator2.next();
					if(oldAssignment.getName().equals(assignment.getName())) {
						bFound = true;
						break;
					}
					if (oldAssignment.getName().equals(assignment.getOldName())) {
						command.executeCommand(context, MessageFormat.format(MQL_ADD_ASSIGNMENT, new Object[] {getName(), assignmentType.toLowerCase(), assignment.getName()}));
						command.executeCommand(context, MessageFormat.format(MQL_REMOVE_ASSIGNMENT, new Object[] {getName(), assignmentType.toLowerCase(), oldAssignment.getOldName()}));
						bFound = true;
						break;
					}
				}

				if(!bFound) {
					command.executeCommand(context, MessageFormat.format(MQL_REMOVE_ASSIGNMENT, new Object[] {getName(), assignmentType.toLowerCase(), oldAssignment.getOldName()}));
				}
			}

		}
	}
	public void savePersonTypes(Context context, MQLCommand command) throws MatrixException {
		Set<String> oldPersonTypes = getPersonTypes(this);
		Set<String> personTypes = getPersonTypes(false);

		String typeClause = "";
		for (String personType : PERSON_TYPES) {
			if ((personTypes.contains(personType)) && (!oldPersonTypes.contains(personType))) {
				typeClause = typeClause + (!typeClause.equals("") ? "," : "") + personType;
			}
			if ((oldPersonTypes.contains(personType)) && (!personTypes.contains(personType))) {
				typeClause = typeClause + (!typeClause.equals("") ? "," : "") + "not" + personType;
			}
		}

		if (!typeClause.equals("")) {
			command.executeCommand(context, MessageFormat.format(MQL_MODIFY_PERSON_TYPES, new Object[] { getName(), typeClause }));
		}
	}

	public void saveRights(Context context, MQLCommand command, boolean adminRights) throws MatrixException {
		Set<String> oldRights = adminRights ? getAdminRights(this) : getAccessRights(this);
		Set<String> rights = adminRights ? getAdminRights(false) : getAccessRights(false);

		String rightsClause = "";
		boolean atLeastOneChange = false;
		for (String oneAdminRight : adminRights ? ADMIN_OPTIONS : ACCESS_OPTIONS) {
			if (rights.contains(oneAdminRight)) {
				rightsClause = rightsClause + (!rightsClause.equals("") ? "," : "") + oneAdminRight;
			}
			if ((rights.contains(oneAdminRight)) && (!oldRights.contains(oneAdminRight))) {
				atLeastOneChange = true;
			}
			if ((oldRights.contains(oneAdminRight)) && (!rights.contains(oneAdminRight))) {
				atLeastOneChange = true;
			}
		}

		if (atLeastOneChange) {
			command.executeCommand(context, MessageFormat.format(adminRights ? MQL_MODIFY_ADMIN_RIGHTS : MQL_MODIFY_ACCESS_RIGHTS, new Object[] { getName(), rightsClause }));
		}
	}

	public void save() {
		try {
			MQLCommand command = new MQLCommand();
			Context context = getContext();
			this.person.open(context);
			try {
				String modString = "";
				String personName = this.person.getName();
				boolean changedName = !personName.equals(getName());
				if (changedName) {
					modString = modString + " name \"" + getName() + "\"";
				}

				command.executeCommand(context, MessageFormat.format(MQL_INFO, new Object[] { this.person.getName() }));
				String[] info = command.getResult().trim().split("\\|");
				if (!info[1].equals(getFullName())) {
					modString = modString + " fullname \"" + getFullName() + "\"";
				}
				if (!info[2].equals(getComment())) {
					modString = modString + " description \"" + getComment() + "\"";
				}
				boolean oldIsHidden = info[3].equalsIgnoreCase("true");
				if (oldIsHidden != isHidden()) {
					modString = modString + (isHidden() ? " hidden" : " nothidden");
				}

				String oldAddress = info.length > 4 ? info[4] : "";
				if (!oldAddress.equals(getAddress())) {
					modString = modString + " address \"" + getAddress() + "\"";
				}
				String oldPhone = info.length > 5 ? info[5] : "";
				if (!oldPhone.equals(getPhone())) {
					modString = modString + " phone \"" + getPhone() + "\"";
				}
				String oldFax = info.length > 6 ? info[6] : "";
				if (!oldFax.equals(getFax())) {
					modString = modString + " fax \"" + getFax() + "\"";
				}
				String oldEmail = info.length > 7 ? info[7] : "";
				if (!oldEmail.equals(getEmail())) {
					modString = modString + " email \"" + getEmail() + "\"";
				}
				command.executeCommand(context, MessageFormat.format(MQL_INFO_SITE, new Object[] { this.person.getName() }));
				String sSite = getSite(false) != null ? getSite(false).getName() : "";
				if (!command.getResult().trim().equals(sSite)) {
					modString = modString + " site \"" + sSite + "\"";
				}
				String sVault = getVault(false) != null ? getVault(false).getName() : "";
				command.executeCommand(context, MessageFormat.format(MQL_INFO_VAULT, new Object[] { this.person.getName() }));
				if (!command.getResult().trim().equals(sVault)) {
					modString = modString + " vault \"" + sVault + "\"";
				}

				String oldPassword = info.length > 8 ? info[8] : "";
				String oldPasswordOption = "";
				if (oldPassword.equals("<NONE>")) {
					oldPasswordOption = "No Password";
				} else if (oldPassword.equals("<DISABLED>")) {
					oldPasswordOption = "Disabled";
				} else if ((info.length > 9) && (info[9].equalsIgnoreCase("true"))) {
					oldPasswordOption = "Change Required";
				} else if ((info.length > 10) && (info[10].equalsIgnoreCase("true"))) {
					oldPasswordOption = "Never Expires";
				}

				if (!oldPasswordOption.equals(this.passwordOption)) {
					if (oldPasswordOption.equals("Change Required")) {
						modString = modString + " !passwordexpired";
					} else if (oldPasswordOption.equals("Never Expires")) {
						modString = modString + " !neverexpire";
					}
					if (this.passwordOption.equals("Change Required")) {
						modString = modString + " passwordexpired";
					} else if (this.passwordOption.equals("Never Expires")) {
						modString = modString + " neverexpire";
					} else if (this.passwordOption.equals("Disabled")) {
						modString = modString + " disable password";
					} else if (this.passwordOption.equals("No Password")) {
						modString = modString + " no password";
					} else if ((this.password.equals("")) && ((oldPasswordOption.equals("No Password")) || (oldPasswordOption.equals("Disabled")))) {
						modString = modString + " password \"" + this.password + "\"";
					}
				}

				if (!this.password.equals("")) {
					modString = modString + " password \"" + this.password + "\"";
				}

				if (!modString.equals("")) {
					command.executeCommand(context, "modify person \"" + personName + "\" " + modString + ";");
				}

				if (changedName) {
					this.person = new Person(this.name);
				}

				saveAssignments(context, command, "Role");
				saveAssignments(context, command, "Group");
				savePersonTypes(context, command);
				saveRights(context, command, false);
				saveRights(context, command, true);

				clearCache();
				EnoTreeGroup.clearCache();
				EnoTreeRole.clearCache();
				refresh();
			} finally {
				this.person.close(context);
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
			this.children.addAll(getRoles(false));
			this.children.addAll(getGroups(false));
		}
		return (EnoTreeBusiness[])this.children.toArray(new EnoTreeBusiness[this.children.size()]);
	}

	public static void clearCache() {
		allPersons = null;
	}
}