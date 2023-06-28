package com.fileuploadUsingSpringboot.upload.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.fileuploadUsingSpringboot.model.FileInfo;
import com.fileuploadUsingSpringboot.service.FilesStoreService;
import com.fileuploadUsingSpringboot.upload.message.Response;

@Controller
//@CrossOrigin("http://localhost:4200")
public class FileUploadController {

  @Autowired
  FilesStoreService storageService;

  @PostMapping("/upload")
  public ResponseEntity<Response> uploadFile(@RequestParam("file") MultipartFile file) {
    String message = "";
    try {
      storageService.save(file);

      message = "File successfully uploaded: " + file.getOriginalFilename();
      return ResponseEntity.status(HttpStatus.OK).body(new Response(message));
    } catch (Exception e) {
    	//System.out.println(e);
    	//System.out.println(e);
      message = "Could not upload the file: " + file.getOriginalFilename() + "!" ;
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new Response(message));
    }
  }

  @GetMapping("/files")
  public ResponseEntity<List<FileInfo>> getListFiles() {
    List<FileInfo> fileInfos = storageService.loadAll().map(path -> {
      String filename = path.getFileName().toString();
      String url = MvcUriComponentsBuilder
          .fromMethodName(FileUploadController.class, "getFile", path.getFileName().toString()).build().toString();

      return new FileInfo(filename, url);
    }).collect(Collectors.toList());

    return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
  }

  @GetMapping("/files/{filename:.+}")
  public ResponseEntity<Resource> getFile(@PathVariable String filename) {
    Resource file = storageService.load(filename);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
  }
}
