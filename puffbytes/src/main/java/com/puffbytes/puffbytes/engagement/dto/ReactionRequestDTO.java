package com.puffbytes.puffbytes.engagement.dto;

import com.puffbytes.puffbytes.engagement.enums.ReactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReactionRequestDTO {

    @NotNull(message = "Reaction type is required")
    private ReactionType reactionType;
}