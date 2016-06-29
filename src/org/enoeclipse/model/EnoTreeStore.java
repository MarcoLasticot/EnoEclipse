package org.enoeclipse.model;

import java.util.ArrayList;
import java.util.Collections;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import matrix.db.Context;
import matrix.db.Store;
import matrix.db.StoreItr;
import matrix.db.StoreList;
import matrix.util.MatrixException;


public class EnoTreeStore extends EnoTreeBusiness {
	Store store;
	private static ArrayList<EnoTreeStore> allStores;
	protected String description;

	public EnoTreeStore(String name) throws EnoEclipseException, MatrixException {
		super("Vault", name);
		this.store = new Store(name);
	}

	public void refresh() throws EnoEclipseException, MatrixException {
		super.refresh();
		this.store = new Store(getName());
		fillBasics();
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void fillBasics() {
		try {
			Context context = getContext();
			this.store.open(context);
			try {
				this.name = this.store.getName();
				this.description = this.store.getDescription(context);
			} finally {
				this.store.close(context);
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public static ArrayList<EnoTreeStore> getAllStores(boolean refresh) throws MatrixException, EnoEclipseException {
		if ((refresh) || (allStores == null)) {
			Context context = getContext();
			StoreList vl = Store.getStores(context, true);
			allStores = new ArrayList<EnoTreeStore>();
			StoreItr pi = new StoreItr(vl);
			while (pi.next()) {
				Store p = pi.obj();
				EnoTreeStore vault = new EnoTreeStore(p.getName());
				allStores.add(vault);
			}
			Collections.sort(allStores);
		}
		return allStores;
	}

	public static String[] getAllVaultNames(boolean refresh) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeStore> allTypes = getAllStores(refresh);

		String[] retStore = new String[allTypes.size()];
		for (int i = 0; i < retStore.length; i++) {
			retStore[i] = ((EnoTreeStore)allTypes.get(i)).getName();
		}
		return retStore;
	}

	public void save() {
	}

	public static void clearCache() {
		allStores = null;
	}
}