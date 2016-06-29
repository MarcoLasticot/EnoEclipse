package org.enoeclipse.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import matrix.db.BusinessType;
import matrix.db.BusinessTypeItr;
import matrix.db.BusinessTypeList;
import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.db.RelationshipType;
import matrix.db.RelationshipTypeItr;
import matrix.db.RelationshipTypeList;
import matrix.util.MatrixException;
import matrix.util.StringList;


public class EnoTreeType extends EnoTreeBusiness implements IAttributable, ITriggerable {
	BusinessType businessType;
	ArrayList<EnoTreeAttribute> attributes;
	protected String description;
	protected boolean hidden;
	protected boolean abstractType;
	protected EnoTreeType parentType;
	protected EnoTreeType oldParentType;
	protected ArrayList<EnoTreeType> childTypes;
	protected ArrayList<EnoTreeRelationship> fromRelationships;
	protected ArrayList<EnoTreeRelationship> toRelationships;
	protected ArrayList<EnoTreePolicy> policies;
	protected static String MQL_INFO = "print type \"{0}\" select description hidden abstract dump |;";
	protected static String MQL_INFO_POLICY = "print type \"{0}\" select policy dump |;";
	protected static String MQL_INFO_ATTRIBUTE = "print type \"{0}\" select attribute dump |;";
	protected static String MQL_ADD_ATTRIBUTE = "modify type \"{0}\" add attribute \"{1}\";";
	protected static String MQL_REMOVE_ATTRIBUTE = "modify type \"{0}\" remove attribute \"{1}\";";
	protected static final int INFO_DESCRIPTION = 0;
	protected static final int INFO_HIDDEN = 1;
	protected static final int INFO_ABSTRACT = 2;
	protected static ArrayList<EnoTreeType> allTypes;

	public EnoTreeType(String name) throws EnoEclipseException, MatrixException {
		super("Type", name);
		this.businessType = new BusinessType(name, getContext().getVault());
	}

	public void refresh() throws EnoEclipseException, MatrixException {
		super.refresh();
		this.businessType = new BusinessType(this.name, getContext().getVault());
		fillBasics();
		getPolicies(true);
		this.attributes = getAttributes(true);
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

	public boolean isAbstractType() {
		return this.abstractType;
	}

	public void setAbstractType(boolean abstractType) {
		this.abstractType = abstractType;
	}

	public static ArrayList<EnoTreeType> getAllTypes(boolean refresh) throws MatrixException, EnoEclipseException {
		if ((refresh) || (allTypes == null)) {
			Context context = getContext();
			BusinessTypeList btl = BusinessType.getBusinessTypes(context, true);
			allTypes = new ArrayList<EnoTreeType>();
			BusinessTypeItr bti = new BusinessTypeItr(btl);
			while (bti.next()) {
				BusinessType bt = bti.obj();
				EnoTreeType type = new EnoTreeType(bt.getName());
				allTypes.add(type);
			}
			Collections.sort(allTypes);
		}
		return allTypes;
	}

	public static String[] getAllTypeNames(boolean refresh) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeType> allTypes = getAllTypes(refresh);

		String[] retVal = new String[allTypes.size()];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = ((EnoTreeType)allTypes.get(i)).getName();
		}
		return retVal;
	}

	public static ArrayList<EnoTreeAttribute> getAttributes(EnoTreeType type) {
		ArrayList<EnoTreeAttribute> retAttributes = new ArrayList<EnoTreeAttribute>();
		try {
			Context context = getContext();
			type.businessType.open(context);
			EnoTreeType parentType = type.getParentType(false);
			try {
				MQLCommand command = new MQLCommand();
				command.executeCommand(context, MessageFormat.format(MQL_INFO_ATTRIBUTE, new Object[] { type.name }));

				String[] attributes = command.getResult().trim().split("\\|");
				String as[];
				int j = (as = attributes).length;
				for(int i = 0; i < j; i++) {
					String at = as[i];
					EnoTreeAttribute attribute = new EnoTreeAttribute(at);
					attribute.setParent(type);
					attribute.setFrom(true);
					attribute.setRelType("contains");
					if (parentType != null) {
						ArrayList<EnoTreeAttribute> parentAttributes = parentType.getAttributes(false);
						for (Iterator<EnoTreeAttribute> iterator = parentAttributes.iterator(); iterator.hasNext();) {
							EnoTreeAttribute parentAttribute = (EnoTreeAttribute)iterator.next();
							if (parentAttribute.getName().equals(attribute.getName())) {
								attribute.setInherited(true);
								break;
							}
						}
					}
					retAttributes.add(attribute);
				}
			} finally {
				type.businessType.close(context);
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

	public static ArrayList<EnoTreePolicy> getPolicies(EnoTreeType type) {
		ArrayList<EnoTreePolicy> retPolicies = new ArrayList<EnoTreePolicy>();
		try {
			Context context = getContext();
			EnoTreeType parentType = type.getParentType(false);
			MQLCommand command = new MQLCommand();

			command.executeCommand(context, MessageFormat.format(MQL_INFO_POLICY, new Object[] { type.getName() }));
			String[] p = command.getResult().split("\\|");
			for (int i = 0; i < p.length; i++) {
				String pname = p[i].trim();
				if (!pname.equals("")) {
					EnoTreePolicy policy = new EnoTreePolicy(pname);
					policy.setParent(type);
					policy.setFrom(true);
					policy.setRelType("policy");
					if (parentType != null) {
						ArrayList<EnoTreePolicy> parentPolicies = parentType.getPolicies(false);
						for (Iterator<EnoTreePolicy> iterator = parentPolicies.iterator(); iterator.hasNext();) {
							EnoTreePolicy parentPolicy = (EnoTreePolicy)iterator.next();
							if (parentPolicy.getName().equals(policy.getName())) {
								policy.setInherited(true);
								break;
							}
						}
					}
					retPolicies.add(policy);
				}
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return retPolicies;
	}

	public ArrayList<EnoTreePolicy> getPolicies(boolean forceRefresh) {
		if ((forceRefresh) || (this.policies == null)) {
			this.policies = getPolicies(this);
		}
		return this.policies;
	}

	public String[] getPolicyNames(boolean forceRefresh) {
		ArrayList<EnoTreePolicy> policies = getPolicies(forceRefresh);
		String[] retVal = new String[policies.size()];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = ((EnoTreePolicy)policies.get(i)).getName();
		}
		return retVal;
	}

	public static ArrayList<EnoTreeType> getChildTypes(EnoTreeType type) {
		ArrayList<EnoTreeType> retTypes = new ArrayList<EnoTreeType>();
		try {
			Context context = getContext();
			type.businessType.open(context);
			try {
				BusinessTypeList btl = type.businessType.getChildren(context);
				BusinessTypeItr itBusiness = new BusinessTypeItr(btl);
				while (itBusiness.next()) {
					BusinessType bt = itBusiness.obj();
					EnoTreeType child = new EnoTreeType(bt.getName());
					child.setParent(type);
					child.setFrom(true);
					child.setRelType("inherits");
					retTypes.add(child);
				}
			} finally {
				type.businessType.close(context);
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return retTypes;
	}

	public ArrayList<EnoTreeType> getChildTypes(boolean forceRefresh) {
		if ((forceRefresh) || (this.childTypes == null)) {
			this.childTypes = getChildTypes(this);
		}
		return this.childTypes;
	}

	public static ArrayList<EnoTreeRelationship> getRelationships(EnoTreeType type, boolean from) {
		ArrayList<EnoTreeRelationship> retRels = new ArrayList<EnoTreeRelationship>();
		try {
			Context context = getContext();
			type.businessType.open(context);
			try {
				RelationshipTypeList rtl = type.businessType.getRelationshipTypes(context, !from, from, false);
				RelationshipTypeItr itBusiness = new RelationshipTypeItr(rtl);
				while (itBusiness.next()) {
					RelationshipType rt = itBusiness.obj();
					EnoTreeRelationship child = new EnoTreeRelationship(rt.getName());
					child.setParent(type);
					child.setFrom(from);
					child.setRelType(from ? "from" : "to");
					retRels.add(child);
				}
			} finally {
				type.businessType.close(context);
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return retRels;
	}

	public ArrayList<EnoTreeRelationship> getRelationships(boolean forceRefresh, boolean from) {
		if (from) {
			if ((forceRefresh) || (this.fromRelationships == null)) {
				this.fromRelationships = getRelationships(this, from);
			}
		}
		else if ((forceRefresh) || (this.toRelationships == null)) {
			this.toRelationships = getRelationships(this, from);
		}

		return from ? this.fromRelationships : this.toRelationships;
	}

	public void fillBasics() {
		try {
			Context context = getContext();
			this.businessType.open(context);
			try {
				this.name = this.businessType.getName();
				MQLCommand command = new MQLCommand();
				command.executeCommand(context, MessageFormat.format(MQL_INFO, new Object[] { this.name }));

				String[] info = command.getResult().trim().split("\\|");
				this.description = info[0];
				this.hidden = info[1].equalsIgnoreCase("true");
				this.abstractType = info[2].equalsIgnoreCase("true");

				getParentType(true);
			} finally {
				this.businessType.close(context);
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public EnoTreeType getParentType(boolean forceRefresh) throws MatrixException, EnoEclipseException {
		if ((forceRefresh) || (this.parentType == null)) {
			Context context = getContext();
			StringList parents = this.businessType.getParents(context);
			if (parents.size() > 0) {
				this.parentType = new EnoTreeType((String)parents.get(0));
				this.parentType.setFrom(false);
				this.parentType.setRelType("inherits");
			} else {
				this.parentType = null;
			}
		}
		return this.parentType;
	}

	public void setParentType(String parentName) throws EnoEclipseException, MatrixException {
		this.oldParentType = this.parentType;
		if (parentName != null) {
			this.parentType = new EnoTreeType(parentName);
			this.parentType.setFrom(false);
			this.parentType.setRelType("inherits");
		} else {
			this.parentType = null;
		}
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

	public void addPolicy() throws EnoEclipseException, MatrixException {
		addPolicy(new EnoTreePolicy(""));
	}

	public void addPolicy(EnoTreePolicy newPolicy) {
		this.policies.add(newPolicy);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoBusinessViewer)iterator.next()).addProperty(newPolicy); 
		}
	}

	public void removePolicy(EnoTreePolicy policy) {
		if (this.policies == null) {
			getPolicies(true);
		}
		this.policies.remove(policy);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoBusinessViewer)iterator.next()).removeProperty(policy);
		}
	}

	public void savePolicies(Context context, MQLCommand command) {
		ArrayList<EnoTreePolicy> oldPolicies = getPolicies(this);

		for (int i = 0; i < this.policies.size(); i++) {
			if (!((EnoTreePolicy)this.policies.get(i)).getOldName().equals("")) {
				continue;
			}
			((EnoTreePolicy)this.policies.get(i)).saveAddType(this);
		}

		if (oldPolicies != null) {
			for (int i = 0; i < oldPolicies.size(); i++) {
				boolean bFound = false;
				EnoTreePolicy oldPolicy = (EnoTreePolicy)oldPolicies.get(i);
				for (int j = 0; j < this.policies.size(); j++) {
					EnoTreePolicy policy = (EnoTreePolicy)this.policies.get(j);

					if (oldPolicy.getName().equals(policy.getName())) {
						bFound = true;
						break;
					}
					if (oldPolicy.getName().equals(policy.getOldName())) {
						policy.saveAddType(this);
						oldPolicy.saveRemoveType(this);
						bFound = true;
						break;
					}
				}
				if (bFound) {
					continue;
				}
				oldPolicy.saveRemoveType(this);
			}
		}
	}

	public void saveAttributes(Context context, MQLCommand command) throws MatrixException {
		ArrayList<EnoTreeAttribute> oldAttributes = getAttributes(this);

		for (int i = 0; i < this.attributes.size(); i++) {
			if (!((EnoTreeAttribute)this.attributes.get(i)).getOldName().equals("")) {
				continue;
			}
			command.executeCommand(context, MessageFormat.format(MQL_ADD_ATTRIBUTE, new Object[] { getName(), ((EnoTreeAttribute)this.attributes.get(i)).getName() }));
		}

		if (oldAttributes != null) {
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
	}

	public void save() {
		try {
			MQLCommand command = new MQLCommand();
			Context context = getContext();
			this.businessType.open(context);
			try {
				String modString = "";
				String typeName = this.businessType.getName();
				boolean changedName = !typeName.equals(getName());
				if (changedName) {
					modString = modString + " name \"" + getName() + "\"";
				}

				command.executeCommand(context, MessageFormat.format(MQL_INFO, new Object[] { this.businessType.getName() }));
				String[] info = command.getResult().trim().split("\\|");
				if (!info[0].equals(getDescription())) {
					modString = modString + " description \"" + getDescription() + "\"";
				}
				boolean oldIsHidden = info[1].equalsIgnoreCase("true");
				if (oldIsHidden != isHidden()) {
					modString = modString + (isHidden() ? " hidden" : " nothidden");
				}
				if (((this.oldParentType != null) && (!this.oldParentType.equals(this.parentType))) || ((this.parentType != null) && (!this.parentType.equals(this.oldParentType)))) {
					if (this.parentType == null) {
						modString = modString + " remove derived ";
					} else {
						modString = modString + " derived \"" + this.parentType.getName() + "\"";
					}
				}
				boolean oldIsAbstract = info[2].equalsIgnoreCase("true");
				if (oldIsAbstract != this.abstractType) {
					modString = modString + (this.abstractType ? " abstract true" : " abstract false");
				}

				if (!modString.equals("")) {
					command.executeCommand(context, "modify type \"" + typeName + "\" " + modString + ";");
				}

				if (changedName) {
					this.businessType = new BusinessType(this.name, getContext().getVault());
				}
				saveAttributes(context, command);
				savePolicies(context, command);
				saveTriggers(context, command);

				allTypes = null;
				refresh();
			} finally {
				this.businessType.close(context);
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
			this.children.addAll(getChildTypes(false));
			this.children.addAll(getAttributes(false));
			this.children.addAll(getPolicies(false));
			this.children.addAll(getRelationships(false, true));
			this.children.addAll(getRelationships(false, false));

			if (getParentType(false) != null) {
				this.children.add(getParentType(false));
			}
		}
		return (EnoTreeBusiness[])this.children.toArray(new EnoTreeBusiness[this.children.size()]);
	}

	public static void clearCache() {
		allTypes = null;
	}
}