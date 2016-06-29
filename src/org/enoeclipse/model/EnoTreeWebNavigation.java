package org.enoeclipse.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;


public abstract class EnoTreeWebNavigation extends EnoTreeWeb {
	protected static String MQL_INFO = "print {0} \"{1}\" select name description hidden label href alt dump |;";
	protected static String MQL_INFO_SETTING = "modify {0} \"{1}\" select setting dump |;";
	protected String description;
	protected String label;
	protected String href;
	protected String alt;
	protected boolean hidden;
	protected String realType;
	protected static ArrayList<EnoTreeWebNavigation> allItems;
	protected static final int INFO_NAME = 0;
	protected static final int INFO_DESCRIPTION = 1;
	protected static final int INFO_HIDDEN = 2;
	protected static final int INFO_LABEL = 3;
	protected static final int INFO_HREF = 4;
	protected static final int INFO_ALT = 5;
	public static final String[] ALL_WEB_TYPES = { "Menu", "Command" };

	public EnoTreeWebNavigation(String type, String name) throws EnoEclipseException, MatrixException {
		super(type, name);
		this.realType = type.toLowerCase();
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getHref() {
		return this.href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getAlt() {
		return this.alt;
	}

	public void setAlt(String alt) {
		this.alt = alt;
	}

	public boolean isHidden() {
		return this.hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public void fillBasics() {
		try {
			Context context = getContext();
			MQLCommand command = new MQLCommand();
			command.executeCommand(context, MessageFormat.format(MQL_INFO, new Object[] { this.realType, this.name }));

			String[] info = command.getResult().trim().split("\\|");
			this.name = info[0];
			if (info.length > 1) {
				this.description = info[1];
			} else {
				this.description = "";
			}
			if (info.length > 2) {
				this.hidden = info[2].equalsIgnoreCase("true");
			} else {
				this.hidden = false;
			}
			if (info.length > 3) {
				this.label = info[3];
			} else {
				this.label = "";
			}
			if (info.length > 4) {
				this.href = info[4];
			} else {
				this.href = "";
			}
			if (info.length > 5) {
				this.alt = info[5];
			} else {
				this.alt = "";
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public static ArrayList<EnoTreeWebNavigation> getAllItems(boolean forceRefresh) {
		if ((forceRefresh) || (allItems == null)) {
			try {
				allItems = new ArrayList<EnoTreeWebNavigation>();
				List<EnoTreeWebMenu> menus = EnoTreeWebMenu.getAllMenus(forceRefresh);
				List<EnoTreeWebCommand> commands = EnoTreeWebCommand.getAllCommands(forceRefresh);
				for (Iterator<EnoTreeWebMenu> iterator = menus.iterator(); iterator.hasNext(); ) { 
					Object item = iterator.next();
					allItems.add((EnoTreeWebNavigation)item);
				}
				for (Iterator<EnoTreeWebCommand> iterator1 = commands.iterator(); iterator1.hasNext(); ) {
					Object item = iterator1.next();
					allItems.add((EnoTreeWebNavigation)item);
				}
				Collections.sort(allItems);
			} catch (Exception ex) {
				EnoEclipseLogger.getLogger().severe(ex.getMessage());
				return null;
			}
		}
		return allItems;
	}

	public static String[] getAllItemNames(boolean refresh) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeWebNavigation> allItems = getAllItems(refresh);

		String[] retVal = new String[allItems.size()];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = ((EnoTreeWebNavigation)allItems.get(i)).getName();
		}
		return retVal;
	}

	public void refresh() throws EnoEclipseException, MatrixException {
		super.refresh();
		fillBasics();
	}

	protected ArrayList<EnoTreeWebMenu> getParentMenus() throws EnoEclipseException, MatrixException {
		ArrayList<EnoTreeWebMenu> retMenus = new ArrayList<EnoTreeWebMenu>();
		ArrayList<EnoTreeWebMenu> allMenus = EnoTreeWebMenu.getAllMenus(false);
		for (int i = 0; i < allMenus.size(); i++) {
			EnoTreeWebMenu storedMenu = (EnoTreeWebMenu)allMenus.get(i);
			ArrayList<EnoTreeWebNavigation> childItems = storedMenu.getChildrenItems(false);
			for (Iterator<EnoTreeWebNavigation> iterator = childItems.iterator(); iterator.hasNext();) {
				EnoTreeWebNavigation childItem = (EnoTreeWebNavigation)iterator.next();
				if (childItem.getClass() == getClass() && name.equals(childItem.getName())) {
					EnoTreeWebMenu oneMenu = new EnoTreeWebMenu(storedMenu.getName());
					oneMenu.setFrom(false);
					oneMenu.setRelType("contains");
					oneMenu.setParent(this);
					retMenus.add(oneMenu);
				}
			}
		}

		return retMenus;
	}

	public static void clearCache() {
		allItems = null;
		EnoTreeWeb.clearCache();
	}
}