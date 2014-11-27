package com.example.gtask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Switch;

import com.example.gtask.adapter.TaskListAdapter;
import com.example.gtask.geofencing.Task;
import com.example.gtask.model.TaskListItem;

public class TasksToDoFragment extends Fragment {

	public TasksToDoFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_tasks_to_do,
				container, false);
		ListView todoList = (ListView) rootView.findViewById(R.id.todo_list);

		ArrayList<TaskListItem> items = new ArrayList<TaskListItem>();

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
					
					if(task.isActive()) //if task is active adds it to the task list
					{
						TaskListItem item = new TaskListItem(task.getTitle(),
								task.getDescription(), 0,f.getName());
	
						// Reads image associated with the task(Screen shot)
						Resources res = getResources();
						//BitmapDrawable bd = new BitmapDrawable(res, bmp);
	
						item.setbMap(task.getPicture());
						
						items.add(item);
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		TaskListAdapter tA = new TaskListAdapter(getActivity(), items);
		todoList.setAdapter(tA);
		
		Switch highAccuracy=(Switch)rootView.findViewById(R.id.switch1); //gets the reference to high accuracy switch
		
		SharedPreferences settings = getActivity().getApplicationContext().getSharedPreferences("com.example.gtask.number", Context.MODE_PRIVATE);
		boolean switchStatus = settings.getBoolean("switchStatus", false);
		
		highAccuracy.setChecked(switchStatus);//loads last saved status of the high accuracy mode
		

		

		return rootView;
	}
}
