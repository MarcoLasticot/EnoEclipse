package org.enoeclipse.model;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.enoeclipse.exception.EnoEclipseException;

import matrix.util.MatrixException;


public class EnoTreeStateUserAccess extends EnoTreeBusiness
{
	public static final String ACCESS_OWNER = "owner";
	public static final String ACCESS_PUBLIC = "public";
	public static final String ACCESS_USER = "user";
	private EnoTreeUser user;
	private String name;
	private Set<String> accessRights;
	private String filter;
	private String userType;
	private String userBasicType = "user";
	protected static ArrayList<EnoTreeBusiness> allUsers;

	public EnoTreeStateUserAccess(String basicType, String name)
			throws EnoEclipseException, MatrixException
	{
		super("StateUserAccess", name);
		this.userBasicType = basicType;
		this.accessRights = new TreeSet();
		this.user = EnoTreeUser.getInstance(name);
		if (this.user != null)
			this.userType = this.user.getType();
	}

	public String getUserBasicType()
	{
		return this.userBasicType;
	}
	public void setUserBasicType(String userType) {
		this.userBasicType = userType;
	}

	public String getUserType() {
		return this.userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}
	public Set<String> getAccessRights() {
		return this.accessRights;
	}
	public String getAccessRightCommaSeparated() {
		String retVal = "";
		for (String ar : this.accessRights) {
			if (!retVal.equals("")) {
				retVal = retVal + ",";
			}
			retVal = retVal + ar;
		}
		return retVal;
	}
	public String getDifferenceCommaSeparated(EnoTreeStateUserAccess oldUserAccess) {
		String retVal = "";
		boolean[] bFound = new boolean[oldUserAccess.getAccessRights().size()];
		boolean bFoundNew;
		for (String newAccessRight : getAccessRights()) {
			bFoundNew = false;
			int i = 0;
			for (String oldAccessRight : oldUserAccess.getAccessRights()) {
				if (newAccessRight.equals(oldAccessRight)) {
					bFound[i] = true;
					bFoundNew = true;
					break;
				}
				i++;
			}
			if (!bFoundNew) {
				if (!retVal.equals("")) {
					retVal = retVal + ",";
				}
				retVal = retVal + newAccessRight;
			}
		}

		int i = 0;
		for (String oldAccessRight : oldUserAccess.getAccessRights()) {
			if(!bFound[i]) {
				retVal = retVal + "not" + oldAccessRight;
			}
			i++;
		}

		return retVal;
	}
	public void setAccessRights(Set<String> accessRights) {
		this.accessRights = EnoTreePerson.splitAccessRights((String[])accessRights.toArray(new String[accessRights.size()]));
	}
	public void setAccessRights(String sAccessRights) {
		this.accessRights = EnoTreePerson.splitAccessRights(sAccessRights);
	}
	public String getFilter() {
		return this.filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}

	public void setName(String name)
	{
		super.setName(name);
	}

	public EnoTreeUser getUser() {
		return this.user;
	}

	public boolean equals(Object obj)
	{
		if ((obj instanceof EnoTreeStateUserAccess)) {
			EnoTreeStateUserAccess other = (EnoTreeStateUserAccess)obj;

			return (this.userBasicType != null) && (this.userBasicType.equals(other.userBasicType)) && ((this.name == null) || (this.name.equals(other.name)));
		}
		return false;
	}

	public static void clearCache()
	{
		allUsers = null;
	}
}