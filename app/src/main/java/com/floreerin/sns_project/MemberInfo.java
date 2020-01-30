package com.floreerin.sns_project;

public class MemberInfo { // 커스텀 객체 생성
    private String name;
    private String phone;
    private String date;
    private String address;
    private String photoUrl;

    public MemberInfo(String name, String phone, String date, String address, String photoUrl) {
        this.name = name;
        this.phone = phone;
        this.date = date;
        this.address = address;
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
