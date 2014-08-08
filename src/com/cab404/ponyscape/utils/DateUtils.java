package com.cab404.ponyscape.utils;

import android.content.Context;
import com.cab404.ponyscape.R;

import java.util.Calendar;

/**
 * @author cab404
 */
public class DateUtils {

	private static String[] months = {
			"января",
			"февраля",
			"марта",
			"апреля",
			"мая",
			"июня",
			"июля",
			"августа",
			"сентября",
			"октября",
			"ноября",
			"декабря"
	};

	/**
	 * Преобразует дату в строку. В зависимости от ситуации может выдать
	 * "20 секунд назад", "7 минут назад", "сегодня, в 20:00:12" и т.д
	 */
	public static String convertToString(Calendar calendar, Context context) {
		Calendar current = Calendar.getInstance();
		StringBuilder data = new StringBuilder();

		long delta = current.getTimeInMillis() - calendar.getTimeInMillis();

		if (delta < 60000 * 15) {
			if (delta < 60000) {
				data
						.append(delta / 1000).append(' ')
						.append(
								context.getResources()
										.getQuantityString(R.plurals.seconds_ago, (int) (delta / 1000))
						);
			} else
				data.append(delta / 60000).append(' ')
						.append(
								context.getResources()
										.getQuantityString(R.plurals.minutes_ago, (int) (delta / 60000))
						);
		} else {
			if ((
					current.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR))
					&&
					(current.get(Calendar.YEAR) == calendar.get(Calendar.YEAR))
					) {
				data.append("сегодня");

			} else {
				if (current.get(Calendar.YEAR) != calendar.get(Calendar.YEAR))
					data.append(String.format("%1$te %2$s %1$tY года", calendar, months[calendar.get(Calendar.MONTH)]));
				else
					data.append(String.format("%1$te %2$s", calendar, months[calendar.get(Calendar.MONTH)]));
			}

			data.append(", в ");
			data.append(String.format("%1$tk:%1$tM:%1$tS", calendar));

		}

		return data.toString();
	}

}
