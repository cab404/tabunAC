package com.cab404.acli;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.android.AbstractActivity;
import com.cab404.ponyscape.bus.AppContextExecutor;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.views.ViewSugar;
import com.cab404.sjbus.Bus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cab404
 */
public class PartActivity extends AbstractActivity implements FragmentedList {

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
	@Override public Context getContext() {return Static.theme.getContext(this);}


	private static class PartLaunchHandler {
		@Bus.Handler
		public void handleRun(E.Parts.Run run) {
			int id = (int) (Math.random() * Integer.MAX_VALUE);

			Intent intent = new Intent(Static.ctx, PartActivity.class);
			intent.putExtra("part_data_id", id);
			intent.putExtra("floating", run.floating);

			part_data_valve.put(id, run.part);
			Static.bus.send(new E.Android.StartActivity(intent));
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

		root.addView(part.create(getLayoutInflater(), root, getContext()));

	}

	@Override protected void onDestroy() {
		if (part != null && root != null)
			part.onRemove(root.getChildAt(0), root, getContext());
		super.onDestroy();
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void onStart(E.Commands.Run unused) {
		findViewById(R.id.loading).setVisibility(View.VISIBLE);
	}

	@Bus.Handler(executor = AppContextExecutor.class)
	public void onFinish(E.Commands.Finished unused) {
		findViewById(R.id.loading).setVisibility(View.GONE);
	}
}