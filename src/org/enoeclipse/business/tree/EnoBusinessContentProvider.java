package org.enoeclipse.business.tree;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.enoeclipse.model.EnoFilter;
import org.enoeclipse.model.EnoTreeBusiness;

public class EnoBusinessContentProvider implements ITreeContentProvider {
	private EnoFilter filter;

	public Object[] getChildren(Object parentElement) {
		if ((parentElement != null) && ((parentElement instanceof EnoTreeBusiness))) {
			EnoTreeBusiness parentBusiness = (EnoTreeBusiness)parentElement;
			try {
				return parentBusiness.getChildren(false);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public Object getParent(Object element) {
		EnoTreeBusiness treeBusiness = (EnoTreeBusiness)element;
		return treeBusiness.getParent();
	}

	public boolean hasChildren(Object element) {
		EnoTreeBusiness treeBusinessObject = (EnoTreeBusiness)element;
		return treeBusinessObject.hasChildren();
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void setFilter(EnoFilter filter) {
		this.filter = filter;
	}

	public EnoFilter getFilter() {
		return this.filter;
	}
}