/*
 * Copyright 2016 SEARCH-The National Consortium for Justice Information and Statistics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.search.nibrs.admin.uploadfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
public class DownloadService {
	private static final Log log = LogFactory.getLog(DownloadService.class);

    public void downloadZipFile(HttpServletResponse response, List<String> listOfFileNames) {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=download.zip");
        try(ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            for(String fileName : listOfFileNames) {
                FileSystemResource fileSystemResource = new FileSystemResource(fileName);
                ZipEntry zipEntry = new ZipEntry(fileSystemResource.getFilename());
                zipEntry.setSize(fileSystemResource.contentLength());
                zipEntry.setTime(System.currentTimeMillis());

                zipOutputStream.putNextEntry(zipEntry);

                StreamUtils.copy(fileSystemResource.getInputStream(), zipOutputStream);
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
        } catch (IOException e) {
        	log.error(e.getMessage(), e);
        }
    }
    
    public void downloadZipFolder(HttpServletResponse response, String folderName) {
    	response.setContentType("application/zip");
    	File file = new File(folderName);
    	
    	String filename = file.getName();  
    	response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".zip");
    	
    	List<String> listOfFileNames = Arrays.stream(file.listFiles())
    			.map(item -> item.getPath()).collect(Collectors.toList()); 
    	try(ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
    		for(String fileName : listOfFileNames) {
    			FileSystemResource fileSystemResource = new FileSystemResource(fileName);
    			ZipEntry zipEntry = new ZipEntry(fileSystemResource.getFilename());
    			zipEntry.setSize(fileSystemResource.contentLength());
    			zipEntry.setTime(System.currentTimeMillis());
    			
    			zipOutputStream.putNextEntry(zipEntry);
    			InputStream fis = fileSystemResource.getInputStream(); 
    			StreamUtils.copy(fis, zipOutputStream);
    			zipOutputStream.closeEntry();
    			fis.close();
    		}
    		zipOutputStream.finish();
    	} catch (IOException e) {
    		log.error(e.getMessage(), e);
    	}
    }
    
    public void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
}