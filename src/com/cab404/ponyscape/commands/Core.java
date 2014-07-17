package com.cab404.ponyscape.commands;

import android.widget.Toast;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.events.Commands;
import com.cab404.ponyscape.events.Login;
import com.cab404.ponyscape.events.Parts;
import com.cab404.ponyscape.parts.Credits;
import com.cab404.ponyscape.parts.Help;
import com.cab404.ponyscape.utils.Bus;
import com.cab404.ponyscape.utils.Static;

/**
 * @author cab404
 */
@CommandClass(prefix = "")
public class Core {


	@Command(command = "help")
	public void displayHelp() {
		Bus.send(new Parts.Add((new Help())));
		Bus.send(new Commands.Finished());
		Bus.send(new Commands.Clear());
	}

	@Command(command = "about")
	public void displayCredits() {
		Bus.send(new Parts.Add((new Credits())));
		Bus.send(new Commands.Finished());
		Bus.send(new Commands.Clear());
	}


	@Command(command = "clear")
	public void clear() {
		Bus.send(new Parts.Clear());
		Bus.send(new Commands.Finished());
		Bus.send(new Commands.Clear());
	}

	@Command(command = "login")
	public void login() {
		Bus.send(new Login.Requested());
		Bus.send(new Commands.Finished());
		Bus.send(new Commands.Clear());
	}

	@Command(command = "search", params = Str.class)
	public void search(final String term) {
		/*Постим, дабы выйти из первой процедуры запуска.*/
		Static.handler.post(new Runnable() {
			@Override public void run() {
				Bus.send(new Commands.Run("page load \"/search/topics/?q=" + SU.rl(term.replace("\"", "\\\"")) + "\""));
			}
		});
		Bus.send(new Commands.Finished());
		Bus.send(new Commands.Clear());
	}


	@Command(command = "login", params = {Str.class, Str.class})
	public void login(final String login, final String password) {
		new Thread(new Runnable() {
			@Override public void run() {
				final boolean success = (Static.user.login(login, password));

				Static.handler.post(new Runnable() {
					@Override public void run() {
						if (success)
							Toast.makeText(Static.app_context, "Yup", Toast.LENGTH_SHORT).show();
						else
							Toast.makeText(Static.app_context, "Nope", Toast.LENGTH_SHORT).show();
						Bus.send(new Commands.Finished());
						Bus.send(new Commands.Clear());
					}
				});

			}
		}).start();
	}

}
