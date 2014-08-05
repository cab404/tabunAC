package com.cab404.ponyscape.commands;

import android.widget.Toast;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Str;
import com.cab404.moonlight.util.SU;
import com.cab404.ponyscape.bus.events.Commands;
import com.cab404.ponyscape.bus.events.Login;
import com.cab404.ponyscape.bus.events.Parts;
import com.cab404.ponyscape.parts.CreditsPart;
import com.cab404.ponyscape.parts.HelpPart;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.Web;

/**
 * @author cab404
 */
@CommandClass(prefix = "")
public class CoreCommands {


	@Command(command = "help")
	public void displayHelp() {
		Static.bus.send(new Parts.Add((new HelpPart())));
		Static.bus.send(new Commands.Finished());
		Static.bus.send(new Commands.Clear());
	}

	@Command(command = "about")
	public void displayCredits() {
		Static.bus.send(new Parts.Add((new CreditsPart())));
		Static.bus.send(new Commands.Finished());
		Static.bus.send(new Commands.Clear());
	}


	@Command(command = "clear")
	public void clear() {
		Static.bus.send(new Parts.Clear());
		Static.bus.send(new Commands.Finished());
		Static.bus.send(new Commands.Clear());
	}

	@Command(command = "login")
	public void login() {
		Static.bus.send(new Login.Requested());
		Static.bus.send(new Commands.Finished());
		Static.bus.send(new Commands.Clear());
	}

	@Command(command = "search", params = Str.class)
	public void search(final String term) {
		/*Постим, дабы выйти из первой процедуры запуска.*/
		Static.handler.post(new Runnable() {
			@Override public void run() {
				Static.bus.send(new Commands.Run("page load \"/search/topics/?q=" + SU.rl(term.replace("\"", "\\\"")) + "\""));
			}
		});
		Static.bus.send(new Commands.Finished());
		Static.bus.send(new Commands.Clear());
	}


	@Command(command = "login", params = {Str.class, Str.class})
	public void login(final String login, final String password) {
		Web.checkNetworkConnection();

		new Thread(new Runnable() {
			@Override public void run() {
				final boolean success = (Static.user.login(login, password));

				Static.handler.post(new Runnable() {
					@Override public void run() {
						if (success) {
							Toast.makeText(Static.app_context, "Yup", Toast.LENGTH_SHORT).show();
							Static.bus.send(new Commands.Clear());
						} else
							Toast.makeText(Static.app_context, "Nope", Toast.LENGTH_SHORT).show();
						Static.bus.send(new Commands.Finished());
					}
				});

			}
		}).start();
	}

}
