package com.github.eclipse.projectlocationupdater.actions;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import com.github.eclipse.projectlocationupdater.i18n.Messages;

/**
 * A {@link WizardPage} to enter the path to update.
 *
 * @author Max Gensthaler
 */
public class PLUWizardUpdatePage extends WizardPage {
	/** The projects to update by this page. */
	private Collection<IProject> projectsToUpdate;

	private List projectsList;

	/**
	 * Creates a new instance of this class.
	 */
	public PLUWizardUpdatePage() {
		super(Messages.wizard_updatePage_page_name);
		setTitle(Messages.wizard_updatePage_page_title);
		setDescription(Messages.wizard_updatePage_page_description);
	}

	public void setProjectsToUpdate(Collection<IProject> projectsToUpdate) {
		this.projectsToUpdate = projectsToUpdate;
		String[] projectNames = getProjectNames(projectsToUpdate);
		updateProjectsList(projectNames);
	}

	private static String[] getProjectNames(Collection<IProject> projects) {
		// make an array of projects names
		int i = 0;
		String[] projectNames = new String[projects.size()];
		for (IProject project : projects) {
			projectNames[i++] = project.getName();
		}

		// sort them
		Arrays.sort(projectNames);

		return projectNames;
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
//		comp.setLayout(new GridLayout(3, false));

		createProjectsList(comp);

		setControl(comp);
	}

	/**
	 * Creates a list of the projects to update.
	 *
	 * @param parent
	 *            Parent composite.
	 */
	private void createProjectsList(final Composite parent) {
		// make the list widget
		projectsList = new List(parent, SWT.V_SCROLL);

		// let it have some space
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 3;
		projectsList.setLayoutData(gridData);
	}

	private void updateProjectsList(String[] projectNames) {
		projectsList.removeAll();
		for (String projectName : projectNames) {
			projectsList.add(projectName);
		}
	}
}
