package com.alesmontaldo.network_controller.domain.activity.persistence;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "activities")
public class SwimDocument extends ActivityDocument {
    
    private int laps;
    private boolean indoor;
    
    public SwimDocument() {
        super();
        setType("SWIM");
    }
    
    public SwimDocument(String description, int laps, boolean indoor) {
        super(description, "SWIM");
        this.laps = laps;
        this.indoor = indoor;
    }
    
    public int getLaps() {
        return laps;
    }
    
    public void setLaps(int laps) {
        this.laps = laps;
    }
    
    public boolean isIndoor() {
        return indoor;
    }
    
    public void setIndoor(boolean indoor) {
        this.indoor = indoor;
    }
}
