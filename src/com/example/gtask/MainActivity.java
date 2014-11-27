package com.example.gtask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;











import com.example.gtask.adapter.NavDrawerListAdapter;
import com.example.gtask.geofencing.GeofenceRemover;
import com.example.gtask.geofencing.GeofenceRequester;
import com.example.gtask.geofencing.GeofenceUtils;
import com.example.gtask.geofencing.MyLocation;
import com.example.gtask.geofencing.SimpleGeofence;
import com.example.gtask.geofencing.Task;
import com.example.gtask.geofencing.GeofenceUtils.REMOVE_TYPE;
import com.example.gtask.geofencing.GeofenceUtils.REQUEST_TYPE;
import com.example.gtask.geofencing.MyLocation.LocationResult;
import com.example.gtask.geofencing.SimpleGeofenceStore;
import com.example.gtask.model.NavDrawerItem;
import com.example.gtask.services.GPSPollerService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends Activity {
	// Main interface

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;
	private RelativeLayout mDrawerRelativeLayout;

	private static final long GEOFENCE_EXPIRATION_IN_HOURS = 24;
	private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS
			* DateUtils.HOUR_IN_MILLIS;

	// Store the current request
	private REQUEST_TYPE mRequestType;

	// Store the current type of removal
	public REMOVE_TYPE mRemoveType;

	// Persistent storage for geofences
	private SimpleGeofenceStore mPrefs;

	// Store a list of geofences to add
	public List<Geofence> mCurrentGeofences;

	// Add geofences handler
	public GeofenceRequester mGeofenceRequester;
	// Remove geofences handler
	public GeofenceRemover mGeofenceRemover;
	
	// Store the list of geofences to remove
    public List<String> mGeofenceIdsToRemove;
    
    /*
     * An instance of an inner class that receives broadcasts from listeners and from the
     * IntentService that receives geofence transition events
     */
    private GeofenceSampleReceiver mBroadcastReceiver;

    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		
		setContentView(R.layout.activity_main);

		mTitle = mDrawerTitle = getTitle();
		
		mBroadcastReceiver = new GeofenceSampleReceiver();
		
		// Create an intent filter for the broadcast receiver
        mIntentFilter = new IntentFilter();
		// Action for broadcast Intents that report successful addition of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

        // Action for broadcast Intents that report successful removal of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);

        // All Location Services sample apps use this category
        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
        
     // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<Geofence>();

        // Instantiate a Geofence requester
        mGeofenceRequester = new GeofenceRequester(this);

        // Instantiate a Geofence remover
        mGeofenceRemover = new GeofenceRemover(this);


		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerRelativeLayout = (RelativeLayout) findViewById(R.id.left_drawer);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// Home
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons
				.getResourceId(0, -1)));
		// Find People
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons
				.getResourceId(1, -1)));
		// Photos
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons
				.getResourceId(2, -1)));
		// Communities, Will add a counter here

		// Recycle the typed array
		navMenuIcons.recycle();

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);
		
		//requestWindowFeature(Window.FEATURE_ACTION_BAR);

		// enabling action bar app icon and behaving it as toggle button
		if(getActionBar()!=null){
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);
		}

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for
									// accessibility
				R.string.app_name // nav drawer close - description for
									// accessibility
		) {
			@Override
			public void onDrawerClosed(View view) {
				if(getActionBar()!=null)
					getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				if(getActionBar()!=null)
					getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		if (savedInstanceState == null) {
			// on first time display view for first nav item
			displayView(0);
		}
		
		/*
         * Record the request as an ADD. If a connection error occurs,
         * the app can automatically restart the add request if Google Play services
         * can fix the error
         */
        mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;

        /*
         * Check for Google Play services. Do this after
         * setting the request type. If connecting to Google Play services
         * fails, onActivityResult is eventually called, and it needs to
         * know what type of request was in progress.
         */
        if (!servicesConnected()) {

            return;
        }
        
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
					
					if(task.isActive()) //if task is active add a geofence
					{
						double val=task.getRadius();
						//Log.e(f.getName(),f.getName());
						String[] firstNames=f.getName().split("\\.");
						//Log.e(Arrays.toString(firstNames),Arrays.toString(firstNames));
						SimpleGeofence gf=new SimpleGeofence(firstNames[0].substring(4),task.getLatitude(),task.getLongitude(),(float)val,GEOFENCE_EXPIRATION_IN_MILLISECONDS,Geofence.GEOFENCE_TRANSITION_ENTER);
						mCurrentGeofences.add(gf.toGeofence());
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		try {
            // Try to add geofences
			if(mCurrentGeofences.size()!=0)
			{
				Log.e(mCurrentGeofences.size()+"",mCurrentGeofences.size()+"");
	            mGeofenceRequester.addGeofences(mCurrentGeofences);
			}
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, R.string.add_geofences_already_requested_error,
                        Toast.LENGTH_LONG).show();
        }

		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		getMenuInflater().inflate(R.menu.main_activity_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		case R.id.action_new:
			newTask();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	

	/***
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerRelativeLayout);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		if(getActionBar()!=null)
			getActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);
		}

	}

	private void newTask() {
		Intent intent = new Intent(this, NewTaskActivity.class);
		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				Intent refresh = new Intent(this, MainActivity.class);
				startActivity(refresh);
				this.finish();
			}
		} else if (requestCode == GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST) {
			switch (resultCode) {
			// If Google Play services resolved the problem
			case Activity.RESULT_OK:

				// If the request was to add geofences
				if (GeofenceUtils.REQUEST_TYPE.ADD == mRequestType) {

					// Toggle the request flag and send a new request
					mGeofenceRequester.setInProgressFlag(false);

					// Restart the process of adding the current geofences
					mGeofenceRequester.addGeofences(mCurrentGeofences);

					// If the request was to remove geofences
				} else if (GeofenceUtils.REQUEST_TYPE.REMOVE == mRequestType) {

					// Toggle the removal flag and send a new removal request
					mGeofenceRemover.setInProgressFlag(false);

					// If the removal was by Intent
					if (GeofenceUtils.REMOVE_TYPE.INTENT == mRemoveType) {

						// Restart the removal of all geofences for the
						// PendingIntent
						mGeofenceRemover
								.removeGeofencesByIntent(mGeofenceRequester
										.getRequestPendingIntent());

						// If the removal was by a List of geofence IDs
					} else {

						// Restart the removal of the geofence list
						mGeofenceRemover
								.removeGeofencesById(mGeofenceIdsToRemove);
					}
				}
				break;

			// If any other result was returned by Google Play services
			default:

				// Report that Google Play services was unable to resolve the
				// problem.
				Log.d(GeofenceUtils.APPTAG, getString(R.string.no_resolution));
			}
		}

	}

	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position) {
		case 0:
			fragment = new TasksToDoFragment();
			break;
		case 1:
			fragment = new TasksNearbyFragment();
			break;
		case 2:
			fragment = new TasksCompletedFragment();
			break;
		default:
			break;
		}

		if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.frame_container, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {

			// In debug mode, log the status
			Log.d(GeofenceUtils.APPTAG,
					getString(R.string.play_services_available));

			// Continue
			return true;

			// Google Play services was not available for some reason
		} else {

			// Display an error dialog
			return false;
		}

	}
	
	/**
     * Define a Broadcast receiver that receives updates from connection listeners and
     * the geofence transition service.
     */
    public class GeofenceSampleReceiver extends BroadcastReceiver {
        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Check the action code and determine what to do
            String action = intent.getAction();

            // Intent contains information about errors in adding or removing geofences
            if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

                handleGeofenceError(context, intent);

            // Intent contains information about successful addition or removal of geofences
            } else if (
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)
                    ||
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

                handleGeofenceStatus(context, intent);

            // Intent contains information about a geofence transition
            } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

                handleGeofenceTransition(context, intent);

            // The Intent contained an invalid action
            } else {
                Log.e(GeofenceUtils.APPTAG, getString(R.string.invalid_action_detail, action));
                Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_LONG).show();
            }
        }

        /**
         * If you want to display a UI message about adding or removing geofences, put it here.
         *
         * @param context A Context for this component
         * @param intent The received broadcast Intent
         */
        private void handleGeofenceStatus(Context context, Intent intent) {

        }

        /**
         * Report geofence transitions to the UI
         *
         * @param context A Context for this component
         * @param intent The Intent containing the transition
         */
        private void handleGeofenceTransition(Context context, Intent intent) {
            /*
             * If you want to change the UI when a transition occurs, put the code
             * here. The current design of the app uses a notification to inform the
             * user that a transition has occurred.
             */
        }

        /**
         * Report addition or removal errors to the UI, using a Toast
         *
         * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
         */
        private void handleGeofenceError(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            Log.e(GeofenceUtils.APPTAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    
    public void openViewTask(View view)
    {
    	
    	Intent in=new Intent(this,ViewTaskActivity.class);
    	startActivity(in);
    }
    
    //when switch is clicked
    public void onClickSwitch(View view)
    {
    	Switch switch1=(Switch)view;
    	SharedPreferences settings = this.getApplicationContext().getSharedPreferences("com.example.gtask.number", Context.MODE_PRIVATE);
    	
    	settings.edit().putBoolean("switchStatus", switch1.isChecked()).apply();
    	
    	GPSPollerService.stop=true; //stops the GPS polling service by setting stop flag to true
    	if(switch1.isChecked())
    	{
    		GPSPollerService.stop=false;
    		startService(new Intent(this, GPSPollerService.class));
    	}
    }

}