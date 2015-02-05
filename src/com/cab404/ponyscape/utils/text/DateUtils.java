package com.cab404.ponyscape.utils.text;

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
	 */
	public static String convertToString(Calendar calendar, Context context) {
		Calendar current = Calendar.getInstance();

		long abs_cal = calendar.getTimeInMillis();
		long abs_cur = current.getTimeInMillis();

		StringBuilder data = new StringBuilder();

		long delta = abs_cur - abs_cal;

		/* Если меньше 15 минут */
		if (delta < 60000 * 15) {
			/* Если меньше минуты */
			if (delta < 60000) {
				data
						.append(delta / 1000).append(' ')
						.append(
								Plurals.get(R.array.seconds_ago, (int) (delta / 1000))
						);
			} else
				data.append(delta / 60000).append(' ')
						.append(
                                Plurals.get(R.array.minutes_ago, (int) (delta / 60000))
						);
		} else {
			if ((current.get(Calendar.YEAR) == calendar.get(Calendar.YEAR))) {
				if (current.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)) {
					data.append("сегодня");
				} else if (current.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) + 1) {
					data.append("вчера");
				} else {
					data.append(String.format("%1$te %2$s", calendar, months[calendar.get(Calendar.MONTH)]));
				}
			} else {
				data.append(String.format("%1$te %2$s %1$tY года", calendar, months[calendar.get(Calendar.MONTH)]));
			}

			data.append(" в ");

			if (calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.HOUR_OF_DAY) % 12 == 0) {
				if (calendar.get(Calendar.HOUR_OF_DAY) == 12)
					data.append("полдень");
				if (calendar.get(Calendar.HOUR_OF_DAY) == 0)
					data.append("полночь");
			} else
				data.append(String.format("%1$tk:%1$tM"/*:%1$tS"*/, calendar));

		}

		return data.toString();
	}

}
