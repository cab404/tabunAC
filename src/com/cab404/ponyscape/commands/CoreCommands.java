package com.cab404.ponyscape.commands;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.parts.CreditsPart;
import com.cab404.ponyscape.parts.EditorPart;
import com.cab404.ponyscape.parts.HelpPart;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.state.AliasUtils;
import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author cab404
 */
@CommandClass(prefix = "")
public class CoreCommands {


	@Command(command = "help")
	public void displayHelp() {
		Static.bus.send(new E.Parts.Run((new HelpPart()), true));
		Static.bus.send(new E.Commands.Finished());
		Static.bus.send(new E.Commands.Clear());
	}

	@Command(command = "about")
	public void displayCredits() {
		Static.bus.send(new E.Parts.Run((new CreditsPart()), true));
		Static.bus.send(new E.Commands.Finished());
		Static.bus.send(new E.Commands.Clear());
	}


	@Command(command = "aliases")
	public void aliases() {
		final StringBuilder aliases = new StringBuilder();
		JSONArray shortcuts = (JSONArray) Static.cfg.get("main.shortcuts");

		if (shortcuts == null) shortcuts = new JSONArray();

		for (Object string_actulally : shortcuts) {
			final AliasUtils.Alias shortcut = new AliasUtils.Alias(string_actulally.toString());
			aliases.append(shortcut.name).append("->").append(shortcut.command).append("\n");
		}

		EditorPart editorPart = new EditorPart("Редактируем меню", aliases, new EditorPart.EditorActionHandler() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean finished(CharSequence text) {
				List<String> lines = SU.split(text.toString(), "\n");
				Collection<AliasUtils.Alias> new_shortcuts = new ArrayList<>();


				int line_num = 0;
				for (String line : lines) {
					line_num++;

					if (line.isEmpty()) continue;

					if (!line.contains("->")) {
						Static.bus.send(new E.Commands.Error("Нет разделителя в строке " + line_num));
						return false;
					} else {
						List<String> parts = SU.split(line, "->", 2);
						AliasUtils.Alias n = new AliasUtils.Alias(parts.get(0), parts.get(1));
						new_shortcuts.add(n);
					}

				}

				AliasUtils.setAliases(new_shortcuts);

				Static.bus.send(new E.Aliases.Update());
				Static.bus.send(new E.Commands.Finished());
				Static.bus.send(new E.Commands.Clear());
				return true;
			}
			@Override public void cancelled() {

			}
		}, new EditorPart.EditorPlugin[]{});

		Static.bus.send(new E.Parts.Run(editorPart, true));

	}

	@Command(command = "clear")
	public void clear() {
		Static.bus.send(new E.Parts.Clear());
		Static.bus.send(new E.Commands.Finished());
		Static.bus.send(new E.Commands.Clear());
	}

	@Command(command = "login")
	public void login() {
		try {
			Static.bus.send(
					new E.Android.StartActivityForResult(
							new Intent("everypony.tabun.auth.TOKEN_REQUEST"),
							new E.Android.StartActivityForResult.ResultHandler() {
								@Override public void handle(int resultCode, Intent data) {
									if (resultCode == Activity.RESULT_OK) {
										Toast.makeText(Static.app_context, "Вошли", Toast.LENGTH_SHORT).show();
										Static.user = TabunAccessProfile.parseString(data.getStringExtra("everypony.tabun.cookie"));
										Static.bus.send(new E.Login.Success());
										Static.bus.send(new E.Commands.Clear());
										Static.bus.send(new E.Commands.Finished());
									} else {
										Toast.makeText(Static.app_context, "Не вошли", Toast.LENGTH_SHORT).show();
										Static.bus.send(new E.Login.Failure());
										Static.bus.send(new E.Commands.Clear());
										Static.bus.send(new E.Commands.Finished());
									}
								}
								@Override public void error(Throwable e) {
									Toast.makeText(Static.app_context, "Не вошли, нет Tabun.Auth", Toast.LENGTH_SHORT).show();
									Intent download = new Intent(
											Intent.ACTION_VIEW,
											Uri.parse("market://details?id=everypony.tabun.auth")
									);
									Static.bus.send(new E.Android.StartActivity(download));
									Static.bus.send(new E.Commands.Clear());
									Static.bus.send(new E.Commands.Finished());
								}

							}
					)
			);
		} catch (ActivityNotFoundException e) {
			Static.bus.send(new E.Commands.Error("TabunAuth не установлен."));
		}

	}

	@Command(command = "search", params = Str.class)
	public void search(final String term) {
		Static.bus.send(new E.Commands.Run("page load \"/search/topics/?q=" + SU.rl(term.replace("\"", "\\\"")) + "\""));
		Static.bus.send(new E.Commands.Finished());
		Static.bus.send(new E.Commands.Clear());
	}


	@Command(command = "login", params = {Str.class, Str.class})
	public void login(final String login, final String password) {
		Simple.checkNetworkConnection();

		Static.bus.send(new E.Commands.Hide());

		new Thread(new Runnable() {
			@Override public void run() {

				boolean run_result;
				try {
					run_result = Static.user.login(login, password);
				} catch (Exception e) {
					run_result = false;
				}

				final boolean success = run_result;

				Static.handler.post(new Runnable() {
					@Override public void run() {
						if (success) {
							Toast.makeText(Static.app_context, "Вошли", Toast.LENGTH_SHORT).show();
							Static.cfg.put("main.profile", Static.user.serialize());
							Static.bus.send(new E.Login.Success());
							Static.cfg.save();
							Static.bus.send(new E.Commands.Clear());
						} else {
							Toast.makeText(Static.app_context, "Не вошли", Toast.LENGTH_SHORT).show();
							Static.bus.send(new E.Login.Failure());
						}
						Static.bus.send(new E.Commands.Finished());
					}
				});

			}
		}).start();
	}

}
