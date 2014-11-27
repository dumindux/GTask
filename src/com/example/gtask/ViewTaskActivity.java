package com.example.gtask;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import com.example.gtask.geofencing.Task;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;



/**
 * @author Dumindu Buddhika
 *
 */
public class ViewTaskActivity extends Activity {
	
	GoogleMap mMap;
	Circle circle;
	TextView latitude, longitude, radius, title, description;
	CheckBox repeated;
	String id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_task);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		id=getIntent().getStringExtra("id");
		setTitle("View Task");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_task, menu);
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
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState)
	{
		latitude = (TextView) this.findViewById(R.id.latitude);
		longitude = (TextView) this.findViewById(R.id.longitude);
		radius = (TextView) this.findViewById(R.id.radiusEdit);
		title = (TextView) this.findViewById(R.id.title);
		description = (TextView) this.findViewById(R.id.description);
		repeated=(CheckBox)this.findViewById(R.id.repeat);
		
		Fragment frag = getFragmentManager().findFragmentById(R.id.container);

		MapFragment mf = (MapFragment) frag.getFragmentManager()
				.findFragmentById(R.id.map);
		mMap = mf.getMap();
		Task task=null;
		
		try {
			FileInputStream fos = new FileInputStream(Environment
					.getExternalStorageDirectory().getPath()
					+ "/Gtask/Tasks/" + id);
			ObjectInputStream read = new ObjectInputStream(fos);

			task = (Task) read.readObject();
			read.close();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//loads data to views from the file relevant to the task
		latitude.setText(task.getLatitude()+"");
		longitude.setText(task.getLongitude()+"");
		radius.setText(task.getRadius()+"");
		title.setText(task.getTitle());
		description.setText(task.getDescription());
		repeated.setChecked(task.isRepeated());
		
		MarkerOptions mo = new MarkerOptions().position(new LatLng(task.getLatitude(),task.getLongitude()));
		mMap.addMarker(mo);
		circle = mMap.addCircle(new CircleOptions()
				.center(new LatLng(task.getLatitude(),task.getLongitude()))
				.fillColor(Color.argb(50, 0, 0, 255))
				.strokeWidth(0)
				.radius(Double.parseDouble(radius.getText().toString())));
		
		CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(new LatLng(task.getLatitude(),task.getLongitude()), 15);
	    mMap.animateCamera(yourLocation);
	    
		super.onPostCreate(savedInstanceState);
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
			View rootView = inflater.inflate(R.layout.fragment_view_task,
					container, false);
			return rootView;
		}
	}

}
