package com.cab404.ponyscape.parts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Comment;
import com.cab404.libtabun.data.Letter;
import com.cab404.libtabun.data.Topic;
import com.cab404.libtabun.data.Type;
import com.cab404.libtabun.requests.CommentAddRequest;
import com.cab404.libtabun.requests.CommentEditRequest;
import com.cab404.libtabun.requests.LSRequest;
import com.cab404.libtabun.requests.RefreshCommentsRequest;
import com.cab404.moonlight.util.exceptions.MoonlightFail;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.parts.editor.EditorPart;
import com.cab404.ponyscape.utils.Simple;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.animation.Anim;
import com.cab404.ponyscape.utils.images.LevelDrawable;
import com.cab404.ponyscape.utils.state.ArchiveUtils;
import com.cab404.ponyscape.utils.state.Keys;
import com.cab404.ponyscape.utils.text.Plurals;
import com.cab404.ponyscape.utils.views.DoubleClickListener;
import com.cab404.ponyscape.utils.views.LeveledListView;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cab404
 */
public class CommentListPart extends Part {

    /**
     * Список отступов комментариев.
     */
    private Map<Integer, Integer> levels;

    private CommentListAdapter adapter;
    /**
     * Список комментариев
     */
    private List<Comment> comments;

    /**
     * Там, где хранятся комментарии
     */
    private LeveledListView listView;

    /**
     * Root-батюшка
     */
    private ViewGroup view;

    private final int id;
    private final boolean isLetter;
    private Part topicPart;

    private int max_level = 0;
    private static final int LADDER_MARGIN = 3;
    int comment_ladder =
            (int) (
                    Static.ctx.getResources().getDisplayMetrics().density
                            * Static.cfg.ensure(Keys.COMMENTS_LADDER, 25)
            );

    /**
     * То, в чем лежат кнопки управления и пост.
     */
    private View list_root;

    public CommentListPart(int id, boolean isLetter) {
        this.id = id;
        this.isLetter = isLetter;
        comments = new ArrayList<>();
        levels = new HashMap<>();
    }

    public int indexOf(int id) {

        for (int i = 0; i < comments.size(); i++)
            if (comments.get(i).id == id)
                return i;

        return -1;
    }


    public synchronized void add(Comment comment) {
        // Проверка на заглушку.
        if (comment.deleted) return;

        // Проверка на отсутствие родителя.
        if (comment.parent != 0 && !levels.containsKey(comment.parent)) return;

        // Проверка на повторения.
        for (Comment cm : comments) if (comment.id == cm.id) return;

        if (comment.parent == 0) {
            levels.put(comment.id, 0);
        } else {
            int level = levels.get(comment.parent) + 1;
            levels.put(comment.id, level);
            max_level = Math.max(level, max_level);

            for (int i = indexOf(comment.parent) + 1; i < comments.size(); i++)
                if (levels.get(comments.get(i).id) < level) {
                    comments.add(i, comment);
                    return;
                }
        }
        comments.add(comment);
    }

    /**
     * Возвращает высоту бара.
     */
    private int getBarHeight() {
        return view.findViewById(R.id.bar).getHeight();
    }

    public synchronized void add(Topic topic) {
        View topic_view = ((TopicPart) (topicPart = new TopicPart(topic)))
                .create(LayoutInflater.from(getContext()), listView, getContext());
        ((TopicPart) topicPart).setLink("");

        if (bar_on_top) {
            // Ужс. Добавляем марджин сверху, чтобы бар не накладывался на заголовок.
            ((LinearLayout.LayoutParams) ((LinearLayout) topic_view)
                    .getChildAt(0).getLayoutParams()).topMargin += getBarHeight();
        }

        listView.addHeaderView(topic_view);
        listView.setAdapter(adapter);
    }

    public synchronized void add(Letter letter) {
        View letter_view = ((LetterPart) (topicPart = new LetterPart(letter)))
                .create(LayoutInflater.from(getContext()), listView, getContext());

        if (bar_on_top) {
            // Ужс. Добавляем марджин сверху, чтобы бар не накладывался на заголовок.
            ((LinearLayout.LayoutParams) ((LinearLayout) letter_view)
                    .getChildAt(0).getLayoutParams()).topMargin += getBarHeight();
        }

        listView.addHeaderView(letter_view);
        listView.setAdapter(adapter);
    }


    public void update() {
        if (listView != null)
            adapter.notifyDataSetChanged();
        updateNew();

        listView.setRightMargin((max_level + LADDER_MARGIN) * comment_ladder);
        adapter.selected = -1;
    }

    public synchronized void updateCache() {
        if (isLetter ? ArchiveUtils.isLetterInArchive(id) : ArchiveUtils.isPostInArchive(id)) {
            ArchiveUtils.Save save
                    = isLetter ? ArchiveUtils.saveLetter(id) : ArchiveUtils.savePost(id);
            if (isLetter)
                save.setHeader(((LetterPart) topicPart).letter);
            else
                save.setHeader(((TopicPart) topicPart).topic);

            for (Comment cm : comments)
                save.addComment(cm);

            save.write();
        }
    }

    private int max_comment_id() {
        int max = 0;
        for (Comment comment : comments)
            max = Math.max(comment.id, max);
        return max;
    }

    public void select(int index, int from) {
        if (index == -1) return;

        adapter.selected = index;
        if (Build.VERSION.SDK_INT < 11)
            listView.setSelection(index + 1);
        else if (index - from > -30 && index - from < 10)
            listView.smoothScrollToPositionFromTop(index + 1, getBarHeight());
        else if (Build.VERSION.SDK_INT > 20)
            listView.setSelectionFromTop(index + 1, getBarHeight());
        else
            listView.setSelection(index + 1);

        adapter.setOffset(levels.get(comments.get(index).id));
    }

    /**
     * Переходит к следующему комментарию (в новых).
     */
    private int last = 0;

    private void move() {
        adapter.notifyDataSetChanged();
        for (int i = 0; i < comments.size(); i++)
            if (i > adapter.selected && comments.get(i).is_new) {
//				comments.get(i).is_new = false;
                select(i, last);
                last = i;
                break;
            }
        updateNew();
    }

    /**
     * Обновляет список новых комментариев (не загружает их с сервера.)
     */
    private void updateNew() {
        int new_c = 0;

        for (int i = 0; i < comments.size(); i++) {
            if (i > adapter.selected && comments.get(i).is_new)
                new_c++;
        }

        if (new_c == 0)
            view.findViewById(R.id.switch_button).setVisibility(View.GONE);
        else
            view.findViewById(R.id.switch_button).setVisibility(View.VISIBLE);

        ((TextView) view.findViewById(R.id.switch_text)).setText(
                new_c + " " + Plurals.get(R.array.new_comments, new_c)
        );
    }


    /**
     * Убирает все отметки новых комментариев.
     */
    private void invalidateNew() {
        for (Comment comment : comments)
            comment.is_new = false;
        update();
    }

    /**
     * Тянет новые комментарии с сервера
     */
    public void refresh() {
        new Thread("Update thread " + id) {
            @Override
            public void run() {
                Static.bus.send(new E.Commands.Run("luna"));
                Static.bus.send(new E.Status("Обновляю комментарии..."));

                RefreshCommentsRequest request =
                        new RefreshCommentsRequest(isLetter ? Type.TALK : Type.TOPIC, id, max_comment_id());
                try {

                    request.exec(Static.user);

                    for (Comment comment : request.comments)
                        add(comment);

                    if (!request.comments.isEmpty())
                        updateCache();

                    Static.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            update();
                        }
                    });

                } catch (MoonlightFail f) {
                    Static.bus.send(new E.Commands.Failure("Не удалось обновить список комментариев."));
                } finally {
                    Static.bus.send(new E.Commands.Finished());
                    Static.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // возвращаем стрелочку
                            Anim.swapIcon(
                                    ((ImageView) view.findViewById(R.id.update)),
                                    getContext().getResources().getDrawable(R.drawable.ic_update)
                            );
                        }
                    });

                }
            }
        }.start();
    }

    /**
     * Добавляет новый комментарий или редактирует существующий.
     */
    private void comment(final Comment comment, final boolean isEditing) {
        if (isEditing && isLetter) return;

        final String[] reply = getContext().getResources().getStringArray(R.array.reply_to);

        final String title = isEditing ?
                "Редактируем ошибки"
                :
                (comment == null ?
                        "Отвечаем в пост"
                        :
                        String.format(reply[((int) (Math.random() * reply.length))], comment.author.login));

        EditorPart editorPart = new EditorPart(title, isEditing ? comment.text : "", new EditorPart.EditorActionHandler() {
            @Override
            public boolean finished(final CharSequence text) {
                if (text.length() > 3000 || text.length() < 2) {
                    Simple.msg("Текст комментария должен быть от 2 до 3000 символов и не содержать разного рода каку");
                    return false;
                }

                final LSRequest request =
                        isEditing ?
                                new CommentEditRequest(
                                        comment.id,
                                        text.toString()
                                ) {
                                    @Override
                                    protected void handle(JSONObject object) {
                                        super.handle(object);
                                        if (success) {
                                            comment.text = (String) object.get("sText");
                                            adapter.comment_cache.remove(comment);
                                        }
                                    }
                                }
                                :
                                new CommentAddRequest(
                                        isLetter ? Type.TALK : Type.BLOG,
                                        id,
                                        comment == null ? 0 : comment.id,
                                        text.toString()
                                );

                final EditorPart.EditorActionHandler handler = this;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Static.bus.send(new E.Commands.Run("luna"));
                        Static.bus.send(new E.Status(isEditing ? "Редактирую комментарий..." : "Отправляю комментарий..."));

                        String msg = isEditing ? "Не удалось отредактировать комментарий." : "Не удалось добавить комментарий.";
                        try {
                            boolean success = request.exec(Static.user).success();
                            msg = request.msg;

                            if (!success)
                                throw new MoonlightFail("breakout");
                            else {
                                Static.bus.send(new E.Commands.Success(msg));
                            }

                            refresh();

                        } catch (MoonlightFail f) {
                            Static.bus.send(new E.Commands.Failure(msg));
                            Static.bus.send(new E.Parts.Run(new EditorPart(title, text, handler), true));
                        } finally {
                            Static.bus.send(new E.Commands.Finished());
                        }

                    }
                }).start();

                return true;
            }

            @Override
            public void cancelled() {

            }
        });

        Static.bus.send(new E.Parts.Run(editorPart, true));

    }

    boolean bar_on_top = Static.cfg.ensure(Keys.COMMENTS_BAR_ON_TOP, false);

    @SuppressLint("NewApi")
    @Override
    protected View create(LayoutInflater inflater, final ViewGroup viewGroup, final Context context) {
        Static.bus.register(this);

        view = (ViewGroup) inflater.inflate(R.layout.part_comment_list, viewGroup, false);
        listView = (LeveledListView) view.findViewById(R.id.comment_list);
        listView.setHorisontallyScrollable(Static.cfg.ensure(Keys.COMMENTS_MANUAL_SCROLL, false));

        view.findViewById(R.id.reply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View onClick) {
                comment(null, false);
            }
        });
        view.findViewById(R.id.switch_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View onClick) {
                move();
            }
        });

        view.findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View onClick) {
                // показываем, что грузим комментарии
                Anim.swapIcon(
                        ((ImageView) view.findViewById(R.id.update)),
                        Simple.makeLuna()
                );
                invalidateNew();
                refresh();
            }
        });

        listView.setFastScrollEnabled(Static.cfg.ensure(Keys.COMMENTS_FAST_SCROLL, false));
        listView.setScrollingCacheEnabled(Static.cfg.ensure(Keys.COMMENTS_SCROLL_CACHE, false));

        view.findViewById(R.id.down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select(comments.size() - 1, 1);
                adapter.selected = -1;
            }
        });
        view.findViewById(R.id.up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select(0, 5000);
                adapter.selected = -1;
            }
        });

		/* Настраиваем бар */
        View bar = view.findViewById(R.id.bar);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bar.getLayoutParams();
        if (!bar_on_top)
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bar.getBackground().setAlpha(150);
        /* Ставим для того, чтобы бар не пропускал на нижние вьюхи нажатия.*/
        bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        DisplayMetrics dm = context.getResources().getDisplayMetrics();

        View footer = new View(context);
        footer.setLayoutParams(new AbsListView.LayoutParams(0, Math.min(dm.widthPixels, dm.heightPixels) / 2));

        adapter = new CommentListAdapter(context);
        listView.addFooterView(footer);

        return view;
    }

    @Override
    protected void onRemove(View view, ViewGroup parent, Context context) {
        super.onRemove(view, parent, context);

        Static.bus.unregister(this);

        for (CommentPart part : adapter.comment_cache.values()) part.kill();

        if (topicPart instanceof LetterPart)
            ((LetterPart) topicPart).onRemove(null, null, null);
        if (topicPart instanceof TopicPart)
            ((TopicPart) topicPart).onRemove(null, null, null);
    }

    /**
     * Адаптер списка комментариев.
     */
    private class CommentListAdapter extends BaseAdapter {

        private final Context context;
        private int selected = -1;

        private HashMap<Comment, CommentPart> comment_cache;
        private LevelDrawable level_indicator;
        private int c_level = 0;

        public CommentListAdapter(Context context) {
            this.context = context;
            comment_cache = new HashMap<>();
            level_indicator = new LevelDrawable(context.getResources(), 0);
        }

        @Override
        public int getCount() {
            return comments.size() == 0 ? 1 : comments.size();
        }

        @Override
        public Object getItem(int i) {
            return comments.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        /**
         * Сдвигает все текущие комментарии на новый уровень.
         */
        public void setOffset(int offset) {
            if (c_level != offset) {
                c_level = offset;

				/* Иногда появляются проблемы с кэшем прорисовки на старых девайсах (привет, LazyOne) */
                if (Build.VERSION.SDK_INT < 11) {
                    ViewGroup.LayoutParams params = listView.getLayoutParams();
                    DisplayMetrics dm = Static.ctx.getResources().getDisplayMetrics();
                    int nw = dm.widthPixels + offset * comment_ladder;
                    if (nw < params.width)
                        params.width = nw;
                }

//				listView.scrollTo(offset * comment_ladder, listView.getScrollY());

                Anim.shift(listView, offset * comment_ladder, 100, null);
            }
        }


        /**
         * Передвигает дерево по максимальному комменту.
         */
        private void autoshift() {
            int max = 0;
            for (int i = 0; i < listView.getCount(); i++) {
                View child = listView.getChildAt(i);
                if (child == null) continue;
                Object tag = child.getTag(R.id.id);

                if (tag != null)
                    max = Math.max((Integer) tag, max);

            }

            max = max - autoshift_offset > 0 ? max - autoshift_offset : 0;
            setOffset(max);

        }

        double scaleComment = Static.cfg.ensure(Keys.COMMENTS_SCALE_WIDTH, 1.0d);
        boolean autoshift = Static.cfg.ensure(Keys.COMMENTS_AUTOSHIFT, false);
        boolean doubleclick = Static.cfg.ensure(Keys.COMMENTS_DOUBLECLICK_SHIFT, false);
        int autoshift_offset = Static.cfg.ensure(Keys.COMMENTS_AUTOSHIFT_OFFSET, 0);
        boolean show_levels = Static.cfg.ensure(Keys.COMMENTS_SHOW_LEVELS, false);

        @SuppressWarnings({"AssignmentToMethodParameter", "deprecation"})
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

			/* Отметка о пустой секции комментариев. */
            if (comments.size() == 0) {
                return LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.no_comments, viewGroup, false);
            } else if (view != null && view.findViewById(R.id.avatar) == null)
                view = null;


            final Comment comment = comments.get(i);
            CommentPart part;

			/* Проверяем кэш на наличие собранных вьюх */
            if (!comment_cache.containsKey(comment)) {
                part = new CommentPart(comment, isLetter);
                comment_cache.put(comment, part);
            } else {
                part = comment_cache.get(comment);
            }

			/* Проверяем вьюху на наличие вьюхи */
            if (view == null)
                view = part.create(LayoutInflater.from(viewGroup.getContext()), viewGroup, viewGroup.getContext());
            else
                part.convert(view, viewGroup.getContext());

            view.setTag(R.id.id, levels.get(comment.id));

			/* Начинаем колбаситься */
            LinearLayout.LayoutParams rootLayoutParams = (LinearLayout.LayoutParams) view.findViewById(R.id.root).getLayoutParams();
            final int level = levels.get(comment.id);
            final int comment_pixel_offset = levels.get(comment.id) * comment_ladder;

			/* Достаём отступы */
            View right_margin = view.findViewById(R.id.end_clickable_margin);
            View left_margin = view.findViewById(R.id.start_clickable_margin);

			/* Ставим двигалку */
            View.OnClickListener shiftInvoker = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listView.getScrollX() == comment_pixel_offset)
                        setOffset(0);
                    else
                        setOffset(level);
                }
            };
            if (doubleclick) {
                final View.OnClickListener lst = shiftInvoker;
                shiftInvoker = new DoubleClickListener() {
                    @Override
                    public void act(View v) {
                        lst.onClick(v);
                    }
                };
            }


            /**
             *  Сдвигаем всё нафиг.
             */
            if (autoshift) autoshift();

            view.findViewById(R.id.data).setOnClickListener(shiftInvoker);
            right_margin.setOnClickListener(shiftInvoker);
            left_margin.setOnClickListener(shiftInvoker);
            left_margin.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    select(indexOf(comment.parent), indexOf(comment.id));
                    return true;
                }
            });

			/* Достаём размер экрана */
            int dWidth = (int) (context.getResources().getDisplayMetrics().widthPixels * scaleComment);
            int dHeight = context.getResources().getDisplayMetrics().heightPixels;

			/* Проставляем отступы */
            left_margin.getLayoutParams().width = comment_pixel_offset;
            right_margin.getLayoutParams().width = ((max_level + LADDER_MARGIN) * comment_ladder - comment_pixel_offset);

			/* Ставим размер самого комментария */
            rootLayoutParams.width = Math.min(dWidth, dHeight);

			/* Ставим размер основы вьюхи комментария, на ней сидит и марджин со всем фонами, и root*/
            view.getLayoutParams().width =
                    rootLayoutParams.width
                            + left_margin.getLayoutParams().width
                            + right_margin.getLayoutParams().width;

			/* Ставим цвет всего, что не правый марджин в цвет уровня комментария + 1*/
            if (show_levels) {
                view.setBackgroundColor(level_indicator.getLastColor(left_margin.getLayoutParams().width));
                left_margin.setBackgroundDrawable(level_indicator);
            }

			/* Ставим слушалки на кнопки */
            view.findViewById(R.id.reply).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    comment(comment, false);
                }
            });

            view.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    comment(comment, true);
                }
            });

			/* Отмечаем выделенный коммент */
            if (selected == i)
                view.findViewById(R.id.root).setBackgroundColor(context.getResources().getColor(R.color.bg_item_selected));

            return view;

        }
    }

}
