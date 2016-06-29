package org.enoeclipse.model;

import java.util.ArrayList;
import java.util.Collections;

import org.enoeclipse.exception.EnoEclipseException;

import matrix.db.Group;
import matrix.db.GroupItr;
import matrix.db.GroupList;
import matrix.util.MatrixException;


public class EnoTreeGroup extends EnoTreeAssignment {
	private static ArrayList<EnoTreeGroup> allGroups;

	public EnoTreeGroup(String name) throws EnoEclipseException, MatrixException {
		super("Group", name);
	}

	public static ArrayList<EnoTreeGroup> getAllGroups(boolean refresh) throws MatrixException, EnoEclipseException {
		if ((refresh) || (allGroups == null)) {
			GroupList pl = Group.getGroups(getContext(), true);
			allGroups = new ArrayList<EnoTreeGroup>();
			GroupItr pi = new GroupItr(pl);
			while (pi.next()) {
				Group p = pi.obj();
				EnoTreeGroup role = new EnoTreeGroup(p.getName());
				allGroups.add(role);
			}
			Collections.sort(allGroups);
		}
		return allGroups;
	}

	public static void clearCache() {
		allGroups = null;
	}
}