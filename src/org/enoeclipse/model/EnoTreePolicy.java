package org.enoeclipse.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.db.Policy;
import matrix.db.PolicyItr;
import matrix.db.PolicyList;
import matrix.util.MatrixException;


public class EnoTreePolicy extends EnoTreeBusiness {
	Policy policy;
	ArrayList<EnoTreeType> types;
	ArrayList<EnoTreeState> states;
	private static ArrayList<EnoTreePolicy> allPolicies;
	protected static String MQL_INFO_STORE = "print policy \"{0}\" select store dump |;";
	protected static String MQL_INFO_TYPE = "print policy \"{0}\" select type dump |;";
	protected static String MQL_INFO_STATE = "print policy \"{0}\" select state dump |;";
	protected static String MQL_ADD_TYPE = "modify policy \"{0}\" add type \"{1}\";";
	protected static String MQL_REMOVE_TYPE = "modify policy \"{0}\" remove type \"{1}\";";
	protected static String MQL_ADD_STATE = "modify policy \"{0}\" add state \"{1}\";";
	protected static String MQL_ADD_STATE_BEFORE = "modify policy \"{0}\" add state \"{1}\" before \"{2}\";";
	protected static String MQL_REMOVE_STATE = "modify policy \"{0}\" remove state \"{1}\";";
	protected String description;
	protected String sequence;
	protected boolean hidden;
	protected EnoTreeStore store;

	public EnoTreePolicy(String name) throws EnoEclipseException, MatrixException {
		super("Policy", name);
		this.policy = new Policy(name);
	}

	public void refresh() throws EnoEclipseException, MatrixException {
		super.refresh();
		this.policy = new Policy(getName());
		fillBasics();
		this.states = null;
		this.types = null;
	}

	public String getSequence() {
		return this.sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
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

	public void setStore(EnoTreeStore store) {
		this.store = store;
	}

	public void fillBasics() {
		try {
			Context context = getContext();
			this.policy.open(context);
			try {
				this.name = this.policy.getName();
				this.description = this.policy.getDescription(context);
				this.sequence = this.policy.getSequence();

				this.store = getStore(true);
			} finally {
				this.policy.close(context);
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public void saveAddType(EnoTreeType type) {
		try {
			Context context = getContext();
			MQLCommand command = new MQLCommand();

			command.executeCommand(context, MessageFormat.format(MQL_ADD_TYPE, new Object[] { getName(), type.getName() }));
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public void saveRemoveType(EnoTreeType type) {
		try {
			Context context = getContext();
			MQLCommand command = new MQLCommand();

			command.executeCommand(context, MessageFormat.format(MQL_REMOVE_TYPE, new Object[] { this.policy.getName(), type.getName() }));
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public static ArrayList<EnoTreeType> getTypes(EnoTreePolicy policy) {
		ArrayList<EnoTreeType> retTypes = new ArrayList<EnoTreeType>();
		try {
			Context context = getContext();
			MQLCommand command = new MQLCommand();

			command.executeCommand(context, MessageFormat.format(MQL_INFO_TYPE, new Object[] { policy.getName() }));
			String[] t = command.getResult().split("\\|");
			for (int i = 0; i < t.length; i++) {
				String tname = t[i].trim();
				if (!tname.equals("")) {
					EnoTreeType type = new EnoTreeType(tname);
					retTypes.add(type);
				}
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return retTypes;
	}

	public ArrayList<EnoTreeType> getTypes(boolean forceRefresh) {
		if ((forceRefresh) || (this.types == null)) {
			this.types = getTypes(this);
		}
		return this.types;
	}

	public void addRange(EnoTreeType newType) {
		this.types.add(newType);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoTypeViewer)iterator.next()).addType(newType); 
		}
	}

	public void removeRange(EnoTreeRange range) {
		if (range == null) {
			getTypes(false);
		}
		this.types.remove(range);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoRangeViewer)iterator.next()).removeRange(range);
		}
	}

	public void addState() throws EnoEclipseException, MatrixException {
		addState(new EnoTreeState("", this));
	}

	public void insertState(int index) throws EnoEclipseException, MatrixException {
		insertState(new EnoTreeState("", this), index);
	}

	public void addState(EnoTreeState newState) {
		this.states.add(newState);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext())
			((IEnoStateViewer)iterator.next()).addProperty(newState); 
	}

	public void insertState(EnoTreeState newState, int index) {
		this.states.add(index, newState);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoStateViewer)iterator.next()).insertProperty(newState, index); 
		}
	}

	public void removeState(EnoTreeState state) {
		if (this.states == null) {
			getStates(false);
		}
		this.states.remove(state);
		Iterator<IEnoBusinessViewer> iterator = this.changeListeners.iterator();
		while (iterator.hasNext()) {
			((IEnoStateViewer)iterator.next()).removeProperty(state);
		}
	}

	public String[] getStateNames(boolean forceRefresh) {
		ArrayList<EnoTreeState> states = getStates(forceRefresh);
		String[] retVal = new String[states.size()];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = ((EnoTreeState)states.get(i)).getName();
		}
		return retVal;
	}

	public static ArrayList<EnoTreeState> getStates(EnoTreePolicy policy) {
		ArrayList<EnoTreeState> retStates = new ArrayList<EnoTreeState>();
		try {
			Context context = getContext();
			MQLCommand command = new MQLCommand();

			command.executeCommand(context, MessageFormat.format(MQL_INFO_STATE, new Object[] { policy.getName() }));
			String[] t = command.getResult().split("\\|");
			for (int i = 0; i < t.length; i++) {
				String tname = t[i].trim();
				if (!tname.equals("")) {
					EnoTreeState state = new EnoTreeState(tname, policy);
					retStates.add(state);
				}
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return retStates;
	}

	public ArrayList<EnoTreeState> getStates(boolean forceRefresh) {
		if ((forceRefresh) || (this.states == null)) {
			this.states = getStates(this);
		}
		return this.states;
	}

	public void saveStates(Context context, MQLCommand command) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeState> oldStates = getStates(this);

		for (int i = 0; i < this.states.size(); i++) {
			if (!((EnoTreeState)this.states.get(i)).getOldName().equals("")) {
				continue;
			}
			boolean bFoundExistingState = false;
			String sFoundExistingState = null;
			for (int j = i + 1; j < this.states.size(); j++) {
				if (!((EnoTreeState)this.states.get(j)).getOldName().equals("")) {
					bFoundExistingState = true;
					sFoundExistingState = ((EnoTreeState)this.states.get(j)).getOldName();
					break;
				}
			}
			if (!bFoundExistingState) {
				command.executeCommand(context, MessageFormat.format(MQL_ADD_STATE, new Object[] { this.name, ((EnoTreeState)this.states.get(i)).getName() }));
			} else {
				command.executeCommand(context, MessageFormat.format(MQL_ADD_STATE_BEFORE, new Object[] { this.name, ((EnoTreeState)this.states.get(i)).getName(), sFoundExistingState }));
			}
			this.states.set(i, new EnoTreeState(((EnoTreeState)this.states.get(i)).getName(), this));
		}

		if (oldStates != null) {
			for (int i = 0; i < oldStates.size(); i++) {
				boolean bFound = false;
				EnoTreeState oldState = (EnoTreeState)oldStates.get(i);
				for (int j = 0; j < this.states.size(); j++) {
					EnoTreeState state = (EnoTreeState)this.states.get(j);

					if (oldState.getName().equals(state.getName())) {
						bFound = true;
						break;
					}
					if (oldState.getName().equals(state.getOldName())) {
						String newName = state.getName();
						state.setName(state.getOldName());
						state.refresh();
						state.setName(newName);
						state.save();
						bFound = true;
						break;
					}
				}
				if (bFound) {
					continue;
				}
				command.executeCommand(context, MessageFormat.format(MQL_REMOVE_STATE, new Object[] { this.name, ((EnoTreeState)oldStates.get(i)).getName() }));
			}
		}
	}

	public static EnoTreeStore getStore(EnoTreePolicy policy) {
		try {
			Context context = getContext();
			MQLCommand command = new MQLCommand();
			command.executeCommand(context, MessageFormat.format(MQL_INFO_STORE, new Object[] { policy.getName() }));
			String storeName = command.getResult().trim();
			if ((storeName != null) && (!storeName.equals(""))) {
				return new EnoTreeStore(storeName);
			}
			return null;
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
		return null;
	}

	public EnoTreeStore getStore(boolean forceRefresh) {
		if ((forceRefresh) || (this.store == null)) {
			this.store = getStore(this);
		}
		return this.store;
	}

	public static ArrayList<EnoTreePolicy> getAllPolicies(boolean refresh) throws MatrixException, EnoEclipseException {
		if ((refresh) || (allPolicies == null)) {
			PolicyList pl = Policy.getPolicies(getContext());
			allPolicies = new ArrayList<EnoTreePolicy>();
			PolicyItr pi = new PolicyItr(pl);
			while (pi.next()) {
				Policy p = pi.obj();
				EnoTreePolicy policy = new EnoTreePolicy(p.getName());
				allPolicies.add(policy);
			}
			Collections.sort(allPolicies);
		}
		return allPolicies;
	}

	public void save() {
		try {
			MQLCommand command = new MQLCommand();
			Context context = getContext();
			this.policy.open(context);
			try {
				String modString = "";
				String policyName = this.policy.getName();
				if (!policyName.equals(getName())) {
					modString = modString + " name " + getName();
				}
				if (!this.policy.getDescription(context).equals(getDescription())) {
					modString = modString + " description \"" + getDescription() + "\"";
				}
				if (!this.policy.getSequence().equals(getSequence())) {
					modString = modString + " sequence \"" + getSequence() + "\"";
				}

				EnoTreePolicy oldPolicy = new EnoTreePolicy(this.policy.getName());
				EnoTreeStore oldStore = getStore(oldPolicy);
				if (((oldStore == null) && (getStore(false) != null)) || ((oldStore != null) && (getStore(false) != null) && (!oldStore.equals(getSequence())))) {
					modString = modString + " store \"" + getStore(false).getName() + "\"";
				}
				if (!modString.equals("")) {
					command.executeCommand(context, "modify policy \"" + policyName + "\" " + modString + ";");
				}

				saveStates(context, command);

				refresh();
			} finally {
				this.policy.close(context);
			}
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
			this.children.addAll(getStates(false));
			this.children.addAll(getTypes(false));
		}
		return (EnoTreeBusiness[])this.children.toArray(new EnoTreeBusiness[this.children.size()]);
	}

	public static void clearCache() {
		allPolicies = null;
	}
}