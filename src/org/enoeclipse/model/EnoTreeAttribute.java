package org.enoeclipse.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import matrix.db.AttributeType;
import matrix.db.AttributeTypeItr;
import matrix.db.AttributeTypeList;
import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;
import matrix.util.StringList;


public class EnoTreeAttribute extends EnoTreeBusiness implements ITriggerable {
	AttributeType attribute;
	ArrayList<EnoTreeRange> ranges;
	private static ArrayList<EnoTreeAttribute> allAttributes;
	protected String description;
	protected String defaultValue;
	protected String attributeType;
	protected boolean hidden;
	protected boolean multiline;
	public static final String ATTRIBUTE_TYPE_STRING = "string";
	public static final String ATTRIBUTE_TYPE_BOOLEAN = "boolean";
	public static final String ATTRIBUTE_TYPE_REAL = "real";
	public static final String ATTRIBUTE_TYPE_INTEGER = "integer";
	public static final String ATTRIBUTE_TYPE_TIMESTAMP = "timestamp";
	public static final String[] ATTRIBUTE_TYPES = { "string", "boolean", 
		"real", "integer", "timestamp" };

	public EnoTreeAttribute(String name) throws EnoEclipseException, MatrixException {
		super("Attribute", name);
		this.attribute = new AttributeType(name);
	}

	public void refresh() throws EnoEclipseException, MatrixException {
		super.refresh();
		this.attribute = new AttributeType(getName());
		fillBasics();
		this.ranges = getRanges(true);
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAttributeType() {
		return this.attributeType;
	}

	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}

	public String getDefaultValue() {
		return this.defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
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

	public boolean isMultiline() {
		return this.multiline;
	}

	public void setMultiline(boolean multiline) {
		this.multiline = multiline;
	}

	public static ArrayList<EnoTreeAttribute> getAllAttributes(boolean refresh) throws MatrixException, EnoEclipseException {
		if ((refresh) || (allAttributes == null)) {
			AttributeTypeList atl = AttributeType.getAttributeTypes(getContext(), true);
			allAttributes = new ArrayList<EnoTreeAttribute>();
			AttributeTypeItr ati = new AttributeTypeItr(atl);
			while (ati.next()) {
				AttributeType at = ati.obj();
				EnoTreeAttribute attribute = new EnoTreeAttribute(at.getName());
				allAttributes.add(attribute);
			}
			Collections.sort(allAttributes);
		}
		return allAttributes;
	}

	public void fillBasics() {
		try {
			Context context = getContext();
			this.attribute.open(context);
			try {
				this.name = this.attribute.getName();
				this.description = this.attribute.getDescription();
				this.defaultValue = this.attribute.getDefaultValue();
				this.attributeType = this.attribute.getDataType();
				this.hidden = this.attribute.isHidden();
			} finally {
				this.attribute.close(context);
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public ArrayList<EnoTreeRange> getRanges(boolean forceRefresh) {
		if ((forceRefresh) || (this.ranges == null)) {
			try {
				Context context = getContext();
				this.attribute.open(context);
				try {
					StringList choices = this.attribute.getChoices(context);
					this.ranges = new ArrayList<EnoTreeRange>();
					if (choices != null) {
						for (int i = 0; i < choices.size(); i++) {
							this.ranges.add(new EnoTreeRange((String)choices.get(i)));
						}
					}
				} finally {
					this.attribute.close(context);
				}
			} catch (Exception ex) {
				EnoEclipseLogger.getLogger().severe(ex.getMessage());
				return new ArrayList<EnoTreeRange>();
			}
		}
		return this.ranges;
	}

	public void addRange() throws EnoEclipseException, MatrixException {
		addRange(new EnoTreeRange(""));
	}

	public void addRange(EnoTreeRange newRange) {
		this.ranges.add(newRange);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoBusinessViewer)iterator.next()).addProperty(newRange); 
		}
	}

	public void removeRange(EnoTreeRange range) {
		if (range == null) {
			getRanges(false);
		}
		this.ranges.remove(range);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoBusinessViewer)iterator.next()).removeProperty(range);
		}
	}

	public void save() {
		try {
			MQLCommand command = new MQLCommand();
			Context context = getContext();
			this.attribute.open(context);
			try {
				String modString = "";
				String attributeName = this.attribute.getName();
				if (!attributeName.equals(getName())) {
					modString = modString + " name " + getName();
				}
				if (!this.attribute.getDescription().equals(getDescription())) {
					modString = modString + " description " + getDescription();
				}
				if (!this.attribute.getDefaultValue().equals(getDefaultValue())) {
					modString = modString + " default " + getDefaultValue();
				}
				if (this.attribute.isHidden() != isHidden()) {
					modString = modString + (isHidden() ? " hidden" : " nothidden");
				}
				if (!modString.equals("")) {
					command.executeCommand(context, "modify attribute " + attributeName + " " + modString + ";");
				}

				StringList choices = this.attribute.getChoices(context);
				Set<EnoTreeRange> newRanges = new HashSet<EnoTreeRange>();
				Set<EnoTreeRange> removedRanges = new HashSet<EnoTreeRange>();
				Set<EnoTreeRange> changedRanges = new HashSet<EnoTreeRange>();

				for (int i = 0; i < getRanges(false).size(); i++) {
					if (!((EnoTreeRange)this.ranges.get(i)).getOldName().equals("")) {
						continue;
					}
					newRanges.add((EnoTreeRange)this.ranges.get(i));
				}

				if (choices != null) {
					for (int i = 0; i < choices.size(); i++) {
						boolean bFound = false;
						String choice = (String)choices.get(i);
						for (int j = 0; j < this.ranges.size(); j++) {
							EnoTreeRange range = (EnoTreeRange)this.ranges.get(j);
							if (choice.equals(range.getName())) {
								bFound = true;
								break;
							}
							if (choice.equals(range.getOldName())) {
								changedRanges.add(range);
								this.ranges.remove(j);
								bFound = true;
								break;
							}
						}
						if (bFound) {
							continue;
						}
						removedRanges.add(new EnoTreeRange(choice));
					}

				}

				String addRange = "";
				String removeRange = "";
				if (newRanges.size() > 0) {
					Iterator<EnoTreeRange> itNew = newRanges.iterator();
					while (itNew.hasNext()) {
						EnoTreeRange newAttribute = (EnoTreeRange)itNew.next();
						if (!newAttribute.getName().equals("")) {
							addRange = addRange + " add range = " + newAttribute.getName();
						}
					}

				}

				if (removedRanges.size() > 0) {
					Iterator<EnoTreeRange> itRemoved = removedRanges.iterator();
					while (itRemoved.hasNext()) {
						EnoTreeRange removedAttribute = (EnoTreeRange)itRemoved.next();
						removeRange = removeRange + " remove range = " + removedAttribute.getOldName();
					}
				}

				if (changedRanges.size() > 0) {
					Iterator<EnoTreeRange> itChanged = changedRanges.iterator();
					while (itChanged.hasNext()) {
						EnoTreeRange changedAttribute = (EnoTreeRange)itChanged.next();
						addRange = addRange + " add range = " + changedAttribute.getName();
						removeRange = removeRange + " remove range = " + changedAttribute.getOldName();
					}
				}

				if (!addRange.equals("")) {
					command.executeCommand(context, "modify attribute " + getName() + " " + addRange + ";");
				}

				if (!removeRange.equals("")) {
					command.executeCommand(context, "modify attribute " + getName() + " " + removeRange + ";");
				}

				saveTriggers(context, command);

				allAttributes = null;
				refresh();
			} finally {
				this.attribute.close(context);
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	protected ArrayList<EnoTreeType> getTypes() throws EnoEclipseException, MatrixException {
		ArrayList<EnoTreeType> retTypes = new ArrayList<EnoTreeType>();
		ArrayList<EnoTreeType> allTypes = EnoTreeType.getAllTypes(false);
		for (int i = 0; i < allTypes.size(); i++) {
			EnoTreeType storedType = (EnoTreeType)allTypes.get(i);
			ArrayList<EnoTreeAttribute> typeAttributes = storedType.getAttributes(false);
			for (Iterator<EnoTreeAttribute> iterator = typeAttributes.iterator(); iterator.hasNext();) {
				EnoTreeAttribute typeAttribute = (EnoTreeAttribute)iterator.next();
				if (name.equals(typeAttribute.getName())) {
					EnoTreeType oneType = new EnoTreeType(storedType.getName());
					oneType.setFrom(false);
					oneType.setRelType("contains");
					oneType.setParent(this);
					EnoTreeType parentType = oneType.getParentType(false);
					if (parentType != null) {
						ArrayList<EnoTreeAttribute> parentAttributes = parentType.getAttributes(false);
						for (Iterator<EnoTreeAttribute> iterator1 = parentAttributes.iterator(); iterator1.hasNext();) {
							EnoTreeAttribute parentAttribute = (EnoTreeAttribute)iterator1.next();
							if (parentAttribute.getName().equals(name)) {
								oneType.setInherited(true);
								break;
							}
						}

					}
					retTypes.add(oneType);
					break;
				}
			}

		}
		return retTypes;
	}

	protected ArrayList<EnoTreeRelationship> getRelationships() throws EnoEclipseException, MatrixException {
		ArrayList<EnoTreeRelationship> retRelationships = new ArrayList<EnoTreeRelationship>();
		ArrayList<EnoTreeRelationship> allRelationships = EnoTreeRelationship.getAllRelationships(false);
		for (int i = 0; i < allRelationships.size(); i++) {
			EnoTreeRelationship storedRelationship = (EnoTreeRelationship)allRelationships.get(i);
			ArrayList<EnoTreeAttribute> relAttributes = storedRelationship.getAttributes(false);
			for (Iterator<EnoTreeAttribute> iterator = relAttributes.iterator(); iterator.hasNext();) {
				EnoTreeAttribute relAttribute = (EnoTreeAttribute)iterator.next();
				if (name.equals(relAttribute.getName())) {
					EnoTreeRelationship oneType = new EnoTreeRelationship(storedRelationship.getName());
					oneType.setFrom(false);
					oneType.setRelType("contains");
					oneType.setParent(this);
					retRelationships.add(oneType);
				}
			}

		}
		return retRelationships;
	}

	public EnoTreeBusiness[] getChildren(boolean forceUpdate) throws EnoEclipseException, MatrixException {
		if (forceUpdate) {
			this.children = null;
		}
		if (this.children == null) {
			this.children = new ArrayList<EnoTreeBusiness>();
			this.children.addAll(getTypes());
			this.children.addAll(getRelationships());
		}
		return (EnoTreeBusiness[])this.children.toArray(new EnoTreeBusiness[this.children.size()]);
	}

	public void propertyChanged(EnoTreeBusiness task) {
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoBusinessViewer)iterator.next()).updateProperty(task);
		}
	}

	public void removeChangeListener(IEnoBusinessViewer viewer) {
		this.changeListeners.remove(viewer);
	}

	public void addChangeListener(IEnoBusinessViewer viewer) {
		this.changeListeners.add(viewer);
	}

	public static void clearCache() {
		allAttributes = null;
	}
}