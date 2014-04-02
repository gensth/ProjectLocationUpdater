package com.github.eclipse.projectlocationupdater.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.github.eclipse.projectlocationupdater.Activator;

/**
 * An action to run the {@link PLUWizard}.
 *
 * @author Max Gensthaler
 */
public class PLUWizardAction implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {
	private ISelection selection;

	@Override
	public void init(IWorkbenchWindow paramIWorkbenchWindow) {
		// nothing to do
	}

	@Override
	public void dispose() {
		// nothing to do
	}

	/**
	 * Computes the list of the projects to update. They must be closed to be
	 * selected.
	 *
	 * @return The projects to update
	 * @author Thomas Calmant
	 */
	private Collection<IProject> getSelectedProjects() {
        final Collection<IProject> selectedProjects = new ArrayList<IProject>();
        if (selection instanceof IStructuredSelection) {
            for (final Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				final Object element = it.next();
				IProject project = null;

				if (element instanceof IProject) {
					// Is the element a project ?
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					// Is the element adaptable to a project ?
					project = (IProject) ((IAdaptable) element)
							.getAdapter(IProject.class);
				}

				if (project != null) {
					// Add the found project, if it is closed
					selectedProjects.add(project);
				}
			}
		}

        return selectedProjects;
	}

	@Override
	public void run(IAction action) {
		// get the shell
		IWorkbench workbench = Activator.getDefault().getWorkbench();
		Shell shell = workbench.getActiveWorkbenchWindow().getShell();

		// collect the projects
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		Collection<IProject> preselectedProjects = getSelectedProjects();

		// create and open the wizard
		PLUWizard wizard = new PLUWizard(allProjects, preselectedProjects);
		WizardDialog wizardDialog = new WizardDialog(shell, wizard);
		if (wizardDialog.open() == Window.OK) {
			System.out.println("Ok pressed");
		} else {
			System.out.println("Cancel pressed");
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActivePart(IAction arg0, IWorkbenchPart arg1) {
		// nothing to do
	}
}
