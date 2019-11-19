package yskim.sample.aidl.server;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

public class ServerService extends Service {

    private final static String TAG = "ServerService";
    private RemoteCallbackList<IRemoteCallback> callbackList = new RemoteCallbackList<>();
    private int value;

    public ServerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return new IRemoteServiceImpl();
    }

    private class IRemoteServiceImpl extends IRemoteService.Stub {

        @Override
        public int sum(int a, int b) throws RemoteException {
            return a + b;
        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public boolean registerCallback(IRemoteCallback callback) throws RemoteException {
            return callbackList.register(callback);
        }

        @Override
        public boolean unregisterCallback(IRemoteCallback callback) throws RemoteException {
            return callbackList.unregister(callback);
        }
    }

    private final static int MSG_BROADCAST = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_BROADCAST: {
                    value++;
                    int size = callbackList.beginBroadcast();
                    for (int i = 0; i < size; i++) {
                        try {
                            callbackList.getBroadcastItem(i).onUpdate(value);
                        } catch (RemoteException e) {
                            Log.e(TAG, "exception", e);
                        }
                    }
                    callbackList.finishBroadcast();
                    sendEmptyMessageDelayed(MSG_BROADCAST, 1000);
                }
                break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        handler.sendEmptyMessageDelayed(MSG_BROADCAST, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
