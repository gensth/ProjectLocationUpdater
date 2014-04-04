package com.github.eclipse.projectlocationupdater.actions;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.github.eclipse.projectlocationupdater.i18n.Messages;

/**
 * A {@link WizardPage} to enter the path to update.
 *
 * @author Max Gensthaler
 */
public class PLUWizardUpdatePage extends WizardPage {
	private Text currentLocationText;
	private Text newLocationText;

	/**
	 * Creates a new instance of this class.
	 */
	public PLUWizardUpdatePage() {
		super(Messages.wizard_updatePage_page_name);
		setTitle(Messages.wizard_updatePage_page_title);
		setDescription(Messages.wizard_updatePage_page_description);
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(3, false));

		createCurrentLocation(comp);
		createNewLocation(comp);

		setControl(comp);
	}

	private void createCurrentLocation(Composite composite) {
		Label currentLocationLabel = new Label(composite, SWT.NONE);
		currentLocationLabel.setText(Messages.proppage_currentLocation);

		currentLocationText = new Text(composite, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gd.widthHint = 40;
		currentLocationText.setLayoutData(gd);

		Display display = Display.getCurrent();
		Color gray = display.getSystemColor(SWT.COLOR_DARK_GRAY);
		currentLocationText.setForeground(gray);
	}

	private void createNewLocation(final Composite composite) {
		Label newLocationLabel = new Label(composite, SWT.NONE);
		newLocationLabel.setText(Messages.proppage_newLocation);

		newLocationText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd.widthHint = 40;
		newLocationText.setLayoutData(gd);
		newLocationText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				PLUWizardUpdatePage.this.setPageComplete(!newLocationText.getText().isEmpty());
			}
		});

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

	public void setPreviousPath(String previousPath) {
		// init the text fields
		if (currentLocationText != null) {
			currentLocationText.setText(previousPath);
		}
		if (newLocationText != null) {
			newLocationText.setText(previousPath);
		}
	}
}
