package com.alesmontaldo.network_controller.domain.activity.persistence;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "activities")
public class RowingDocument extends ActivityDocument {
    
    private int split;
    
    public RowingDocument() {
        super();
        setType("ROWING");
    }
    
    public RowingDocument(String description, int split) {
        super(description, "ROWING");
        this.split = split;
    }
    
    public int getSplit() {
        return split;
    }
    
    public void setSplit(int split) {
        this.split = split;
    }
}
