package org.search.nibrs.admin.uploadfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    			
    			StreamUtils.copy(fileSystemResource.getInputStream(), zipOutputStream);
    			zipOutputStream.closeEntry();
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