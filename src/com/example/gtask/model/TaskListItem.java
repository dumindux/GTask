package com.example.gtask.model;



public class TaskListItem {
	
	private String title;
	private String description;
	private int icon;
	private String bMap;
	private String id;
	
	public TaskListItem(String title, String description, int icon,String id) {
		super();
		this.title = title;
		this.description = description;
		this.icon = icon;
		this.id=id;
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

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getbMap() {
		return bMap;
	}

	public void setbMap(String bMap) {
		this.bMap = bMap;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	

}
