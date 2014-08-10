package com.cab404.ponyscape.parts;

/**
 * @author cab404
 */
public class ErrorPart extends AbstractTextPart {

	@Override protected CharSequence getText() {
		return "Ошибка: Страница не найдена или доступ запрещён.";
	}

}
