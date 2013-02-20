/*
 * Copyright 2011-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.hadoop.admin.cli.commands;

import java.io.File;
import java.io.FileFilter;
import java.util.logging.Logger;

import org.springframework.core.io.FileSystemResource;
import org.springframework.data.hadoop.admin.cli.commands.BaseCommand;
import org.springframework.shell.commands.OsCommands;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Commands to operate on templates.
 *  * 
 * @author Jarred Li
 *
 */
@Component
public class TemplateCommand extends BaseCommand implements CommandMarker {

	
	private static final Logger LOGGER = HandlerUtils.getLogger(OsCommands.class);
	
	/**
	 * list uploaded templates
	 * 
	 */
	@CliCommand(value = "template list", help = "list template files")
	public void listFiles() {
		setCommandURL("templates.json");
		callGetService();
	}

	/**
	 * download the file from server
	 * 
	 * @param path
	 */
	@CliCommand(value = "template download", help = "download  template file")
	public void downloadFile(@CliOption(key = { "path" }, mandatory = true, help = "the path of downloaded file") final String path) {
		String url = "templates/";
		url = url + path;
		setCommandURL(url);
		String fileName = path.substring(path.lastIndexOf("/") + 1);
		callDownloadFile(fileName);
	}

	/**
	 * upload files. If the "from" is directory, all the files in that directory will be uploaded to server but without recursion.
	 * If the "from" is file, it will be uploaded to server.
	 * @param from The source file, can be a file or directory name
	 * @param to  The server path
	 */
	@CliCommand(value = "template upload", help = "upload template file")
	public void uploadFile(@CliOption(key = { "localPath" }, mandatory = true, help = "the path of source folder") final String localPath, @CliOption(key = { "serverPath" }, mandatory = true, help = "the path to store uploaded files") final String serverPath) {
		File file = new File(localPath);
		if (!file.exists()) {
			LOGGER.warning("the source path does not exist");
			return;
		}
		String url = "templates/";
		url += serverPath;
		super.setCommandURL(url);
		if (file.isFile()) {
			uploadOneFile(file);
		}
		else if (file.isDirectory()) {
			uploadFilesInDirectory(file);
		}
	}

	/**
	 * delete the files in the server.
	 * 
	 * @param path Path in the server.
	 */
	@CliCommand(value = "template delete", help = "delete template file")
	public void deleteFile(@CliOption(key = { "path" }, mandatory = true, help = "the path of delete file") final String path) {
		String url = "templates/";
		setCommandURL(url);
		MultiValueMap<String, String> mvm = new LinkedMultiValueMap<String, String>();
		mvm.add("pattern", path);
		mvm.add("_method", "DELETE");
		mvm.add("delete", "Delete");
		callPostService(mvm);

	}

	@CliAvailabilityIndicator({ "template list", "template download", "template upload", "template delete" })
	public boolean isCommandsAvailable() {
		return isServiceUrlSet();
	}


	/**
	 * Upload one file to server.
	 * 
	 * @param file The file to be uploaded.
	 */
	private void uploadOneFile(File file) {
		MultiValueMap<String, Object> mvm = new LinkedMultiValueMap<String, Object>();
		mvm.add("file", new FileSystemResource(file));
		callPostService(mvm);
	}

	/**
	 * Upload the files in the directory. Here we upload files by category. 
	 * 
	 * @param directory Directory contains files to be uploaded.
	 */
	private void uploadFilesInDirectory(File directory) {
		uploadFilesByCategory(directory, ".zip");
	}

	/**
	 * Upload same category files.
	 * 
	 * @param directory The directory contains files to be uploaded
	 * @param fileSuffix The file suffix 
	 */
	private void uploadFilesByCategory(File directory, final String fileSuffix) {
		File[] files = directory.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().toLowerCase().endsWith(fileSuffix);
			}
		});
		for (File file : files) {
			if (file.isDirectory()) {
				LOGGER.warning("can only upload files not folder");
				continue;
			}
			else {
				uploadOneFile(file);
			}
		}
	}

}
