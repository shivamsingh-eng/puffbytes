package com.puffbytes.puffbytes.engagement.service.impl;

import com.puffbytes.puffbytes.engagement.entity.PostEngagementStats;
import com.puffbytes.puffbytes.engagement.repository.PostEngagementStatsRepository;
import com.puffbytes.puffbytes.engagement.service.interfaces.PostEngagementStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostEngagementStatsServiceImpl implements PostEngagementStatsService {
    private final PostEngagementStatsRepository postEngagementStatsRepository;

    @Override
    public Map<String, PostEngagementStats> getStatsByPostIds(List<String> postIds) {

        List<PostEngagementStats> statsList = postEngagementStatsRepository.findByPostIdIn(postIds);

        return statsList.stream()
                .collect(Collectors.toMap(
                        PostEngagementStats::getPostId,
                        stats -> stats
                ));
    }
}
