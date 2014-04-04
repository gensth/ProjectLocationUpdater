package com.github.eclipse.projectlocationupdater.actions;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.github.eclipse.projectlocationupdater.i18n.Messages;

/**
 * A {@link WizardPage} to enter the path to update.
 *
 * @author Max Gensthaler
 */
public class PLUWizardUpdatePage extends WizardPage {
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
//		comp.setLayout(new GridLayout(3, false));

		// TODO implement

		setControl(comp);
	}
}
