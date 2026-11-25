package com.example.c20232884;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class HandleOnlyBottomSheetBehavior<V extends View> extends BottomSheetBehavior<V> {

    public HandleOnlyBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
        View handleView = child.findViewById(R.id.btn_schedule_handle);
        if (handleView != null) {
            int[] location = new int[2];
            handleView.getLocationOnScreen(location);
            int handleTop = location[1];

            int handleBottom = handleTop + handleView.getHeight();

            // 손가락 터치 위치가 핸들 바 범위를 벗어나면 드래그 감지 거부
            if (event.getRawY() < handleTop || event.getRawY() > handleBottom) {
                return false;
            }
        }
        return super.onInterceptTouchEvent(parent, child, event);
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        // super.onNestedPreScroll(...)을 호출하지 않음으로써 목록 스크롤이 시트를 당기지 못하게
    }
}