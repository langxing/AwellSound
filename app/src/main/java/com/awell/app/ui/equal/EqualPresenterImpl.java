package com.awell.app.ui.equal;

import com.awell.app.R;

public class EqualPresenterImpl extends Contract.EqualPresenter {

    @Override
    public void initData() {
        int[][] dataArray = {
                mContext.getResources().getIntArray(R.array.aps_default),
                mContext.getResources().getIntArray(R.array.aps_normal),
                mContext.getResources().getIntArray(R.array.aps_jazz),
                mContext.getResources().getIntArray(R.array.aps_pop),
                mContext.getResources().getIntArray(R.array.aps_rock),
                mContext.getResources().getIntArray(R.array.aps_classical),
                mContext.getResources().getIntArray(R.array.aps_bass),
                mContext.getResources().getIntArray(R.array.aps_treble)
        };
        int[][] newArray = new int[dataArray.length][8];
        for (int i = 0; i < dataArray.length; i++) {
            newArray[i] = convert12to8(dataArray[i]);
        }
        mView.setData(newArray);
    }

    private int[] convert12to8(int[] oldEq) {
        if (oldEq.length != 12) return oldEq;
        int[] newEq = new int[8];
        // 映射索引关系（基于采样密度优化）
        int[] map = {0, 2, 3, 5, 6, 8, 10, 11};
        for (int i = 0; i < 8; i++) {
            newEq[i] = oldEq[map[i]];
        }
        return newEq;
    }

}
