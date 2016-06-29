package org.enoeclipse.enovia;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.enoeclipse.Activator;
import org.enoeclipse.utils.EnoEclipseUtils;

import matrix.db.Context;
import matrix.util.MatrixException;

public class EnoviaOperations implements IRunnableWithProgress {

	private String host;
	private String user;
	private String password;

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			monitor.beginTask("Connecting to ENOVIA Host " + this.host + " as user " + this.user, -1);
			Context context = new Context(this.host);
			context.setUser(this.user);
			context.setPassword(this.password);
			context.connect();
			while (!monitor.isCanceled()) {
				monitor.worked(-1);
				if (context.isConnected()) {
					Activator.getDefault().setContext(context);
					Activator.getDefault().setHost(this.host);
					Activator.getDefault().setUser(this.user);
					EnoEclipseUtils.triggerOnOff();
					break;
				}
			}
			monitor.done();
		} catch (MatrixException e) {
			throw new InvocationTargetException(e);
		}
	}
	
	public void login() throws MatrixException {
		Context context = new Context(this.host);
		context.setUser(this.user);
		context.setPassword(this.password);
		context.connect();
		if (context.isConnected()) {
			Activator.getDefault().setContext(context);
			EnoEclipseUtils.triggerOnOff();
		}
	}

}
