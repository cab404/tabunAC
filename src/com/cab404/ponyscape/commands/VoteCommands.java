package com.cab404.ponyscape.commands;

import android.util.Log;
import com.cab404.jconsol.annotations.Command;
import com.cab404.jconsol.annotations.CommandClass;
import com.cab404.jconsol.converters.Int;
import com.cab404.libtabun.data.Type;
import com.cab404.libtabun.requests.VoteRequest;
import com.cab404.ponyscape.bus.events.Commands;
import com.cab404.ponyscape.bus.events.GotData;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;

/**
 * @author cab404
 */
@CommandClass(prefix = "votefor")
public class VoteCommands {

	@Command(command = "post", params = {Int.class, Int.class})
	public void topic(final Integer id, final Integer vote) {
		vote(Type.TOPIC, id, vote);
	}

	@Command(command = "comment", params = {Int.class, Int.class})
	public void comment(final Integer id, final Integer vote) {
		vote(Type.COMMENT, id, vote);
	}


	@Command(command = "user", params = {Int.class, Int.class})
	public void user(Integer id, Integer vote) {
		vote(Type.USER, id, vote);
	}


	@Command(command = "blog", params = {Int.class, Int.class})
	public void blog(Integer id, Integer vote) {
		vote(Type.BLOG, id, vote);
	}

	void send(Type type, int id, float result) {
		switch (type) {
			case COMMENT:
				Static.bus.send(new GotData.Vote.Comment(id, Math.round(result)));
				break;
			case TOPIC:
				Static.bus.send(new GotData.Vote.Topic(id, Math.round(result)));
				break;
			case BLOG:
				Static.bus.send(new GotData.Vote.Blog(id, result));
				break;
			case USER:
				Static.bus.send(new GotData.Vote.User(id, result));
				break;
		}
	}

	void vote(final Type type, final int id, final int vote) {
		Simple.checkNetworkConnection();

		final VoteRequest request = new VoteRequest(id, vote, type);
		new Thread(new Runnable() {
			@Override public void run() {
				try {
					request.exec(Static.user);

					if (request.success()) {
						Static.bus.send(new Commands.Success(request.msg));
						send(type, id, request.result);
					} else
						Static.bus.send(new Commands.Error(request.msg));

				} catch (Exception e) {
					Static.bus.send(new Commands.Error("Не удалось проголосовать."));
					Log.e("VOTE", "ERR", e);
				} finally {
					Static.bus.send(new Commands.Clear());
					Static.bus.send(new Commands.Finished());
				}
			}
		}).start();

	}

}
