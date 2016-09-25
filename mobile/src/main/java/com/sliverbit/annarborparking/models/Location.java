package com.sliverbit.annarborparking.models;

import android.support.annotation.Keep;

/**
 * Created by tdeland on 3/5/16.
 */
@Keep
public class Location {
    private String latitude;
    private String longitude;
    private String locationCode;
    private String location;
    private String address;
    private String numberOfSpaces;
    private String hgtRestriction;
    private String publicParking;
    private String eveningsWkndParking;
    private String quickPay;
    private String monthlyParking;
    private String discountCouponsAccepted;
    private String handicapSpaces;
    private String eVCharging;
    private String bicycleParking;
    private String mopedParking;
    private String note;

    public Location(String[] fields) {
        this.latitude = fields[0];
        this.longitude = fields[1];
        this.locationCode = fields[2];
        this.location = fields[3];
        this.address = fields[4];
        this.numberOfSpaces = fields[5];
        this.hgtRestriction = fields[6];
        this.publicParking = fields[7];
        this.eveningsWkndParking = fields[8];
        this.quickPay = fields[9];
        this.monthlyParking = fields[10];
        this.discountCouponsAccepted = fields[11];
        this.handicapSpaces = fields[12];
        this.eVCharging = fields[13];
        this.bicycleParking = fields[14];
        this.mopedParking = fields[15];
        this.note = fields[16];
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNumberOfSpaces() {
        return numberOfSpaces;
    }

    public void setNumberOfSpaces(String numberOfSpaces) {
        this.numberOfSpaces = numberOfSpaces;
    }

    public String getHgtRestriction() {
        return hgtRestriction;
    }

    public void setHgtRestriction(String hgtRestriction) {
        this.hgtRestriction = hgtRestriction;
    }

    public String getPublicParking() {
        return publicParking;
    }

    public void setPublicParking(String publicParking) {
        this.publicParking = publicParking;
    }

    public String getEveningsWkndParking() {
        return eveningsWkndParking;
    }

    public void setEveningsWkndParking(String eveningsWkndParking) {
        this.eveningsWkndParking = eveningsWkndParking;
    }

    public String getQuickPay() {
        return quickPay;
    }

    public void setQuickPay(String quickPay) {
        this.quickPay = quickPay;
    }

    public String getMonthlyParking() {
        return monthlyParking;
    }

    public void setMonthlyParking(String monthlyParking) {
        this.monthlyParking = monthlyParking;
    }

    public String getDiscountCouponsAccepted() {
        return discountCouponsAccepted;
    }

    public void setDiscountCouponsAccepted(String discountCouponsAccepted) {
        this.discountCouponsAccepted = discountCouponsAccepted;
    }

    public String getHandicapSpaces() {
        return handicapSpaces;
    }

    public void setHandicapSpaces(String handicapSpaces) {
        this.handicapSpaces = handicapSpaces;
    }

    public String geteVCharging() {
        return eVCharging;
    }

    public void seteVCharging(String eVCharging) {
        this.eVCharging = eVCharging;
    }

    public String getBicycleParking() {
        return bicycleParking;
    }

    public void setBicycleParking(String bicycleParking) {
        this.bicycleParking = bicycleParking;
    }

    public String getMopedParking() {
        return mopedParking;
    }

    public void setMopedParking(String mopedParking) {
        this.mopedParking = mopedParking;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
