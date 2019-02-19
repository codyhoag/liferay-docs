package com.liferay.documentation.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		distDir = args[2];
		String fullZipParam = args[3];
		boolean fullZip = Boolean.parseBoolean(fullZipParam);

		if (fullZip) {
			System.exit(0);
		}

		File dir = new File("../" + docDir);
		String absDir = dir.getAbsolutePath();
		File commitFile = new File(absDir + "/last-publication-commit.txt");

		String headCommit = getHeadCommit();

		// Create new metadata file with current HEAD commit, if one doesn't exist.
		if (!commitFile.exists()) {
			System.out.println("Creating ./last-publication-commit.txt file. "
					+ "Subsequent dists will generate Zip with only modified files.");

			generateLatestCommitFile(headCommit);
			System.exit(0);
		}

		// If a metadata file exists, copy the last published commit and find all
		// modified files since that commit's publication.

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
		diffFormatter.setRepository(git.getRepository());
		diffFormatter.setDetectRenames(true);
		List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

		List<String> modifiedFiles = new ArrayList<String> ();
		List<String> deletedFiles = new ArrayList<String> ();
		HashMap<String, String> renamedFiles = new HashMap<String, String> ();

		for (DiffEntry entry : entries) {

			if (entry.getNewPath().startsWith(docLocation)) {

				if (entry.getChangeType().toString().equals("DELETE")) {
					deletedFiles.add(entry.getOldPath());
				}
				else if (entry.getChangeType().toString().equals("RENAME")) {
					renamedFiles.put(entry.getOldPath(), entry.getNewPath());
				}
				else {
					modifiedFiles.add(entry.getNewPath());
				}
			}
		}

		if (modifiedFiles.isEmpty()) {
			System.out.println("There are no new files to publish!");
			System.exit(0);
		}

		if (!deletedFiles.isEmpty() || !renamedFiles.isEmpty()) {
			writeDeletedTextFile(deletedFiles, renamedFiles);
		}

		repo.close();

		return modifiedFiles;
	}

	private static Repository openGitRepository() throws IOException {

		FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
		Repository repo = repoBuilder.readEnvironment().findGitDir().build();

		return repo;
	}

	private static void writeDeletedTextFile(List<String> deletedFiles, HashMap<String, String> renamedFiles)
			throws IOException {

		PrintWriter writer = new PrintWriter(distDir + "/delete-files.txt", "UTF-8");
		writer.println("DELETED:\n");

		for (String file : deletedFiles) {
			writer.println(file);
		}

		writer.println("");
		writer.println("\nRENAMED:\n");

		for (Map.Entry<String, String> entry : renamedFiles.entrySet()) {
			writer.println("Old article to delete: " + entry.getKey() + " (renamed/moved to: " + entry.getValue() + ")");
		}

		writer.close();
	}

	private static String distDir;
}