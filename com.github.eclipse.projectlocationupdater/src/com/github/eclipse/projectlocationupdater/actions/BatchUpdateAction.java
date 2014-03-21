package com.github.eclipse.projectlocationupdater.actions;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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

	/** The location updater */
	private final LocationUpdater pLocationUpdater = new LocationUpdater();

	/** Current selection */
	private ISelection pSelection;

	/**
	 * Returns the common part of the locations of the given projects
	 * 
	 * @param aProjects
	 *            A list of closed projects to relocate
	 * @return THe common part of the path to projects (or an empty string)
	 */
	private String getCommonPath(final Collection<IProject> aProjects) {

		if (aProjects.size() == 1) {
			// Only one element...
			try {
				return pLocationUpdater.readProjectLocation(aProjects
						.iterator().next());
			} catch (final IOException e) {
				// TODO Log it
				e.printStackTrace();
				return "";
			}
		}

		// Make an array of projects names
		final Collection<String> projectLocations = new LinkedList<String>();
		for (final IProject project : aProjects) {
			try {
				projectLocations.add(pLocationUpdater
						.readProjectLocation(project));
			} catch (final IOException e) {
				// TODO Log it
				e.printStackTrace();
			}
		}

		// Sort it
		final String[] locationsStr = projectLocations.toArray(new String[0]);
		Arrays.sort(locationsStr);

		// Common part can be determined by first and last elements of the array
		return getCommonPathPrefix(locationsStr[0],
				locationsStr[locationsStr.length - 1]);
	}

	/**
	 * Returns the longest common part of the given two strings
	 * 
	 * @param aString
	 *            A string
	 * @param aOther
	 *            Another string
	 * @return The common part of the strings
	 */
	private String getCommonPathPrefix(final String aString, final String aOther) {

		// Compute the common part
		final IPath path = new Path(aString);
		final IPath otherPath = new Path(aOther);
		final int matchingSegments = path.matchingFirstSegments(otherPath);

		// Make the common path
		return path.removeLastSegments(path.segmentCount() - matchingSegments)
				.toString();
	}

	/**
	 * Computes the list of the projects to update. They must be closed to be
	 * selected.
	 * 
	 * @return The projects to update
	 */
	private Collection<IProject> getSelectedProjects() {

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

		return selectedProjects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(final IAction aAction) {

		// Prepare selection list
		final Collection<IProject> selectedProjects = getSelectedProjects();
		if (selectedProjects.isEmpty()) {
			// No valid project found, do nothing
			// TODO: log/trace it
			return;
		}

		// Compute the common path part
		final String commonPath = getCommonPath(selectedProjects);
		if (commonPath.isEmpty()) {
			// No common path
			// TODO: open a dialog and log it
			return;
		}

		// Get the shell
		final Shell shell = Activator.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getShell();

		final LocationUpdateDialog dialog = new LocationUpdateDialog(shell,
				selectedProjects, commonPath);
		if (dialog.open() != Window.OK) {
			// User click CANCEL: do nothing
			return;
		}

		// Get the common part and the new path
		final String newPath = dialog.getNewLocation();
		if (newPath.equals(commonPath)) {
			// Nothing to do
			return;
		}

		// Update project
		for (final IProject project : selectedProjects) {
			try {
				pLocationUpdater.updateLocationSubstring(project, commonPath,
						newPath);

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
