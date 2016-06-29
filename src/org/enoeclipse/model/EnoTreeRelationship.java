package org.enoeclipse.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import com.matrixone.apps.domain.DomainRelationship;

import matrix.db.BusinessType;
import matrix.db.BusinessTypeItr;
import matrix.db.BusinessTypeList;
import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.db.RelationshipType;
import matrix.db.RelationshipTypeItr;
import matrix.db.RelationshipTypeList;
import matrix.util.MatrixException;


public class EnoTreeRelationship extends EnoTreeBusiness
implements IAttributable, ITriggerable
{
	RelationshipType relationshipType;
	protected String description;
	protected boolean hidden;
	protected boolean preventDuplicates;
	protected ArrayList<EnoTreeType> fromTypes;
	protected ArrayList<EnoTreeType> toTypes;
	protected DirectionInfo fromInfo;
	protected DirectionInfo toInfo;
	protected static String MQL_INFO = "print relationship \"{0}\" select description hidden preventduplicates dump |;";
	protected static String MQL_DIRECTION_INFO = "print relationship \"{0}\" select {1}cardinality {1}reviseaction {1}cloneaction {1}propagatemodify {1}propagateconnection dump |;";
	protected static String MQL_ADD_ATTRIBUTE = "modify relationship \"{0}\" add attribute \"{1}\";";
	protected static String MQL_REMOVE_ATTRIBUTE = "modify relationship \"{0}\" remove attribute \"{1}\";";
	protected static String MQL_ADD_TYPES = "modify relationship \"{0}\" {1} add type \"{2}\";";
	protected static String MQL_REMOVE_TYPES = "modify relationship \"{0}\" {1} remove type \"{2}\";";
	protected static String MQL_MODIFY_DIRECTION_INFO = "modify relationship \"{0}\" {1} cardinality {2} revision {3} clone {4}  {5}  {6};";
	protected static final int INFO_DESCRIPTION = 0;
	protected static final int INFO_HIDDEN = 1;
	protected static final int INFO_PREVENT_DUPLICATES = 2;
	protected static final int DIRECTION_INFO_CARDINALITY = 0;
	protected static final int DIRECTION_INFO_REVISION = 1;
	protected static final int DIRECTION_INFO_CLONE = 2;
	protected static final int DIRECTION_INFO_PROPAGATE_MODIFY = 3;
	protected static final int DIRECTION_INFO_PROPAGATE_CONNECTION = 4;
	public static String[] CARDINALITIES = { "One", "N" };
	public static String[] REVISION_ACTIONS = { "none", "float", "replicate" };
	public static String[] CLONE_ACTIONS = { "none", "float", "replicate" };
	protected static ArrayList<EnoTreeRelationship> allRelationships;

	public EnoTreeRelationship(String name)
			throws EnoEclipseException, MatrixException
	{
		super("Relationship", name);
		this.relationshipType = new RelationshipType(name);
	}

	public void refresh() throws EnoEclipseException, MatrixException
	{
		super.refresh();
		this.relationshipType = new RelationshipType(this.name);
		fillBasics();
		this.attributes = getAttributes(true);
		this.fromTypes = getTypes(true, true);
		this.toTypes = getTypes(true, false);
		fillDirectionInfo(true);
		fillDirectionInfo(false);
	}

	public String getDescription()
	{
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

	public boolean isPreventDuplicates() {
		return this.preventDuplicates;
	}

	public void setPreventDuplicates(boolean preventDuplicates) {
		this.preventDuplicates = preventDuplicates;
	}

	public DirectionInfo getFromInfo() {
		return this.fromInfo;
	}

	public void setFromInfo(DirectionInfo fromInfo) {
		this.fromInfo = fromInfo;
	}

	public DirectionInfo getToInfo() {
		return this.toInfo;
	}

	public void setToInfo(DirectionInfo toInfo) {
		this.toInfo = toInfo;
	}

	public static ArrayList<EnoTreeRelationship> getAllRelationships(boolean refresh) throws MatrixException, EnoEclipseException {
		if ((refresh) || (allRelationships == null)) {
			Context context = getContext();
			RelationshipTypeList rtl = RelationshipType.getRelationshipTypes(context, true);
			allRelationships = new ArrayList<EnoTreeRelationship>();
			RelationshipTypeItr rti = new RelationshipTypeItr(rtl);
			while (rti.next()) {
				RelationshipType rt = rti.obj();
				EnoTreeRelationship rel = new EnoTreeRelationship(rt.getName());
				allRelationships.add(rel);
			}
			Collections.sort(allRelationships);
		}
		return allRelationships;
	}

	public static String[] getAllRelationshipNames(boolean refresh) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeRelationship> allRelationships = getAllRelationships(refresh);

		String[] retVal = new String[allRelationships.size()];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = ((EnoTreeRelationship)allRelationships.get(i)).getName();
		}
		return retVal;
	}

	public static ArrayList<EnoTreeAttribute> getAttributes(EnoTreeRelationship relationship) {
		ArrayList<EnoTreeAttribute> retAttributes = new ArrayList<EnoTreeAttribute>();
		try {
			Context context = getContext();
			relationship.relationshipType.open(context);
			try {
				Map<String,String> mapAttributes = DomainRelationship.getTypeAttributes(context, relationship.getName(), true);
				for (Iterator<String> localIterator = mapAttributes.keySet().iterator(); localIterator.hasNext();) { 
					Object oAttribute = localIterator.next();

					EnoTreeAttribute attribute = new EnoTreeAttribute((String)oAttribute);
					attribute.setParent(relationship);
					attribute.setFrom(true);
					attribute.setRelType("contains");
					retAttributes.add(attribute); }
			}
			finally {
				relationship.relationshipType.close(context);
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return retAttributes;
	}

	public ArrayList<EnoTreeAttribute> getAttributes(boolean forceRefresh) {
		if ((forceRefresh) || (this.attributes == null)) {
			this.attributes = getAttributes(this);
		}
		return this.attributes;
	}

	public void addType(boolean from) throws EnoEclipseException, MatrixException {
		addType(new EnoTreeType(""), from);
	}

	public void addType(EnoTreeType newType, boolean from) {
		if(from) {
			fromTypes.add(newType);
		} else {
			toTypes.add(newType);
		}
		for (Iterator<IEnoBusinessViewer> iterator = changeListeners.iterator(); iterator.hasNext();) {
			IEnoBusinessViewer contentProvider = (IEnoBusinessViewer)iterator.next();
//			if ((contentProvider instanceof org.mxeclipse.business.table.type.MxTypeComposite.MxTypeContentProvider) && from == ((org.mxeclipse.business.table.type.MxTypeComposite.MxTypeContentProvider)contentProvider).getFrom()) {
//				contentProvider.addProperty(newType);
//			}
		}

	}

	public void removeType(EnoTreeType type, boolean from) {
		if (from) {
			if (fromTypes == null) {
				getTypes(false, true);
			}
			fromTypes.remove(type);
		} else {
			if(toTypes == null) {
				getTypes(false, false);
			}
			toTypes.remove(type);
		}
		for (Iterator<IEnoBusinessViewer> iterator = changeListeners.iterator(); iterator.hasNext();) {
			IEnoBusinessViewer contentProvider = (IEnoBusinessViewer)iterator.next();
//			if ((contentProvider instanceof org.mxeclipse.business.table.type.MxTypeComposite.MxTypeContentProvider) && from == ((org.mxeclipse.business.table.type.MxTypeComposite.MxTypeContentProvider)contentProvider).getFrom()) {
//				contentProvider.removeProperty(type);
//			}
		}

	}

	public void fillBasics() {
		try {
			Context context = getContext();
			this.relationshipType.open(context);
			try {
				this.name = this.relationshipType.getName();

				MQLCommand command = new MQLCommand();
				command.executeCommand(context, MessageFormat.format(MQL_INFO, new Object[] { this.name }));

				String[] info = command.getResult().trim().split("\\|");
				this.description = info[0];
				this.hidden = info[1].equalsIgnoreCase("true");
				this.preventDuplicates = info[2].equalsIgnoreCase("true");
			} finally {
				this.relationshipType.close(context);
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public static DirectionInfo getDirectionInfo(EnoTreeRelationship relationship, boolean from) {
		DirectionInfo directionInfo = relationship. new DirectionInfo();
		try {
			Context context = getContext();
			MQLCommand command = new MQLCommand();
			command.executeCommand(context, MessageFormat.format(MQL_DIRECTION_INFO, new Object[] { relationship.getName(), from ? "from" : "to" }));

			String[] info = command.getResult().trim().split("\\|");

			directionInfo.setCardinality(info[0]);
			directionInfo.setRevision(info[1]);
			directionInfo.setClone(info[2]);
			directionInfo.setPropagateConnection(info[4].equalsIgnoreCase("true"));
			directionInfo.setPropagateModify(info[3].equalsIgnoreCase("true"));
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return directionInfo;
	}

	public void fillDirectionInfo(boolean from) {
		DirectionInfo directionInfo = getDirectionInfo(this, from);
		if (from) {
			this.fromInfo = directionInfo;
		} else {
			this.toInfo = directionInfo;
		}
	}

	public static ArrayList<EnoTreeType> getTypes(EnoTreeRelationship relationship, boolean from) {
		ArrayList<EnoTreeType> retTypes = new ArrayList<EnoTreeType>();
		try {
			Context context = getContext();
			relationship.relationshipType.open(context);
			try {
				BusinessTypeList btl = from ? relationship.relationshipType.getFromTypes(context) : relationship.relationshipType.getToTypes(context);
				BusinessTypeItr itBusiness = new BusinessTypeItr(btl);
				EnoTreeType child;
				while (itBusiness.next()) {
					BusinessType bt = itBusiness.obj();
					child = new EnoTreeType(bt.getName());
					child.setParent(relationship);
					child.setFrom(from);
					child.setRelType(from ? "from" : "to");
					retTypes.add(child);
				}

				for (Iterator<EnoTreeType> iterator = retTypes.iterator(); iterator.hasNext();) {
					child = (EnoTreeType)iterator.next();
					EnoTreeType parentType = child.getParentType(false);
					if (parentType != null) {
						for (Iterator<EnoTreeType> iterator1 = retTypes.iterator(); iterator1.hasNext();) {
							EnoTreeType retType = (EnoTreeType)iterator1.next();
							if (retType.getName().equals(parentType.getName())) {
								child.setInherited(true);
								break;
							}
						}

					}
				}
			} finally {
				relationship.relationshipType.close(context);
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return retTypes;
	}

	public ArrayList<EnoTreeType> getTypes(boolean forceRefresh, boolean from) {
		if (from) {
			if ((forceRefresh) || (this.fromTypes == null)) {
				this.fromTypes = getTypes(this, from);
			}
			return this.fromTypes;
		}
		if ((forceRefresh) || (this.toTypes == null)) {
			this.toTypes = getTypes(this, from);
		}
		return this.toTypes;
	}

	public void saveAttributes(Context context, MQLCommand command) throws MatrixException {
		ArrayList<EnoTreeAttribute> oldAttributes = getAttributes(this);

		for (int i = 0; i < this.attributes.size(); i++) {
			if (!((EnoTreeAttribute)this.attributes.get(i)).getOldName().equals("")) {
				continue;
			}
			command.executeCommand(context, MessageFormat.format(MQL_ADD_ATTRIBUTE, new Object[] { getName(), ((EnoTreeAttribute)this.attributes.get(i)).getName() }));
		}

		if (oldAttributes != null)
			for (int i = 0; i < oldAttributes.size(); i++) {
				boolean bFound = false;
				EnoTreeAttribute oldAttribute = (EnoTreeAttribute)oldAttributes.get(i);
				for (int j = 0; j < this.attributes.size(); j++) {
					EnoTreeAttribute attribute = (EnoTreeAttribute)this.attributes.get(j);

					if (oldAttribute.getName().equals(attribute.getName())) {
						bFound = true;
						break;
					}
					if (oldAttribute.getName().equals(attribute.getOldName())) {
						command.executeCommand(context, MessageFormat.format(MQL_ADD_ATTRIBUTE, new Object[] { getName(), attribute.getName() }));
						command.executeCommand(context, MessageFormat.format(MQL_REMOVE_ATTRIBUTE, new Object[] { getName(), oldAttribute.getOldName() }));
						bFound = true;
						break;
					}
				}
				if (bFound) {
					continue;
				}
				command.executeCommand(context, MessageFormat.format(MQL_REMOVE_ATTRIBUTE, new Object[] { getName(), oldAttribute.getOldName() }));
			}
	}

	public void saveTypes(Context context, MQLCommand command, boolean from) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeType> oldTypes = getTypes(this, from);
		ArrayList<EnoTreeType> types = from ? this.fromTypes : this.toTypes;

		String sAdded = "";
		String sRemoved = "";

		for (int i = 0; i < types.size(); i++) {
			if (!((EnoTreeType)types.get(i)).getOldName().equals("")) {
				continue;
			}
			sAdded = sAdded + (!sAdded.equals("") ? "," : "") + ((EnoTreeType)types.get(i)).getName();
		}

		if (oldTypes != null) {
			for (int i = 0; i < oldTypes.size(); i++) {
				boolean bFound = false;
				EnoTreeType oldType = (EnoTreeType)oldTypes.get(i);
				for (int j = 0; j < types.size(); j++) {
					EnoTreeType type = (EnoTreeType)types.get(j);

					if (oldType.getName().equals(type.getName())) {
						bFound = true;
						break;
					}
					if (oldType.getName().equals(type.getOldName())) {
						sAdded = sAdded + (!sAdded.equals("") ? "," : "") + type.getName();
						sRemoved = sRemoved + (!sRemoved.equals("") ? "," : "") + oldType.getOldName();
						bFound = true;
						break;
					}
				}
				if (bFound) {
					continue;
				}
				sRemoved = sRemoved + (!sRemoved.equals("") ? "," : "") + oldType.getOldName();
			}
		}
		command.executeCommand(context, MessageFormat.format(MQL_ADD_TYPES, new Object[] { getName(), from ? "from" : "to", sAdded }));
		command.executeCommand(context, MessageFormat.format(MQL_REMOVE_TYPES, new Object[] { getName(), from ? "from" : "to", sRemoved }));
	}

	public void saveDirectionInfo(Context context, MQLCommand command, boolean from) throws MatrixException, EnoEclipseException {
		DirectionInfo oldDirectionInfo = getDirectionInfo(this, from);
		DirectionInfo directionInfo = from ? this.fromInfo : this.toInfo;
		command.executeCommand(context, MessageFormat.format(MQL_MODIFY_DIRECTION_INFO, new Object[] { getName(), from ? "from" : "to", 
				directionInfo.getCardinality().equals("One") ? "1" : directionInfo.getCardinality(), directionInfo.getRevision(), directionInfo.getClone(), 
						directionInfo.isPropagateModify() != oldDirectionInfo.isPropagateModify() ? (directionInfo.isPropagateModify() ? "" : "!") + "propagatemodify" : "", 
								directionInfo.isPropagateConnection() != oldDirectionInfo.isPropagateConnection() ? (directionInfo.isPropagateConnection() ? "" : "!") + "propagateconnection" : "" }));
	}

	public void save() {
		try {
			MQLCommand command = new MQLCommand();
			Context context = getContext();
			this.relationshipType.open(context);
			try {
				String modString = "";
				String relationshipName = this.relationshipType.getName();
				boolean changedName = !relationshipName.equals(getName());
				if (changedName) {
					modString = modString + " name \"" + getName() + "\"";
				}

				command.executeCommand(context, MessageFormat.format(MQL_INFO, new Object[] { this.relationshipType.getName() }));
				String[] info = command.getResult().trim().split("\\|");
				if (!info[0].equals(getDescription())) {
					modString = modString + " description \"" + getDescription() + "\"";
				}
				boolean oldIsHidden = info[1].equalsIgnoreCase("true");
				if (oldIsHidden != isHidden()) {
					modString = modString + (isHidden() ? " hidden" : " nothidden");
				}
				boolean oldPreventDuplicates = info[2].equalsIgnoreCase("true");
				if (oldPreventDuplicates != this.preventDuplicates) {
					modString = modString + (this.preventDuplicates ? " preventduplicates" : " !preventduplicates");
				}

				if (!modString.equals("")) {
					command.executeCommand(context, "modify relationship \"" + relationshipName + "\" " + modString + ";");
				}

				if (changedName) {
					this.relationshipType = new RelationshipType(this.name);
				}
				saveAttributes(context, command);
				saveTypes(context, command, true);
				saveDirectionInfo(context, command, true);
				saveTypes(context, command, false);
				saveDirectionInfo(context, command, false);
				saveTriggers(context, command);

				allRelationships = null;
				this.attributes = null;
				this.fromTypes = null;
				this.toTypes = null;
				refresh();
			} finally {
				this.relationshipType.close(context);
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

			this.children.addAll(getAttributes(false));
			this.children.addAll(getTypes(false, true));
			this.children.addAll(getTypes(false, false));
		}
		return (EnoTreeBusiness[])this.children.toArray(new EnoTreeBusiness[this.children.size()]);
	}

	public static void clearCache() {
		allRelationships = null;
	}

	public class DirectionInfo {
		private String cardinality;
		private String revision;
		private String clone;
		private boolean propagateModify;
		private boolean propagateConnection;

		public DirectionInfo() {
		}

		public String getCardinality() {
			return this.cardinality;
		}
		public void setCardinality(String cardinality) {
			this.cardinality = cardinality;
		}
		public String getClone() {
			return this.clone;
		}
		public void setClone(String clone) {
			this.clone = clone;
		}
		public boolean isPropagateConnection() {
			return this.propagateConnection;
		}
		public void setPropagateConnection(boolean propagateConnection) {
			this.propagateConnection = propagateConnection;
		}
		public boolean isPropagateModify() {
			return this.propagateModify;
		}
		public void setPropagateModify(boolean propagateModify) {
			this.propagateModify = propagateModify;
		}
		public String getRevision() {
			return this.revision;
		}
		public void setRevision(String revision) {
			this.revision = revision;
		}
	}
}