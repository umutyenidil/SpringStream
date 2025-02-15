package com.umutyenidil.springstream.controllers;

import com.umutyenidil.springstream.entities.Video;
import com.umutyenidil.springstream.payload.CustomMessage;
import com.umutyenidil.springstream.services.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/videos"
)
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

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

        if (savedVideo == null){
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
}
