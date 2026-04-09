package com.puffbytes.puffbytes.feed.service.interfaces;

import com.puffbytes.puffbytes.feed.dto.FeedDTO;

import java.util.List;

public interface FeedService {

    List<FeedDTO> getFeed(String userId, int page, int size);
}