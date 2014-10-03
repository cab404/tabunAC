package com.cab404.ponyscape.utils;

import android.content.Context;
import android.os.Handler;
import com.cab404.jconsol.CommandManager;
import com.cab404.libtabun.pages.TabunPage;
import com.cab404.libtabun.util.TabunAccessProfile;
import com.cab404.ponyscape.utils.images.Images;
import com.cab404.ponyscape.utils.state.Settings;
import com.cab404.theme_dances.ThemeManager;
import com.cab404.sjbus.Bus;

import java.util.ArrayList;

/**
 * Static
 *
 * @author cab404
 */
public class Static {

	/**
	 * То, что хранит, когда мы забываем.
	 */
	public static ArrayList<String> history = null;

	/**
	 * То, что работает, пока мы пишем.
	 */
	public static CommandManager cm = null;

	/**
	 * Явное.
	 */
	public static Settings cfg = null;

	/**
	 * То, что скрыто от глаз
	 */
	public static Settings obscure = null;

	/**
	 * То, что нас объединяет.
	 */
	public static Bus bus = null;

	/**
	 * Юзер.
	 */
	public static TabunAccessProfile user = null;

	/**
	 * Последняя загруженная страница
	 */
	public static TabunPage last_page = null;

	/**
	 * Контекст приложения
	 */
	public static Context ctx = null;
	/**
	 * Handler цикла прорисовки
	 */
	public static Handler handler = null;
	/**
	 * Всякие тематические ThreadPool-ы
	 */
	public static Pools pools = null;

	/**
	 * Загрузщик картинок
	 */
	public static Images img = null;


	/**
	 * Темы
	 */
	public static ThemeManager theme = null;

}
