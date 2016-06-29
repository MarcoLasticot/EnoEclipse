package org.enoeclipse.model;

public class EnoAttribute {
	private String name;
	private String value;

	public EnoAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public EnoAttribute() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean equals(Object obj) {
		if ((obj instanceof EnoAttribute)) {
			EnoAttribute attribute = (EnoAttribute)obj;
			return (this.name.equals(attribute.name)) && (this.value.equals(attribute.value));
		}
		return false;
	}
}