package org.enoeclipse.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.enoeclipse.Activator;
import org.enoeclipse.business.tree.EnoBusinessContentProvider;
import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;


public class EnoTreeBusiness implements Comparable<Object>, Cloneable {
	public static final String REL_TYPE_CONTAINS = "contains";
	public static final String REL_TYPE_INHERITS = "inherits";
	public static final String REL_TYPE_POLICY = "policy";
	public static final String REL_TYPE_FROM_TYPE = "from";
	public static final String REL_TYPE_TO_TYPE = "to";
	public static final String REL_TYPE_ACCESS = "access";
	protected String type;
	protected String name;
	protected String oldName;
	protected boolean inherited;
	protected boolean from;
	protected String relType;
	protected EnoTreeBusiness parent;
	protected ArrayList<EnoTreeBusiness> children;
	protected ArrayList<EnoTreeAttribute> attributes;
	protected ArrayList<EnoTreeTrigger> triggers;
	private EnoBusinessContentProvider contentProvider;
	protected Set<IEnoBusinessViewer> changeListeners = new HashSet<IEnoBusinessViewer>();

	protected static String MQL_ADD_TRIGGER = "modify {0} \"{1}\" add trigger {2} {3} \"{4}\" input \"{5}\";";
	protected static String MQL_ADD_STATE_TRIGGER = "modify policy \"{0}\" state \"{1}\" add trigger {2} {3} \"{4}\" input \"{5}\";";
	protected static String MQL_REMOVE_TRIGGER = "modify {0} \"{1}\" remove trigger {2} {3};";
	protected static String MQL_REMOVE_STATE_TRIGGER = "modify policy \"{0}\" state \"{1}\" remove trigger {2} {3};";

	//protected static String MQL_SIMPLE_LIST = "list {0};";
	protected static String MQL_SIMPLE_LIST_NEW = "list $1";
	//protected static String MQL_SIMPLE_PRINT = "print {0} \"{1}\";";
	protected static String MQL_SIMPLE_PRINT_NEW = "print $1 $2";
	//protected static String MQL_LIST_ALL = "list {0};";
	protected static String MQL_LIST_ALL_NEW = "list $1";
	protected static String MQL_CREATE_NEW = "add {0} \"{1}\";";
	protected static String MQL_CREATE_NEW_WEBFORM = "add {0} \"{1}\" web;";
	protected static String MQL_CREATE_NEW_TABLE = "add {0} \"{1}\" system;";
	protected static String MQL_CREATE_NEW_ATTRIBUTE = "add {0} \"{1}\" type {2};";
	protected static String MQL_DELETE = "delete {0} \"{1}\";";
	protected static String MQL_DELETE_TABLE = "delete {0} \"{1}\" system;";

	public EnoTreeBusiness() {
		this.children = new ArrayList<EnoTreeBusiness>();
	}

	public String getOldName() {
		return this.oldName;
	}

	public void addChild(EnoTreeBusiness child) {
		this.children.add(child);
		child.setParent(this);
	}

	protected EnoTreeBusiness(String type, String name) throws EnoEclipseException, MatrixException {
		this();
		this.type = type;
		this.oldName = name;
		this.name = name;
	}

	public static EnoTreeBusiness createBusiness(String type, String name) throws MatrixException, EnoEclipseException {
		EnoTreeBusiness newObject = null;
		if (type.equals("Attribute")) {
			newObject = new EnoTreeAttribute(name);
		} else if (type.equals("Type")) {
			newObject = new EnoTreeType(name);
		} else if (type.equals("Relationship")) {
			newObject = new EnoTreeRelationship(name);
		} else if (type.equals("Policy")) {
			newObject = new EnoTreePolicy(name);
		} else if (type.equals("Person")) {
			newObject = new EnoTreePerson(name);
		} else if (type.equals("Association")) {
			newObject = new EnoTreeAssociation(name);
		} else if (type.equals("Role")) {
			newObject = new EnoTreeRole(name);
		} else if (type.equals("Group")) {
			newObject = new EnoTreeGroup(name);
		} else if (type.equals("Program")) {
			newObject = new EnoTreeProgram(name);
		} else if (type.equals("Index")) {
			newObject = new EnoTreeIndex(name);
		} else if (type.equals("Menu")) {
			newObject = new EnoTreeWebMenu(name);
		} else if (type.equals("Command")) {
			newObject = new EnoTreeWebCommand(name);
		} else if (type.equals("Table")) {
			newObject = new EnoTreeWebTable(name);
		} else {
			newObject = new EnoTreeBusiness(type, name);
		}
		return newObject;
	}

	public EnoTreeBusiness[] getChildren(boolean forceUpdate) throws EnoEclipseException, MatrixException {
		if (forceUpdate) {
			this.children = null;
		}
		if (this.children == null) {
			this.children = new ArrayList<EnoTreeBusiness>();
		}

		return (EnoTreeBusiness[])this.children.toArray(new EnoTreeBusiness[this.children.size()]);
	}

	public EnoTreeBusiness getParent() {
		return this.parent;
	}

	public void setParent(EnoTreeBusiness parent) {
		this.parent = parent;
	}

	public static Context getContext() throws EnoEclipseException, MatrixException {
		Context context = Activator.getDefault().getContext();
		if ((context != null) && (context.isConnected())) {
			return context;
		}
		throw new EnoEclipseException("No user connected to ENOVIA");
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return this.type;
	}

	public boolean isInherited() {
		return this.inherited;
	}

	public void setInherited(boolean inherited) {
		this.inherited = inherited;
	}

	public boolean isFrom() {
		return this.from;
	}

	public void setFrom(boolean from) {
		this.from = from;
	}

	public String getRelType() {
		return this.relType;
	}

	public void setRelType(String relType) {
		this.relType = relType;
	}

	public static String[] getNames(ArrayList<EnoTreeBusiness> al) {
		String[] retVal = new String[al.size()];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = ((EnoTreeBusiness)al.get(i)).getName();
		}
		return retVal;
	}

	public ArrayList<EnoTreeAttribute> getAttributes(boolean forceRefresh) {
		return null;
	}

	public void addAttribute() throws EnoEclipseException, MatrixException {
		addAttribute(new EnoTreeAttribute(""));
	}

	public void addAttribute(EnoTreeAttribute newAttribute) {
		this.attributes.add(newAttribute);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoBusinessViewer)iterator.next()).addProperty(newAttribute); 
		}
	}

	public void removeAttribute(EnoTreeAttribute attribute) {
		if (this.attributes == null) {
			getAttributes(false);
		}
		this.attributes.remove(attribute);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoBusinessViewer)iterator.next()).removeProperty(attribute);
		}
	}

	public ArrayList<EnoTreeTrigger> getTriggers(boolean forceRefresh) {
		if ((forceRefresh) || (this.triggers == null)) {
			this.triggers = EnoTreeTrigger.getTriggersForObject(this);
		}
		return this.triggers;
	}

	public void addTrigger() throws EnoEclipseException, MatrixException {
		addTrigger(new EnoTreeTrigger(this));
	}

	public void addTrigger(EnoTreeTrigger newTrigger) {
		this.triggers.add(newTrigger);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoBusinessViewer)iterator.next()).addProperty(newTrigger); 
		}
	}

	public void removeTrigger(EnoTreeTrigger trigger) {
		if (this.triggers == null) {
			getTriggers(false);
		}
		this.triggers.remove(trigger);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoBusinessViewer)iterator.next()).removeProperty(trigger);
		}
	}

	public void saveTriggers(Context context, MQLCommand command) throws MatrixException {
		ArrayList<EnoTreeTrigger> oldTriggers = EnoTreeTrigger.getTriggersForObject(this);

		for (int i = 0; i < this.triggers.size(); i++) {
			if (!((EnoTreeTrigger)this.triggers.get(i)).getOldName().equals("")) {
				continue;
			}
			EnoTreeTrigger t = (EnoTreeTrigger)this.triggers.get(i);
			if (!this.type.equals("State")) {
				command.executeCommand(context, MessageFormat.format(MQL_ADD_TRIGGER, new Object[] { getType(), getName(), t.getEventType().toLowerCase(), t.getTriggerType(), t.getMainProgramName(), t.getArgs() }));
			} else {
				command.executeCommand(context, MessageFormat.format(MQL_ADD_STATE_TRIGGER, new Object[] { ((EnoTreeState)this).getPolicy().getName(), getName(), t.getEventType().toLowerCase(), t.getTriggerType(), t.getMainProgramName(), t.getArgs() }));
			}

		}

		if (oldTriggers != null)
			for (int i = 0; i < oldTriggers.size(); i++) {
				boolean bFound = false;
				EnoTreeTrigger oldTrigger = (EnoTreeTrigger)oldTriggers.get(i);
				for (int j = 0; j < this.triggers.size(); j++) {
					EnoTreeTrigger trigger = (EnoTreeTrigger)this.triggers.get(j);

					if (oldTrigger.getName().equals(trigger.getName())) {
						bFound = true;
						break;
					}
					if (oldTrigger.getName().equals(trigger.getOldName())) {
						if (!this.type.equals("State")) {
							command.executeCommand(context, MessageFormat.format(MQL_REMOVE_TRIGGER, new Object[] { getType(), getName(), oldTrigger.getEventType(), oldTrigger.getTriggerType() }));
							command.executeCommand(context, MessageFormat.format(MQL_ADD_TRIGGER, new Object[] { getType(), getName(), trigger.getEventType(), trigger.getTriggerType(), trigger.getMainProgramName(), trigger.getArgs() }));
						} else {
							command.executeCommand(context, MessageFormat.format(MQL_REMOVE_STATE_TRIGGER, new Object[] { ((EnoTreeState)this).getPolicy().getName(), getName(), oldTrigger.getEventType(), oldTrigger.getTriggerType() }));
							command.executeCommand(context, MessageFormat.format(MQL_ADD_STATE_TRIGGER, new Object[] { ((EnoTreeState)this).getPolicy().getName(), getName(), trigger.getEventType(), trigger.getTriggerType(), trigger.getMainProgramName(), trigger.getArgs() }));
						}
						bFound = true;
						break;
					}
				}
				if (bFound) {
					continue;
				}
				if (!this.type.equals("State")) {
					command.executeCommand(context, MessageFormat.format(MQL_REMOVE_TRIGGER, new Object[] { getType(), getName(), oldTrigger.getEventType(), oldTrigger.getTriggerType() }));
				} else {
					command.executeCommand(context, MessageFormat.format(MQL_REMOVE_STATE_TRIGGER, new Object[] { ((EnoTreeState)this).getPolicy().getName(), getName(), oldTrigger.getEventType(), oldTrigger.getTriggerType() }));
				}
			}
	}

	public static String[] findAdminObjects(String typeName, String namePattern) {
		String pattern = namePattern;
		StringBuffer query = new StringBuffer("list ");

		if ("WebForm".equalsIgnoreCase(typeName)) {
			query.append("Form");
		} else {
			query.append(typeName);
		}
		if (!typeName.equals("Association")) {
			query.append(" '").append(pattern);
			query.append("' select name ");
			if (("WebForm".equalsIgnoreCase(typeName)) || ("Form".equalsIgnoreCase(typeName))) {
				query.append("web ");
			}
			query.append("dump |");
		}
		try {
			Context context = getContext();
			MQLCommand command = new MQLCommand();
			boolean executed = command.executeCommand(context, query.toString());
			if (executed) {
				String result = command.getResult();
				String[] ret = result.split("\n");
				return ret;
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return new String[0];
	}

	public void save() {
	}

	public static EnoTreeBusiness create(String type, String name, String attributeType) throws EnoEclipseException, MatrixException {
		Context context = getContext();
		MQLCommand command = new MQLCommand();
		if (type.equals("Program")) {
			EnoTreeProgram.allPrograms = null;
		}
		if (type.equals("WebForm")) {
			command.executeCommand(context, MessageFormat.format(MQL_CREATE_NEW_WEBFORM, new Object[] { "Form".toLowerCase(), name }));
		} else if (type.equals("Table")) {
			command.executeCommand(context, MessageFormat.format(MQL_CREATE_NEW_TABLE, new Object[] { "Table".toLowerCase(), name }));
		} else if (type.equals("Attribute")) {
			command.executeCommand(context, MessageFormat.format(MQL_CREATE_NEW_ATTRIBUTE, new Object[] { type.toLowerCase(), name, attributeType }));
		} else {
			command.executeCommand(context, MessageFormat.format(MQL_CREATE_NEW, new Object[] { type.toLowerCase(), name }));
		}
		if (!command.getError().equals("")) {
			throw new EnoEclipseException(command.getError());
		}

		return createBusiness(type, name);
	}

	public void delete() throws EnoEclipseException, MatrixException {
		Context context = getContext();
		MQLCommand command = new MQLCommand();
		if (this.type.equals("WebForm")) {
			command.executeCommand(context, MessageFormat.format(MQL_DELETE, new Object[] { "Form".toLowerCase(), this.name }));
		} else if (this.type.equals("Table")) {
			command.executeCommand(context, MessageFormat.format(MQL_DELETE_TABLE, new Object[] { "Table".toLowerCase(), this.name }));
		} else {
			command.executeCommand(context, MessageFormat.format(MQL_DELETE, new Object[] { this.type.toLowerCase(), this.name }));
		}
		if (!command.getError().equals("")) {
			throw new EnoEclipseException(command.getError());
		}
	}

	public boolean hasChildren() {
		return (this.children != null) && (this.children.size() > 0);
	}

	public int compareTo(Object o) {
		if ((o instanceof EnoTreeBusiness)) {
			EnoTreeBusiness otherObject = (EnoTreeBusiness)o;

			int retVal = getType().compareToIgnoreCase(otherObject.getType());
			if (retVal == 0) {
				return getName().compareToIgnoreCase(otherObject.getName());
			}
			return retVal;
		}

		return -1;
	}

	public void setContentProvider(EnoBusinessContentProvider provider) {
		this.contentProvider = provider;
	}

	public void refresh() throws EnoEclipseException, MatrixException {
		if ((this instanceof ITriggerable)) {
			getTriggers(true);
		}
	}

	public void removeChangeListener(IEnoBusinessViewer viewer) {
		this.changeListeners.remove(viewer);
	}

	public void addChangeListener(IEnoBusinessViewer viewer) {
		this.changeListeners.add(viewer);
	}

	public void propertyChanged(EnoTreeBusiness task) {
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoBusinessViewer)iterator.next()).updateProperty(task);
		}
	}
}