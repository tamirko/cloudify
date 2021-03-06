/*******************************************************************************
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *******************************************************************************/
package org.cloudifysource.rest.repo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.cloudifysource.dsl.internal.CloudifyConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * A class for uploading files and getting uploaded files.
 * @author yael
 * 
 */
@Component
public class UploadRepo {
//	@Value(value = "${upload.cleanupTimeoutSeconds}")
	private int cleanupTimeoutSeconds = CloudifyConstants.DEFAULT_UPLOAD_TIMEOUT_SECOND;
	// load from property, default to tempdir
	private static final File DEFAULT_UPLOAD_BASE_DIR = new File(System.getProperty("java.io.tmpdir"));
	private File baseDir = DEFAULT_UPLOAD_BASE_DIR;
	private ScheduledExecutorService executor;
	private File restUploadDir;

	/**
	 * creating the upload directory and initializing scheduled thread.
	 * 
	 * @throws IOException .
	 */
	public void init() throws IOException {
		createUploadDir();
		createScheduledExecutor();
	}

	private void createScheduledExecutor() {
		final CleanUploadDirThread cleanupThread =
				new CleanUploadDirThread(restUploadDir, cleanupTimeoutSeconds * 1000);
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(cleanupThread, 0, cleanupTimeoutSeconds, TimeUnit.SECONDS);
		
	}
	
	/**
	 * 
	 * @throws IOException .
	 */
	public void destroy() throws IOException {
		executor.shutdown();
		FileUtils.deleteDirectory(restUploadDir);
	}
	
	private void reset() {
		executor.shutdownNow();
		createScheduledExecutor();
	}

	private void createUploadDir() throws IOException {
		restUploadDir = new File(baseDir, CloudifyConstants.UPLOADS_FOLDER_NAME);
		if (restUploadDir.exists()) {
			FileUtils.deleteDirectory(restUploadDir);
		}
		restUploadDir.mkdirs();
	}

	private void copyMultipartFileToLocalFile(final MultipartFile srcFile, final File storedFile)
			throws IOException {
		if (srcFile == null) {
			return;
		}
		srcFile.transferTo(storedFile);
		storedFile.deleteOnExit();

	}

	/**
	 * Creates a new folder with a randomly generated name (using the UUID class) which holds the uploaded file.
	 * The folder located at the main upload folder in {@link #baseDir}.
	 * This uploaded file and its folder will be deleted after {@link #cleanupTimeoutSeconds} seconds.
	 * 
	 * @param fileName
	 * 			The name of the uploaded file.
	 * 			If null, the multipartFile's original file name will be used as the file's name.
	 * @param multipartFile
	 *          The file to upload.
	 * @return the uploaded key.
	 * @throws IOException .
	 */
	public String put(final String fileName, final MultipartFile multipartFile) throws IOException {
		final String dirName = UUID.randomUUID().toString();
		final File srcDir = new File(restUploadDir, dirName);
		srcDir.mkdirs();
		String name = fileName == null ? multipartFile.getOriginalFilename() : fileName;
		final File storedFile = new File(srcDir, name);
		copyMultipartFileToLocalFile(multipartFile, storedFile);
		return dirName;
	}

	/**
	 * Gets the file stored in a directory with the given name (uploadDirName).
	 * 
	 * @param uploadDirName
	 *            - the name of the upload file's directory.
	 * @return the suitable file or null if a file with that name doesn't exist.
	 */
	public File get(final String uploadDirName) {
		if (restUploadDir == null || !restUploadDir.exists()) {
			return null;
		}

		final File[] files = restUploadDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(final File dir, final String name) {
				return name.equals(uploadDirName);
			}
		});
		if (files != null && files.length > 0) {
			final File dir = files[0];
			return dir.listFiles()[0];
		}
		return null;
	}

	public File getRestUploadDir() {
		return restUploadDir;
	}
	
	/**
	 * Sets the cleanup timeout and reset the scheduled thread.
	 * @param cleanupTimeoutSeconds .
	 */
	public void resetTimeout(final int cleanupTimeoutSeconds) {
		this.setCleanupTimeoutSeconds(cleanupTimeoutSeconds);
		reset();
	}

	public File getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(final File baseDir) {
		this.baseDir = baseDir;
	}

	public int getCleanupTimeoutSeconds() {
		return cleanupTimeoutSeconds;
	}

	public void setCleanupTimeoutSeconds(final int cleanupTimeoutSeconds) {
		this.cleanupTimeoutSeconds = cleanupTimeoutSeconds;
	}
}
