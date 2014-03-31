package com.github.eclipse.projectlocationupdater.properties;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import com.github.eclipse.projectlocationupdater.LocationUpdater;
import com.github.eclipse.projectlocationupdater.i18n.Messages;

/**
 * Property page for closed projects to update the project location.
 * <p>
 * Background:<br/>
 * Eclipse uses absolute paths for projects within a workspace.<br/>
 * With this property page you can change the absolute project path.
 * <p>
 * The .location file is written by the eclipse platform at
 * {@link org.eclipse.core.internal.resources.LocalMetaArea#writePrivateDescription(org.eclipse.core.resources.IProject)}.
 *
 * @author Max Gensthaler
 */
@SuppressWarnings("restriction")
public class ProjectLocationUpdaterPropertyPage extends PropertyPage {
	private static final String USAGE = Messages.proppage_usage;
	private static final String CURRENT_LOCATION = Messages.proppage_currentLocation;
	private static final String NEW_LOCATION = Messages.proppage_newLocation;
	private static final String BROWSE_TEXT = Messages.proppage_browse;
	private static final String PROJECT_OPEN_WARNING = Messages.proppage_projectOpenWarning;

	private IProject myProject;

	private Text currentLocationText;
	private Text newLocationText;

	public ProjectLocationUpdaterPropertyPage() {
		super();
	}

	private IProject getMyProject() {
		if (myProject == null) {
			myProject = ((IResource) getElement()).getProject();
		}
		return myProject;
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		addUsage(composite);
		if (((IResource) getElement()).getProject().isOpen()) {
			addProjectOpenWarning(composite);
		}
		addSeparatorLabel(composite);
		addCurrentLocation(composite);
		addNewLocation(composite);
		return composite;
	}

	private void addSeparatorLabel(Composite composite) {
		Label separatorLabel = new Label(composite, SWT.NONE); // separator label
		separatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
	}

	private void addUsage(Composite composite) {
		Label usageLabel = new Label(composite, SWT.NONE);
		usageLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		usageLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		usageLabel.setText(USAGE);
	}

	private void addProjectOpenWarning(Composite composite) {
		Label warningLabel = new Label(composite, SWT.NONE);
		warningLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		// warningLabel.setImage(JFaceResources.getImageRegistry().getDescriptor("org.eclipse.jface.fieldassist.IMG_DEC_FIELD_WARNING").createImage());
		warningLabel.setText(PROJECT_OPEN_WARNING);
	}

	private void addCurrentLocation(Composite composite) {
		Label currentLocationLabel = new Label(composite, SWT.NONE);
		currentLocationLabel.setText(CURRENT_LOCATION);

		String currentLocation;
		try {
			currentLocation = LocationUpdater.readProjectLocation(getMyProject());
		} catch (IOException e) {
			currentLocation = ((IResource) getElement()).getProject().getLocation().toString();
		}
		currentLocationText = new Text(composite, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gd.widthHint = 40;
		currentLocationText.setLayoutData(gd);
		currentLocationText.setText(currentLocation);
	}

	private void addNewLocation(final Composite composite) {
		Label newLocationLabel = new Label(composite, SWT.NONE);
		newLocationLabel.setText(NEW_LOCATION);

		newLocationText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd.widthHint = 40;
		newLocationText.setLayoutData(gd);
		newLocationText.setText(currentLocationText.getText());

		final String workspaceLocation = ((IResource) getElement()).getWorkspace().getRoot().getLocation().toString();
		Button browseButton = new Button(composite, SWT.NONE);
		browseButton.setText(BROWSE_TEXT);
		browseButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dd = new DirectoryDialog(composite.getShell(), SWT.OPEN);
				dd.setFilterPath(workspaceLocation);
				String selected = dd.open();
				newLocationText.setText(selected);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				//
			}
		});
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		newLocationText.setText(currentLocationText.getText());
	}

	@Override
	public boolean performOk() {
		Path currentLocationPath = new Path(currentLocationText.getText());
		Path newLocationPath = new Path(newLocationText.getText());
		if (newLocationPath.equals(currentLocationPath)) {
			// nothing to do, nothing changed
		} else {
			try {
				LocationUpdater.writeProjectLocation(getMyProject(), newLocationPath);
			} catch (IOException e) {
				MessageDialog.openError(getShell(), Messages.errorDialog_title, Messages.errorDialog_errorOnApplyPrefix + e.getMessage());
				throw new RuntimeException("Error in " + ProjectLocationUpdaterPropertyPage.class.getName(), e); //$NON-NLS-1$
				// return false;
			}
		}
		return true;
	}
}