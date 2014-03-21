package com.github.eclipse.projectlocationupdater;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.core.internal.localstore.ILocalStoreConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Project location updater utility
 * 
 * "restriction" warnings are suppressed: we need do access Eclipse internal
 * constants
 * 
 * @author Max Gensthaler
 * @author Thomas Calmant
 */
@SuppressWarnings("restriction")
public class LocationUpdater {

	/** URI prefix and file protocol prefix */
	private static final String FILE_URI_PREFIX = LocationUpdater.URI_PREFIX
			+ "file:";

	/** URI prefix in location file */
	private static final String URI_PREFIX = "URI//";

	/** Constant path to the workspace projects locations storage */
	private static final IPath WORKSPACE_PROJECT_SETTINGS_RELPATH = new Path(
			".metadata/.plugins/org.eclipse.core.resources/.projects");

	/**
	 * Retrieves the path to the .location file of a project in its workspace
	 * 
	 * @param aProject
	 *            Any project
	 * @return An IPath to its .location file
	 */
	public IPath getProjectLocationFile(final IProject aProject) {

		// Get the workspace root path
		final IPath workspaceLocation = aProject.getWorkspace().getRoot()
				.getLocation();

		// Forge the location file name
		return workspaceLocation.append(WORKSPACE_PROJECT_SETTINGS_RELPATH)
				.append(aProject.getName()).append(".location");
	}

	/**
	 * Reads the content of a project location file
	 * 
	 * @param aLocationPath
	 *            Path to a project location file
	 * @return The value of the project location
	 * @throws IOException
	 *             Error reading the location file
	 */
	public String readProjectLocation(final IPath aLocationPath)
			throws IOException {

		DataInputStream in = null;
		try {
			// Read the location file
			in = new DataInputStream(
					new FileInputStream(aLocationPath.toFile()));

			// Ignore the begin chunk
			in.skipBytes(ILocalStoreConstants.BEGIN_CHUNK.length);

			// Parse the location
			String projectLocationStr = in.readUTF();
			if (projectLocationStr.startsWith(FILE_URI_PREFIX)) {
				projectLocationStr = projectLocationStr
						.substring(FILE_URI_PREFIX.length());
				if (systemIsWindows()
						&& projectLocationStr.matches("^/[a-zA-Z]:")) {
					// remove trailing "/" from absolute path on windows
					projectLocationStr = projectLocationStr.substring(1);
				}
			}

			return projectLocationStr;

		} finally {
			// Be nice
			if (in != null) {
				in.close();
			}
		}
	}

	/**
	 * Checks if we're running on Windows
	 * 
	 * @return True if the OS name contains "windows"
	 */
	private boolean systemIsWindows() {
		return System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0;
	}
}
