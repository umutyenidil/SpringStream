package com.umutyenidil.springstream.services.impl;

import com.umutyenidil.springstream.entities.Video;
import com.umutyenidil.springstream.repositories.VideoRepository;
import com.umutyenidil.springstream.services.VideoService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${files.hsl}")
    private String HSL_DIR;

    @PostConstruct
    public void init() {

        File file = new File(DIR);

        File file1 = new File(HSL_DIR);

        if (!file1.exists()) {
            boolean folderCreated = file1.mkdirs();

            log.info("HSL Folder {}", folderCreated ? "created" : "not created");
        }

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

            Video savedVideo = videoRepository.save(video);

            this.processVideo(video.getVideoId());



            return savedVideo;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return null;
    }

    @Override
    public Video get(
            String videoId
    ) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
    }

    @Override
    public Video getByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getAll() {
        return videoRepository.findAll();
    }

    @Override
    public String processVideo(
            String videoId
    ) {

        try {
            Video video = this.get(videoId);

            String videoFilePath = video.getFilePath();

            Path videoPath = Paths.get(videoFilePath);

            Path outputPath = Paths.get(HSL_DIR, videoId);

            Files.createDirectories(outputPath);

            String ffmpegCmd = String.format(
                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"  \"%s/master.m3u8\" ",
                    videoPath, outputPath, outputPath
            );

            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", ffmpegCmd);
            processBuilder.inheritIO();

            Process process = processBuilder.start();
            int exit = process.waitFor();
            if (exit != 0) {
                throw new RuntimeException("Video processing failed");
            }

            return videoId;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
