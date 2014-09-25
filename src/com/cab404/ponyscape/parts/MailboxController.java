package com.cab404.ponyscape.parts;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cab404.acli.Part;
import com.cab404.libtabun.requests.LetterListRequest;
import com.cab404.moonlight.util.exceptions.MoonlightFail;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;

/**
 * @author cab404
 */
public class MailboxController extends Part {
	@Override protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
		View view = inflater.inflate(R.layout.part_mailbox_controller, viewGroup, false);

		view.findViewById(R.id.select_all).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				Static.bus.send(new E.Letters.SelectAll());
			}
		});

		view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				final E.Letters.CallSelected selected = new E.Letters.CallSelected();
				Static.bus.send(selected);

				final LetterListRequest req = new LetterListRequest(
						LetterListRequest.Action.DELETE,
						selected.ids.toArray(new Integer[selected.ids.size()])
				);

				new Thread() {
					@Override public void run() {
						try {
							req.exec(Static.user);
							E.Letters.UpdateDeleted deleted = new E.Letters.UpdateDeleted();
							deleted.ids = selected.ids;

							Static.bus.send(new E.Commands.Success(req.msg));
							Static.bus.send(deleted);

						} catch (MoonlightFail f) {
							Log.w("MSG", f);
							Static.bus.send(new E.Commands.Error("Не удалось удалить письма."));
						}

					}
				}.start();
			}
		});


		view.findViewById(R.id.read_all).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				final E.Letters.CallSelected selected = new E.Letters.CallSelected();
				Static.bus.send(selected);

				final LetterListRequest req = new LetterListRequest(
						LetterListRequest.Action.READ,
						selected.ids.toArray(new Integer[selected.ids.size()])
				);

				new Thread() {
					@Override public void run() {
						try {
							req.exec(Static.user);
							E.Letters.UpdateNew deleted = new E.Letters.UpdateNew();
							deleted.ids = selected.ids;

							Static.bus.send(new E.Commands.Success(req.msg));
							Static.bus.send(deleted);

						} catch (MoonlightFail f) {
							Log.w("MSG", f);
							Static.bus.send(new E.Commands.Error("Не удалось отметить письма как прочитанные."));
						}

					}
				}.start();
			}
		});

		view.findViewById(R.id.write).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				Static.bus.send(new E.Commands.Run("mail write"));
			}
		});

		return view;
	}
}
