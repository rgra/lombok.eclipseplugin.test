package lombok.eclipse.refactoring;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LombokEclipseRefactoringTests {

	private static JavaTestProject fProject;

	@BeforeClass
	public static void setUp() throws Exception {
		fProject = new JavaTestProject();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		fProject.delete();
	}

	// TODO use runner
	@Test
	public void runTests() throws Exception {
		File beforeDir = new File("test/before");
		File afterDir = new File("test/after");
		File[] files = beforeDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".java");
			}
		});

		if (files != null) {
			for (File before : files) {
				File after = new File(afterDir, before.getName());
				test(before, after);
			}
		}
	}

	private void test(File before, File after) throws Exception {
		IFolder testFolder = fProject.createSourceFolder();
		IFile file = fProject.createFile(testFolder, before.getName(), new FileInputStream(before));
		try {
			RefactoringContribution contribution = RefactoringCore
					.getRefactoringContribution(LombokRefactoringDescriptor.ID);
			LombokRefactoringDescriptor descriptor = (LombokRefactoringDescriptor) contribution.createDescriptor();

			descriptor.getArguments().setProject(fProject.getProject());
			descriptor.getArguments().setElements(
					Arrays.asList(RefactoringElement.Factory.create(fProject.getProject())));
			descriptor.getArguments().setRefactorToString(true);
			descriptor.getArguments().setRefactorEqualsAndHashCode(true);
			descriptor.getArguments().setRefactorGetters(true);
			descriptor.getArguments().setRefactorSetters(true);

			perform(descriptor);

			String actual = fProject.getContent(file);

			String expected = fProject.getContent(new FileInputStream(after));

			assertEquals(before.getName(), expected, actual);
		} finally {
			file.delete(true, null);
		}
	}

	// private Change perform(Change change) throws CoreException {
	// PerformChangeOperation op = new PerformChangeOperation(change);
	// op.run(null);
	// assertTrue(op.changeExecuted());
	// return op.getUndoChange();
	// }

	private Change perform(RefactoringDescriptor descriptor) throws CoreException {
		RefactoringStatus status = new RefactoringStatus();
		Refactoring refactoring = descriptor.createRefactoring(status);
		assertTrue(status.isOK());

		PerformRefactoringOperation op = new PerformRefactoringOperation(refactoring,
				CheckConditionsOperation.ALL_CONDITIONS);
		op.run(null);
		RefactoringStatus validationStatus = op.getValidationStatus();
		assertTrue(!validationStatus.hasFatalError() && !validationStatus.hasError());
		return op.getUndoChange();
	}

}
