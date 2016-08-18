package cz.library.anim;

import android.animation.*;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

public final class AnimatorCompat  {

    public static Animator ofInt(int value){
        return new Animator(value);
    }

    public static Animator ofFloat(float value){
        return new Animator(value);
    }

    public static class Animator{

        private final Handler handler=new Handler(Looper.getMainLooper());
        private List<AnimatorListener> listeners = new ArrayList<>();
        private List<AnimatorUpdateListener> updateListeners = new ArrayList<>();
        private long startTime;
        private long duration = 200;
        private float fraction = 0f;
        private Object target;
        private Object animatorValue;

        private boolean started = false;
        private boolean ended = false;

        private Animator(Object v) {
            this.target=v;
        }

        private Runnable loopRunnable = new Runnable() {
            @Override
            public void run() {
                long dt = System.currentTimeMillis() - startTime;
                float fraction = dt * 1f / duration;
                if(target instanceof Integer){
                    Integer target = (Integer) Animator.this.target;
                    Animator.this.animatorValue=Integer.valueOf((int) (target *fraction));
                } else if(target instanceof Float){
                    Float target = (Float) Animator.this.target;
                    Animator.this.animatorValue= target *fraction;
                }
                if (fraction > 1f ) {
                    fraction = 1f;
                    Animator.this.animatorValue=target;
                }
                Animator.this.fraction = fraction;
                notifyUpdateListeners(fraction);
                if (Animator.this.fraction >= 1f) {
                    dispatchEnd();
                } else {
                    handler.postDelayed(loopRunnable, 16);
                }
            }
        };

        private void notifyUpdateListeners(float fraction) {
            for (int i = updateListeners.size() - 1; i >= 0; i--) {
                updateListeners.get(i).onAnimationUpdate(this,fraction);
            }
        }


        public void addListener(AnimatorListenerAdapter listener) {
            listeners.add(listener);
        }

        public void setDuration(long duration) {
            if (!started) {
                this.duration = duration;
            }
        }

        public void start() {
            if (started) return;
            started = true;
            dispatchStart();
            fraction = 0f;
            startTime = System.currentTimeMillis();
            handler.postDelayed(loopRunnable, 16);
        }


        private void dispatchStart() {
            for (int i = listeners.size() - 1; i >= 0; i--) {
                listeners.get(i).onAnimationStart(this);
            }
        }

        private void dispatchEnd() {
            for (int i = listeners.size() - 1; i >= 0; i--) {
                listeners.get(i).onAnimationEnd(this);
            }
        }

        private void dispatchCancel() {
            for (int i = listeners.size() - 1; i >= 0; i--) {
                listeners.get(i).onAnimationCancel(this);
            }
        }


        public void cancel() {
            if (ended)  return;
            ended = true;
            if (started) {
                dispatchCancel();
            }
            dispatchEnd();
        }


        public void addUpdateListener(AnimatorUpdateListener animatorUpdateListener) {
            updateListeners.add(animatorUpdateListener);
        }


        public float getAnimatedFraction() {
            return fraction;
        }

        public Object getAnimatedValue(){
            return animatorValue;
        }

        public Integer getAnimatedIntValue(){
            return ((Integer)animatorValue);
        }

        public Float getAnimatedFloatValue(){
            return ((Float)animatorValue);
        }
    }
}