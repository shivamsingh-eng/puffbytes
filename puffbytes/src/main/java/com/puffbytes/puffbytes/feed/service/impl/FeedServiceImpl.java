package com.puffbytes.puffbytes.feed.service.impl;

import com.puffbytes.puffbytes.feed.dto.FeedDTO;
import com.puffbytes.puffbytes.feed.service.interfaces.FeedService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class FeedServiceImpl implements FeedService {

    @Override
    public List<FeedDTO> getFeedForUser(String userId) {
        return Collections.emptyList();
    }
}
