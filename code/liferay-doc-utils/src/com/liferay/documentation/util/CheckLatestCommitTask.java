package com.liferay.documentation.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

public class CheckLatestCommitTask {

	public static void main(String[] args) throws IOException {

		String docDir = args[0];
		String docLocation = args[1];
		String fullZipParam = args[2];
		boolean fullZip = Boolean.parseBoolean(fullZipParam);
		
		if (fullZip) {
			System.exit(0);
		}
		
		String headCommit = getHeadCommit();

		File dir = new File("../" + docDir);
		String absDir = dir.getAbsolutePath();
		File commitFile = new File(absDir + "/last-publication-commit.txt");

		// Create new metadata file with current HEAD commit, if one doesn't exist.
		if (!commitFile.exists()) {
			System.out.println("Creating ./last-publication-commit.txt file");
			generateLatestCommitFile(headCommit);
		}
		// If a metadata file exists, copy the last published commit and find all
		// modified files since that commit's publication.
		else {
			String lastPublishedCommit = FileUtils.readFileToString(commitFile);

			if (!headCommit.equals(lastPublishedCommit)) {
				List<String> modifiedFiles = getModifiedFiles(lastPublishedCommit, docLocation);

				// build out Zip with these new modified file paths
				// Logic...

				generateLatestCommitFile(headCommit);
			}
			else {
				System.out.println("There are no new files to publish!");
				System.exit(0);
			}
		}
	}

	private static void generateLatestCommitFile(String headCommit)
			throws IOException {

		PrintWriter writer = new PrintWriter("last-publication-commit.txt", "UTF-8");
		writer.print(headCommit);
		writer.close();
	}

	private static String getHeadCommit() throws IOException {

		Repository repo = openGitRepository();
		Ref head = repo.getRef("HEAD");
		String headCommit = head.getObjectId().getName();
		repo.close();

        return headCommit;
	}

	private static List<String> getModifiedFiles(String commit, String docLocation)
			throws IOException {

		Repository repo = openGitRepository();
		Git git = new Git(repo);
		ObjectReader reader = git.getRepository().newObjectReader();

		CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
		ObjectId oldTree = git.getRepository().resolve(commit + "^{tree}");
		oldTreeIter.reset(reader, oldTree);

		CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
		ObjectId newTree = git.getRepository().resolve("HEAD^{tree}");
		newTreeIter.reset(reader, newTree);

		DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
		diffFormatter.setRepository( git.getRepository());
		List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

		List<String> modifiedFiles = new ArrayList<String>();

		for (DiffEntry entry : entries) {

			if (entry.getNewPath().startsWith(docLocation)) {
				modifiedFiles.add(entry.getNewPath());
				System.out.println(entry.getNewPath());
			}
		}

		//Verify this works as expected when finished
		if (modifiedFiles.isEmpty()) {
			System.out.println("There are no new files to publish!");
			System.exit(0);
		}

		repo.close();

		return modifiedFiles;
	}

	private static Repository openGitRepository() throws IOException {

		FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
		Repository repo = repoBuilder.readEnvironment().findGitDir().build();

		return repo;
	}
}