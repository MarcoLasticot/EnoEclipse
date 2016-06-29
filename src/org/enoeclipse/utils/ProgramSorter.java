package org.enoeclipse.utils;

import java.util.Comparator;

public class ProgramSorter implements Comparator<Object> {
	
	public int compare(Object arg0, Object arg1) {
		String prog1 = (String)arg0;
		String prog2 = (String)arg1;
		return prog1.compareToIgnoreCase(prog2);
	}
}