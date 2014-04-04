package com.github.eclipse.projectlocationupdater.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import com.github.eclipse.projectlocationupdater.LocationUpdater;
import com.github.eclipse.projectlocationupdater.i18n.Messages;

/**
 * A {@link Wizard} for selecting projects and updating their location.
 *
 * @author Max Gensthaler
 */
public class PLUWizard extends Wizard {
	private PLUWizardProjectsPage projectsPage;
	private PLUWizardUpdatePage updatePage;

	/** All available projects (input, unmodifiable). */
	private final Collection<IProject> allProjects;
	/**
	 * The reselected projects (input, unmodifiable). All elements must be contained in
	 * {@link #allProjects}.
	 */
	private final Collection<IProject> preselectedProjects;

	/**
	 * Creates a new instance of this class.
	 *
	 * @param allProjects
	 *            All available projects (input, unmodifiable).
	 * @param preselectedProjects
	 *            The reselected projects (input, unmodifiable). All elements must be contained in
	 *            {@link #allProjects}.
	 */
	public PLUWizard(Collection<IProject> allProjects, Collection<IProject> preselectedProjects) {
		super();
		setNeedsProgressMonitor(true);

		this.allProjects = allProjects;
		this.preselectedProjects = preselectedProjects;
	}

	@Override
	public void addPages() {
		projectsPage = new PLUWizardProjectsPage(allProjects, preselectedProjects);
		addPage(projectsPage);
		updatePage = new PLUWizardUpdatePage();
		addPage(updatePage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == projectsPage) {
			Collection<IProject> selectedProjects = projectsPage.getSelectedProjects();
			String pathToUpdate = getPathToUpdate(selectedProjects);
			updatePage.setPreviousPath(pathToUpdate);
			return updatePage;
		}
		return null;
	}

	@Override
	public boolean performFinish() {
		// TODO implement
		return false;
	}

	/**
	 * Determines the path to update from the selected project(s), either the full path of a single
	 * selected project or the longest common path prefix of multiple selected projects.
	 *
	 * @param projects
	 *            The projects which were selected from the user to update.
	 * @return
	 */
	private String getPathToUpdate(Collection<IProject> projects) {
		assert !projects.isEmpty();
		if (projects.size() == 1) {
			return getProjectLocation(projects.iterator().next());
		}
		return getCommonPathPrefix(getProjectLocations(projects));
	}

	/**
	 * Retrieve the location of the given project.
	 *
	 * @param project
	 *            The project to retrieve the location for.
	 * @return The absolute path to the project.
	 */
	private String getProjectLocation(IProject project) {
		try {
			return LocationUpdater.readProjectLocation(project);
		} catch (IOException e) {
			MessageDialog.openError(getShell(), Messages.errorDialog_title, Messages.errorDialog_errorReadProjLocPrefix + e.getMessage());
			throw new RuntimeException("Error in " + PLUWizard.class.getName(), e); //$NON-NLS-1$
		}
	}

	/**
	 * Retrieve the locations of the given projects.
	 *
	 * @param projects
	 *            The projects to retrieve the locations for.
	 * @return The absolute paths to the projects.
	 */
	private Collection<String> getProjectLocations(Collection<IProject> projects) {
        final Collection<String> projectLocations = new ArrayList<String>();
        for (final IProject project : projects) {
        	projectLocations.add(getProjectLocation(project));
		}
        return projectLocations;
	}

	/**
	 * Returns the longest common prefix of the given paths.
	 *
     * @param paths
	 *            The paths to find the longest common prefix for.
	 * @return The common part of the paths.
	 */
	private static String getCommonPathPrefix(Collection<String> paths) {
		assert !paths.isEmpty();

		// the common prefix can be determined by first and last elements of the sorted array
		String[] pathsArr = paths.toArray(new String[0]);
		Arrays.sort(pathsArr);

        String firstPath = pathsArr[0];
        String lastPath = pathsArr[pathsArr.length - 1];

		return getCommonPathPrefix(firstPath, lastPath);
	}

	/**
	 * Returns the longest common prefix of the given two paths.
	 *
     * @param firstPath
	 *            The first path.
     * @param secondPath
	 *            The second path.
	 * @return The common part of the paths.
	 */
    private static String getCommonPathPrefix(String firstPath, String secondPath) {
		// Compute the common part
        IPath path = new Path(firstPath);
        IPath otherPath = new Path(secondPath);
		int matchingSegments = path.matchingFirstSegments(otherPath);

		// Make the common path
        int segmentCountToRemove = path.segmentCount() - matchingSegments;
        return path.removeLastSegments(segmentCountToRemove).toString();
	}
}
