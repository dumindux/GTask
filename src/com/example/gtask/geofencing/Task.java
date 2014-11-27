package com.example.gtask.geofencing;

import java.io.Serializable;

public class Task implements Serializable{
	
	private Double latitude;
	private Double longitude;
	private Double radius;
	private String title;
	private String description;
	private String picture;
	private boolean isRepeated;
	private boolean active;
	
	
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean isRepeated() {
		return isRepeated;
	}
	public void setRepeated(boolean isRepeated) {
		this.isRepeated = isRepeated;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPicture() {
		return picture;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public Double getRadius() {
		return radius;
	}
	public void setRadius(Double radius) {
		this.radius = radius;
	}
	
	
}
