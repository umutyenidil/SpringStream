package com.umutyenidil.springstream.services.impl;

import com.umutyenidil.springstream.entities.Video;
import com.umutyenidil.springstream.repositories.VideoRepository;
import com.umutyenidil.springstream.services.VideoService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;

    @Value("${files.video}")
    private String DIR;

    @PostConstruct
    public void init() {
        File file = new File(DIR);

        if (!file.exists()) {
            boolean folderCreated = file.mkdirs();

            log.info("Folder {}", folderCreated ? "created" : "not created");
        }
    }

    private String generateUniqueFilename(MultipartFile file) {
        // Get the original filename
        String originalFilename = file.getOriginalFilename();

        // Generate a unique UUID
        String uuid = UUID.randomUUID().toString();

        // Get the file extension
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Combine the UUID with the file extension
        return uuid + extension;
    }

    @Override
    public Video save(
            Video video,
            MultipartFile file
    ) {

        try {

            String filename = generateUniqueFilename(file);
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();

            String cleanFileName = StringUtils.cleanPath(filename);
            String cleanFolder = StringUtils.cleanPath(DIR);

            Path path = Paths.get(cleanFolder, cleanFileName);

            Files.copy(
                    inputStream,
                    path,
                    StandardCopyOption.REPLACE_EXISTING
            );

            video.setContentType(contentType);
            video.setFilePath(path.toString());

            videoRepository.save(video);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return null;
    }

    @Override
    public Video get(String videoId) {
        return null;
    }

    @Override
    public Video getByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getAll() {
        return List.of();
    }
}
