package org.enoeclipse.model;

import java.util.ArrayList;
import java.util.Collections;

import org.enoeclipse.exception.EnoEclipseException;

import matrix.db.Role;
import matrix.db.RoleItr;
import matrix.db.RoleList;
import matrix.util.MatrixException;

public class EnoTreeRole extends EnoTreeAssignment {
	private static ArrayList<EnoTreeRole> allRoles;

	public EnoTreeRole(String name) throws EnoEclipseException, MatrixException {
		super("Role", name);
	}

	public static ArrayList<EnoTreeRole> getAllRoles(boolean refresh) throws MatrixException, EnoEclipseException {
		if ((refresh) || (allRoles == null)) {
			RoleList pl = Role.getRoles(getContext(), true);
			allRoles = new ArrayList<EnoTreeRole>();
			RoleItr pi = new RoleItr(pl);
			while (pi.next()) {
				Role p = pi.obj();
				EnoTreeRole role = new EnoTreeRole(p.getName());
				allRoles.add(role);
			}
			Collections.sort(allRoles);
		}
		return allRoles;
	}

	public static void clearCache() {
		allRoles = null;
	}
}