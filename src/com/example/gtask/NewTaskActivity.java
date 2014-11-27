package com.example.gtask;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

//import com.example.gtask.R;
import com.example.gtask.geofencing.Task;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class NewTaskActivity extends Activity {
	//creates a new task

	GoogleMap mMap;
	Marker marker;
	Circle circle;
	TextView latitude, longitude;
	EditText radius, title, description;
	CheckBox repeated;
	int numberOfTasks=-1;
	boolean isNewTask=false;
	SharedPreferences settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_task);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment(), "tag")
					.commit();

		}

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {

		latitude = (TextView) this.findViewById(R.id.latitude);
		longitude = (TextView) this.findViewById(R.id.longitude);
		radius = (EditText) this.findViewById(R.id.radiusEdit);
		title = (EditText) this.findViewById(R.id.title);
		description = (EditText) this.findViewById(R.id.description);
		repeated=(CheckBox)this.findViewById(R.id.repeat);
			

		radius.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// Updates radius on the map
				if (circle != null && arg0!=null && !arg0.toString().trim().equals("")) {
					circle.setRadius(Double.parseDouble(arg0.toString()));
				}

			}

		}); // adds a listener to catch text changed event to update it on the
			// map

		Fragment frag = getFragmentManager().findFragmentById(R.id.container);

		MapFragment mf = (MapFragment) frag.getFragmentManager()
				.findFragmentById(R.id.map);
		mMap = mf.getMap();
		
		mMap.setMyLocationEnabled(true);
		
		
		numberOfTasks=getIntent().getIntExtra("id",-1); //check whether task needs to be edited or newly created
		if(numberOfTasks==-1)
		{
			isNewTask=true;
			settings = getApplicationContext().getSharedPreferences("com.example.gtask.number", Context.MODE_PRIVATE);
			numberOfTasks = settings.getInt("numberOfTasks", 0); //get the sequence number to save next task
			
		}
		
		if(isNewTask)
		{
			//Zoom in to last known location
			try{
				Criteria criteria = new Criteria();
			    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			    String provider = locationManager.getBestProvider(criteria, false);
			    Location location = locationManager.getLastKnownLocation(provider);
			    LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());
			    
			    CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
			    mMap.animateCamera(yourLocation);
			}
			catch(Exception e)
			{
				
			    LatLng coordinate = new LatLng(7.38, 80.77);
			    
			    CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 7);
			    mMap.animateCamera(yourLocation);
			}
		}
		else
		{
			
			setTitle("Edit Task");
			try {
				FileInputStream fos = new FileInputStream(Environment
						.getExternalStorageDirectory().getPath()
						+ "/Gtask/Tasks/task" + numberOfTasks+".gtsk");
				ObjectInputStream read;
				read = new ObjectInputStream(fos);
				Task task = (Task) read.readObject();
				read.close();
				
				//sets values of the fields as the task being edited
				latitude.setText(task.getLatitude()+"");
				longitude.setText(task.getLongitude()+"");
				radius.setText(task.getRadius()+"");
				title.setText(task.getTitle());
				description.setText(task.getDescription());
				repeated.setChecked(task.isRepeated());
				
				MarkerOptions mo = new MarkerOptions().position(new LatLng(task.getLatitude(),task.getLongitude()));
				marker = mMap.addMarker(mo);
				circle = mMap.addCircle(new CircleOptions()
						.center(new LatLng(task.getLatitude(),task.getLongitude()))
						.fillColor(Color.argb(50, 0, 0, 255))
						.strokeWidth(0)
						.radius(task.getRadius()));
				
				CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(new LatLng(task.getLatitude(),task.getLongitude()), 15);
			    mMap.animateCamera(yourLocation);
				
			} catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}
		
		
		mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

			@Override
			public void onMapClick(LatLng point) {
				// TODO Auto-generated method stub
				// lstLatLngs.add(point);
				if (marker != null) {
					marker.remove();
					circle.remove();
				}
				MarkerOptions mo = new MarkerOptions().position(point);
				marker = mMap.addMarker(mo);
				circle = mMap.addCircle(new CircleOptions()
						.center(point)
						.fillColor(Color.argb(50, 0, 0, 255))
						.strokeWidth(0)
						.radius(Double.parseDouble(radius.getText().toString())));
				latitude.setText(marker.getPosition().latitude + "");
				longitude.setText(marker.getPosition().longitude + "");
				mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
			}
		});
		super.onPostCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_task, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		if (id == R.id.action_done) {
			Log.e("sdf", "Sdg");
			if (circle != null && !title.getText().toString().trim().equals("") && !radius.getText().toString().trim().equals("")) {
				
				
				
				captureMapScreen(numberOfTasks);
				Task newTask = new Task();
				newTask.setLatitude(circle.getCenter().latitude);
				newTask.setLongitude(circle.getCenter().longitude);
				newTask.setRadius(circle.getRadius());
				newTask.setTitle(title.getText().toString());
				newTask.setDescription(description.getText().toString());
				newTask.setPicture("Image"+numberOfTasks+".jpg");
				newTask.setRepeated(repeated.isChecked());
				newTask.setActive(true);
				
				
				Log.e(numberOfTasks+"", numberOfTasks+"");
				

				try {
					FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/Gtask/Tasks/task"+numberOfTasks+".gtsk" );
					ObjectOutputStream save = new ObjectOutputStream(fos);
					save.writeObject( newTask );
					save.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				if(isNewTask)
					settings.edit().putInt("numberOfTasks", numberOfTasks+1).apply();

				setResult(RESULT_OK, null);
				finish(); // goes back to the parent activity
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_new_task,
					container, false);

			return rootView;
		}
	}

	public void captureMapScreen(final int num) {
		// get a snapshot of the google map;

		SnapshotReadyCallback callback = new SnapshotReadyCallback() {
			Bitmap bitmap;
			String filename="Image"+num;
			@Override
			public void onSnapshotReady(Bitmap snapshot) {
				// TODO Auto-generated method stub
				bitmap = snapshot;

				try {///mnt/sdcard/gtask
					FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/Gtask/Images/"+filename+".jpg");

					// above "/mnt ..... png" => is a storage path (where image
					// will be stored) + name of image you can customize as per
					// your Requirement

					bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		mMap.snapshot(callback);
	}

}
