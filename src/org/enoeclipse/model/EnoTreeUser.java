package org.enoeclipse.model;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import matrix.util.MatrixException;

public abstract class EnoTreeUser extends EnoTreeBusiness {
	private static ArrayList<EnoTreeUser> allUsers;
	public static String[] ALL_USER_TYPES = {
			"Person",
			"Role", 
			"Group",
			"Association"
	};

	public EnoTreeUser(String type, String name) throws EnoEclipseException, MatrixException {
		super(type, name);
	}

	public void setType(String type) {
		this.type = type;
	}

	public static EnoTreeUser createInstance(String name) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, CloneNotSupportedException {
		for (EnoTreeUser u : getAllUsers(false)) {
			if (u.getName().equals(name)) {
				return (EnoTreeUser)u.clone();
			}
		}

		return null;
	}

	public static EnoTreeUser getInstance(String name) {
		for (EnoTreeUser u : getAllUsers(false)) {
			if (u.getName().equals(name)) {
				return u;
			}
		}
		return null;
	}

	public static ArrayList<EnoTreeUser> getAllUsers(boolean forceRefresh) {
		if ((forceRefresh) || (allUsers == null)) {
			try {
				allUsers = new ArrayList<EnoTreeUser>();
				List<EnoTreeUser> lstUsers = new ArrayList<EnoTreeUser>();
				lstUsers.addAll(EnoTreePerson.getAllPersons(false));
				lstUsers.addAll(EnoTreeRole.getAllRoles(false));
				lstUsers.addAll(EnoTreeGroup.getAllGroups(false));
				lstUsers.addAll(EnoTreeAssociation.getAllAssociations(false));
				for (Iterator<EnoTreeUser> localIterator = lstUsers.iterator(); localIterator.hasNext();) { 
					Object person = localIterator.next();
					allUsers.add((EnoTreeUser)person);
				}
				Collections.sort(allUsers);
			} catch (Exception ex) {
				EnoEclipseLogger.getLogger().severe(ex.getMessage());
				return null;
			}
		}
		return allUsers;
	}

	public static String[] getAllUserNames(boolean refresh) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeUser> allTypes = getAllUsers(refresh);

		String[] retVal = new String[allTypes.size()];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = ((EnoTreeUser)allTypes.get(i)).getName();
		}
		return retVal;
	}

	public static ArrayList<EnoTreeUser> getAllUsers(boolean forceRefresh, String userType) {
		if ((forceRefresh) || (allUsers == null)) {
			getAllUsers(forceRefresh);
		}
		ArrayList<EnoTreeUser> alSpecificUsers = new ArrayList<EnoTreeUser>();
		for (EnoTreeUser u : allUsers) {
			if (u.getType().equals(userType)) {
				alSpecificUsers.add(u);
			}
		}
		return alSpecificUsers;
	}

	public static String[] getAllUserNames(boolean refresh, String userType) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeUser> allTypes = getAllUsers(refresh, userType);

		if (userType.equals("")) {
			return new String[] { "owner", "public" };
		}
		String[] retVal = new String[allTypes.size()];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = ((EnoTreeUser)allTypes.get(i)).getName();
		}
		return retVal;
	}

	public static void clearCache() {
		allUsers = null;
	}
}