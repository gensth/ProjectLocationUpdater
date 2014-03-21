package com.github.eclipse.projectlocationupdater.actions;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Requests the new location to the user
 * 
 * @author Thomas Calmant
 */
public class LocationUpdateDialog extends TitleAreaDialog {

	/** Current common path part (read only) */
	private final String pCommonPath;

	/** New common path part */
	private Text pNewPathText;

	/** The modified project */
	private final Collection<IProject> pProjects;

	/** Workspace root path */
	private final String pWorkspaceRoot;

	/**
	 * Instantiate a new location update dialog
	 * 
	 * @param aParentShell
	 *            The parent shell
	 * @param aProjects
	 *            Projects that will be configured
	 */
	public LocationUpdateDialog(final Shell aParentShell,
			final Collection<IProject> aProjects) {

		super(aParentShell);

		// Get the workspace root
		pWorkspaceRoot = aProjects.iterator().next().getWorkspace().getRoot()
				.getLocation().toString();

		// Copy the projects list
		pProjects = new LinkedList<IProject>(aProjects);

		// TODO: Compute the common path part
		pCommonPath = "toto";
	}

	/**
	 * Shows the common part of the project locations
	 * 
	 * @param aComposite
	 *            Parent composite
	 */
	private void addCommonLocation(final Composite aComposite) {

		final Label currentLocationLabel = new Label(aComposite, SWT.NONE);
		currentLocationLabel.setText("Common part of locations:");

		final Text currentLocationText = new Text(aComposite, SWT.SINGLE
				| SWT.READ_ONLY | SWT.BORDER);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gd.widthHint = 40;
		currentLocationText.setLayoutData(gd);
		currentLocationText.setText(pCommonPath);
	}

	/**
	 * Lets the user enter the new path
	 * 
	 * @param aComposite
	 *            Parent composite
	 */
	private void addNewLocation(final Composite aComposite) {
		final Label newLocationLabel = new Label(aComposite, SWT.NONE);
		newLocationLabel.setText("New location:");

		pNewPathText = new Text(aComposite, SWT.SINGLE | SWT.BORDER);
		final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1);
		gd.widthHint = 40;
		pNewPathText.setLayoutData(gd);
		pNewPathText.setText(pCommonPath);

		final Button browseButton = new Button(aComposite, SWT.NONE);
		browseButton.setText("...");
		browseButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// Do nothing
			}

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final DirectoryDialog dd = new DirectoryDialog(aComposite
						.getShell(), SWT.OPEN);
				dd.setFilterPath(pWorkspaceRoot);
				final String selected = dd.open();
				pNewPathText.setText(selected);
			}
		});
	}

	/**
	 * Shows the list of selected projects
	 * 
	 * @param aComposite
	 *            Parent composite
	 */
	private void addProjectsList(final Composite aComposite) {

		// Make an array of projects names
		int i = 0;
		final String[] projectNames = new String[pProjects.size()];
		for (final IProject project : pProjects) {
			projectNames[i++] = project.getName();
		}

		// Sort it
		Arrays.sort(projectNames);

		// Make the list widget
		final List projectsList = new List(aComposite, SWT.V_SCROLL);
		for (final String projectName : projectNames) {
			projectsList.add(projectName);
		}

		// Let it have some space
		final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 3;
		projectsList.setLayoutData(gridData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.
	 * swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(final Composite aParent) {

		// Call parent
		final Control content = super.createContents(aParent);

		// Set the title
		setTitle("Project location updater");

		// Set the message
		setMessage("Update the location of " + pProjects.size() + " project(s)");
		return content;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(final Composite aParent) {

		// Call parent
		final Composite dialogArea = (Composite) super
				.createDialogArea(aParent);

		// Prepare the layout
		final Composite composite = new Composite(dialogArea, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(3, false));

		// Show the list of the modified projects
		addProjectsList(composite);

		// Current path
		addCommonLocation(composite);

		// New path
		addNewLocation(composite);
		return composite;
	}

	/**
	 * Returns a couple of Strings: the common part of the current path to be
	 * replaced and its replacement
	 * 
	 * @return A 2-elements array
	 */
	public String[] getPathModification() {

		return new String[] { pCommonPath, pNewPathText.getText() };
	}
}
