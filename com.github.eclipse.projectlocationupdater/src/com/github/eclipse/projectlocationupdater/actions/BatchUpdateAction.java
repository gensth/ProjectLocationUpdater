package com.github.eclipse.projectlocationupdater.actions;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.github.eclipse.projectlocationupdater.Activator;
import com.github.eclipse.projectlocationupdater.LocationUpdater;

/**
 * Action associated to the "Update Location" pop-up menu
 * 
 * @author Thomas Calmant
 */
public class BatchUpdateAction implements IObjectActionDelegate {

	/** Current selection */
	private ISelection pSelection;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(final IAction aAction) {

		// Prepare selection list
		final Set<IProject> selectedProjects = new LinkedHashSet<IProject>();
		if (pSelection instanceof IStructuredSelection) {
			for (final Iterator<?> it = ((IStructuredSelection) pSelection)
					.iterator(); it.hasNext();) {

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

				if (project != null && !project.isOpen()) {
					// Add the found project, if it is closed
					selectedProjects.add(project);
				}
			}
		}

		if (selectedProjects.isEmpty()) {
			// No valid project found, do nothing
			return;
		}

		// Get the shell
		final Shell shell = Activator.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getShell();

		final LocationUpdateDialog dialog = new LocationUpdateDialog(shell,
				selectedProjects);
		if (dialog.open() != Window.OK) {
			// User click CANCEL: do nothing
			return;
		}

		// Get the common part and the new path
		final String[] results = dialog.getPathModification();
		final String commonPart = results[0];
		final String newPath = results[1];

		// Update project
		final LocationUpdater updater = new LocationUpdater();
		for (final IProject project : selectedProjects) {
			try {
				updater.updateLocationSubstring(project, commonPart, newPath);

			} catch (final IOException ex) {
				// TODO Use a logger
				System.err.println("Error updating the location file: " + ex);
				ex.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
	 * .IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(final IAction aAction,
			final ISelection aSelection) {

		pSelection = aSelection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.
	 * action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(final IAction aAction,
			final IWorkbenchPart aTargetPart) {
		// Nothing to do
	}
}
