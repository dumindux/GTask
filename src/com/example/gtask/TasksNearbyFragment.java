package com.example.gtask;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import com.example.gtask.geofencing.Task;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

public class TasksNearbyFragment extends Fragment {
	
	private static View view;
	private GoogleMap mMap;
    
     
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
  
        /*View rootView = inflater.inflate(R.layout.fragment_tasks_nearby, container, false);
          
        return rootView;*/
    	
    	if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_tasks_nearby, container, false);
        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }
        
        //Fragment frag = getFragmentManager().findFragmentById(R.id.container);
        MapFragment mf = (MapFragment) this.getFragmentManager()
				.findFragmentById(R.id.map);
		mMap = mf.getMap();
        
        mMap.setMyLocationEnabled(true);
        
        File sdCardRoot = Environment.getExternalStorageDirectory();
		File yourDir = new File(sdCardRoot, "Gtask/Tasks");
		for (File f : yourDir.listFiles()) {
			if (f.isFile())
				try {
					// Reads task file
					FileInputStream fos = new FileInputStream(Environment
							.getExternalStorageDirectory().getPath()
							+ "/Gtask/Tasks/" + f.getName());
					ObjectInputStream read = new ObjectInputStream(fos);

					Task task = (Task) read.readObject();
					read.close();
					if(task.isActive())
					{
						MarkerOptions mo = new MarkerOptions().position(new LatLng(task.getLatitude(),task.getLongitude()));
						Marker marker = mMap.addMarker(mo);
						marker.setTitle(task.getTitle());
						
					}
					
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			
		}
        
        

        try{
	        Criteria criteria = new Criteria();
		    LocationManager locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
		    String provider = locationManager.getBestProvider(criteria, false);
		    Location location = locationManager.getLastKnownLocation(provider);
		    LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());
		    CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 14);
		    mMap.animateCamera(yourLocation);
        }
        catch(Exception e){
        	LatLng coordinate = new LatLng(7.38, 80.77);
		    
		    CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 7);
		    mMap.animateCamera(yourLocation);
        }  
        
        Switch highAccuracy=(Switch)view.findViewById(R.id.switch1);
		
		SharedPreferences settings = getActivity().getApplicationContext().getSharedPreferences("com.example.gtask.number", Context.MODE_PRIVATE);
		boolean switchStatus = settings.getBoolean("switchStatus", false);
		
		highAccuracy.setChecked(switchStatus);
		
        return view;
    }
    

    
   /* @Override
    public void onDestroyView()
    {
    	super.onDestroy();
    	Fragment fragment = (getFragmentManager().findFragmentById(R.id.map));
    	FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
    	ft.remove(fragment);
    	ft.commit();
    	
    }*/
}
