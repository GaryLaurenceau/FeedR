package com.sokss.feedr.app.utils;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;

import java.util.Map;

/**
 * Created by gary on 28/11/14.
 */

public class SwipeListener extends BaseSwipeListViewListener {

    @Override
    public void onOpened(int position, boolean toRight) {
    }

    @Override
    public void onClosed(int position, boolean fromRight) {
    }

    @Override
    public void onListChanged() {
    }

    @Override
    public void onMove(int position, float x) {
    }

    @Override
    public void onStartOpen(int position, int action, boolean right) {
    }

    @Override
    public void onStartClose(int position, boolean right) {
    }

    @Override
    public void onClickFrontView(int position) {
    }

    @Override
    public void onClickBackView(int position) {
    }

    @Override
    public void onDismiss(int[] reverseSortedPositions) {
//        for (int position : reverseSortedPositions) {
//            int i = 0;
//            for (Map.Entry<String, Conversation> c : me.getConversationsSort().entrySet()) {
//                if (i == position) {
//                    me.getConversations().remove(c.getKey());
//                    adapter.setConversations(me.getConversationsSort());
//                    adapter.notifyDataSetChanged();
//                    return;
//                }
//                ++i;
//            }
//        }
    }
}