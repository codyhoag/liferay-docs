package com.liferay.documentation.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
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
		String zipName = args[4];

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


/////////////////////

			Set<String> modifiedImages = new HashSet<String>();
			Set<String> modifiedArticles = new HashSet<String>();

			// Separate modified/new MD files and images
			for (String file : modifiedFiles) {
				if (file.endsWith(".png") || file.endsWith(".jpg")) {
					modifiedImages.add(file);
				}
				else if (file.endsWith(".markdown") || file.endsWith(".md")) {
					modifiedArticles.add(file);
				}
				else {
					continue;
				}
			}

			// Unzip dist Zip
			unzipFile(docDir, zipName);
			File unzippedDir = new File("../" + docDir + "/" + zipName);
			
			// Find all MD files in directory
			Set<File> zipMarkdownFiles = getMarkdownFiles(unzippedDir);
			
			
			Set<String> articlesWithModifiedImages = getArticlesWithModifiedImages(zipMarkdownFiles, modifiedImages, docDir);

			modifiedArticles.addAll(articlesWithModifiedImages);
			System.out.println("modifiedArticles: " + modifiedArticles);
			Set<String> modifiedArticlesAbsolutePaths = new HashSet<String>();

			for (String modifiedArticle : modifiedArticles) {
				File article = new File(modifiedArticle);
				String articleAbsPath = article.getCanonicalPath();
				System.out.println("absPath: " + articleAbsPath);
				modifiedArticlesAbsolutePaths.add(articleAbsPath);
			}
			
			// Find and add all modified/new MD files' intro file
			Set<String> introFiles = getIntroFiles(modifiedArticlesAbsolutePaths, docDir, docLocation);
			modifiedArticles.addAll(introFiles);

			// Scan each MD file for remainder of images to include in ZIP file. When
			// re-importing a new MD file, all of its images must also be re-imported.
			Set<String> markdownImages = scanMarkdownForAllImages(modifiedArticles);

			modifiedImages.addAll(markdownImages);

				try {
					System.out.println("Creating ../dist/diffs.zip file");
					FileOutputStream fileOutputStream = new FileOutputStream("dist/diffs.zip");
					ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

					for (String markdown : modifiedArticles) {
						addToZipFile(markdown, zipOutputStream);
					}
					for (String image : modifiedImages) {
						addToZipFile(image, zipOutputStream);
					}

					zipOutputStream.close();
					fileOutputStream.close();

				} catch (IOException e) {
					e.printStackTrace();
				}

/////////////////////

			generateLatestCommitFile(headCommit);
		}
		else {
			System.out.println("There are no new files to publish!");
			System.exit(0);
		}
	}

	private static void addToZipFile(String modFile, ZipOutputStream zipOutputStream)
			throws FileNotFoundException, IOException {

		System.out.println("Adding " + modFile + " to zip file");

		File file = new File(modFile);
		FileInputStream fileInputStream = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(modFile);
		zipOutputStream.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int len;
		while ((len = fileInputStream.read(bytes)) >= 0) {
			zipOutputStream.write(bytes, 0, len);
		}

		zipOutputStream.closeEntry();
		fileInputStream.close();
	}

private static Set<String> getIntroFiles(Set<String> markdownFiles, String docDir, String docLocation) {

	Set<String> fileList = new HashSet<String>();
	
	for (String markdownFile: markdownFiles) {

		docLocation = docLocation.replace("/", File.separator);
		
		// docLocation replicated twice for currently unknown reason. Fix that here.
		markdownFile = markdownFile.replace(docLocation + File.separator + "articles", "articles");
		
		File article = new File(markdownFile);
		File parentDir = article.getParentFile();

		boolean containsIntro = true;

		while (containsIntro) {
			
			File[] parentFiles = parentDir.listFiles();

			System.out.println("parentFiles: " + parentFiles);
			
			containsIntro = false;

			for (File file : parentFiles) {
				if (file.getName().endsWith("introduction.markdown") ||
						file.getName().endsWith("intro.markdown")) {

					fileList.add(file.toString());
					System.out.println("Test Z");
					containsIntro = true;
				}
			}

			parentDir = parentDir.getParentFile();
		}
	}
	
	return fileList;
	
	/**
	Set<String> introFiles = new HashSet<String>();

		for (String markdownFile : markdownFiles) {
			if (!markdownFile.contains("intro.markdown") ||
					!markdownFile.contains("introduction.markdown")) {

				File file = new File(markdownFile);
				
				System.out.println("file: " + file.toString());

				File parentDir = file.getParentFile();
				
				
				File[] dirIntroFiles = parentDir.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.equals("intro.markdown") ||
								name.equals("introduction.markdown");
					}
				});**/
				/**
				File[] parentDirFiles = parentDir.listFiles();
				Set<File> dirIntroFiles = new HashSet<File>();
				
				System.out.println("parentDirFiles: " + parentDirFiles);
				
				System.out.println("Test2");
				for (File dirIntroFile : parentDirFiles) {
					if (dirIntroFile.toString().equals("intro.markdown") ||
					dirIntroFile.toString().equals("introduction.markdown")) {
						dirIntroFiles.add(dirIntroFile);
					}
				}

				System.out.println("dirIntroFiles: " + dirIntroFiles);
				
				System.out.println("Test3");
				for (File introFile : dirIntroFiles) {
					introFiles.add(introFile.toString());
				}
				System.out.println("Test4");
			}
		}

		return introFiles;**/
	}

	private static Set<File> getMarkdownFiles(File dir) {

		Set<File> chFiles = new HashSet<File>();
		File articleDir = new File(dir.getAbsolutePath() + "/articles");
		File[] articles = (File[])ArrayUtils.addAll(articleDir.listFiles());

		for (File article : articles) {

			if (article.getName().contains(".")) {
				continue;
			}

			File[] allFiles = article.listFiles();

			for (File file : allFiles) {

				if (!file.toString().endsWith("markdown") && !file.toString().endsWith("md")) {
					continue;
				}

				chFiles.add(file);
			}
		}
		
		return chFiles;
	}

	private static void generateLatestCommitFile(String headCommit)
			throws IOException {

		PrintWriter writer = new PrintWriter("last-publication-commit.txt", "UTF-8");
		writer.print(headCommit);
		writer.close();
	}
	
	private static Set<String> getArticlesWithModifiedImages(Set<File> zipMarkdownFiles, Set<String> modifiedImages, String docDir) {

		Set<File> zipMarkdownFilesWithImageFinal = new HashSet<File>();
		Set<String> filesWithImagePath = new HashSet<String>();

		for (String img : modifiedImages) {

			// Scan directory's MD files for modified/new image
			Set<File> zipMarkdownFilesWithImage = scanMarkdownForImage(img, zipMarkdownFiles);

			// Add the set of MD files that contain the image to a master set
			for (File file : zipMarkdownFilesWithImage) {
				zipMarkdownFilesWithImageFinal.add(file);
				System.out.println("New image " + img + " found in file " + file.getName());			
			}
		}

		// Convert the list of MD files (the modified images' MD files) to
		// readable directory paths
		for (File file : zipMarkdownFilesWithImageFinal) {
			int x = file.toString().indexOf(docDir, file.toString().indexOf(docDir) + 1);
			int y = x + docDir.length() + 1;
			String filePath = file.toString().substring(y, file.toString().length());
			filesWithImagePath.add(filePath);
		}

		return filesWithImagePath;
		
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

				if (entry.getChangeType().toString().equals("RENAME")) {
					renamedFiles.put(entry.getOldPath(), entry.getNewPath());
				}
				else {
					modifiedFiles.add(entry.getNewPath());
				}
			}
			else if (entry.getOldPath().startsWith(docLocation) &&
					entry.getChangeType().toString().equals("DELETE")) {
				deletedFiles.add(entry.getOldPath());
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

	private static Set<String> scanMarkdownForAllImages(Set<String> modifiedArticles) {
		
		Set<File> markdownImages = new HashSet<File>();
		Set<String> markdownImagesString = new HashSet<String>();

		for (String modifiedArticle : modifiedArticles) {

			Scanner scanner = null;
			File file = new File(modifiedArticle);
			
			try {
				scanner = new Scanner(file);

				while (scanner.hasNextLine()) {
					String lineFromFile = scanner.nextLine();

					if (lineFromFile.contains(".png")) { 
						int w = lineFromFile.indexOf(".png");
						int x = w + 4;
						int y = lineFromFile.indexOf("../../images");

						if (y < 0) {
							continue;
						}

						int z = y + 6;
						String img = lineFromFile.substring(z, x);

						File markdownImage = new File(img);

						markdownImages.add(markdownImage);
					}
				}
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		for (File markdownImage : markdownImages) {
			markdownImagesString.add(markdownImage.toString());
		}

		return markdownImagesString;
	}

	private static Set<File> scanMarkdownForImage(String img, Set<File> files) {

		Set<File> filesWithImage = new HashSet<File>();

		for (File file : files) {

			Scanner scanner = null;
			try {
				scanner = new Scanner(file);

				while (scanner.hasNextLine()) {
					String lineFromFile = scanner.nextLine();

					if (lineFromFile.contains(img)) { 
						filesWithImage.add(file);
					}
				}
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return filesWithImage;
	}

	private static void unzipFile(String docDir, String zipName)
			throws IOException {

		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		
		//System.out.println("Test");

		File zipFile = new File("../" + docDir + "/" + zipName + ".zip");
		File destinationDir = new File("../" + docDir + "/" + zipName);
		ZipFile zip = new ZipFile(zipFile);
		Enumeration<? extends ZipEntry> zipEntries = zip.entries();

		while (zipEntries.hasMoreElements()) {
			ZipEntry entry = zipEntries.nextElement();

			if (entry.isDirectory()) {
				File newDir = new File(destinationDir, entry.getName());
				newDir.mkdirs();
			} else {
				BufferedInputStream inputStream = new BufferedInputStream(zip.getInputStream(entry));
				File outputFile = new File(destinationDir, entry.getName());
				BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}

				inputStream.close();
				outputStream.close();
			}
		}
		zip.close();
		//zipFile.delete();
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