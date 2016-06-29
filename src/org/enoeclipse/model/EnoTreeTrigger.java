package org.enoeclipse.model;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import com.matrixone.apps.domain.util.MqlUtil;

import matrix.db.Context;
import matrix.util.MatrixException;

public class EnoTreeTrigger extends EnoTreeBusiness
{
	protected String eventType;
	protected String triggerType;
	protected String mainProgramName = "emxTriggerManager";
	protected String args;
	protected String[] availableEventTypes;
	protected EnoTreeBusiness parentAdminObject;
	public static final String TRIGGER_OBJECT_TYPE = "eService Trigger Program Parameters";
	public static final String TRIGGER_TYPE_ACTION = "action";
	public static final String TRIGGER_TYPE_CHECK = "check";
	public static final String TRIGGER_TYPE_OVERRIDE = "override";
	public static final String[] TRIGGER_TYPES = { "check", "override", "action" };
	public static final String EVENT_ATTRIBUTE_REVISION = "Modify";
	public static final String[] EVENTS_ATTRIBUTE = { "Modify" };
	public static final String EVENT_TYPE_CHANGEVAULT = "ChangeVault";
	public static final String EVENT_TYPE_CHANGEPOLICY = "ChangePolicy";
	public static final String EVENT_TYPE_CHECKOUT = "Checkout";
	public static final String EVENT_TYPE_CREATE = "Create";
	public static final String EVENT_TYPE_GRANT = "Grant";
	public static final String EVENT_TYPE_MODIFYDESCRIPTION = "ModifyDescription";
	public static final String EVENT_TYPE_REVOKE = "Revoke";
	public static final String EVENT_TYPE_CHANGENAME = "ChangeName";
	public static final String EVENT_TYPE_CHANGETYPE = "ChangeType";
	public static final String EVENT_TYPE_CONNECT = "Connect";
	public static final String EVENT_TYPE_DELETE = "Delete";
	public static final String EVENT_TYPE_LOCK = "Lock";
	public static final String EVENT_TYPE_REMOVEFILE = "RemoveFile";
	public static final String EVENT_TYPE_UNLOCK = "Unlock";
	public static final String EVENT_TYPE_CHANGEOWNER = "ChangeOwner";
	public static final String EVENT_TYPE_CHECKIN = "Checkin";
	public static final String EVENT_TYPE_COPY = "Copy";
	public static final String EVENT_TYPE_DISCONNECT = "Disconnect";
	public static final String EVENT_TYPE_MODIFYATTRIBUTE = "ModifyAttribute";
	public static final String EVENT_TYPE_REVISION = "Revision";
	public static final String[] EVENTS_TYPE = { "ChangeVault", "ChangePolicy", "Checkout", "Create", 
			"Grant", "ModifyDescription", "Revoke", "ChangeName", "ChangeType", "Connect", 
			"Delete", "Lock", "RemoveFile", "Unlock", "ChangeOwner", "Checkin", 
			"Copy", "Disconnect", "ModifyAttribute", "Revision" };
	public static final String EVENT_STATE_APPROVE = "Approve";
	public static final String EVENT_STATE_ENABLE = "Enable";
	public static final String EVENT_STATE_PROMOTE = "Promote";
	public static final String EVENT_STATE_UNSIGN = "Unsign";
	public static final String EVENT_STATE_DEMOTE = "Demote";
	public static final String EVENT_STATE_IGNORE = "Ignore";
	public static final String EVENT_STATE_REJECT = "Reject";
	public static final String EVENT_STATE_DISABLE = "Disable";
	public static final String EVENT_STATE_OVERRIDE = "Override";
	public static final String EVENT_STATE_SCHEDULE = "Schedule";
	public static final String[] EVENTS_STATE = { "Approve", "Enable", "Promote", "Unsign", 
			"Demote", "Ignore", "Reject", "Disable", "Override", "Schedule" };
	public static final String EVENT_RELATIONSHIP_CREATE = "Create";
	public static final String EVENT_RELATIONSHIP_DELETE = "Delete";
	public static final String EVENT_RELATIONSHIP_MODIFYATTRIBUTE = "ModifyAttribute";
	public static final String EVENT_RELATIONSHIP_FREEZE = "Freeze";
	public static final String EVENT_RELATIONSHIP_THAW = "Thaw";
	public static final String EVENT_RELATIONSHIP_MODIFYFROM = "ModifyFrom";
	public static final String EVENT_RELATIONSHIP_MODIFYTO = "ModifyTo";
	public static final String[] EVENTS_RELATIONSHIP = { "Create", "Delete", "ModifyAttribute", 
			"Freeze", "Thaw", "ModifyFrom", "ModifyTo" };
	public static final String TRIGGER = "trigger";
	public static final String INHERITED_TRIGGER = "inherited trigger";
	protected String description;
	protected String sequence;
	protected static final String TRIGGER_NAME = "{0}-{1}-{2}-{3}-{4}";

	public EnoTreeTrigger(EnoTreeBusiness parentAdminObject, String eventType, String triggerType, String programName, String args)
			throws EnoEclipseException, MatrixException
	{
		super("Trigger", MessageFormat.format("{0}-{1}-{2}-{3}-{4}", new Object[] { parentAdminObject.getType(), parentAdminObject.getName(), eventType, triggerType, args }));
		this.parentAdminObject = parentAdminObject;
		this.eventType = eventType;
		this.triggerType = triggerType;
		this.mainProgramName = programName;
		this.args = args;
		resolveAvailableEventTypes();
	}

	public static ArrayList<EnoTreeTrigger> getTriggersForObject(EnoTreeBusiness parentAdminObject) {
		ArrayList<EnoTreeTrigger> retTriggers = new ArrayList<EnoTreeTrigger>();
		try {
//      	MQLCommand command = new MQLCommand();
			Context context = getContext();
			String objType = parentAdminObject.getType().toLowerCase();
			String objName = parentAdminObject.getName();
			String myLineBeginning = "";
			if (objType.equalsIgnoreCase("State")) {
				objType = "Policy";
				objName = ((EnoTreeState)parentAdminObject).getPolicy().getName();
				myLineBeginning = "state " + parentAdminObject.getName();
			}
//      	command.executeCommand(context, MessageFormat.format(MQL_SIMPLE_PRINT, new Object[] { objType, objName }));
			String[] lines = MqlUtil.mqlCommand(context, MQL_SIMPLE_PRINT_NEW, objType, objName).split("\n");
			boolean bProcessing = !objType.equals("Policy");
			for (int i = 0; i < lines.length; i++) {
				lines[i] = lines[i].trim();
				if (objType.equalsIgnoreCase("Policy")) {
					if (lines[i].startsWith(myLineBeginning))
						bProcessing = true;
					else if (((lines[i].startsWith("state")) || (lines[i].startsWith("property")) || (lines[i].startsWith("nothidden")) || (lines[i].startsWith("hidden"))) && 
							(bProcessing))
					{
						break;
					}
				}
				if ((!bProcessing) || (
						(!lines[i].startsWith("trigger")) && (!lines[i].startsWith("inherited trigger")))) continue;
				retTriggers.addAll(getTriggersFromLine(parentAdminObject, lines[i]));
			}
		}
		catch (Exception ex)
		{
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return retTriggers;
	}

	public static ArrayList<EnoTreeTrigger> getTriggersFromLine(EnoTreeBusiness parentAdminObject, String multiDescription) throws MatrixException, EnoEclipseException {
		multiDescription = multiDescription.trim();
		String prefix = "";
		boolean bInherited = false;
		if (multiDescription.toLowerCase().startsWith("trigger")) {
			prefix = "trigger";
			bInherited = false;
		} else if (multiDescription.toLowerCase().startsWith("inherited trigger")) {
			prefix = "inherited trigger";
			bInherited = true;
		}
		ArrayList<EnoTreeTrigger> newTriggers = new ArrayList<EnoTreeTrigger>();
		multiDescription = multiDescription.substring(prefix.length()).trim();
		if (!multiDescription.equals("")) {
			String[] singles = multiDescription.split(",");

			for (int i = 0; i < singles.length; i++) {
				EnoTreeTrigger newTrigger = getTriggerInstance(parentAdminObject, singles[i]);
				newTrigger.setInherited(bInherited);
				newTriggers.add(newTrigger);
			}
		}
		return newTriggers;
	}

	public static EnoTreeTrigger getTriggerInstance(EnoTreeBusiness parentAdminObject, String triggerDescription) throws MatrixException, EnoEclipseException {
		String event = null;
		String triggerType = null;
		String programName = null;
		String args = null;
		int indexOfColon = triggerDescription.indexOf(':');
		if (indexOfColon > -1) {
			String eventAndType = triggerDescription.substring(0, indexOfColon);
			if (eventAndType.toLowerCase().endsWith("action"))
				triggerType = "action";
			else if (eventAndType.toLowerCase().endsWith("check"))
				triggerType = "check";
			else if (eventAndType.toLowerCase().endsWith("override")) {
				triggerType = "override";
			}
			if (triggerType == null) {
				throw new EnoEclipseException("Couldn't parse trigger string " + triggerDescription);
			}
			event = eventAndType.substring(0, eventAndType.length() - triggerType.length());

			triggerDescription = triggerDescription.substring(indexOfColon + 1);
			int indexOfOpen = triggerDescription.indexOf('(');
			int indexOfClosed = triggerDescription.indexOf(')');
			programName = triggerDescription.substring(0, indexOfOpen);
			args = triggerDescription.substring(indexOfOpen + 1, indexOfClosed);
		} else {
			throw new EnoEclipseException("Couldn't parse trigger string " + triggerDescription);
		}
		EnoTreeTrigger newTrigger = new EnoTreeTrigger(parentAdminObject, event, triggerType, programName, args);
		return newTrigger;
	}

	public EnoTreeTrigger(EnoTreeBusiness parentAdminObject)
			throws EnoEclipseException, MatrixException
	{
		super("Trigger", "");
		this.parentAdminObject = parentAdminObject;
		resolveAvailableEventTypes();
	}

	public static String[] getAvailableEventTypes(EnoTreeBusiness adminObject) {
		String[] eventTypes = (String[])null;
		if ((adminObject instanceof EnoTreeType))
			eventTypes = EVENTS_TYPE;
		else if ((adminObject instanceof EnoTreeAttribute))
			eventTypes = EVENTS_ATTRIBUTE;
		else if ((adminObject instanceof EnoTreeRelationship))
			eventTypes = EVENTS_RELATIONSHIP;
		else if ((adminObject instanceof EnoTreeState))
			eventTypes = EVENTS_STATE;
		else {
			eventTypes = new String[0];
		}
		return eventTypes;
	}

	protected void resolveAvailableEventTypes() {
		this.availableEventTypes = getAvailableEventTypes(this.parentAdminObject);
	}

	public void refresh() throws EnoEclipseException, MatrixException
	{
		super.refresh();
		fillBasics();
	}

	public String getSequence()
	{
		return this.sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getArgs()
	{
		return this.args;
	}

	public void setArgs(String args) {
		this.args = args;
		setName(MessageFormat.format("{0}-{1}-{2}-{3}-{4}", new Object[] { this.parentAdminObject.getType(), this.parentAdminObject.getName(), this.eventType, this.triggerType, args }));
	}

	public String[] getAvailableEventTypes() {
		return this.availableEventTypes;
	}

	public String getEventType() {
		return this.eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
		setName(MessageFormat.format("{0}-{1}-{2}-{3}-{4}", new Object[] { this.parentAdminObject.getType(), this.parentAdminObject.getName(), eventType, this.triggerType, this.args }));
	}

	public String getMainProgramName() {
		return this.mainProgramName;
	}

	public void setMainProgramName(String mainProgramName) {
		this.mainProgramName = mainProgramName;
	}

	public EnoTreeBusiness getParentAdminObject() {
		return this.parentAdminObject;
	}

	public void setParentAdminObject(EnoTreeBusiness parentAdminObject) {
		this.parentAdminObject = parentAdminObject;
	}

	public String getTriggerType() {
		return this.triggerType;
	}

	public void setTriggerType(String triggerType) {
		this.triggerType = triggerType;
		setName(MessageFormat.format("{0}-{1}-{2}-{3}-{4}", new Object[] { this.parentAdminObject.getType(), this.parentAdminObject.getName(), this.eventType, triggerType, this.args }));
	}

	public String[] getTriggerObjectNames() {
		String[] retVal = this.args.split(" ");
		if ((retVal.length > 0) && (retVal[0].trim().equals(""))) {
			return new String[0];
		}
		return retVal;
	}

	public void fillBasics() {
		try {
//			matrix.db.Context context = getContext();
		} catch(Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public void save() {
		try {
//			MQLCommand command = new MQLCommand();
//			matrix.db.Context context = getContext();
		} catch(Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public static void main(String[] args) {
		try {
			ArrayList<EnoTreeTrigger> t = getTriggersForObject(new EnoTreeType("custModule"));
			System.out.println(((EnoTreeTrigger)t.get(0)).getName());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}