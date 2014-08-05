package com.cab404.ponyscape.commands;

import android.util.Log;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Int;
import com.cab404.jconsol.converters.Str;
import com.cab404.libtabun.data.Type;
import com.cab404.libtabun.requests.VoteRequest;
import com.cab404.ponyscape.bus.events.Commands;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.Web;

/**
 * @author cab404
 */
@CommandClass(prefix = "votefor")
public class VoteCommands {

	@Command(command = "post", params = {Int.class, Int.class})
	public void post(Integer id, Integer vote) {
		Web.checkNetworkConnection();

		final VoteRequest request = new VoteRequest(id, vote, Type.COMMENT);

		new Thread(new Runnable() {
			@Override public void run() {
				try {
					request.exec(Static.user, Static.last_page);
					Static.handler.post(new Runnable() {
						@Override public void run() {
							Static.bus.send(new Commands.Clear());
							Static.bus.send(new Commands.Finished());
						}
					});
				} catch (Exception e) {
					Log.e("VOTE_POST", "ERR", e);
				}
			}
		}).start();
	}

	@Command(command = "comment", params = {Int.class, Int.class})
	public void comment(Integer id, Integer vote) {
		Web.checkNetworkConnection();

		final VoteRequest request = new VoteRequest(id, vote, Type.COMMENT);
		new Thread(new Runnable() {
			@Override public void run() {
				try {
					request.exec(Static.user, Static.last_page);
					Static.handler.post(new Runnable() {
						@Override public void run() {
							Static.bus.send(new Commands.Clear());
							Static.bus.send(new Commands.Finished());
						}
					});
				} catch (Exception e) {
					Log.e("VOTE_COMMENT", "ERR", e);
				}
			}
		}).start();
	}


	@Command(command = "user", params = Str.class)
	public void user(String name) {
		Web.checkNetworkConnection();

	}


	@Command(command = "blog", params = {Int.class, Int.class})
	public void blog(Integer id, Integer vote) {
		Web.checkNetworkConnection();

		final VoteRequest request = new VoteRequest(id, vote, Type.COMMENT);
		new Thread(new Runnable() {
			@Override public void run() {
				try {
					request.exec(Static.user, Static.last_page);
					Static.handler.post(new Runnable() {
						@Override public void run() {
							Static.bus.send(new Commands.Clear());
							Static.bus.send(new Commands.Finished());
						}
					});
				} catch (Exception e) {
					Log.e("VOTE_BLOG", "ERR", e);
				}
			}
		}).start();
	}

}
