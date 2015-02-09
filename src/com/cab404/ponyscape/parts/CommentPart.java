package com.cab404.ponyscape.parts;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Comment;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.animation.Anim;
import com.cab404.ponyscape.utils.text.DateUtils;
import com.cab404.ponyscape.utils.text.HtmlRipper;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */
public class CommentPart extends Part {

    private final boolean isLetter;
    private CharSequence text = null;
    public final Comment comment;
    private HtmlRipper ripper;

    View view;

    public CommentPart(Comment comment, boolean isLetter) {
        this.isLetter = isLetter;
        Static.bus.register(this);
        this.comment = comment;
    }

    @Override
    public View create(LayoutInflater inflater, ViewGroup viewGroup, final Context context) {

        view = inflater.inflate(R.layout.part_comment, viewGroup, false);
        convert(view, context);
        return view;
    }

    public void convert(final View view, Context context) {
        this.view = view;

        TextView rating = (TextView) view.findViewById(R.id.rating);
        View minus = view.findViewById(R.id.minus);
        View plus = view.findViewById(R.id.plus);
        View root = view.findViewById(R.id.root);

		/* Выставляем текст */
        {
            ViewGroup content = (ViewGroup) view.findViewById(R.id.content);

            if (ripper == null) {
                ripper = new HtmlRipper(content);
                ripper.escape(comment.text);
            } else {
                ripper.changeLayout(content);
            }

        }

		/* Загрузка аватарки. Складываем ссылку в тег картинки. */
        {
            ImageView avatar = (ImageView) view.findViewById(R.id.avatar);

            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Static.bus.send(new E.Commands.Run("user load \"" + comment.author.login + "\""));
                }
            });
            if (!comment.author.small_icon.equals(avatar.getTag())) {
                avatar.setTag(comment.author.small_icon);
                avatar.setImageDrawable(new ColorDrawable(Static.ctx.getResources().getColor(R.color.bg_item_shadow)));
                Static.img.download(comment.author.small_icon);
            }

        }

		/* Собираем и раскрашиваем дату и ник */
        {
            Spannable date =
                    new SpannableStringBuilder("" +
                            comment.author.login
                            + " "
                            + DateUtils.convertToString(comment.date, context)
                    );

            date.setSpan(
                    new ForegroundColorSpan(context.getResources().getColor(R.color.bg_item_label)),
                    0,
                    comment.author.login.length(),
                    0
            );

            ((TextView) view.findViewById(R.id.data)).setText(date);

        }

		/* Выставляем рейтинг */
        {
            rating
                    .setText(comment.votes > 0 ? "+" + comment.votes : "" + comment.votes);
        }

		/* Если это письмо, то отключаем рейтинг и редактирование. */
        {
            if (!isLetter) {
                plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Static.bus.send(new E.Commands.Run("votefor comment " + comment.id + " +1"));
                    }
                });

                minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Static.bus.send(new E.Commands.Run("votefor comment " + comment.id + " -1"));
                    }
                });

            }
        }

		/* Избранное */
        {
            ImageView fav = (ImageView) view.findViewById(R.id.favourite);
            fav.setTag(comment.id);

            fav.setColorFilter(
                    Static.ctx.getResources().getColor
                            (
                                    comment.in_favs ?
                                            R.color.bg_item_fav
                                            :
                                            R.color.bg_item_shadow
                            )
            );

            fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (comment.in_favs) {
                        Static.bus.send(new E.Commands.Run("fav comment " + comment.id + " -"));
                    } else {
                        Static.bus.send(new E.Commands.Run("fav comment " + comment.id + " +"));
                    }
                }
            });
        }

		/* Если письмо, то отключаем ненужное. */
        {
            if (isLetter) {
                plus.setVisibility(View.GONE);
                minus.setVisibility(View.GONE);
                view.findViewById(R.id.edit).setVisibility(View.GONE);
                view.findViewById(R.id.rating).setVisibility(View.GONE);
                view.findViewById(R.id.favourite).setVisibility(View.GONE);
            }

        }

		/* Устанавливаем боевую расскраску, если коммент новый */
        {
            if (comment.is_new)
                root
                        .setBackgroundColor(
                                context.getResources().getColor(R.color.bg_item_new)
                        );
            else
                root
                        .setBackgroundColor(
                                context.getResources().getColor(R.color.bg_item)
                        );
        }

		/* Вишенка на торте */
        ((TextView) view.findViewById(R.id.id)).setText("#" + comment.id);


    }

    public void kill() {
        Static.bus.unregister(this);
        ripper.destroy();
        ripper = null;
    }

    @Bus.Handler
    public void handleVoteChange(final E.GotData.Vote.Comment vote) {
        if (comment.id == vote.id)
            comment.votes = vote.votes;

        if (comment.id == vote.id) {
            Static.handler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            final TextView rating = (TextView) view.findViewById(R.id.rating);
                            Anim.swapText(rating, (comment.votes > 0 ? "+" : "") + comment.votes);
                        }
                    }
            );
        }
    }

    @Bus.Handler
    public void handleFavChange(final E.GotData.Fav.Comment fav) {
        if (comment.id == fav.id)
            comment.in_favs = fav.added;

        if (view.findViewById(R.id.favourite).getTag().equals(fav.id)) {
            Static.handler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            Anim.recolorIcon(
                                    (ImageView) view.findViewById(R.id.favourite),
                                    Static.ctx.getResources().getColor
                                            (
                                                    comment.in_favs ?
                                                            R.color.bg_item_fav
                                                            :
                                                            R.color.bg_item_shadow
                                            ));
                        }
                    }
            );
        }
    }

    @Bus.Handler
    public void handleAvatar(final E.GotData.Image.Loaded img) {
        if (view == null) return;
        if (img.src.equals(view.findViewById(R.id.avatar).getTag())) {
            Static.handler.post(new Runnable() {
                @Override
                public void run() {
                    ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
//					Anim.swapIcon(avatar, new BitmapDrawable(Static.ctx.getResources(), img.loaded));
                    avatar.setImageBitmap(img.loaded);
                }
            });
        }
    }

    @Override
    protected void onRemove(View view, ViewGroup parent, Context context) {
        super.onRemove(view, parent, context);
        kill();
    }
}