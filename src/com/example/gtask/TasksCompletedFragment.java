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

import com.example.gtask.adapter.TaskListCompletedAdapter;
import com.example.gtask.geofencing.Task;
import com.example.gtask.model.TaskListCompletedItem;

public class TasksCompletedFragment extends Fragment {

	public TasksCompletedFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_tasks_completed,
				container, false);
		ListView todoList = (ListView) rootView
				.findViewById(R.id.completed_list);

		ArrayList<TaskListCompletedItem> items = new ArrayList<TaskListCompletedItem>();

		/*
		 * for (int i = 0; i < 10; i++) { TaskListItem item = new TaskListItem(i
		 * + "-title", i + "-description", R.drawable.stark); items.add(item); }
		 */

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

					if (!task.isActive()) // if task is not active adds it to
											// the completed task list
					{
						TaskListCompletedItem item = new TaskListCompletedItem(
								task.getTitle(), task.getDescription(), 0,
								f.getName());

						// Reads image associated with the task(Screen shot)
						Resources res = getResources();
						// BitmapDrawable bd = new BitmapDrawable(res, bmp);

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

		TaskListCompletedAdapter tA = new TaskListCompletedAdapter(
				getActivity(), items);
		todoList.setAdapter(tA);

		Switch highAccuracy = (Switch) rootView.findViewById(R.id.switch1);

		SharedPreferences settings = getActivity().getApplicationContext()
				.getSharedPreferences("com.example.gtask.number",
						Context.MODE_PRIVATE);
		boolean switchStatus = settings.getBoolean("switchStatus", false);

		highAccuracy.setChecked(switchStatus);
		/*
		 * todoList.setItemsCanFocus(true); todoList.setFocusable(false);
		 * todoList.setFocusableInTouchMode(false);
		 * todoList.setClickable(false);
		 */

		return rootView;
	}
}
