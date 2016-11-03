package com.sergeyloginov.sharpperfection.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

import com.sergeyloginov.sharpperfection.R;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private Context context;
    private Drawable divider;
    private int dpLeft;

    public DividerItemDecoration(Context context, int dpLeftMargin) {
        divider = ContextCompat.getDrawable(context, R.drawable.line_divider);
        this.context = context;
        this.dpLeft = dpLeftMargin;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        Resources r = context.getResources();
        int left = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpLeft,
                r.getDisplayMetrics());
        int rightPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16,
                r.getDisplayMetrics());
        int right = parent.getWidth() - rightPx;
        int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                    child.getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();
            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }
}
