package com.github.eclipse.projectlocationupdater.actions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
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
	private final List<IProject> pProjects;

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

		// Copy the projects list
		pProjects = new LinkedList<IProject>(aProjects);

		// TODO: Compute the common path part
		pCommonPath = "toto";
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
		final Composite composite = (Composite) super.createDialogArea(aParent);

		// Prepare the layout
		final GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);

		// TODO: enhance the UI

		// TODO: show the list of the modified projects

		// Current path
		final Label currentPathLabel = new Label(aParent, SWT.NONE);
		currentPathLabel.setLayoutData(gridData);
		currentPathLabel.setText("Current path:");

		final Label currentPathText = new Label(aParent, SWT.BORDER);
		currentPathText.setLayoutData(gridData);
		currentPathText.setText(pCommonPath);

		// New path
		final Label newPathLabel = new Label(aParent, SWT.NONE);
		newPathLabel.setLayoutData(gridData);
		newPathLabel.setText("New path:");

		pNewPathText = new Text(aParent, SWT.BORDER);
		pNewPathText.setLayoutData(gridData);
		pNewPathText.setText(pCommonPath);

		// TODO: add folder selection buttons

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
