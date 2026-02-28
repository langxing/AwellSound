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
        mView.setData(dataArray);
    }

}
