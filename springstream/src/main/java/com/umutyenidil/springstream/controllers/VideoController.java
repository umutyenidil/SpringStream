package com.umutyenidil.springstream.controllers;

import com.umutyenidil.springstream.entities.Video;
import com.umutyenidil.springstream.payload.CustomMessage;
import com.umutyenidil.springstream.services.VideoService;
import com.umutyenidil.springstream.services.impl.VideoServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<List<Video>> getAll(){
        return ResponseEntity
                .ok()
                .body(videoService.getAll());
    }
}
