package com.awell.app.ui.equal;

import android.content.Context;

public interface Contract {

    public interface EqualView {
        void setData(int[][] data);
    }

    public abstract class EqualPresenter {
        protected EqualView mView;
        protected Context mContext;

        public void setContext(Context mContext) {
            this.mContext = mContext;
        }

        public void setView(EqualView mView) {
            this.mView = mView;
        }

        abstract void initData();

    }

}
