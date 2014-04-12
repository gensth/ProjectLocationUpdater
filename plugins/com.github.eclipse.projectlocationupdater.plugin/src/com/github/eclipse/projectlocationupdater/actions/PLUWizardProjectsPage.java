package com.github.eclipse.projectlocationupdater.actions;

import static com.github.eclipse.projectlocationupdater.utils.ListUtil.filterToList;
import static com.github.eclipse.projectlocationupdater.utils.ListUtil.filterToSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
import com.github.eclipse.projectlocationupdater.utils.Predicate;

/**
 * A {@link WizardPage} to select the projects to update.
 *
 * @author Max Gensthaler
 */
public class PLUWizardProjectsPage extends WizardPage {
	private static final Comparator<IProject> PROJECT_NAME_COMPARATOR = new Comparator<IProject>() {
		@Override
		public int compare(IProject a, IProject b) {
			return a.getName().compareTo(b.getName());
		}
	};
	private static final Predicate<IProject> PROJECT_VISIBLE_PREDICATE = new Predicate<IProject>() {
		@Override
		public boolean apply(IProject project) {
			// true, if this project lays outside of the workspace
			return project.getRawLocation() != null;
		}
	};

	private TableViewer tableViewer;

	/** All available projects (input). */
	private final List<IProject> allProjects;
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
	public PLUWizardProjectsPage(Collection<IProject> allProjects, Collection<IProject> preselectedProjects) {
		super(Messages.wizard_projectsPage_page_name);
		setTitle(Messages.wizard_projectsPage_page_title);
		setDescription(Messages.wizard_projectsPage_page_description);

		List<IProject> allProjectsList = filterToList(allProjects, PROJECT_VISIBLE_PREDICATE);
		Collections.sort(allProjectsList, PROJECT_NAME_COMPARATOR);
		this.allProjects = allProjectsList;

		this.preselectedProjects = filterToSet(preselectedProjects, PROJECT_VISIBLE_PREDICATE);
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

		updatePageComplete();
	}

	private void updatePageComplete() {
		boolean anyItemChecked = false;
		for (TableItem item : tableViewer.getTable().getItems()) {
			if (item.getChecked()) {
				anyItemChecked = true;
				break;
			}
		}
		setPageComplete(anyItemChecked);
	}

	private void createProjectsTable(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		table.setLayoutData(data);

		createProjectTableColumns(parent, tableViewer);

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setInput(allProjects);

		Display display = Display.getCurrent();
		Color gray = display.getSystemColor(SWT.COLOR_GRAY);
		for (TableItem item : table.getItems()) {
			IProject project = (IProject) item.getData();
			if (project.isOpen()) {
				// disable rows of open projects
				item.setGrayed(true);
				item.setForeground(gray);
			} else {
				if (preselectedProjects.contains(project)) {
					item.setChecked(true);
				}
			}
		}
		table.addListener(SWT.Selection, new DisableGrayedItemSelectionListener(table));
		table.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				if (evt.detail == SWT.CHECK) {
					updatePageComplete();
					// only selection events
					return;
				}

				TableItem item = (TableItem) evt.item;
				if (item == null) {
					// likely CMD+A/STRG+A was pressed
					selectAll(true);
					updatePageComplete();
				} else if (!((IProject) item.getData()).isOpen()) {
					item.setChecked(!item.getChecked());
					updatePageComplete();
				}
			}
		});

		tableViewer.setSelection(new StructuredSelection(preselectedProjects));


	}

	private static void createProjectTableColumns(Composite parent, TableViewer tableViewer) {
		String[] titles = new String[] { Messages.wizard_projectsPage_projectTable_header_projectName, Messages.wizard_projectsPage_projectTable_header_reasonWhyDisabled };
		int[] bounds = { 150, 200 };

		// the first column shows the project name
		TableViewerColumn col = createTableViewerColumn(tableViewer, titles[0], bounds[0]);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IProject project = (IProject) element;
				return project.getName();
			}
		});

		// the second column the disabled reason
		col = createTableViewerColumn(tableViewer, titles[1], bounds[1]);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IProject project = (IProject) element;
				if (project.isOpen()) {
					return Messages.wizard_projectsPage_projectTable_itemText_disabledReasonOpen;
				}
				return "-"; //$NON-NLS-1$
			}
		});
}

	private static TableViewerColumn createTableViewerColumn(TableViewer tableViewer, String title, int width) {
		TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(width);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	private void createSelectButtons(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		comp.setLayout(new GridLayout(1, false));

		Button selAllButton = new Button(comp, SWT.PUSH);
		selAllButton.setText(Messages.wizard_projectsPage_projectTable_button_selectAll);
		selAllButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
		selAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectAll(true);
			}
		});
		selAllButton.addSelectionListener(new PageCompleteSelectAllAdapter(this, true));

		Button deselAllButton = new Button(comp, SWT.PUSH);
		deselAllButton.setText(Messages.wizard_projectsPage_projectTable_button_deselectAll);
		deselAllButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
		deselAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectAll(false);
			}
		});
		deselAllButton.addSelectionListener(new PageCompleteSelectAllAdapter(this, false));


		comp.pack();
	}

	private void selectAll(boolean checkAllTableItems) {
		for (TableItem item : tableViewer.getTable().getItems()) {
			// ignore disabled rows
			if (item.getGrayed()) {
				continue;
			}

			item.setChecked(checkAllTableItems);
		}
	}

	public Collection<IProject> getSelectedProjects() {
		Collection<IProject> result = new ArrayList<IProject>();
		for (TableItem item : tableViewer.getTable().getItems()) {
			if (item.getChecked()) {
				result.add((IProject) item.getData());
			}
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
				item.setChecked(false);
			} finally {
				table.setRedraw(true);
			}
		}
	}

	private static class PageCompleteSelectAllAdapter extends SelectionAdapter {
		private final WizardPage wizardPage;
		private final boolean pageCompleteOnSelect;

		public PageCompleteSelectAllAdapter(WizardPage wizardPage, boolean pageCompleteOnSelect) {
			this.wizardPage = wizardPage;
			this.pageCompleteOnSelect = pageCompleteOnSelect;
		}

		@Override
		public void widgetSelected(SelectionEvent evt) {
			wizardPage.setPageComplete(pageCompleteOnSelect);
		}
	}
}
