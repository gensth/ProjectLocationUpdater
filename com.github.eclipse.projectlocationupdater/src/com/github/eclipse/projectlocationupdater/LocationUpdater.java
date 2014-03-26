package com.github.eclipse.projectlocationupdater;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.internal.localstore.ILocalStoreConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * A utility class to read/write the location of a project.
 * 
 * "restriction" warnings are suppressed: we need do access Eclipse internal
 * constants.
 * 
 * @author Max Gensthaler
 * @author Thomas Calmant
 */
@SuppressWarnings("restriction")
public class LocationUpdater {
	/** URI prefix in location file. */
	private static final String URI_PREFIX = "URI//";

	/** URI prefix and file protocol prefix. */
	private static final String FILE_URI_PREFIX = URI_PREFIX + "file:";

	/** Constant path to the workspace projects locations storage. */
	private static final IPath WORKSPACE_PROJECT_SETTINGS_RELPATH = new Path(".metadata/.plugins/org.eclipse.core.resources/.projects");

	/** <code>true</code> if we're running on Windows, else <code>false</code>. */
	private static final boolean OS_IS_WINDOWS = System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0;

	/**
	 * Retrieves the path to the .location file of a project in its workspace.
	 * 
	 * @param project
	 *            Any project
	 * @return An IPath to its .location file
	 */
	private IPath getProjectLocationFile(final IProject project) {
		// Get the workspace root path
		final IPath workspaceLocation = project.getWorkspace().getRoot().getLocation();

		// Forge the location file name
		return workspaceLocation.append(WORKSPACE_PROJECT_SETTINGS_RELPATH)
				.append(project.getName()).append(".location");
	}

	/**
	 * Reads the content of a project location file.
	 * 
	 * @param projectLocationFile
	 *            Path to a project location file
	 * @return The value of the project location
	 * @throws IOException
	 *             Error reading the location file
	 */
	private String readProjectLocation(final IPath projectLocationFile) throws IOException {
		DataInputStream in = null;
		try {
			// Read the location file
			in = new DataInputStream(new FileInputStream(projectLocationFile.toFile()));

			// Ignore the begin chunk
			in.skipBytes(ILocalStoreConstants.BEGIN_CHUNK.length);

			// Parse the location
			String projectLocation = in.readUTF();
			if (projectLocation.startsWith(FILE_URI_PREFIX)) {
				projectLocation = projectLocation.substring(FILE_URI_PREFIX.length());
				if (OS_IS_WINDOWS && projectLocation.matches("^/[a-zA-Z]:")) {
					// remove trailing "/" from absolute path on windows
					projectLocation = projectLocation.substring(1);
				}
			}

			return projectLocation;
		} finally {
			// Be nice
			if (in != null) {
				in.close();
			}
		}
	}

	/**
	 * Reads the content of a project location file.
	 * 
	 * @param project
	 *            Project to locate
	 * @return The value of the project location
	 * @throws IOException
	 *             Error reading the location file
	 */
	public String readProjectLocation(final IProject project) throws IOException {
		IPath projectLocationFile = getProjectLocationFile(project);
		return readProjectLocation(projectLocationFile);
	}

	/**
	 * Updates a substring of the location.
	 * 
	 * @param project
	 *            Project to be updated
	 * @param previousPrefix
	 *            Prefix path to be replaced
	 * @param newPrefix
	 *            Replacement path
	 * @throws IOException
	 *             Error reading or writing the project location file
	 */
	public void updateLocationSubstring(final IProject project, final String previousPrefix, final String newPrefix) throws IOException {
		// Read the current location
		final IPath projectLocationFile = getProjectLocationFile(project);
		final String currentLocation = readProjectLocation(projectLocationFile);

		// Replace the substring
		final String newLocation = currentLocation.replace(previousPrefix, newPrefix);

		// Store the new location
		writeProjectLocation(projectLocationFile, new Path(newLocation));
	}

	/**
	 * Writes the content of a project location file.
	 * 
	 * @param projectLocationFile
	 *            Path to the project .location file
	 * @param newLocation
	 *            The new path to the project
	 * @throws IOException
	 *             Error reading or writing the location file
	 */
	private void writeProjectLocation(final IPath projectLocationFile, final IPath newLocation) throws IOException {
		// Keep existing data
		String[] referenceNames;

		// Read the existing file
		DataInputStream in = null;
		try {
			in = new DataInputStream(new FileInputStream(projectLocationFile.toFile()));

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
			out = new DataOutputStream(new FileOutputStream(projectLocationFile.toFile()));

			// Write the begin chunk
			out.write(ILocalStoreConstants.BEGIN_CHUNK);

			// Write the new location
			out.writeUTF(URI_PREFIX + newLocation.toFile().toURI().toString());

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
