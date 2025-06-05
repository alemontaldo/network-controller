package com.alesmontaldo.network_controller.domain.activity;

import com.alesmontaldo.network_controller.domain.comment.Comment;
import java.util.List;

public interface Activity {

	String id();

	String description();

	List<Comment> comments();

}
