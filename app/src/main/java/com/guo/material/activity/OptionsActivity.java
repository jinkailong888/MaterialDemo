package com.guo.material.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guo.material.R;
import com.guo.material.widget.options.ActivityCompatICS;
import com.guo.material.widget.options.ActivityOptionsCompatICS;
import com.guo.material.widget.options.transition.TransitionCompat;
import com.guo.material.widget.reveal.RevealBackgroundView;

public class OptionsActivity extends AppCompatActivity implements RevealBackgroundView.OnStateChangeListener {

    public static boolean isSceneAnim = false;
    private RevealBackgroundView reveal_view;
    private ImageView orginalImageView;
    private TextView orginalTextView;
    private ImageView chromeIView;
    private RelativeLayout rl_content;
    private Intent intent;
    // 通过handler来相应动画操作，进行动画元素的恢复
    public static MyHandler handler;
    private int screen_width, screen_height;

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 123) {
                orginalImageView.setVisibility(View.INVISIBLE);
                orginalTextView.setVisibility(View.INVISIBLE);
                chromeIView.setVisibility(View.INVISIBLE);
            } else if (msg.what == 321) {
                orginalImageView.setVisibility(View.VISIBLE);
                orginalTextView.setVisibility(View.VISIBLE);
                chromeIView.setVisibility(View.VISIBLE);
                isSceneAnim = false;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        handler = new MyHandler();

        intent = new Intent(OptionsActivity.this, TargetActivity.class);

        orginalImageView = (ImageView) findViewById(R.id.original_imageView);
        orginalTextView = (TextView) findViewById(R.id.original_textView);
        chromeIView = (ImageView) findViewById(R.id.chrome_imageView);
        reveal_view = (RevealBackgroundView) findViewById(R.id.reveal_view);
        rl_content = (RelativeLayout) findViewById(R.id.rl_content);

//        final int[] location = getIntent().getIntArrayExtra("location");
//        Log.d("location", location[0] + "  " + location[1]);
        reveal_view.setFillPaintColor(getResources().getColor(R.color.triangle));
        reveal_view.setOnStateChangeListener(this);
        screen_width = getResources().getDisplayMetrics().widthPixels;
        screen_height = getResources().getDisplayMetrics().heightPixels;
//        reveal_view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                reveal_view.getViewTreeObserver().removeOnPreDrawListener(this);
//                reveal_view.startFromLocation(new int[]{location[0], location[1]});
////                reveal_view.startFromLocation(new int[]{screen_width / 2, screen_height / 2});
//                return true;
//            }
//        });
    }


    @SuppressWarnings("unchecked")
    public void buttonListener(View views) {
        switch (views.getId()) {
            case R.id.custom_button:
                customAnim();
                //startActivity(intent);
                break;
            case R.id.scaleUp_button:
                scaleUpAnim(orginalImageView);
                break;
            case R.id.thumbnail_button:
                thumbNailScaleAnim(chromeIView);
                break;
            case R.id.scene_button:
                screenTransitAnimByPair(
                        Pair.create((View) orginalImageView, R.id.target_imageView),
                        Pair.create((View) orginalTextView, R.id.target_textView),
                        Pair.create((View) chromeIView, R.id.target_chrome_imageView));
                break;

            default:
                break;
        }
    }

    public void customAnim() {
        ActivityOptionsCompatICS options = ActivityOptionsCompatICS.makeCustomAnimation(this,
                R.anim.slide_bottom_in, R.anim.slide_top_out);
        ActivityCompatICS.startActivity(OptionsActivity.this, intent, options.toBundle());
    }

    public void scaleUpAnim(View view) {
        ActivityOptionsCompatICS options = ActivityOptionsCompatICS.makeScaleUpAnimation(view,
                0, 0, //拉伸开始的坐标
                view.getMeasuredWidth(), view.getMeasuredHeight());//初始的宽高
        ActivityCompatICS.startActivity(OptionsActivity.this, intent, options.toBundle());
    }

    public void thumbNailScaleAnim(ImageView view) {
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache();
        ActivityOptionsCompatICS options = ActivityOptionsCompatICS.makeThumbnailScaleUpAnimation(
                view, bitmap, 0, 0);
        // Request the activity be started, using the custom animation options.
        ActivityCompatICS.startActivity(OptionsActivity.this, intent, options.toBundle());
        //view.setDrawingCacheEnabled(false);
    }

    public void screenTransitAnim(View v, int id) {
        isSceneAnim = true;
        ActivityOptionsCompatICS options = ActivityOptionsCompatICS.
                makeSceneTransitionAnimation(this, v, id);
        ActivityCompatICS.startActivity(OptionsActivity.this, intent, options.toBundle());
    }

    /**
     * 这里可以用多个元素或者是单个元素进行动画，这里用了多个元素。为了保证动画效果，这里进行了渐变操作，
     * 在handle中进行了恢复操作，这样让动画看起来平滑了很多
     *
     * @param views
     */
    @SuppressWarnings("unchecked")
    public void screenTransitAnimByPair(Pair<View, Integer>... views) {
        isSceneAnim = true;
        ActivityOptionsCompatICS options = ActivityOptionsCompatICS.makeSceneTransitionAnimation(
                OptionsActivity.this, views);
        ActivityCompatICS.startActivity(OptionsActivity.this, intent, options.toBundle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ActivityOptionsCompatICS.RESULT_CODE) {
            //Toast.makeText(this, "Back", 0).show();
        }
    }

    @Override
    public void onBackPressed() {
        TransitionCompat.finishAfterTransition(this, R.anim.slide_left_in, R.anim.slide_right_out);
//		TransitionCompat.finishAfterTransition(this);
    }

    @Override
    public void onStateChange(int state) {
        switch (state) {
            case RevealBackgroundView.STATE_FILL_STARTED:
                break;
            case RevealBackgroundView.STATE_FINISHED:
                rl_content.setVisibility(View.VISIBLE);
                break;
        }
    }
}

