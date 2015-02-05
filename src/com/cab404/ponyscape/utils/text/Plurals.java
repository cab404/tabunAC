package com.cab404.ponyscape.utils.text;

import com.cab404.ponyscape.utils.Static;

/**
 * Utils for working with
 * Created at 3:06 on 05.02.15
 *
 * @author cab404
 */
public class Plurals {

    /**
     * [
     * 0 "секунд",
     * 1 "секунда",
     * 2 "секунды"
     * ]
     *
     * [
     * 0 "часов",
     * 1 "час",
     * 2 "часа"
     * ]
     *
     * [
     * 0 "минут",
     * 1 "минута",
     * 2 "минуты"
     * ]
     * <p/>
     * *
     */
    public static String get(int plurals_id, int how_much) {
        String[] plurals = Static.ctx.getResources().getStringArray(plurals_id);

        how_much *= how_much < 0 ? -1 : 1;
        int form;

        if (how_much % 10 == 0)
            form = 0;
        else {
            if (how_much > 10 && how_much < 20)
                form = 0;
            else switch (how_much % 10) {
                case 1:
                    form = 1;
                    break;
                case 2:
                case 3:
                case 4:
                    form = 2;
                    break;
                default:
                    form = 0;
            }
        }

        return plurals[form];
    }
}
