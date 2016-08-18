package cz.library.anim;

public interface AnimatorListener {


    void onAnimationStart(AnimatorCompat.Animator animation);


    void onAnimationEnd(AnimatorCompat.Animator animation);


    void onAnimationCancel(AnimatorCompat.Animator animation);


    void onAnimationRepeat(AnimatorCompat.Animator animation);
}