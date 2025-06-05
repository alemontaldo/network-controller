package com.alesmontaldo.network_controller.domain.activity.persistence;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "activities")
public class RunDocument extends ActivityDocument {
    
    private int elevation;
    
    public RunDocument() {
        super();
        setType("RUN");
    }
    
    public RunDocument(String description, int elevation) {
        super(description, "RUN");
        this.elevation = elevation;
    }
    
    public int getElevation() {
        return elevation;
    }
    
    public void setElevation(int elevation) {
        this.elevation = elevation;
    }
}
