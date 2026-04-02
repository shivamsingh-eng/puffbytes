package com.puffbytes.puffbytes.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MediaUploadResponseDTO { //this response will be sent when we upload file(image,video,etc)

    private String id;
    private String mediaUrl;
}