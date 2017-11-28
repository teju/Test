package com.biker.model;

/**
 * Created by nz160 on 04-10-2017.
 */

public class BookingList {
    public String getBooking_no() {
        return booking_no;
    }

    public void setBooking_no(String booking_no) {
        this.booking_no = booking_no;
    }

    public String getVehicle_no() {
        return vehicle_no;
    }

    public void setVehicle_no(String vehicle_no) {
        this.vehicle_no = vehicle_no;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmail_id() {
        return email_id;
    }

    public void setEmail_id(String email_id) {
        this.email_id = email_id;
    }

    public String getBooked_on() {
        return booked_on;
    }

    public void setBooked_on(String booked_on) {
        this.booked_on = booked_on;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    String booking_no;
    String vehicle_no;
    String status;
    String email_id;

    public String getConfirm_payment() {
        return confirm_payment;
    }

    public void setConfirm_payment(String confirm_payment) {
        this.confirm_payment = confirm_payment;
    }

    String confirm_payment;

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    String rating;

    public String getIs_paid() {
        return is_paid;
    }

    public void setIs_paid(String is_paid) {
        this.is_paid = is_paid;
    }

    String is_paid;

    public String getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(String booking_id) {
        this.booking_id = booking_id;
    }

    String booked_on;
    String address;
    String booking_id;

    public String getVendor_id() {
        return vendor_id;
    }

    public void setVendor_id(String vendor_id) {
        this.vendor_id = vendor_id;
    }

    String vendor_id;

    public String getVendor_name() {
        return vendor_name;
    }

    public void setVendor_name(String vendor_name) {
        this.vendor_name = vendor_name;
    }

    String vendor_name;

    public String getVendor_nuber() {
        return vendor_nuber;
    }

    public void setVendor_nuber(String vendor_nuber) {
        this.vendor_nuber = vendor_nuber;
    }

    String vendor_nuber;
}
