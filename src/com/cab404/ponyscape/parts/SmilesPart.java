package com.cab404.ponyscape.parts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.cab404.acli.Part;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.parts.editor.EditorPart;
import com.cab404.ponyscape.parts.editor.plugins.EditorPlugin;
import com.cab404.ponyscape.utils.Static;
import com.cab404.sjbus.Bus;
import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cab404
 */
public class SmilesPart extends Part {

	final private SmileHandler handler;
	private List<String> smiles;
	private View view;


	public SmilesPart(SmileHandler handler) {
		this.handler = handler;

		JSONArray raw_smiles = Static.obscure.ensure("editor.smiles", new JSONArray());
		smiles = new ArrayList<>();
		for (Object obj : raw_smiles)
			smiles.add((String) obj);

	}

	@SuppressWarnings("unchecked") private void save() {
		JSONArray raw_smiles = new JSONArray();

		for (String s : smiles)
			raw_smiles.add(s);

		Static.obscure.put("editor.smiles", raw_smiles);
		Static.obscure.save();
	}


	@Bus.Handler
	public void handleImage(final E.GotData.Image.Loaded loaded) {
		if (smiles.contains(loaded.src)) {
			final int id = smiles.indexOf(loaded.src);
			final ImageView image = (ImageView) ((LinearLayout) view.findViewById(R.id.smiles)).getChildAt(id);

			Static.handler.post(new Runnable() {
				@Override public void run() {
					image.setImageBitmap(loaded.loaded);
				}
			});
		}
	}

	@Bus.Handler
	public void handleImageError(final E.GotData.Image.Error error) {
		if (smiles.contains(error.src)) {
			final int id = smiles.indexOf(error.src);
			final ImageView image = (ImageView) ((LinearLayout) view.findViewById(R.id.smiles)).getChildAt(id);

			Static.handler.post(new Runnable() {
				@Override public void run() {
					image.setImageResource(R.drawable.ic_image_error);
				}
			});
		}
	}

	private void build() {
		LinearLayout smile_list = (LinearLayout) view.findViewById(R.id.smiles);
		smile_list.removeAllViews();

		for (final String smile : smiles) {
			final ImageView smile_image = new ImageView(smile_list.getContext());
			smile_image.setLayoutParams(
					new LinearLayout.LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.MATCH_PARENT
					)
			);
			smile_image.setImageResource(R.drawable.ic_image_loading);
			smile_list.addView(smile_image);

			smile_image.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					handler.handle(smile);
					delete();
				}
			});

			Static.img.download(smile);

		}


	}


	private void edit() {
		EditorPart editor = new EditorPart("Введите адреса картинок, каждый на новой строке", SU.join(smiles, "\n"), new EditorPart.EditorActionHandler() {
			@Override public boolean finished(CharSequence text) {
				smiles = new ArrayList<>();

				for (String s : SU.split(text.toString(), "\n"))
					if (!smiles.contains(s))
						smiles.add(s);

				save();
				build();
				return true;
			}
			@Override public void cancelled() {}
		},
				new EditorPlugin[0]
		);
		Static.bus.send(new E.Parts.Run(editor, true));
	}

	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		Static.bus.register(this);

		view = inflater.inflate(R.layout.part_smilopack, viewGroup, false);
		view.findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				edit();
			}
		});
		build();
		return view;
	}

	@Override protected void onRemove(View view, ViewGroup parent, Context context) {
		Static.bus.unregister(this);
		super.onRemove(view, parent, context);
	}
	public static interface SmileHandler {
		public void handle(String address);
	}

}
