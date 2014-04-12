package com.github.eclipse.projectlocationupdater.actions;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.github.eclipse.projectlocationupdater.i18n.Messages;

/**
 * A {@link WizardPage} to enter the path to update.
 *
 * @author Max Gensthaler
 */
public class PLUWizardUpdatePage extends WizardPage {
	private Text previousLocationText;
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

		createPreviousLocation(comp);
		createNewLocation(comp);

		setControl(comp);
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
		previousLocationText.addListener(SWT.Traverse, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				if (evt.detail == SWT.TRAVERSE_RETURN) {
					if (newLocationText.setFocus()) {
						selectNewLocation();
					}
				}
			}
		});
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
				updatePageComplete();
			}
		});
		newLocationText.addListener(SWT.Traverse, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				if (evt.detail == SWT.TRAVERSE_RETURN) {
					selectNewLocation();
				}
			}
		});

		Button browseButton = new Button(composite, SWT.NONE);
		browseButton.setText(Messages.proppage_browse);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				selectNewLocation();
			}
		});
	}

	private void updatePageComplete() {
		String previousLocation = previousLocationText.getText();
		assert !previousLocation.isEmpty();
		String newLocation = newLocationText.getText();
		boolean newLocationSet = !newLocation.isEmpty() && !newLocation.equals(previousLocation);
		setPageComplete(newLocationSet);
	}

	private void selectNewLocation() {
		DirectoryDialog dd = new DirectoryDialog(getShell(), SWT.OPEN);

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

	public void setPreviousLocation(String previousLocation) {
		// init the text fields
		if (previousLocationText != null) {
			previousLocationText.setText(previousLocation);
		}
		if (newLocationText != null) {
			newLocationText.setText(previousLocation);
		}
	}

	public String getNewLocation() {
		String newLocation = newLocationText.getText();
		assert !newLocation.isEmpty();
		return newLocation;
	}
}
