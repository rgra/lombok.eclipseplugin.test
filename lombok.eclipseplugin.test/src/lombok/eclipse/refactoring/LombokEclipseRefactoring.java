package lombok.eclipse.refactoring;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LombokEclipseRefactoring {

	private static final Object LINE_SEPARATOR = System.getProperty("line.separator");
	private SimpleTestProject fProject;

	@Before
	public void setUp() throws Exception {
		this.fProject = new SimpleTestProject();
	}

	@After
	public void tearDown() throws Exception {
		this.fProject.delete();
	}

	@Test
	public void toStringTest() throws Exception {

		StringBuilder b = new StringBuilder();
		b.append("public class Test {").append(LINE_SEPARATOR);
		b.append("public String toString() {").append(LINE_SEPARATOR);
		b.append("return \"\";").append(LINE_SEPARATOR);
		b.append("}").append(LINE_SEPARATOR);
		b.append("}").append(LINE_SEPARATOR);
		String content = b.toString();

		IFolder testFolder = this.fProject.createSourceFolder();
		IFile file = this.fProject.createFile(testFolder, "Test.java", content);

		System.out.println(file.toString());

		RefactoringContribution contribution = RefactoringCore
				.getRefactoringContribution(LombokRefactoringDescriptor.ID);
		LombokRefactoringDescriptor descriptor = (LombokRefactoringDescriptor) contribution.createDescriptor();

		descriptor.getArguments().setProject(this.fProject.getProject());
		descriptor.getArguments().setElements(
				Arrays.asList(RefactoringElement.Factory.create(this.fProject.getProject())));
		descriptor.getArguments().setRefactorToString(true);

		Change undoChange = perform(descriptor);

		String actual = this.fProject.getContent(file);
		System.out.println(actual);

		StringBuilder expected = new StringBuilder();
		expected.append("import lombok.ToString;").append(LINE_SEPARATOR);
		expected.append("").append(LINE_SEPARATOR);
		expected.append("@ToString").append(LINE_SEPARATOR);
		expected.append("public class Test {").append(LINE_SEPARATOR);
		expected.append("}").append(LINE_SEPARATOR);

		assertEquals(expected.toString(), actual);

	}

	private Change perform(Change change) throws CoreException {
		PerformChangeOperation op = new PerformChangeOperation(change);
		op.run(null);
		assertTrue(op.changeExecuted());
		return op.getUndoChange();
	}

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
