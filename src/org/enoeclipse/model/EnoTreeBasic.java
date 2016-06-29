package org.enoeclipse.model;

import java.util.ArrayList;

import org.enoeclipse.exception.EnoEclipseException;

import matrix.util.MatrixException;


public class EnoTreeBasic extends EnoTreeAttribute {
	
	public static String[] ALL_BASICS = { "type",
		"name", 
		"revision", 
		"policy", 
		"current", 
		"owner", 
		"locker", 
		"modified", 
	"originated" };
	
	private static ArrayList<EnoTreeBasic> basics;

	public EnoTreeBasic(String name) throws EnoEclipseException, MatrixException {
		super(name);
		this.type = "Basic";
	}

	public static ArrayList<EnoTreeBasic> getAllBasics() throws EnoEclipseException, MatrixException {
		if (basics == null) {
			basics = new ArrayList<EnoTreeBasic>();
			for (int i = 0; i < ALL_BASICS.length; i++) {
				EnoTreeBasic b = new EnoTreeBasic(ALL_BASICS[i]);
				basics.add(b);
			}
		}
		return basics;
	}
}