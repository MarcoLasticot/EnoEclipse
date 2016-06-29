package org.enoeclipse.model;

import java.util.ArrayList;
import java.util.Collections;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import matrix.db.Context;
import matrix.db.Vault;
import matrix.db.VaultItr;
import matrix.db.VaultList;
import matrix.util.MatrixException;

public class EnoTreeVault extends EnoTreeBusiness {
	Vault vault;
	private static ArrayList<EnoTreeVault> allVaults;
	protected String description;

	public EnoTreeVault(String name) throws EnoEclipseException, MatrixException {
		super("Vault", name);
		this.vault = new Vault(name);
	}

	public void refresh() throws EnoEclipseException, MatrixException {
		super.refresh();
		this.vault = new Vault(getName());
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
			this.vault.open(context);
			try {
				this.name = this.vault.getName();
				this.description = this.vault.getDescription(context);
			} finally {
				this.vault.close(context);
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public static ArrayList<EnoTreeVault> getAllVaults(boolean refresh) throws MatrixException, EnoEclipseException {
		if ((refresh) || (allVaults == null)) {
			Context context = getContext();
			VaultList vl = Vault.getVaults(context, true);
			allVaults = new ArrayList<EnoTreeVault>();
			VaultItr pi = new VaultItr(vl);
			while (pi.next()) {
				Vault p = pi.obj();
				EnoTreeVault vault = new EnoTreeVault(p.getName());
				allVaults.add(vault);
			}
			Collections.sort(allVaults);
		}
		return allVaults;
	}

	public static String[] getAllVaultNames(boolean refresh) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeVault> allTypes = getAllVaults(refresh);

		String[] retVal = new String[allTypes.size()];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = ((EnoTreeVault)allTypes.get(i)).getName();
		}
		return retVal;
	}

	public void save() {
	}

	public static void clearCache() {
		allVaults = null;
	}
}