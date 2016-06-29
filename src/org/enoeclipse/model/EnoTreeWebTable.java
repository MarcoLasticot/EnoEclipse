package org.enoeclipse.model;

import java.util.ArrayList;
import java.util.Iterator;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import com.matrixone.apps.domain.util.MqlUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.db.Table;
import matrix.util.MatrixException;


public class EnoTreeWebTable extends EnoTreeBusiness
{
	Table table;
	ArrayList<EnoTreeWebColumn> columns;
	private static ArrayList<EnoTreeWebTable> allTables;
	//protected static String MQL_INFO = "print table \"{0}\" system select name description hidden dump |;";
	protected static String MQL_INFO_NEW = "print table $1 system select name description hidden dump |";
	//protected static String MQL_INFO_COLUMN = "print table \"{0}\" system select column dump |;";
	protected static String MQL_INFO_COLUMN_NEW = "print table $1 system select column dump |";
	//protected static String MQL_INSERT_COLUMN = "modify table \"{0}\" system add column \"{1}\" order {2};";
	protected static String MQL_INSERT_COLUMN_NEW = "modify table $1 system add column $2 order $3";
	//protected static String MQL_REMOVE_COLUMN = "modify table \"{0}\" system remove column \"{1}\";";
	protected static String MQL_REMOVE_COLUMN_NEW = "modify table $1 system remove column $2";
	protected static final int INFO_NAME = 0;
	protected static final int INFO_DESCRIPTION = 1;
	protected static final int INFO_HIDDEN = 2;
	protected String description;
	protected boolean hidden;

	public EnoTreeWebTable(String name)
			throws EnoEclipseException, MatrixException
	{
		super("Table", name);
	}

	public void refresh() throws EnoEclipseException, MatrixException
	{
		super.refresh();
		fillBasics();
		this.columns = null;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
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
			//MQLCommand command = new MQLCommand();
			//command.executeCommand(context, MessageFormat.format(MQL_INFO, new Object[] { this.name }));

			String[] info = MqlUtil.mqlCommand(context, MQL_INFO_NEW, this.name).split("\\|");
			this.name = info[0];
			this.description = info[1];
			this.hidden = info[2].equalsIgnoreCase("true");
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public void addColumn() throws EnoEclipseException, MatrixException {
		addColumn(new EnoTreeWebColumn("", this, getColumns(false).size()));
	}

	public void insertColumn(int index) throws EnoEclipseException, MatrixException {
		for (int i = index; i < getColumns(false).size(); i++) {
			((EnoTreeWebColumn)getColumns(false).get(i)).setOrder(i + 1);
		}
		insertColumn(new EnoTreeWebColumn("", this, index), index);
	}

	public void addColumn(EnoTreeWebColumn newColumn) {
		this.columns.add(newColumn);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext())
			((IEnoStateViewer)iterator.next()).addProperty(newColumn); 
	}

	public void insertColumn(EnoTreeWebColumn newColumn, int index) {
		this.columns.add(index, newColumn);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext())
			((IEnoStateViewer)iterator.next()).insertProperty(newColumn, index); 
	}

	public void removeColumn(EnoTreeWebColumn column) {
		if (this.columns == null) {
			getColumns(false);
		}
		this.columns.remove(column);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext())
			((IEnoStateViewer)iterator.next()).removeProperty(column);
	}

	public String[] getColumnNames(boolean forceRefresh) {
		ArrayList<EnoTreeWebColumn> columns = getColumns(forceRefresh);
		String[] retVal = new String[columns.size()];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = ((EnoTreeWebColumn)columns.get(i)).getName();
		}
		return retVal;
	}

	public static ArrayList<EnoTreeWebColumn> getColumns(EnoTreeWebTable table) {
		ArrayList<EnoTreeWebColumn> retColumns = new ArrayList<EnoTreeWebColumn>();
		try {
			Context context = getContext();
			//MQLCommand command = new MQLCommand();
			//command.executeCommand(context, MessageFormat.format(MQL_INFO_COLUMN, new Object[] { table.getName() }));
			String[] t = MqlUtil.mqlCommand(context, MQL_INFO_COLUMN_NEW, table.getName()).split("\\|");
			for (int i = 0; i < t.length; i++) {
				String tname = t[i].trim();
				if (!tname.equals("")) {
					EnoTreeWebColumn column = new EnoTreeWebColumn(tname, table, i);
					retColumns.add(column);
				}
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return retColumns;
	}

	public ArrayList<EnoTreeWebColumn> getColumns(boolean forceRefresh) {
		if ((forceRefresh) || (this.columns == null)) {
			this.columns = getColumns(this);
		}
		return this.columns;
	}

	public String prepareSaveColumns(Context context, MQLCommand command) throws MatrixException, EnoEclipseException {
		String retVal = "";
		for (int i = 0; i < this.columns.size(); i++) {
			((EnoTreeWebColumn)this.columns.get(i)).setOrder(i);
			retVal = retVal + ((EnoTreeWebColumn)this.columns.get(i)).prepareSave();
		}
		return retVal;
	}

	public static ArrayList<EnoTreeWebTable> getAllTables(boolean refresh) throws MatrixException, EnoEclipseException {
		if (refresh || allTables == null) {
			allTables = new ArrayList<EnoTreeWebTable>();
			Context context = getContext();
			//MQLCommand command = new MQLCommand();
			//command.executeCommand(context, MQL_LIST_ALL);
			String lines[] = MqlUtil.mqlCommand(context, MQL_LIST_ALL_NEW, "table").split("\n");
			for (int i = 0; i < lines.length; i++) {
				String name = lines[i] = lines[i].trim();
				allTables.add(new EnoTreeWebTable(name));
			}

		}
		return allTables;
	}

	public void save()
	{
		try {
			MQLCommand command = new MQLCommand();
			Context context = getContext();

			EnoTreeWebTable oldTable = new EnoTreeWebTable(this.oldName);
			oldTable.fillBasics();

			String modString = "";
			if ((oldTable.getName() == null) || (!oldTable.getName().equals(getName()))) {
				modString = modString + " name \"" + getName() + "\"";
			}
			if ((oldTable.getDescription() == null) || (!oldTable.getDescription().equals(getDescription()))) {
				modString = modString + " description \"" + getDescription() + "\"";
			}
			if (oldTable.isHidden() != isHidden()) {
				modString = modString + " " + (isHidden() ? "" : "!") + "hidden";
			}

			modString = modString + prepareSaveColumns(context, command);

			if (!modString.equals("")) {
				//command.executeCommand(context, "delete table \"" + this.oldName + "\" system;");
				MqlUtil.mqlCommand(context, "delete table $1 system", this.oldName);
				//command.executeCommand(context, "add table \"" + this.oldName + "\" system " + modString + ";");
				MqlUtil.mqlCommand(context, "add table $1 system " + modString, this.oldName);
			}

			refresh();
		}
		catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public EnoTreeBusiness[] getChildren(boolean forceUpdate) throws EnoEclipseException, MatrixException {
		if (forceUpdate) {
			this.children = null;
		}
		if (this.children == null) {
			this.children = new ArrayList<EnoTreeBusiness>();
			this.children.addAll(getColumns(false));
		}
		return (EnoTreeBusiness[])this.children.toArray(new EnoTreeBusiness[this.children.size()]);
	}

	public static void clearCache() {
		allTables = null;
	}
}