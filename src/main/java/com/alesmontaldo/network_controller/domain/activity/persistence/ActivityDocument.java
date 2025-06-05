package com.alesmontaldo.network_controller.domain.activity.persistence;

import com.alesmontaldo.network_controller.domain.comment.persistence.CommentDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "activities")
public abstract class ActivityDocument {
    
    @Id
    protected String id;
    protected String description;
    protected String type;
    protected List<CommentDocument> comments = new ArrayList<>();
    
    public ActivityDocument() {
    }
    
    public ActivityDocument(String description, String type) {
        this.description = description;
        this.type = type;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public List<CommentDocument> getComments() {
        return comments;
    }
    
    public void setComments(List<CommentDocument> comments) {
        this.comments = comments;
    }
    
    public void addComment(CommentDocument comment) {
        this.comments.add(comment);
    }
}
