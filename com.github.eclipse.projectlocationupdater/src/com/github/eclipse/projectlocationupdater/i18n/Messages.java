package com.github.eclipse.projectlocationupdater.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * @author Max Gensthaler
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

	public static String errorDialog_errorOnApplyPrefix;
	public static String errorDialog_errorReadProjLocPrefix;
	public static String errorDialog_title;
	public static String proppage_browse;
	public static String proppage_currentLocation;
	public static String proppage_newLocation;
	public static String proppage_projectOpenWarning;
	public static String proppage_usage;
	public static String wizard_projectsPage_page_description;
	public static String wizard_projectsPage_page_name;
	public static String wizard_projectsPage_page_title;
	public static String wizard_projectsPage_projectTable_button_deselectAll;
	public static String wizard_projectsPage_projectTable_button_selectAll;
	public static String wizard_projectsPage_projectTable_header_projectName;
	public static String wizard_projectsPage_projectTable_header_reasonWhyDisabled;
	public static String wizard_projectsPage_projectTable_itemText_disabledReasonOpen;
	public static String wizard_updatePage_page_description;
	public static String wizard_updatePage_page_name;
	public static String wizard_updatePage_page_title;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		//
	}
}
