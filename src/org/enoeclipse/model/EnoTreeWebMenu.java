package org.enoeclipse.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import com.matrixone.apps.domain.util.MqlUtil;

import matrix.db.Context;
import matrix.util.MatrixException;


public class EnoTreeWebMenu extends EnoTreeWebNavigation {
	//protected static String MQL_INFO_MENUS = "print menu \"{0}\" select menu dump |;";
	protected static String MQL_INFO_MENUS_NEW = "print menu $1 select $2 dump $3";
	protected static String MQL_INFO_COMMANDS = "print menu $1 select $2 dump $3";
	protected ArrayList<EnoTreeWebMenu> parentMenus;
	protected ArrayList<EnoTreeWebMenu> childMenus;
	protected ArrayList<EnoTreeWebCommand> childCommands;
	protected ArrayList<EnoTreeWebNavigation> childrenItems;
	private static ArrayList<EnoTreeWebMenu> allMenus;
	protected static final String CHILDREN = "children";

	public EnoTreeWebMenu(String name) throws EnoEclipseException, MatrixException {
		super("Menu", name);
	}

	public void refresh() throws EnoEclipseException, MatrixException {
		super.refresh();
		fillBasics();

		this.childMenus = null;
	}

	public void fillBasics() {
		super.fillBasics();
	}

	public static ArrayList<EnoTreeWebMenu> getAllMenus(boolean refresh) throws MatrixException, EnoEclipseException {
		if(refresh || allMenus == null) {
			allMenus = new ArrayList<EnoTreeWebMenu>();
			matrix.db.Context context = getContext();
			//MQLCommand command = new MQLCommand();
			//command.executeCommand(context, MessageFormat.format(MQL_LIST_ALL, new Object[] {"Menu".toLowerCase()}));
			String lines[] = MqlUtil.mqlCommand(context, MQL_LIST_ALL_NEW, "Menu".toLowerCase()).split("\n");
			for (int i = 0; i < lines.length; i++) {
				String name = lines[i] = lines[i].trim();
				allMenus.add(new EnoTreeWebMenu(name));
			}

			Collections.sort(allMenus);
		}
		return allMenus;
	}

	public static String[] getAllMenuNames(boolean refresh) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeWebMenu> allMenus = getAllMenus(refresh);

		String[] retVal = new String[allMenus.size()];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = ((EnoTreeWebMenu)allMenus.get(i)).getName();
		}
		return retVal;
	}

	public static ArrayList<EnoTreeWebNavigation> getChildrenItems(EnoTreeWebMenu menu) {
		ArrayList<EnoTreeWebNavigation> retMenus = new ArrayList<EnoTreeWebNavigation>();
		try {
			Context context = getContext();

//			MQLCommand command = new MQLCommand();
//			command.executeCommand(context, MessageFormat.format(EnoTreeState.MQL_SIMPLE_PRINT, new Object[] { "Menu", menu.getName() }));

			String[] lines = MqlUtil.mqlCommand(context, MQL_SIMPLE_PRINT_NEW, "Menu", menu.getName()).split("\n");
			String myLineBeginning = "children";
			boolean bProcessing = false;
			for (int i = 0; i < lines.length; i++) {
				lines[i] = lines[i].trim();
				if (lines[i].startsWith(myLineBeginning)) {
					bProcessing = true;
				} else if ((lines[i].startsWith("Menu".toLowerCase())) || (lines[i].startsWith("Command".toLowerCase()))) {
					if (bProcessing) {
						int indexOfBlank = lines[i].indexOf(" ");
						String t = lines[i].substring(0, indexOfBlank);
						String tname = lines[i].substring(indexOfBlank).trim();

						EnoTreeWebNavigation child = null;
						if (t.equalsIgnoreCase("Menu")) {
							child = new EnoTreeWebMenu(tname);
						} else {
							child = new EnoTreeWebCommand(tname);
						}
						child.setFrom(true);
						child.setRelType("contains");
						child.setParent(menu);
						retMenus.add(child);
					}
				}
				else {
					bProcessing = false;
				}
			}

			return retMenus;
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return null;
	}

	public ArrayList<EnoTreeWebNavigation> getChildrenItems(boolean forceRefresh) {
		if ((forceRefresh) || (this.childrenItems == null)) {
			this.childrenItems = getChildrenItems(this);
		}
		return this.childrenItems;
	}

	public void addChildItem(boolean bCommand) throws EnoEclipseException, MatrixException {
		if (bCommand) {
			addChildItem(new EnoTreeWebCommand(""));
		} else {
			addChildItem(new EnoTreeWebMenu(""));
		}
	}

	public void insertChildItem(int index, boolean bCommand) throws EnoEclipseException, MatrixException {
		if (bCommand) {
			insertChildItem(new EnoTreeWebCommand(""), index);
		} else {
			insertChildItem(new EnoTreeWebMenu(""), index);
		}
	}

	public void addChildItem(EnoTreeWebNavigation newItem) {
		this.childrenItems.add(newItem);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoStateViewer)iterator.next()).addProperty(newItem); 
		}
	}

	public void insertChildItem(EnoTreeWebNavigation newItem, int index) {
		this.childrenItems.add(index, newItem);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoStateViewer)iterator.next()).insertProperty(newItem, index); 
		}
	}

	public void removeChildItem(EnoTreeWebNavigation item) {
		if (this.childrenItems == null) {
			getChildrenItems(false);
		}
		this.childrenItems.remove(item);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoStateViewer)iterator.next()).removeProperty(item);
		}
	}

	public static ArrayList<EnoTreeWebMenu> getChildrenMenus(EnoTreeWebMenu menu)
	{
		ArrayList<EnoTreeWebMenu> retMenus = new ArrayList<EnoTreeWebMenu>();
		try {
			Context context = getContext();
//			MQLCommand command = new MQLCommand();
//			command.executeCommand(context, MessageFormat.format(MQL_INFO_MENUS, new Object[] { menu.getName() }));
			String[] t = MqlUtil.mqlCommand(context, MQL_SIMPLE_PRINT_NEW, menu.getName(), "menu", "|").split("\\|");
			for (int i = 0; i < t.length; i++) {
				String tname = t[i].trim();
				if (!tname.equals("")) {
					EnoTreeWebMenu a = new EnoTreeWebMenu(tname);
					a.setFrom(true);
					a.setRelType("contains");
					a.setParent(menu);
					retMenus.add(a);
				}
			}
			Collections.sort(retMenus);
			return retMenus;
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}return null;
	}

	public ArrayList<EnoTreeWebMenu> getChildrenMenus(boolean forceRefresh)
	{
		if ((forceRefresh) || (this.childMenus == null)) {
			this.childMenus = getChildrenMenus(this);
		}
		return this.childMenus;
	}

	public String prepareSaveMenus(EnoTreeWebMenu oldMenu) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeWebNavigation> subItems = getChildrenItems(false);
		ArrayList<EnoTreeWebNavigation> oldSubItems = oldMenu.getChildrenItems(false);
		String sAdded = "";
		String sRemoved = "";
		for(Iterator<EnoTreeWebNavigation> iterator = oldSubItems.iterator(); iterator.hasNext();) {
			EnoTreeWebNavigation oldItem = (EnoTreeWebNavigation)iterator.next();
			sRemoved = (new StringBuilder(String.valueOf(sRemoved))).append(" remove ").append(oldItem.getType().toLowerCase()).append(" \"").append(oldItem.getName()).append("\"").toString();
		}

		for(Iterator<EnoTreeWebNavigation> iterator1 = subItems.iterator(); iterator1.hasNext();) {
			EnoTreeWebNavigation item = (EnoTreeWebNavigation)iterator1.next();
			sAdded = (new StringBuilder(String.valueOf(sAdded))).append(" add ").append(item.getType().toLowerCase()).append(" \"").append(item.getName()).append("\"").toString();
		}

		return (new StringBuilder(String.valueOf(sRemoved))).append(sAdded).toString();
	}

	public void save() {
		try {
			//MQLCommand command = new MQLCommand();
			Context context = getContext();

			String modString = "";
			EnoTreeWebMenu oldMenu = new EnoTreeWebMenu(this.oldName);
			oldMenu.fillBasics();

			if ((oldMenu.getName() == null) || (!oldMenu.getName().equals(getName()))) {
				modString = modString + " name \"" + getName() + "\"";
			}
			if (oldMenu.isHidden() != isHidden()) {
				modString = modString + (isHidden() ? " hidden" : "!hidden");
			}
			if ((oldMenu.getDescription() == null) || (!oldMenu.getDescription().equals(getDescription()))) {
				modString = modString + " description \"" + getDescription() + "\"";
			}
			if ((oldMenu.getLabel() == null) || (!oldMenu.getLabel().equals(getLabel()))) {
				modString = modString + " label \"" + getLabel() + "\"";
			}
			if ((oldMenu.getHref() == null) || (!oldMenu.getHref().equals(getHref()))) {
				modString = modString + " href \"" + getHref() + "\"";
			}
			if ((oldMenu.getAlt() == null) || (!oldMenu.getAlt().equals(getAlt()))) {
				modString = modString + " alt \"" + getAlt() + "\"";
			}

			String sChildMenus = prepareSaveMenus(oldMenu);
			String saveSetting = prepareSaveSettings(oldMenu);
			String saveUsers = prepareSaveUsers(oldMenu);

			modString = modString + sChildMenus + saveSetting + saveUsers;

			if (!modString.equals("")) {
				//command.executeCommand(context, "modify " + this.realType + " \"" + oldMenu.getName() + "\" " + modString + ";");
				MqlUtil.mqlCommand(context, "modify $1 $2" + modString, this.realType, oldMenu.getName());
			}

			this.childMenus = null;
			this.parentMenus = null;
			this.childCommands = null;
			this.childrenItems = null;
			clearCache();
			refresh();
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public EnoTreeBusiness[] getChildren(boolean forceUpdate) throws EnoEclipseException, MatrixException {
		if (forceUpdate) {
			this.children = null;
		}
		if (this.children == null) {
			this.children = new ArrayList<EnoTreeBusiness>();
			this.children.addAll(getChildrenItems(false));
			this.children.addAll(getParentMenus());
		}
		return (EnoTreeBusiness[])this.children.toArray(new EnoTreeBusiness[this.children.size()]);
	}

	public static void clearCache() {
		allMenus = null;
		EnoTreeWebNavigation.clearCache();
	}
}