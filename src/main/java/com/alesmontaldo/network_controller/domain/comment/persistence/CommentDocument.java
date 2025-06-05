package com.alesmontaldo.network_controller.domain.comment.persistence;

public class CommentDocument {
    
    private String text;
    
    public CommentDocument() {
    }
    
    public CommentDocument(String text) {
        this.text = text;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
}
