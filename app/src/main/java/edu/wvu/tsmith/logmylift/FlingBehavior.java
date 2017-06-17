package edu.wvu.tsmith.logmylift;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;

public final class FlingBehavior extends AppBarLayout.Behavior {

    public FlingBehavior() {
    }

    public FlingBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY, boolean consumed) {
        if (target instanceof NestedScrollView) {
            final NestedScrollView scrollingView = (NestedScrollView) target;
            consumed = velocityY > 0 || scrollingView.computeVerticalScrollOffset() > 0;
        }
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }
}