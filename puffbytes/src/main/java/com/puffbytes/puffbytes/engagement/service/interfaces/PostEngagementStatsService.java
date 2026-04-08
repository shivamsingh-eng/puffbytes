package com.puffbytes.puffbytes.engagement.service.interfaces;

import com.puffbytes.puffbytes.engagement.entity.PostEngagementStats;

import java.util.List;
import java.util.Map;

public interface PostEngagementStatsService {
    Map<String, PostEngagementStats> getStatsByPostIds(List<String> postIds);
}
