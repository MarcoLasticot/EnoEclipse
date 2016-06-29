package org.enoeclipse.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import matrix.util.MatrixException;


public class EnoTreeWebSetting extends EnoTreeBusiness {
	protected String value;
	protected String[] range;
	protected String defaultValue;
	protected EnoTreeWeb parentWeb;
	protected static Map<String, ArrayList<EnoTreeWebSetting>> allSettings;

	private EnoTreeWebSetting(String name, String[] range, String defaultValue, EnoTreeWeb parentWeb) throws EnoEclipseException, MatrixException {
		super("WebSetting", name);
		this.range = range;
		this.defaultValue = defaultValue;
		this.parentWeb = parentWeb;
	}

	public EnoTreeWebSetting(String name, EnoTreeWeb parentWeb) throws EnoEclipseException, MatrixException {
		super("WebSetting", name);
		this.parentWeb = parentWeb;
	}

	public void setName(String name) {
		super.setName(name);
		range = null;
		defaultValue = null;
		if (parentWeb != null) {
			ArrayList<EnoTreeWebSetting> alTypeSetting = getAllSettings(parentWeb);
			for (Iterator<EnoTreeWebSetting> iterator = alTypeSetting.iterator(); iterator.hasNext();) {
				EnoTreeWebSetting set = (EnoTreeWebSetting)iterator.next();
				if (set.getName().equals(name)) {
					range = set.getRange();
					defaultValue = set.getDefaultValue();
				}
			}
		}
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String[] getRange() {
		return this.range;
	}

	public String getDefaultValue() {
		return this.defaultValue;
	}

	public static ArrayList<EnoTreeWebSetting> getAllSettings(EnoTreeWeb parent) {
		if (allSettings == null) {
			allSettings = new HashMap<String, ArrayList<EnoTreeWebSetting>>();
		}
		ArrayList<EnoTreeWebSetting> typeSettings = (ArrayList<EnoTreeWebSetting>)allSettings.get(parent.getType());
		if (typeSettings == null) {
			try {
				InputStream ins = EnoTreeWebSetting.class.getResourceAsStream("Settings" + parent.getType() + ".txt");
				BufferedReader reader = new BufferedReader(new InputStreamReader(ins, "utf-8"));

				int index = 0;
				String setName = null;
				String[] setRange = (String[])null;
				String setDefault = null;
				typeSettings = new ArrayList<EnoTreeWebSetting>();
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if(index == 0) {
						setName = line;
						index++;
					} else {
						if(index == 1) {
							if(!line.equals("")) {
								setRange = line.split("\\|");
								for (int i = 0; i < setRange.length; i++) {
									setRange[i] = setRange[i].trim();
								}

							} else {
								setRange = (String[])null;
							}
							index++;
						} else {
							if (index == 2) {
								setDefault = line;
								EnoTreeWebSetting newSetting = new EnoTreeWebSetting(setName, setRange, setDefault, parent);
								typeSettings.add(newSetting);
								index = 0;
							}
						}
					}
				}
				Collections.sort(typeSettings);
				allSettings.put(parent.getType(), typeSettings);

				reader.close();
				ins.close();
			} catch (Exception e) {
				EnoEclipseLogger.getLogger().severe("Unable to retrieve all web settings for type " + parent.getType() + ": " + e.getMessage());
			}
		}
		return typeSettings;
	}

	public static String[] getAllSettingNames(EnoTreeWeb parent) {
		ArrayList<EnoTreeWebSetting> parentSettings = getAllSettings(parent);
		String[] retNames = new String[parentSettings.size()];
		for (int i = 0; i < parentSettings.size(); i++) {
			retNames[i] = ((EnoTreeWebSetting)parentSettings.get(i)).getName();
		}
		return retNames;
	}

	public static EnoTreeWebSetting createInstance(EnoTreeWeb parent, String settingName) throws EnoEclipseException, MatrixException {
		ArrayList<EnoTreeWebSetting> alTypeSetting = getAllSettings(parent);
		for(Iterator<EnoTreeWebSetting> iterator = alTypeSetting.iterator(); iterator.hasNext();) {
			EnoTreeWebSetting set = (EnoTreeWebSetting)iterator.next();
			if(set.getName().equals(settingName)) {
				return new EnoTreeWebSetting(settingName, set.getRange(), set.getDefaultValue(), parent);
			}
		}
		return new EnoTreeWebSetting(settingName, null, null, parent);
	}

	public static void clearCache() {
		allSettings = null;
	}
}