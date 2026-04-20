package com.wanted.projectmodule2lms.global.file;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileController {

    private final ResourceLoader resourceLoader;

    public FileController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/download/{type}/{fileName:.+}")
    public Object downloadFile(@PathVariable String type,
                               @PathVariable String fileName) throws Exception {

        if (!type.equals("assignment") && !type.equals("submission") && !type.equals("section")) {
            ModelAndView mv = new ModelAndView("common/file-error");
            mv.addObject("errorMessage", "잘못된 파일 요청입니다.");
            return mv;
        }

        Resource directoryResource = resourceLoader.getResource("classpath:static/files/" + type);
        Path basePath;

        if (directoryResource.exists()) {
            basePath = directoryResource.getFile().toPath();
        } else {
            basePath = Paths.get("src/main/resources/static/files/" + type);
        }

        Path filePath = basePath.resolve(fileName).normalize();
        Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            ModelAndView mv = new ModelAndView("common/file-error");
            mv.addObject("errorMessage", "파일이 아직 등록되지 않았거나 삭제되었습니다.");
            return mv;
        }

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }
}
