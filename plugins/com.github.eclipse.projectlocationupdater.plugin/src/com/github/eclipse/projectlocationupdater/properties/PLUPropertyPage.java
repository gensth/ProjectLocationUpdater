package com.github.eclipse.projectlocationupdater.properties;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
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
public class PLUPropertyPage extends PropertyPage {
	private IProject myProject;

	private Text previousLocationText;
	private Text newLocationText;

	public PLUPropertyPage() {
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

		createUsage(composite);
		if (((IResource) getElement()).getProject().isOpen()) {
			addProjectOpenWarning(composite);
		}
		createSeparatorLabel(composite);
		createPreviousLocation(composite);
		createNewLocation(composite);
		return composite;
	}

	private void createSeparatorLabel(Composite composite) {
		Label separatorLabel = new Label(composite, SWT.NONE); // separator label
		separatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
	}

	private void createUsage(Composite composite) {
		Label usageLabel = new Label(composite, SWT.NONE);
		usageLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		usageLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		usageLabel.setText(Messages.proppage_usage);
	}

	private void addProjectOpenWarning(Composite composite) {
		Label warningLabel = new Label(composite, SWT.NONE);
		warningLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		// warningLabel.setImage(JFaceResources.getImageRegistry().getDescriptor("org.eclipse.jface.fieldassist.IMG_DEC_FIELD_WARNING").createImage());
		warningLabel.setText(Messages.proppage_projectOpenWarning);
	}

	private void createPreviousLocation(Composite composite) {
		Label previousLocationLabel = new Label(composite, SWT.NONE);
		previousLocationLabel.setText(Messages.proppage_previousLocation);

		previousLocationText = new Text(composite, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gd.widthHint = 40;
		previousLocationText.setLayoutData(gd);

		Display display = Display.getCurrent();
		Color gray = display.getSystemColor(SWT.COLOR_DARK_GRAY);
		previousLocationText.setForeground(gray);

		String previousLocation;
		try {
			previousLocation = LocationUpdater.readProjectLocation(getMyProject());
		} catch (IOException e) {
			previousLocation = ((IResource) getElement()).getProject().getLocation().toString();
		}
		previousLocationText.setText(previousLocation);
	}

	private void createNewLocation(final Composite composite) {
		Label newLocationLabel = new Label(composite, SWT.NONE);
		newLocationLabel.setText(Messages.proppage_newLocation);

		newLocationText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd.widthHint = 40;
		newLocationText.setLayoutData(gd);
		newLocationText.setText(previousLocationText.getText());

		Button browseButton = new Button(composite, SWT.NONE);
		browseButton.setText(Messages.proppage_browse);
		browseButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dd = new DirectoryDialog(composite.getShell(), SWT.OPEN);

				String location = newLocationText.getText();
				if (!location.isEmpty() && new Path(location).toFile().isDirectory()) {
					dd.setFilterPath(location);
				} else {
					String workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
					dd.setFilterPath(workspaceLocation);
				}

				String selected = dd.open();
				if (selected != null) {
					newLocationText.setText(selected);
				}
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
		newLocationText.setText(previousLocationText.getText());
	}

	@Override
	public boolean performOk() {
		Path previousLocationPath = new Path(previousLocationText.getText());
		Path newLocationPath = new Path(newLocationText.getText());
		if (newLocationPath.equals(previousLocationPath)) {
			// nothing to do, nothing changed
		} else {
			try {
				LocationUpdater.writeProjectLocation(getMyProject(), newLocationPath);
			} catch (IOException e) {
				MessageDialog.openError(getShell(), Messages.errorDialog_title, Messages.errorDialog_errorOnApplyPrefix + e.getMessage());
				throw new RuntimeException("Error in " + PLUPropertyPage.class.getName(), e); //$NON-NLS-1$
				// return false;
			}
		}
		return true;
	}
}