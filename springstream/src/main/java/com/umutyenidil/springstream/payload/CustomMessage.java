package com.umutyenidil.springstream.payload;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomMessage {

    private String message;

    private boolean success = false;
}
