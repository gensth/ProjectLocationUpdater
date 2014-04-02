package com.github.eclipse.projectlocationupdater.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.github.eclipse.projectlocationupdater.i18n.Messages;

/**
 * A {@link WizardPage} to select the projects to update.
 *
 * @author Max Gensthaler
 */
public class PLUWizardProjectsPage extends WizardPage {
	private Table table;

	/** All available projects (input). */
	private final Collection<IProject> allProjects;
	/**
	 * The reselected projects (input). All elements must be contained in
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
	public PLUWizardProjectsPage(IProject[] allProjects, Collection<IProject> preselectedProjects) {
		super(Messages.wizard_projectsPage_page_name);
		setTitle(Messages.wizard_projectsPage_page_title);
		setDescription(Messages.wizard_projectsPage_page_description);

		this.allProjects = filterProjects(allProjects);
		this.preselectedProjects = Collections.unmodifiableSet(new HashSet<IProject>(preselectedProjects));
	}

	private static Collection<IProject> filterProjects(IProject[] projects) {
		ArrayList<IProject> result = new ArrayList<IProject>(projects.length);
		for (IProject project : projects) {
			if (!isProjectHidden(project)) {
				result.add(project);
			}
		}
		return result;
	}

	private static boolean isProjectHidden(IProject project) {
		// true, if this project lays in the workspace
		return project.getRawLocation() == null;
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.makeColumnsEqualWidth = false;
		comp.setLayout(layout);

		createProjectsTable(comp);
		createSelectButtons(comp);

		setControl(comp);
	}

	private void createProjectsTable(Composite parent) {
		// create the table
		table = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		table.setLayoutData(data);

		// set the table headers
		String[] titles = { Messages.wizard_projectsPage_projectTable_header_projectName, Messages.wizard_projectsPage_projectTable_header_reasonWhyDisabled };
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);
		}

		// fill the table contents
		Display display = Display.getCurrent();
		Color gray = display.getSystemColor(SWT.COLOR_GRAY);
		for (IProject project : allProjects) {
			String projectName = project.getName();
			String disabledReason = getDisabledReason(project);

			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, projectName);
			if (disabledReason == null) {
				item.setChecked(preselectedProjects.contains(project));
				item.setText(1, "-");
			} else {
				// disable this row
				item.setGrayed(true);
				item.setForeground(gray);
				item.setText(1, disabledReason);
			}
		}

		// disable selection of unselectable rows
		table.addListener(SWT.Selection, new DisableGrayedItemSelectionListener(table));

		// pack the columns
		for (int i = 0; i < titles.length; i++) {
			table.getColumn(i).pack();
		}
	}

	private static String getDisabledReason(IProject project) {
		if (project.isOpen()) {
			return Messages.wizard_projectsPage_projectTable_itemText_disabledReasonOpen;
		}
		return null;
	}

	private void createSelectButtons(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		comp.setLayout(new GridLayout(1, false));

		Button selAllButton = new Button(comp, SWT.PUSH);
		selAllButton.setText(Messages.wizard_projectsPage_projectTable_button_selectAll);
		selAllButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
		selAllButton.addSelectionListener(new TableSelectAllAdapter(table, true));

		Button deselAllButton = new Button(comp, SWT.PUSH);
		deselAllButton.setText(Messages.wizard_projectsPage_projectTable_button_deselectAll);
		deselAllButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
		deselAllButton.addSelectionListener(new TableSelectAllAdapter(table, false));

		comp.pack();
	}

	public Collection<IProject> getSelectedProjects() {
		Collection<IProject> result = new ArrayList<IProject>();
		for (TableItem item : table.getItems()) {
			if (!item.getChecked()) {
				continue;
			}
			IProject project = (IProject) item.getData();
			result.add(project);
			System.out.println("selected: " + project.getName());
		}
		return result;
	}

	private static class DisableGrayedItemSelectionListener implements Listener {
		private final Table table;

		public DisableGrayedItemSelectionListener(Table table) {
			this.table = table;
		}

		@Override
		public void handleEvent(Event evt) {
			// ignore non-check events
			if (evt.detail != SWT.CHECK) {
				return;
			}

			// if the row is not disabled, then handle events as normal
			// just disable event for disabled rows
			TableItem item = (TableItem) evt.item;
			if (!item.getGrayed()) {
				return;
			}

			// otherwise ... disable event
			evt.detail = SWT.NONE;
			evt.type = SWT.None;
			evt.doit = false;
			try {
				table.setRedraw(false);
				item.setChecked(!item.getChecked());
			} finally {
				table.setRedraw(true);
			}
		}
	}

	private static class TableSelectAllAdapter extends SelectionAdapter {
		private final Table table;
		private final boolean select;

		public TableSelectAllAdapter(Table table, boolean select) {
			this.table = table;
			this.select = select;
		}

		@Override
		public void widgetSelected(SelectionEvent evt) {
			for (TableItem item : table.getItems()) {
				// ignore disabled rows
				if (item.getGrayed()) {
					continue;
				}

				item.setChecked(select);
			}
		}
	}
}
