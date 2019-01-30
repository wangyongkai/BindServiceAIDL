package com.testservice.bindservice;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;


/**
 * 计数的服务
 */
public class CountService extends Service {
    /**
     * 创建参数
     */
    boolean threadDisable;
    int count;
    ServiceDataListener listener_service;
    ServiceListener serviceListener;
    Book book = new Book(99, "红楼梦");

    public IBinder onBind(Intent intent) {
        int check = checkCallingOrSelfPermission("aaa.bbb.ccc");
        if (check == PackageManager.PERMISSION_DENIED) {
            Log.d("CountService", "check=" + check);
            return null;
        }
        // TODO Auto-generated method stub
        System.out.println("onBind.....");
        IBinder result = null;
        // if (null == result) result = new ServiceBinder();//同一个进程
        if (null == result) result = new MyBinder();//跨进程
        Toast.makeText(this, "onBind333", Toast.LENGTH_LONG).show();
        System.out.println("CountService中的Binder" + result.getClass());
        Log.d("CountService", "onBind");
        return result;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("CountService", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("CountService", "onUnbind");
        return super.onUnbind(intent);
    }

    public void onCreate() {
        super.onCreate();
        /** 创建一个线程，每秒计数器加一，并在控制台进行Log输出 */
        new Thread(new Runnable() {
            public void run() {
                while (!threadDisable) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                    count++;
                    if (count % 5 == 0) {
                        try {
                            // listener_service.getCountFromService(new Book(100, "水壶"));
                            // serviceListener.sendToActivity(count);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    // Log.v("CountService", "Count is" + count);
                }
            }
        }).start();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d("CountService", "onDestroy");
        /** 服务停止时，终止计数进程 */
        this.threadDisable = true;
    }

    public int getConunt() {
        return count;
    }

    public void setServiceListener(ServiceListener serviceListener) {
        this.serviceListener = serviceListener;
    }

    class MyBinder extends DataService.Stub {


        @Override
        public Book getConuntAIDL(int i) throws RemoteException {
            // SystemClock.sleep(10000L);
            return book;
        }

        @Override
        public void addBook(Book b) throws RemoteException {
            book = b;
        }

        @Override
        public String stopService() {
            // CountService.this.stopSelf();
            Log.d("CountService", "stopService");
            return isBinderAlive() + "";
        }

        @Override
        public void setListener(ServiceDataListener listener) throws RemoteException {
            listener_service = listener;
        }
    }


    //此方法是为了可以在Acitity中获得服务的实例
    class ServiceBinder extends Binder {
        public CountService getService() {
            return CountService.this;
        }

        @Override
        public boolean isBinderAlive() {
            return super.isBinderAlive();
        }
    }
}
