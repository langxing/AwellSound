package com.awell.app;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.awell.app.utils.ApsStation;
import com.awell.app.utils.LogUtil;
import com.awell.app.utils.ToolClass;
import com.awell.kpslibrary.Constant;
import com.awell.kpslibrary.module.AwellAudio;

public class ApsService extends Service {

    private static final int APS_FG_NOTIFICATION_ID = 28;
    private static final String APS_FG_CHANNEL_ID = "awell_asp";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i("onCreate");
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i("onStartCommand");
        startAsForeground();
        try {
            setInitData();
        } catch (Throwable e) {
            LogUtil.i("onStartCommand error = " + e);
        } finally {
            stopForeground(true);
            stopSelf(startId);
        }
        return START_NOT_STICKY;
    }

    private void startAsForeground() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager != null) {
            NotificationChannel channel = new NotificationChannel(
                    APS_FG_CHANNEL_ID, "asp", NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }
        android.app.Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new android.app.Notification.Builder(this, APS_FG_CHANNEL_ID)
                : new android.app.Notification.Builder(this);
        Notification notification = builder
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(getString(R.string.app_name))
                .setShowWhen(false)
                .build();
        startForeground(APS_FG_NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i("onDestroy");
    }

    //Boot initialization
    private void setInitData(){
        int[] dsp = AwellAudio.getIntParameter(Constant.IAUDIOCONTROL.CMD.GETDSP.code, null);
        if (dsp == null || (dsp[0] != Constant.IAUDIOCONTROL.DSP.IC2313.code)){
            return;
        }

        int[] apsFreq = AwellAudio.getIntParameter(Constant.IAUDIOCONTROL.CMD.GETBANDS.code, null);
        int[] apsGainRange = AwellAudio.getIntParameter(Constant.IAUDIOCONTROL.CMD.GETBANDLEVELRANGE.code, null);
        int gainMax = 0;
        if (apsGainRange != null && apsGainRange.length > 1) {
            LogUtil.i("apsGainRange[0] = " + apsGainRange[0] + " apsGainRange[1] = " + apsGainRange[1]);
            gainMax = apsGainRange[1] - apsGainRange[0];
        }

        int bass = ToolClass.getBassGain(this);
        int treble = ToolClass.getTrebleGain(this);
        LogUtil.i("freq = " + apsFreq);
        if (apsFreq != null && apsFreq.length > 1) {
            setPlay(apsFreq[0], bass, gainMax);
            setPlay(apsFreq[1], treble, gainMax);
        }

        //sound
        int[] sound = ApsStation.getSoundData(this,ApsStation.NAME_SEND);

        if (sound != null){
            LogUtil.i("l = " + sound[0] + " t = " + sound[1] + " r = " + sound[2] + " b = " + sound[3]);
            AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETLFOUTFADER.code, new int[]{sound[0]},1);
            AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETRFOUTFADER.code, new int[]{sound[1]},1);
            AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETLROUTFADER.code, new int[]{sound[2]},1);
            AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETRROUTFADER.code, new int[]{sound[3]},1);
        }

        // besLoudness
        boolean besLoudness = ToolClass.getLoudnessGain(this) == 1;
        if (besLoudness){
            AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETLOUDNESSGAIN.code, new int[]{1}, 1);
            ToolClass.setLoudnessGain(this, 1);
        } else {
            AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETLOUDNESSGAIN.code, new int[]{0}, 1);
            ToolClass.setLoudnessGain(this, 0);
        }
    }

    private void setPlay(int freq, int gain, int gainMax) {
        gain = gain - gainMax / 2;
        LogUtil.d("freq = " + freq + " gain = " + gain + " gainMax = " + gainMax);
        AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETBANDLEVEL.code, new int[]{freq, gain}, 2);
    }

}
