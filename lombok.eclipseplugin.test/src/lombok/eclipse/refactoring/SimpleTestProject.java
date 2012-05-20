package lombok.eclipse.refactoring;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

public class SimpleTestProject {

	public static final String TEST_PROJECT_NAME = "TestProject";
	private final IJavaProject javaProject;

	public SimpleTestProject() throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(TEST_PROJECT_NAME);
		project.create(null);
		project.open(null);

		// Because we need a java project, we have to set the Java nature to the
		// created project:

		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });
		project.setDescription(description, null);

		// Now we can create our Java project

		this.javaProject = JavaCore.create(project);
		// (1) We first specify the output location of the compiler (the bin
		// folder):

		IFolder binFolder = project.getFolder("bin");
		binFolder.create(false, true, null);
		this.javaProject.setOutputLocation(binFolder.getFullPath(), null);

		// (2) Define the class path entries. Class path entries define the
		// roots of package fragments:

		List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
		IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
		LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
		for (LibraryLocation element : locations) {
			entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
		}
		// add libs to project class path
		this.javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);

		// (3) We have not yet the source folder created:

		IFolder sourceFolder = project.getFolder("src");
		sourceFolder.create(false, true, null);

		// (4) Now the created source folder should be added to the class
		// entries of the project, otherwise compilation will fail:

		IPackageFragmentRoot root = this.javaProject.getPackageFragmentRoot(sourceFolder);
		IClasspathEntry[] oldEntries = this.javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaCore.newSourceEntry(root.getPath());
		this.javaProject.setRawClasspath(newEntries, null);

	}

	public IJavaProject getProject() {
		return this.javaProject;
	}

	public IFolder createSourceFolder() throws CoreException {
		IFolder folder = this.javaProject.getProject().getFolder("src");
		return folder;
	}

	public IFolder createFolder(IContainer parent, String name) throws CoreException {
		IFolder result = parent.getFolder(new Path(name));
		result.create(true, true, null);
		return result;
	}

	public IFile createFile(IContainer parent, String name, String content) throws CoreException {
		IFile result = parent.getFile(new Path(name));
		result.create(new ByteArrayInputStream(content.getBytes()), true, null);
		return result;
	}

	public void delete() throws CoreException {
		this.javaProject.getProject().delete(true, true, null);
	}

	public String getContent(IFile file) throws CoreException, IOException {
		StringBuffer result = new StringBuffer();

		char[] buffer = new char[1024];
		InputStreamReader reader = new InputStreamReader(file.getContents(false));
		try {
			int amount;
			while ((amount = reader.read(buffer)) != -1) {
				result.append(buffer, 0, amount);
			}
		} finally {
			reader.close();
		}
		return result.toString();
	}
}
