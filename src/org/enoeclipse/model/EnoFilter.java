package org.enoeclipse.model;

import java.util.ArrayList;

public class EnoFilter {
	private String types;
	private String relTypes;
	private boolean relFrom;
	private boolean relTo;
	private ArrayList<EnoAttribute> lstAttributes = new ArrayList<EnoAttribute>();
	private ArrayList<EnoAttribute> lstRelAttributes = new ArrayList<EnoAttribute>();

	public String getRelTypes() {
		return this.relTypes;
	}

	public void setRelTypes(String relTypes) {
		this.relTypes = relTypes;
	}

	public String getTypes() {
		return this.types;
	}

	public void setTypes(String types) {
		this.types = types;
	}

	public void addAttribute(EnoAttribute newAttribute) {
		if (!this.lstAttributes.contains(newAttribute)) {
			this.lstAttributes.add(newAttribute);
		}
	}

	public void addAttribute(String name, String value) {
		EnoAttribute newAttribute = new EnoAttribute(name, value);
		addAttribute(newAttribute);
	}

	public ArrayList<EnoAttribute> getAttributes() {
		return this.lstAttributes;
	}

	public void addRelAttribute(EnoAttribute newAttribute) {
		if (!this.lstRelAttributes.contains(newAttribute)) {
			this.lstRelAttributes.add(newAttribute);
		}
	}

	public void addRelAttribute(String name, String value) {
		EnoAttribute newAttribute = new EnoAttribute(name, value);
		addRelAttribute(newAttribute);
	}

	public ArrayList<EnoAttribute> getRelAttributes() {
		return this.lstRelAttributes;
	}

	public boolean isRelFrom() {
		return this.relFrom;
	}

	public void setRelFrom(boolean relFrom) {
		this.relFrom = relFrom;
	}

	public boolean isRelTo() {
		return this.relTo;
	}

	public void setRelTo(boolean relTo) {
		this.relTo = relTo;
	}
}