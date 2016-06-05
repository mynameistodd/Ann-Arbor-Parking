package com.sliverbit.annarborparking.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tdeland on 3/5/16.
 */
public class Availability {
    private String facility;
    @SerializedName("spacesavail")
    private String spacesavailable;
    private String timestamp;

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public String getSpacesavailable() {
        return spacesavailable;
    }

    public void setSpacesavailable(String spacesavailable) {
        this.spacesavailable = spacesavailable;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
