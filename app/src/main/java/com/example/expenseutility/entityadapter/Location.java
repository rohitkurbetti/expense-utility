package com.example.expenseutility.entityadapter;

public class Location {
    private String country;
    private String city;
    private String description;
    private String imageUrl;



    public Location(String country, String city, String imageUrl) {
        this.country = country;
        this.city = city;
        this.imageUrl = imageUrl;
    }


    public Location(String country, String city, String description, String imageUrl) {
        this.country = country;
        this.city = city;
        this.description = description;
        this.imageUrl = imageUrl;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
