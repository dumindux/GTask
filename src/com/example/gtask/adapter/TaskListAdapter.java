package com.example.gtask.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import com.example.gtask.geofencing.GeofenceUtils;
import com.example.gtask.MainActivity;
import com.example.gtask.NewTaskActivity;
import com.example.gtask.R;
import com.example.gtask.ViewTaskActivity;
import com.example.gtask.geofencing.GeofenceRemover;
import com.example.gtask.geofencing.Task;
import com.example.gtask.model.TaskListItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.PopupMenu.OnMenuItemClickListener;;

public class TaskListAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<TaskListItem> data;
	private static LayoutInflater inflater = null;

	public TaskListAdapter(Activity a, ArrayList<TaskListItem> d) {
		activity = a;
		data = d;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return data.size();

	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int pos = position;
		View vi = convertView;
		ViewHolder holder;

		if (convertView == null) {
			vi = inflater.inflate(R.layout.list_row, null);
			holder = new ViewHolder();
			holder.title = (TextView) vi.findViewById(R.id.title); // title
			holder.description = (TextView) vi.findViewById(R.id.description); // description
			holder.screenshot = (ImageView) vi.findViewById(R.id.screen_shot); // mapscreenshot
			holder.pLayout = (LinearLayout) vi.findViewById(R.id.parent_layout);
			holder.complete = (ImageView) vi.findViewById(R.id.mark_completed);
			holder.menu = (ImageView) vi.findViewById(R.id.menu);
			vi.setTag(holder);
		} else {
			holder = (ViewHolder) vi.getTag();
		}

		TaskListItem item = data.get(position);

		// Setting all values in listview
		holder.title.setText(item.getTitle());
		holder.description.setText(item.getDescription());

		holder.screenshot.setTag(item.getbMap());
		new LoadImagesTask().execute(holder.screenshot);

		final String id1 = item.getId();
		OnClickListener listItemClickListener = new OnClickListener() {

			String id = id1;

			@Override
			public void onClick(View v) {
				Intent in = new Intent(activity, ViewTaskActivity.class);
				in.putExtra("id", id);
				activity.startActivity(in);
			}
		};

		final TaskListAdapter adapter = this;

		OnClickListener listItemClickListener2 = new OnClickListener() {

			String id = id1;
			int position = pos;
			TaskListAdapter adapt = adapter;

			@Override
			public void onClick(View v) {

				Log.e("on click", "complete");
				try {
					
					
					complete(id, position, adapt);
					
				} catch (IOException | ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};

		OnClickListener listItemClickListener3 = new OnClickListener() {

			String id = id1;
			int position = pos;
			TaskListAdapter adapt = adapter;
			
			@Override
			public void onClick(View v) {
				PopupMenu popup = new PopupMenu(activity, v);
			    MenuInflater inflater = popup.getMenuInflater();
			    inflater.inflate(R.menu.item_menu_actions, popup.getMenu());
			    
			    OnMenuItemClickListener listener=new OnMenuItemClickListener(){
			    	@Override
			    	public boolean onMenuItemClick(MenuItem item) {
			    	    switch (item.getItemId()) {
			    	        case R.id.complete:
								try {
									complete(id, position, adapt);
								} catch (ClassNotFoundException | IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
			    	            return true;
			    	        case R.id.delete:
			    	        	delete(id, position, adapt);
			    	            return true;
			    	        case R.id.edit:
			    	        	Intent intent = new Intent(activity, NewTaskActivity.class);
			    	        	intent.putExtra("id", Integer.parseInt(id.split("\\.")[0].substring(4)));
			    	    		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    	    		activity.startActivityForResult(intent, 1);
			    	        	return true;
			    	        default:
			    	            return false;
			    	    }
			    	}
			    };
			    popup.setOnMenuItemClickListener(listener);
			    
			    popup.show();
			    
			}
			
			
		};

		holder.menu.setOnClickListener(listItemClickListener3);
		holder.complete.setOnClickListener(listItemClickListener2);
		holder.pLayout.setOnClickListener(listItemClickListener);

		return vi;
	}
	
	

	public class LoadImagesTask extends AsyncTask<ImageView, Void, Bitmap> {

		ImageView imageView = null;

		@Override
		protected Bitmap doInBackground(ImageView... imageViews) {
			this.imageView = imageViews[0];

			return BitmapFactory.decodeFile(Environment
					.getExternalStorageDirectory().getPath()
					+ "/Gtask/Images/"
					+ imageView.getTag());
		}

		@Override
		protected void onPostExecute(Bitmap result) {

			imageView.setImageBitmap(result);
		}
	}
	
	public void complete(String id,int position,TaskListAdapter adapt) throws StreamCorruptedException, IOException, ClassNotFoundException
	{
		FileInputStream fis;
		fis = new FileInputStream(Environment 
				.getExternalStorageDirectory().getPath()
				+ "/Gtask/Tasks/" + id);
		ObjectInputStream read;
		read = new ObjectInputStream(fis);
		Task task = (Task) read.readObject(); //opens the file saved in the disk to mark is complete
		read.close();
		task.setActive(false);

		FileOutputStream fos = new FileOutputStream(Environment
				.getExternalStorageDirectory().getPath()
				+ "/Gtask/Tasks/" + id);
		ObjectOutputStream write;
		write = new ObjectOutputStream(fos);
		write.writeObject(task);//saves the task
		write.close();

		data.remove(position);
		adapt.notifyDataSetChanged();
		
		MainActivity main=(MainActivity)activity;
		main.mRemoveType=GeofenceUtils.REMOVE_TYPE.LIST;
		main.mGeofenceRemover=new GeofenceRemover(activity);
		main.mGeofenceIdsToRemove=new ArrayList<String>();
		main.mGeofenceIdsToRemove.add(id.split("\\.")[0].substring(4));
		main.mGeofenceRemover.removeGeofencesById(main.mGeofenceIdsToRemove); //removes added geofences corresponding to the marked tasks
		
		
	}

	public void delete(String id,int position,TaskListAdapter adapt)
	{
		File sdCardRoot = Environment.getExternalStorageDirectory();
		File yourFile = new File(sdCardRoot, "Gtask/Tasks/"+id);
		yourFile.delete(); //find and delete the file corresponding to the task in persistent storage 
		
		data.remove(position); //removes the item from active tasks list
		adapt.notifyDataSetChanged();
		
		MainActivity main=(MainActivity)activity;
		main.mRemoveType=GeofenceUtils.REMOVE_TYPE.LIST;
		main.mGeofenceRemover=new GeofenceRemover(activity);
		main.mGeofenceIdsToRemove=new ArrayList<String>();
		main.mGeofenceIdsToRemove.add(id.split("\\.")[0].substring(4));
		main.mGeofenceRemover.removeGeofencesById(main.mGeofenceIdsToRemove); //removes geofences corresponding to the deleted task
		
	}

	public static class ViewHolder {
		TextView title; // title
		TextView description; // description
		ImageView screenshot; // mapscreenshot
		LinearLayout pLayout;
		ImageView complete;
		ImageView menu;
	}
}