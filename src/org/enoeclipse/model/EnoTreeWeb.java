package org.enoeclipse.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import com.matrixone.apps.domain.util.MqlUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;


public abstract class EnoTreeWeb extends EnoTreeBusiness
{
	protected static String MQL_INFO_SETTING = "print {0} \"{1}\" select setting dump |;";
	protected static String MQL_INFO_USER = "print {0} \"{1}\" select user dump |;";
	protected String realType;
	protected ArrayList<EnoTreeWebSetting> settings;
	protected ArrayList<EnoTreeUser> users;
	protected static final String SETTING = "setting";
	protected static final String VALUE = "value";

	public EnoTreeWeb(String type, String name)
			throws EnoEclipseException, MatrixException
	{
		super(type, name);
		this.realType = type.toLowerCase();
	}

	public void addSetting() throws EnoEclipseException, MatrixException {
		EnoTreeWebSetting newSetting = new EnoTreeWebSetting("", this);
		addSetting(newSetting);
	}

	public void addSetting(EnoTreeWebSetting newMenu) {
		getSettings(false).add(newMenu);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			IEnoBusinessViewer contentProvider = (IEnoBusinessViewer)iterator.next();
			contentProvider.addProperty(newMenu);
		}
	}

	public void removeSetting(EnoTreeWebSetting menu) {
		if (this.settings == null) {
			getSettings(false);
		}
		this.settings.remove(menu);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			IEnoBusinessViewer contentProvider = (IEnoBusinessViewer)iterator.next();
			contentProvider.removeProperty(menu);
		}
	}

	public static ArrayList<EnoTreeWebSetting> getSettings(EnoTreeWeb web) {
		ArrayList<EnoTreeWebSetting> retSettings = new ArrayList<EnoTreeWebSetting>();
		try {
			Context context = getContext();

//			MQLCommand command = new MQLCommand();
//			command.executeCommand(context, MessageFormat.format(MQL_SIMPLE_PRINT, new Object[] { web.getType(), web.getName() }));

			String[] lines = MqlUtil.mqlCommand(context, MQL_SIMPLE_PRINT_NEW, web.getType(), web.getName()).split("\n");
			String myLineBeginning = "setting";
			for (int i = 0; i < lines.length; i++) {
				lines[i] = lines[i].trim();
				if (lines[i].startsWith(myLineBeginning)) {
					lines[i] = lines[i].substring(myLineBeginning.length());
					int indexOfValue = lines[i].indexOf("value");
					String setName = lines[i].substring(0, indexOfValue).trim();
					String setValue = lines[i].substring(indexOfValue + "value".length()).trim();

					EnoTreeWebSetting child = EnoTreeWebSetting.createInstance(web, setName);
					child.setValue(setValue);

					child.setFrom(true);
					child.setRelType("contains");
					child.setParent(web);
					retSettings.add(child);
				}
			}
			return retSettings;
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}return null;
	}

	public ArrayList<EnoTreeWebSetting> getSettings(boolean forceRefresh)
	{
		if ((forceRefresh) || (this.settings == null)) {
			this.settings = getSettings(this);
		}
		return this.settings;
	}

	protected String prepareSaveSettings(EnoTreeWeb oldWeb)
	{
		ArrayList<EnoTreeWebSetting> mySettings = getSettings(false);
		ArrayList<EnoTreeWebSetting> oldSettings = oldWeb.getSettings(false);
		String sAdded = "";
		String sRemoved = "";
		for(Iterator<EnoTreeWebSetting> iterator = oldSettings.iterator(); iterator.hasNext();)
		{
			EnoTreeWebSetting oldItem = (EnoTreeWebSetting)iterator.next();
			sRemoved = (new StringBuilder(String.valueOf(sRemoved))).append(" remove setting \"").append(oldItem.getName()).append("\"").toString();
		}

		for(Iterator<EnoTreeWebSetting> iterator1 = mySettings.iterator(); iterator1.hasNext();)
		{
			EnoTreeWebSetting item = (EnoTreeWebSetting)iterator1.next();
			sAdded = (new StringBuilder(String.valueOf(sAdded))).append(" add setting \"").append(item.getName()).append("\" \"").append(item.getValue()).append("\"").toString();
		}

		return (new StringBuilder(String.valueOf(sRemoved))).append(sAdded).toString();
	}

	public void addUser() throws EnoEclipseException, MatrixException {
		EnoTreeUser newUser = new EnoTreePerson("");
		addUser(newUser);
	}

	public void addUser(EnoTreeUser newUser) {
		getUsers(false).add(newUser);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			IEnoBusinessViewer contentProvider = (IEnoBusinessViewer)iterator.next();
			contentProvider.addProperty(newUser);
		}
	}

	public void removeUser(EnoTreeUser user) {
		if (this.users == null) {
			getUsers(false);
		}
		this.users.remove(user);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			IEnoBusinessViewer contentProvider = (IEnoBusinessViewer)iterator.next();
			contentProvider.removeProperty(user);
		}
	}

	public static ArrayList<EnoTreeUser> getUsers(EnoTreeWeb web)
	{
		ArrayList<EnoTreeUser> retSettings = new ArrayList<EnoTreeUser>();
		try {
			Context context = getContext();

			MQLCommand command = new MQLCommand();

			command.executeCommand(context, MessageFormat.format(MQL_INFO_USER, new Object[] { web.getType(), web.getName() }));

			String res = command.getResult().trim();
			if (res.length() != 0) {
				String[] sUsers = res.split("\\|");

				for (String sUser : sUsers) {
					EnoTreeUser user = EnoTreeUser.createInstance(sUser);
					user.setFrom(false);
					user.setRelType("access");
					user.setParent(web);
					retSettings.add(user);
				}
			}
			return retSettings;
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}return null;
	}

	public ArrayList<EnoTreeUser> getUsers(boolean forceRefresh) {
		if ((forceRefresh) || (this.users == null)) {
			this.users = getUsers(this);
		}
		return this.users;
	}

	protected String prepareSaveUsers(EnoTreeWeb oldWeb) {
		ArrayList<EnoTreeUser> myUsers = getUsers(false);
		ArrayList<EnoTreeUser> oldUsers = oldWeb.getUsers(false);
		String sAdded = "";
		String sRemoved = "";
		for(Iterator<EnoTreeUser> iterator = oldUsers.iterator(); iterator.hasNext();) {
			EnoTreeUser oldUser = (EnoTreeUser)iterator.next();
			sRemoved = (new StringBuilder(String.valueOf(sRemoved))).append(" remove user \"").append(oldUser.getName()).append("\"").toString();
		}

		for(Iterator<EnoTreeUser> iterator1 = myUsers.iterator(); iterator1.hasNext();) {
			EnoTreeUser user = (EnoTreeUser)iterator1.next();
			sAdded = (new StringBuilder(String.valueOf(sAdded))).append(" add user \"").append(user.getName()).append("\"").toString();
		}

		return (new StringBuilder(String.valueOf(sRemoved))).append(sAdded).toString();
	}

	public void refresh() throws EnoEclipseException, MatrixException {
		super.refresh();
		this.settings = null;
	}

	public static void clearCache() {
	}
}