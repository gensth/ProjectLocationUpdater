package com.github.eclipse.projectlocationupdater.actions;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * A {@link Wizard} for selecting projects and updating their location.
 *
 * @author Max Gensthaler
 */
public class PLUWizard extends Wizard {
	private PLUWizardProjectsPage projectsPage;
	private PLUWizardUpdatePage updatePage;

	/** All available projects (input, unmodifiable). */
	private final IProject[] allProjects;
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
	public PLUWizard(IProject[] allProjects, Collection<IProject> preselectedProjects) {
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
			updatePage.setProjectsToUpdate(selectedProjects);
			return updatePage;
		}
		return null;
	}

	@Override
	public boolean performFinish() {
		// TODO implement
		return false;
	}
}
