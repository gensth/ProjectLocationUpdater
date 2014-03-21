package com.github.eclipse.projectlocationupdater;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

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
	public IPath getProjectLocationFilePath(final IProject aProject) {

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

	/**
	 * Updates a substring of the location
	 * 
	 * @param aProject
	 *            Project to be updated
	 * @param aToReplace
	 *            String part to be replaced
	 * @param aReplacement
	 *            Replacement string
	 * @throws IOException
	 *             Error reading or writing the project location file
	 */
	public void updateLocationSubstring(final IProject aProject,
			final String aToReplace, final String aReplacement)
			throws IOException {

		// Read the current location
		final IPath locationFilePath = getProjectLocationFilePath(aProject);
		final String currentLocation = readProjectLocation(locationFilePath);

		// Replace the substring
		final String newLocation = currentLocation.replace(aToReplace,
				aReplacement);

		// Make a URI from the new path string
		final URI newLocationURI = new Path(newLocation).toFile().toURI();

		// Store the new location
		writeProjectLocation(locationFilePath, newLocationURI);
	}

	/**
	 * Writes the content of a project location file
	 * 
	 * @param aLocationFilePath
	 *            Path to the project .location file
	 * @param aLocationURI
	 *            URI to the project itself
	 * @throws FileNotFoundException .location
	 *             file not found
	 * @throws IOException
	 *             Error reading or writing the location file
	 */
	public void writeProjectLocation(final IPath aLocationFilePath,
			final URI aLocationURI) throws FileNotFoundException, IOException {

		// Keep existing data
		String[] referenceNames;

		// Read the existing file
		DataInputStream in = null;
		try {
			in = new DataInputStream(new FileInputStream(
					aLocationFilePath.toFile()));

			// Ignore the begin chunk
			in.skipBytes(ILocalStoreConstants.BEGIN_CHUNK.length);

			// Get the current project location
			final String projectLocationStr = in.readUTF();
			assert projectLocationStr.startsWith(URI_PREFIX);

			// Store references
			final int numRefs = in.readInt();
			referenceNames = new String[numRefs];
			for (int i = 0; i < numRefs; i++) {
				referenceNames[i] = in.readUTF();
			}

			// Ignore the end chunk
			in.skipBytes(ILocalStoreConstants.END_CHUNK.length);

		} finally {
			// Always close the file
			if (in != null) {
				in.close();
			}
		}

		// Write the new content
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(new FileOutputStream(
					aLocationFilePath.toFile()));

			// Write the begin chunk
			out.write(ILocalStoreConstants.BEGIN_CHUNK);

			// Write the new location
			out.writeUTF(URI_PREFIX + aLocationURI.toString());

			// Write references
			out.writeInt(referenceNames.length);
			for (final String referenceName : referenceNames) {
				out.writeUTF(referenceName);
			}

			// Write the end chunk
			out.write(ILocalStoreConstants.END_CHUNK);
			out.flush();

		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
}
