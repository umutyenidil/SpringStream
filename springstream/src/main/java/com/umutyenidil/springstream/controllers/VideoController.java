package com.umutyenidil.springstream.controllers;

import ch.qos.logback.core.boolex.EvaluationException;
import com.umutyenidil.springstream.common.Constants;
import com.umutyenidil.springstream.entities.Video;
import com.umutyenidil.springstream.payload.CustomMessage;
import com.umutyenidil.springstream.services.VideoService;
import com.umutyenidil.springstream.services.impl.VideoServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(
        "/api/v1/videos"
)
public class VideoController {

    private final VideoService videoService;

    @Autowired
    public VideoController(VideoServiceImpl videoServiceImpl) {
        this.videoService = videoServiceImpl;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description
    ) {
        Video video = Video.builder()
                .title(title)
                .description(description)
                .videoId(UUID.randomUUID().toString())
                .build();

        Video savedVideo = videoService.save(video, file);

        if (savedVideo == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            CustomMessage.builder()
                                    .message("Something went wrong")
                                    .success(false)
                                    .build()
                    );
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(video);
    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> stream(
            @PathVariable String videoId
    ) {
        Video video = videoService.get(videoId);

        String contentType = video.getContentType();
        if (contentType == null) contentType = "application/octet-stream";

        String filePath = video.getFilePath();

        Resource resource = new FileSystemResource(filePath);

        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping
    public ResponseEntity<List<Video>> getAll() {
        return ResponseEntity
                .ok()
                .body(videoService.getAll());
    }

    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> streamVideoRange(
            @PathVariable String videoId,
            @RequestHeader(value = "Range", required = false) String range
    ) {
        Video video = videoService.get(videoId);

        String contentType = video.getContentType();
        if (contentType == null) contentType = "application/octet-stream";

        Path path = Paths.get(video.getFilePath());

        Resource resource = new FileSystemResource(path);

        long fileLength = path.toFile().length();

        if (range == null) {
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        }

        long rangeStart, rangeEnd;

        String[] ranges = range.replace("bytes=", "").split("-");

        rangeStart = Long.parseLong(ranges[0]);

        rangeEnd = rangeStart + Constants.CHUNK_SIZE - 1;

        if (rangeEnd >= fileLength) {
            rangeEnd = fileLength - 1;
        }

//        if (range.length() > 1) {
//            rangeEnd = Long.parseLong(ranges[1]);
//        } else {
//            rangeEnd = fileLength - 1;
//        }
//
//        if (rangeEnd > fileLength - 1) {
//            rangeEnd = fileLength - 1;
//        }

        InputStream inputStream;

        try {
            inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);

            long contentLength = rangeEnd - rangeStart + 1;

            byte[] data = new byte[(int) contentLength];
            int read = inputStream.read(data, 0, data.length);


            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Range", "bytes=" + rangeStart + "-" + rangeEnd + "/" + fileLength);
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("X-Content-Type-Options", "nosniff");
            headers.setContentLength(contentLength);

            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data));
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
