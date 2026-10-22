package com.campus.app.dto;


public class BudgetPredictionRequest {

    private String activity_type;

    private int participants_expected;

    private int duration_days;

    private int volunteers;

    private String venue_type;

    private int food_required;

    private int printing_required;

    private int certificates_required;

    private int prizes_required;

    private int equipment_required;

    private int guest_required;

    private int previous_similar_events;

    private double previous_avg_cost;


    // Getter and Setter for activity_type

    public String getActivity_type() {
        return activity_type;
    }

    public void setActivity_type(String activity_type) {
        this.activity_type = activity_type;
    }


    // Getter and Setter for participants_expected

    public int getParticipants_expected() {
        return participants_expected;
    }

    public void setParticipants_expected(int participants_expected) {
        this.participants_expected = participants_expected;
    }


    // Getter and Setter for duration_days

    public int getDuration_days() {
        return duration_days;
    }

    public void setDuration_days(int duration_days) {
        this.duration_days = duration_days;
    }


    // Getter and Setter for volunteers

    public int getVolunteers() {
        return volunteers;
    }

    public void setVolunteers(int volunteers) {
        this.volunteers = volunteers;
    }


    // Getter and Setter for venue_type

    public String getVenue_type() {
        return venue_type;
    }

    public void setVenue_type(String venue_type) {
        this.venue_type = venue_type;
    }


    // Getter and Setter for food_required

    public int getFood_required() {
        return food_required;
    }

    public void setFood_required(int food_required) {
        this.food_required = food_required;
    }


    // Getter and Setter for printing_required

    public int getPrinting_required() {
        return printing_required;
    }

    public void setPrinting_required(int printing_required) {
        this.printing_required = printing_required;
    }


    // Getter and Setter for certificates_required

    public int getCertificates_required() {
        return certificates_required;
    }

    public void setCertificates_required(int certificates_required) {
        this.certificates_required = certificates_required;
    }


    // Getter and Setter for prizes_required

    public int getPrizes_required() {
        return prizes_required;
    }

    public void setPrizes_required(int prizes_required) {
        this.prizes_required = prizes_required;
    }


    // Getter and Setter for equipment_required

    public int getEquipment_required() {
        return equipment_required;
    }

    public void setEquipment_required(int equipment_required) {
        this.equipment_required = equipment_required;
    }


    // Getter and Setter for guest_required

    public int getGuest_required() {
        return guest_required;
    }

    public void setGuest_required(int guest_required) {
        this.guest_required = guest_required;
    }


    // Getter and Setter for previous_similar_events

    public int getPrevious_similar_events() {
        return previous_similar_events;
    }

    public void setPrevious_similar_events(int previous_similar_events) {
        this.previous_similar_events = previous_similar_events;
    }


    // Getter and Setter for previous_avg_cost

    public double getPrevious_avg_cost() {
        return previous_avg_cost;
    }

    public void setPrevious_avg_cost(double previous_avg_cost) {
        this.previous_avg_cost = previous_avg_cost;
    }
}