package com.cab404.ponyscape.parts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.cab404.acli.FragmentedList;
import com.cab404.acli.Part;
import com.cab404.libtabun.data.Profile;
import com.cab404.ponyscape.R;
import com.cab404.ponyscape.bus.AppContextExecutor;
import com.cab404.ponyscape.bus.E;
import com.cab404.ponyscape.utils.Static;
import com.cab404.ponyscape.utils.images.BitmapMorph;
import com.cab404.ponyscape.utils.text.HtmlRipper;
import com.cab404.ponyscape.utils.views.RunCommandOnClick;
import com.cab404.ponyscape.utils.views.ViewSugar;
import com.cab404.sjbus.Bus;

/**
 * @author cab404
 */
public class ProfilePart extends Part {

    View view;
    @ViewSugar.Bind(R.id.strength)
    TextView powah;
    @ViewSugar.Bind(R.id.rating)
    TextView ratin;
    @ViewSugar.Bind(R.id.listOfThings)
    ScrollView listOfThings;

    @ViewSugar.Bind(R.id.avatar)
    ImageView avatar;

    @ViewSugar.Bind(R.id.background)
    ImageView background;

    @ViewSugar.Bind(R.id.topPart)
    RelativeLayout topPart;


    @ViewSugar.Bind(R.id.favTopics)
    TextView ft;

    @ViewSugar.Bind(R.id.favComments)
    TextView fc;

    @ViewSugar.Bind(R.id.createdTopics)
    TextView ct;

    @ViewSugar.Bind(R.id.createdComments)
    TextView cc;

    private final Profile profile;
    private HtmlRipper ripper;

    public ProfilePart(Profile profile) {
        this.profile = profile;
    }

    @Bus.Handler
    public void handleImages(final E.GotData.Image.Loaded image) {

        if (image.src.equals(profile.big_icon)) {
            Static.pools.img_oper.execute(new Runnable() {
                @Override
                public void run() {
                    int bevel = getContext().getResources().getDimensionPixelSize(R.dimen.corner_cut);

                    final Bitmap bitmap = BitmapMorph.bevel(
                            BitmapMorph.background(
                                    BitmapMorph.manualCopy(
                                            Static.img.scale(
                                                    image,
                                                    avatar.getWidth(),
                                                    avatar.getHeight()
                                            )
                                    ),
                                    0x44ffffff),
                            bevel);

                    Static.handler.post(
                            new Runnable() {
                                public void run() {
                                    avatar.setImageBitmap(bitmap);
                                }
                            }
                    );
                }
            });
        }


        if (image.src.equals(profile.photo)) {
            Static.pools.img_oper.execute(new Runnable() {
                @Override
                public void run() {
                    final ImageView bg_view = (ImageView) view.findViewById(R.id.background);
                    Bitmap bg;

                    bg = image.loaded;

                    int width = bg.getWidth();
                    int height = (int) (bg.getWidth() * ((float) bg_view.getHeight() / bg_view.getWidth()));
                    int y = (bg.getHeight() - height) / 2;
                    int bevel = getContext().getResources().getDimensionPixelSize(R.dimen.corner_cut);

                    bg =
                            BitmapMorph.bevel(
                                    Static.img.scale(
                                            new E.GotData.Image.Loaded(
                                                    BitmapMorph.blur(
                                                            BitmapMorph.tint(
                                                                    BitmapMorph.cut(
                                                                            bg,
                                                                            new Rect(0, y, width, y + height)
                                                                    ),
                                                                    0xff000000
                                                            ),
                                                            2
                                                    ),
                                                    image.src + "#blur2"
                                            ),
                                            bg_view.getWidth(),
                                            bg_view.getHeight()
                                    ),
                                    bevel);


                    final Bitmap finalBg = bg;
                    Static.handler.post(
                            new Runnable() {
                                public void run() {
                                    bg_view.setImageBitmap(finalBg);
                                }
                            }
                    );
                }
            });

        }

    }

    @Bus.Handler(executor = AppContextExecutor.class)
    public void onVoted(E.GotData.Vote.User e) {
        if (e.id == profile.id) {
            profile.votes = e.votes;
            ((TextView) view.findViewById(R.id.rating)).setText(profile.votes + "");
        }
    }

    @Override
    protected View create(LayoutInflater inflater, ViewGroup viewGroup, Context context) {
        view = inflater.inflate(R.layout.part_user_info, viewGroup, false);
        ViewSugar.bind(this, view);
        Static.bus.register(this);

//        listOfThings.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                System.out.println(background.getHeight());
//                System.out.println(event.getY());
//                System.out.println(listOfThings.getScrollY());
//                System.out.println(topPart.onTouchEvent(event));
////                if (event.getY() < background.getHeight())
////                    return view.onTouchEvent(event);
////                else
//                v.onTouchEvent(event);
//                return true;
//            }
//        });

        ripper = new HtmlRipper((ViewGroup) view.findViewById(R.id.data));
        ripper.escape(profile.about == null ? "Пока ничего не известно..." : profile.about);

        ((TextView) view.findViewById(R.id.name)).setText(profile.name);
        ((TextView) view.findViewById(R.id.nick)).setText(profile.login);
        ((TextView) view.findViewById(R.id.rating)).setText(profile.votes + "");
        ((TextView) view.findViewById(R.id.strength)).setText(profile.strength + "");

        ct.setOnClickListener(new RunCommandOnClick("page load \"/profile/" + profile.login + "/created/topics/\""));
        cc.setOnClickListener(new RunCommandOnClick("page load \"/profile/" + profile.login + "/created/comments/\""));
        ft.setOnClickListener(new RunCommandOnClick("page load \"/profile/" + profile.login + "/favourites/topics/\""));
        fc.setOnClickListener(new RunCommandOnClick("page load \"/profile/" + profile.login + "/favourites/comments/\""));

        view.findViewById(R.id.plus).setOnClickListener(new RunCommandOnClick("votefor user " + profile.id + " 1"));
        view.findViewById(R.id.minus).setOnClickListener(new RunCommandOnClick("votefor user " + profile.id + " -1"));


        if (Build.VERSION.SDK_INT >= 11)
            view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    Static.img.download(profile.photo);
                    Static.img.download(profile.big_icon);
                }
            });

        return view;
    }

    @Override
    protected void onInsert(FragmentedList parent) {
        super.onInsert(parent);
//		Static.img.download(profile.photo);
//		Static.img.download(profile.big_icon);
    }

    @Override
    protected void onRemove(View view, ViewGroup parent, Context context) {
        Static.bus.unregister(this);
        super.onRemove(view, parent, context);
        ripper.destroy();
    }
}
