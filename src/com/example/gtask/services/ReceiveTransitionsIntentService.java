package com.example.gtask.services;

import com.example.gtask.MainActivity;
import com.example.gtask.R;
import com.example.gtask.geofencing.GeofenceUtils;
import com.example.gtask.geofencing.LocationServiceErrorMessages;
import com.example.gtask.geofencing.Task;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.List;

/**
 * This class receives geofence transition events from Location Services, in the
 * form of an Intent containing the transition type and geofence id(s) that
 * triggered the event.
 */
public class ReceiveTransitionsIntentService extends IntentService {

	/**
	 * Sets an identifier for this class' background thread
	 */
	public ReceiveTransitionsIntentService() {
		super("ReceiveTransitionsIntentService");
	}

	/**
	 * Handles incoming intents
	 * 
	 * @param intent
	 *            The Intent sent by Location Services. This Intent is provided
	 *            to Location Services (inside a PendingIntent) when you call
	 *            addGeofences()
	 */
	@Override
	protected void onHandleIntent(Intent intent) {

		// Create a local broadcast Intent
		Intent broadcastIntent = new Intent();

		// Give it the category for all intents sent by the Intent Service
		broadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

		// First check for errors
		if (LocationClient.hasError(intent)) {

			// Get the error code
			int errorCode = LocationClient.getErrorCode(intent);

			// Get the error message
			String errorMessage = LocationServiceErrorMessages.getErrorString(
					this, errorCode);

			// Log the error
			try {
				Log.e(GeofenceUtils.APPTAG,
						getString(R.string.geofence_transition_error_detail,
								errorMessage));
			} catch (Exception e) {

			}

			// Set the action and error message for the broadcast intent
			broadcastIntent
					.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR)
					.putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, errorMessage);

			// Broadcast the error *locally* to other components in this app
			LocalBroadcastManager.getInstance(this).sendBroadcast(
					broadcastIntent);

			// If there's no error, get the transition type and create a
			// notification
		} else {

			// Get the type of transition (entry or exit)
			int transition = LocationClient.getGeofenceTransition(intent);

			// Test that a valid transition was reported
			if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER)

			) {

				// Post a notification
				List<Geofence> geofences = LocationClient
						.getTriggeringGeofences(intent);
				String[] geofenceIds = new String[geofences.size()];
				for (int index = 0; index < geofences.size(); index++) {
					geofenceIds[index] = geofences.get(index).getRequestId();

					// marks the associated task as completed
					FileInputStream fis;
					try {
						fis = new FileInputStream(Environment
								.getExternalStorageDirectory().getPath()
								+ "/Gtask/Tasks/task"
								+ geofenceIds[index]
								+ ".gtsk");
						ObjectInputStream read;
						read = new ObjectInputStream(fis);
						Task task = (Task) read.readObject();
						read.close();
						if (!task.isRepeated())
							task.setActive(false);

						FileOutputStream fos = new FileOutputStream(Environment
								.getExternalStorageDirectory().getPath()
								+ "/Gtask/Tasks/task"
								+ geofenceIds[index]
								+ ".gtsk");
						ObjectOutputStream write;
						write = new ObjectOutputStream(fos);
						write.writeObject(task);
						write.close();

					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				String ids = TextUtils.join(
						GeofenceUtils.GEOFENCE_ID_DELIMITER, geofenceIds);
				String transitionType = getTransitionString(transition);

				try {
					sendNotification(transitionType, ids);
				} catch (StreamCorruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// Log the transition type and a message
				Log.d(GeofenceUtils.APPTAG,
						getString(
								R.string.geofence_transition_notification_title,
								transitionType, ids));
				Log.d(GeofenceUtils.APPTAG,
						getString(R.string.geofence_transition_notification_text));

				// An invalid transition was reported
			} else {
				// Always log as an error
				Log.e(GeofenceUtils.APPTAG,
						getString(R.string.geofence_transition_invalid_type,
								transition));
			}
		}
	}

	/**
	 * Posts a notification in the notification bar when a transition is
	 * detected. If the user clicks the notification, control goes to the main
	 * Activity.
	 * 
	 * @param transitionType
	 *            The type of transition that occurred.
	 * @throws IOException
	 * @throws StreamCorruptedException
	 * @throws ClassNotFoundException
	 * 
	 */
	private void sendNotification(String transitionType, String ids)
			throws StreamCorruptedException, IOException,
			ClassNotFoundException {

		// creates an array with list of IDs
		String[] idList = ids.split(",");

		Log.e("" + idList.length, "" + idList.length);

		// Get an instance of the Notification manager
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		for (String id : idList) {
			FileInputStream fos = new FileInputStream(Environment
					.getExternalStorageDirectory().getPath()
					+ "/Gtask/Tasks/task" + id.trim() + ".gtsk");
			ObjectInputStream read = new ObjectInputStream(fos);

			Task task = (Task) read.readObject();
			read.close();
			// Create an explicit content Intent that starts the main Activity
			Intent notificationIntent = new Intent(getApplicationContext(),
					MainActivity.class);

			// Construct a task stack
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

			// Adds the main Activity to the task stack as the parent
			stackBuilder.addParentStack(MainActivity.class);

			// Push the content Intent onto the stack
			stackBuilder.addNextIntent(notificationIntent);

			// Get a PendingIntent containing the entire back stack
			PendingIntent notificationPendingIntent = stackBuilder
					.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			// Get a notification builder that's compatible with platform
			// versions >= 4
			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					this);

			// Set the notification contents
			long[] vibrate = { 0, 100, 200, 300 };
			builder.setSmallIcon(R.drawable.ic_notification)
					.setContentTitle("Task-" + task.getTitle())
					.setContentText(
							getString(R.string.geofence_transition_notification_text))
					.setContentIntent(notificationPendingIntent)
					.setVibrate(vibrate)
					.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

			// Issue the notification
			mNotificationManager.notify(Integer.parseInt(id), builder.build());
		}
	}

	/**
	 * Maps geofence transition types to their human-readable equivalents.
	 * 
	 * @param transitionType
	 *            A transition type constant defined in Geofence
	 * @return A String indicating the type of transition
	 */
	private String getTransitionString(int transitionType) {
		switch (transitionType) {

		case Geofence.GEOFENCE_TRANSITION_ENTER:
			return getString(R.string.geofence_transition_entered);

		case Geofence.GEOFENCE_TRANSITION_EXIT:
			return getString(R.string.geofence_transition_exited);

		default:
			return getString(R.string.geofence_transition_unknown);
		}
	}
}
