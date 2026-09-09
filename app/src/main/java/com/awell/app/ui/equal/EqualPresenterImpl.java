package com.awell.app.ui.equal;

import com.awell.app.R;
import com.awell.app.utils.LogUtil;

import java.util.Arrays;

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
                mContext.getResources().getIntArray(R.array.aps_treble),
                mContext.getResources().getIntArray(R.array.aps_soft),
                mContext.getResources().getIntArray(R.array.aps_disco)
        };
        for (int i = 0; i < dataArray.length; i++) {
            int[] a = dataArray[i];
            LogUtil.i("equal initData[" + i + "] = " + (a == null ? "null" : "len=" + a.length + " " + Arrays.toString(a)));
        }
        mView.setData(dataArray);
    }

}
