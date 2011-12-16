/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.animation.cts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import java.util.ArrayList;

import com.android.cts.animation.R;

public class AnimationActivity extends Activity {
    private static final String BALL_HEIGHT = "ballwidth";
    private static final String BALL_WIDTH = "ballheight";
    private static final String STARTX = "startx";
    private static final String STARTY = "starty";
    private static final String DELTAX = "deltax";
    private static final String DELTAY = "deltay";
    private static final String DURATION = "duration";

    float mBallHeight = 50f;
    float mBallWidth = 50f;
    float mStartX = 200f;
    float mStartY = 200f;
    float mDeltaX = 200f;
    float mDeltaY = 200f;
    long mDuration = 1000;
    public AnimationView view = null;
    private boolean isAnimationRunning = false;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        Intent intent = this.getIntent();
        Bundle extras = intent.getExtras();
        if(extras != null) {
            mBallHeight = extras.getFloat(BALL_HEIGHT);
            mBallWidth = extras.getFloat(BALL_WIDTH);
            mStartX = extras.getFloat(STARTX);
            mStartY = extras.getFloat(STARTY);
            mDeltaX = extras.getFloat(DELTAX);
            mDeltaY = extras.getFloat(DELTAY);
            mDuration = extras.getInt(DURATION);
        }
        setContentView(R.layout.animation_main);
        view = new AnimationView(this);
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.container);
        viewGroup.addView(view);
    }

    public ValueAnimator createAnimator(Object object,String propertyName, long duration,
            int repeatCount, int repeatMode,
            TimeInterpolator timeInterpolator, float start, float end){
        ValueAnimator valueAnimator = ObjectAnimator.ofFloat(object, propertyName, start,end);
        valueAnimator.setDuration(duration);
        valueAnimator.setRepeatCount(repeatCount);
        valueAnimator.setInterpolator(timeInterpolator);
        valueAnimator.setRepeatMode(repeatMode);
        return valueAnimator;
    }

    public ValueAnimator createAnimatorWithRepeatMode(int repeatMode) {
        return createAnimator(view.newBall, "y", 1000,ValueAnimator.INFINITE, repeatMode,
                new AccelerateInterpolator(), mStartY, mStartY + mDeltaY);
    }

    public ValueAnimator createAnimatorWithRepeatCount(int repeatCount) {
        return createAnimator(view.newBall, "y", 1000,repeatCount, ValueAnimator.REVERSE,
                new AccelerateInterpolator(), mStartY, mStartY + mDeltaY);
    }

    public ValueAnimator createAnimatorWithDuration(long duration) {
        return createAnimator(view.newBall, "y", duration ,ValueAnimator.INFINITE,
                ValueAnimator.REVERSE,new AccelerateInterpolator(), mStartY, mStartY + mDeltaY);
    }

    public void startAnimation(long duration){
        ValueAnimator bounceAnimator = ObjectAnimator.ofFloat(view.newBall, "y", mStartY,
                mStartY + mDeltaY);
        bounceAnimator.setDuration(duration);
        bounceAnimator.setRepeatCount(ValueAnimator.INFINITE);
        bounceAnimator.setInterpolator(new AccelerateInterpolator());
        bounceAnimator.setRepeatMode(ValueAnimator.REVERSE);
        view.bounceAnimator = bounceAnimator;
        view.animateBall();
    }

    public void startAnimation(ValueAnimator valueAnimator){
        view.bounceAnimator = valueAnimator;
        view.animateBall();
        isAnimationRunning = true;
    }

    public class AnimationView extends View {
        private static final int RED = 0xffFF8080;
        private static final int BLUE = 0xff8080FF;
        public ShapeHolder newBall = null;
        public final ArrayList<ShapeHolder> balls = new ArrayList<ShapeHolder>();
        AnimatorSet animation = null;
        public ValueAnimator bounceAnimator;

        public AnimationView(Context context) {
            super(context);
            ValueAnimator colorAnim = ObjectAnimator.ofInt(this, "backgroundColor", RED, BLUE);
            colorAnim.setDuration(1000);
            colorAnim.setEvaluator(new ArgbEvaluator());
            colorAnim.setRepeatCount(1);
            colorAnim.setRepeatMode(ValueAnimator.REVERSE);
            colorAnim.start();
            newBall = addBall(mBallHeight, mBallWidth);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {


            return true;
        }

        public void animateBall() {
            float startY = mStartY;
            float endY = startY + mDeltaY;
            AnimatorSet bouncer = new AnimatorSet();
            bouncer.play(bounceAnimator);
            // Fading animation - remove the ball when the animation is done
            ValueAnimator fadeAnim = ObjectAnimator.ofFloat(newBall, "alpha", 1f, 0f);
            fadeAnim.setDuration(250);
            fadeAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    balls.remove(((ObjectAnimator)animation).getTarget());
                }
            });

            // Sequence the two animations to play one after the other
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(bouncer).before(fadeAnim);
            animatorSet.start();
        }

        private ShapeHolder addBall(float x, float y) {
            OvalShape circle = new OvalShape();
            circle.resize(x, y);
            ShapeDrawable drawable = new ShapeDrawable(circle);
            ShapeHolder shapeHolder = new ShapeHolder(drawable);
            shapeHolder.setX(x - 25f);
            shapeHolder.setY(y - 25f);
            Paint paint = drawable.getPaint();
            shapeHolder.setPaint(paint);
            int red = (int)(Math.random() * 255);
            int green = (int)(Math.random() * 255);
            int blue = (int)(Math.random() * 255);
            int color = 0xff000000 | red << 16 | green << 8 | blue;
            int darkColor = 0xff000000 | red/4 << 16 | green/4 << 8 | blue/4;
            RadialGradient gradient = new RadialGradient(37.5f, 12.5f,
                    50f, color, darkColor, Shader.TileMode.CLAMP);
            paint.setShader(gradient);
            balls.add(shapeHolder);
            return shapeHolder;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            for (int i = 0; i < balls.size(); ++i) {
                ShapeHolder shapeHolder = balls.get(i);
                canvas.save();
                canvas.translate(shapeHolder.getX(), shapeHolder.getY());
                shapeHolder.getShape().draw(canvas);
                canvas.restore();
            }
        }
    }
}

