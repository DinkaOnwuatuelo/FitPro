package com.example.fitpro;

public class User {
    public String fullName;
    public String email;
    public String dateOfBirth;
    public String gender;
    public double height;
    public double weight;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {
    }

    // Constructor for all user data
    public User(String fullName, String email, String dateOfBirth, String gender, double height, double weight) {
        this.fullName = fullName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
    }

    // Getter for dateOfBirth
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    // Getter for gender
    public String getGender() {
        return gender;
    }

    // Getter for height
    public double getHeight() {
        return height;
    }

    // Getter for weight
    public double getWeight() {
        return weight;
    }

}
