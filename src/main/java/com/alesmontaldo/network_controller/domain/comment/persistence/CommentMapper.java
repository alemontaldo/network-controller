package com.alesmontaldo.network_controller.domain.comment.persistence;

import com.alesmontaldo.network_controller.domain.comment.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface CommentMapper {

    Comment toComment(CommentDocument document);

    CommentDocument toDocument(Comment comment);
}
