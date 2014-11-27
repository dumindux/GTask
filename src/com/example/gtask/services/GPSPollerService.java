package com.example.gtask.services;

import com.example.gtask.geofencing.MyLocation;
import com.example.gtask.geofencing.MyLocation.LocationResult;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

public class GPSPollerService extends IntentService {
	
	public static boolean stop=false;


	public GPSPollerService() {
		super("GPS Polling service");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		
		while(!stop){
			LocationResult locationResult = new LocationResult(){
			    @Override
			    public void gotLocation(Location location){
			        //Got the location!
			    }
			};
			MyLocation myLocation = new MyLocation();
			myLocation.getLocation(this, locationResult);
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

}
