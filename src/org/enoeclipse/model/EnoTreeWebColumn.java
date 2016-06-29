package org.enoeclipse.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Vector;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UITable;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;


public class EnoTreeWebColumn extends EnoTreeWeb {
	private EnoTreeWebTable table;
	protected int order;
	protected String columnType = "none";
	protected String expression = "";

	protected String description = "";
	protected String label = "";
	protected String href = "";
	protected String alt = "";
	protected boolean hidden;
	protected boolean editable;
	protected boolean autoheight;
	protected boolean autowidth;
	protected String sortType = "none";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String LABEL = "label";
	public static final String PROPERTY = "property";
	public static final String NOTHIDDEN = "nothidden";
	public static final String HIDDEN = "hidden";
	public static final String EDITABLE = "editable";
	public static final String AUTOHEIGHT = "autoheight";
	public static final String AUTOWIDTH = "autowidth";
	public static final String SORT_TYPE = "sorttype";
	public static final String SORT_TYPE_ALPHA = "alpha";
	public static final String SORT_TYPE_NUMERIC = "numeric";
	public static final String SORT_TYPE_OTHER = "other";
	public static final String SORT_TYPE_NONE = "none";
	public static final String COLUMN_TYPE_BUSINESS = "businessobject";
	public static final String COLUMN_TYPE_RELATIONSHIP = "relationship";
	public static final String COLUMN_TYPE_NONE = "none";
	public static final String[] COLUMN_TYPES = { "none", "businessobject", "relationship" };
	public static final String FILTER = "filter";
	public static final String MQL_SIMPLE_PRINT = "print table {0} system;";

	public EnoTreeWebColumn(String name, EnoTreeWebTable table, int order) throws EnoEclipseException, MatrixException {
		super("Column", name);
		this.table = table;
		this.order = order;
	}

	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getColumnType() {
		return this.columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public String getExpression() {
		return this.expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
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

	public boolean isEditable() {
		return this.editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public boolean getAutoheight() {
		return this.autoheight;
	}

	public void setAutoheight(boolean autoheight) {
		this.autoheight = autoheight;
	}

	public boolean getAutowidth() {
		return this.autowidth;
	}

	public void setAutowidth(boolean autowidth) {
		this.autowidth = autowidth;
	}

	public void refresh() throws EnoEclipseException, MatrixException {
		super.refresh();
		fillBasics();
	}

	public EnoTreeWebTable getTable() {
		return this.table;
	}

	public void fillBasics() {
		try {
			Context context = getContext();

			Vector assignements = new Vector();

			MapList lstColumns = UITable.getColumns(context, this.name, assignements);
			MQLCommand command = new MQLCommand();
			command.executeCommand(context, MessageFormat.format("print table {0} system;", new Object[] { this.table.getName() }));

			String[] lines = command.getResult().split("\n");
			String myLineBeginning = "#" + (this.order + 1);
			this.label = "";
			this.description = "";
			this.columnType = "";
			this.expression = "";
			this.autoheight = false;
			this.autowidth = false;
			this.hidden = false;
			this.editable = true;
			this.sortType = "";
			this.settings = new ArrayList<EnoTreeWebSetting>();
			boolean bProcessing = false;
			for (int i = 0; i < lines.length; i++) {
				lines[i] = lines[i].trim();
				if (lines[i].startsWith(myLineBeginning)) {
					bProcessing = true;
				} else if (lines[i].equals("")) {
					if (bProcessing) {
						break;
					}
				} else {
					if (!bProcessing) {
						continue;
					}
					if (lines[i].startsWith("label")) {
						this.label = lines[i].substring("label".length()).trim();
					} else if (lines[i].startsWith("description")) {
						this.description = lines[i].substring("description".length()).trim();
					} else if (lines[i].startsWith("businessobject")) {
						this.columnType = "businessobject";
						this.expression = lines[i].substring("businessobject".length()).trim();
					} else if (lines[i].startsWith("relationship")) {
						this.columnType = "relationship";
						this.expression = lines[i].substring("relationship".length()).trim();
					} else if (lines[i].startsWith("name")) {
						this.name = lines[i].substring("name".length()).trim();
					} else if (lines[i].startsWith("hidden")) {
						this.hidden = lines[i].endsWith("true");
					} else if (lines[i].startsWith("setting")) {
						lines[i] = lines[i].substring("setting".length());
						int indexOfValue = lines[i].indexOf("value");
						String setName = lines[i].substring(0, indexOfValue).trim();
						String setValue = lines[i].substring(indexOfValue + "value".length()).trim();

						EnoTreeWebSetting child = EnoTreeWebSetting.createInstance(this, setName);
						child.setValue(setValue);

						child.setFrom(true);
						child.setRelType("contains");
						child.setParent(this);
						this.settings.add(child);
					}
				}
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public static ArrayList<EnoTreeWebSetting> getSettings(EnoTreeWebColumn column) {
		if (column.settings == null) {
			column.fillBasics();
		}
		return column.settings;
	}

	public void save() {
		this.table.fillBasics();
		this.table.getColumns(false).set(this.order, this);
		this.table.save();
	}

	public String prepareSave() {
		String modString = " column";
		modString = modString + " label \"" + this.label + "\"";

		if ((this.columnType.equals("")) || (this.columnType.equals("none"))) {
			modString = modString + " set";
		} else {
			modString = modString + " " + this.columnType;
		}
		modString = modString + " \"" + this.expression + "\"";

		modString = modString + " name \"" + getName() + "\"";
		modString = modString + " description \"" + this.description + "\"";

		modString = modString + (this.hidden ? " " : " !") + "hidden";

		return modString;
	}
}