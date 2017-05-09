package com.yalin.style.view.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.yalin.style.R;
import com.yalin.style.event.MainContainerInsetsChangedEvent;
import com.yalin.style.event.SeenTutorialEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * @author jinyalin
 * @since 2017/5/2.
 */

public class TutorialFragment extends BaseFragment {

    private View mTutorialContainer;

    public static TutorialFragment newInstance() {
        return new TutorialFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.layout_include_tutorial_content, container, false);

        root.findViewById(R.id.tutorial_icon_affordance).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EventBus.getDefault().post(new SeenTutorialEvent());
                    }
                });

        mTutorialContainer = root.findViewById(R.id.tutorialContainer);
        return root;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        float animateDistance = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100,
                getResources().getDisplayMetrics());
        View mainTextView = view.findViewById(R.id.tutorial_main_text);
        mainTextView.setAlpha(0);
        mainTextView.setTranslationY(-animateDistance / 5);

        View subTextView = view.findViewById(R.id.tutorial_sub_text);
        subTextView.setAlpha(0);
        subTextView.setTranslationY(-animateDistance / 5);

        final View affordanceView = view.findViewById(R.id.tutorial_icon_affordance);
        affordanceView.setAlpha(0);
        affordanceView.setTranslationY(animateDistance);

        View iconTextView = view.findViewById(R.id.tutorial_icon_text);
        iconTextView.setAlpha(0);
        iconTextView.setTranslationY(animateDistance);

        AnimatorSet set = new AnimatorSet();
        set.setStartDelay(500);
        set.setDuration(250);
        set.playTogether(
                ObjectAnimator.ofFloat(mainTextView, View.ALPHA, 1f),
                ObjectAnimator.ofFloat(subTextView, View.ALPHA, 1f));
        set.start();

        set = new AnimatorSet();
        set.setStartDelay(2000);

        // Bug in older versions where set.setInterpolator didn't work
        Interpolator interpolator = new OvershootInterpolator();
        ObjectAnimator a1 = ObjectAnimator.ofFloat(affordanceView, View.TRANSLATION_Y, 0);
        ObjectAnimator a2 = ObjectAnimator.ofFloat(iconTextView, View.TRANSLATION_Y, 0);
        ObjectAnimator a3 = ObjectAnimator.ofFloat(mainTextView, View.TRANSLATION_Y, 0);
        ObjectAnimator a4 = ObjectAnimator.ofFloat(subTextView, View.TRANSLATION_Y, 0);
        a1.setInterpolator(interpolator);
        a2.setInterpolator(interpolator);
        a3.setInterpolator(interpolator);
        a4.setInterpolator(interpolator);
        set.setDuration(500).playTogether(
                ObjectAnimator.ofFloat(affordanceView, View.ALPHA, 1f),
                ObjectAnimator.ofFloat(iconTextView, View.ALPHA, 1f),
                a1, a2, a3, a4);
        if (Build.VERSION.SDK_INT >= 21) {
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        ImageView emanateView = (ImageView)
                                view.findViewById(R.id.tutorial_icon_emanate);
                        AnimatedVectorDrawable avd = (AnimatedVectorDrawable)
                                getResources().getDrawable(
                                        R.drawable.avd_tutorial_icon_emanate,
                                        getActivity().getTheme());
                        emanateView.setImageDrawable(avd);
                        avd.start();
                    }
                }
            });
        }
        set.start();

        MainContainerInsetsChangedEvent mcisce = EventBus.getDefault().getStickyEvent(
                MainContainerInsetsChangedEvent.class);
        if (mcisce != null) {
            onEventMainThread(mcisce);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEventMainThread(MainContainerInsetsChangedEvent spe) {
        Rect insets = spe.getInsets();
        mTutorialContainer.setPadding(
                insets.left, insets.top, insets.right, insets.bottom);
    }
}
