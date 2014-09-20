package com.cab404.acli;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.events.Android;
import com.cab404.ponyscape.bus.events.Parts;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.views.ViewSugar;
import com.cab404.sjbus.Bus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cab404
 */
public class PartActivity extends Activity implements FragmentedList {

	protected static Map<Integer, Part> part_data_valve = new ConcurrentHashMap<>();

	@ViewSugar.Bind(R.id.root)
	protected LinearLayout root;
	protected Part part;

	@Override public void add(Part part) {throw new UnsupportedOperationException();}
	@Override public void add(Part part, int index) {throw new UnsupportedOperationException();}
	@Override public void remove(Part part) {finish();}
	@Override public Part partAt(int index) {return part;}
	@Override public int indexOf(Part part) {return 0;}
	@Override public int size() {return 1;}
	@Override public Context getContext() {return this;}


	private static class PartLaunchHandler {
		@Bus.Handler
		public void handleRun(Parts.Run run) {
			int id = (int) (Math.random() * Integer.MAX_VALUE);

			Intent intent = new Intent(Static.app_context, PartActivity.class);
			intent.putExtra("part_data_id", id);
			intent.putExtra("floating", run.floating);

			part_data_valve.put(id, run.part);
			Static.bus.send(new Android.StartActivity(intent));
		}
	}

	private static boolean configured = false;
	public static void setup() {
		if (configured) return;
		Static.bus.register(new PartLaunchHandler());
		configured = true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getIntent().getBooleanExtra("floating", false))
			setTheme(R.style.Ponyscape_Dialog);
		else
			setTheme(R.style.Ponyscape);

		setContentView(R.layout.activity_part);

		ViewSugar.bind(this);

		int part_data_id = getIntent().getIntExtra("part_data_id", -1);

		part = part_data_valve.remove(part_data_id);


		if (part == null) {
			finish();
			Log.e("PartActivity", "Data index was empty");
			return;
		}

		part.onInsert(this);

		root.addView(part.create(getLayoutInflater(), root, this));

	}

	@Override protected void onDestroy() {
		super.onDestroy();
		part.onRemove(root.getChildAt(0), root, this);
	}
}