package com.testservice.bindservice;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {


    //见HelloBinder项目 binder的原理

//----------------------------------------------------------------------------------------------------------------------------


//aidl文件到底干啥用的？
//系统会根据aidl生成一个继承IInterface的接口 接口中有静内Stub 静内Stub里有Proxy


//Stub类是干甚？（服务端）
//1.是service中返回的IBinder对象继承的类  2.aidl文件生成的接口中的静态内部类（生成的用于接收client发送过来的消息的）
//可以理解为远程service中的对象 其中封装了onTransact方法 用于在server端接受client发来的消息 并调用aidl中的方法 所以要实现aidl接口


//Proxy（客户端）
//供client调用的代理类 实现aidl文件文件中定义的接口 也就是client拿到的接口的多态对象就是Proxy对象  里面有远程binder对象BinderProxy


//整个流程：
//client通过Proxy对象发送数据给BinderProxy      BinderProxy将数据发送给server中的Stub类


//----------------------------------------------------------------------------------------------------------------------------
    /**
     * 双向通信  一个进程中
     * <p>
     * 同一个进程下，onBind返回的IBinder也就是ServiceConnection返回的IBinder：是自己定义的Binder对象，
     * 对象中包含Service对象，CountService.ServiceBinder。直接强转ServiceConnection返回的IBinder的对象即可拿到CountService.ServiceBinder
     * 这样在activity中ServiceConnection中可以拿到Service对象。进而持有和操作Service数据了,这是主动获取service的数据，service如何
     * 向activity主动传递数据呢，这就用到接口，拿到service对象后，把一个接口传进service对象，回调即可（ServiceListener）。此时，
     * Service如果设置android:process不同进程会报错。
     * <p>
     * <p>
     * Service如果设置android:process则需要AIDL.此时onBind返回的IBinder也就是ServiceConnection返回的IBinder：android.os.BinderProxy
     * ServiceConnection中DataService.Stub.asInterface(service)才可以拿到定义的AIDL接口实例，才可以调用主动持有和操作Service数据。
     * <p>
     * service如何主动去给activity传递数据呢？
     * 再定义个AIDL接口。将接口实例set给第一个AIDL接口,service中持有第一个AIDL接口，间接持有第二个AIDL接口。
     * <p>
     * <p>
     * <p>
     * 如何传递对象？
     * 对象实现Parcelable
     * 增加  对象 xx.aidl 注意包名要和java代码对象包名一致 aidl中只有一个属性定义：parcelable Book;
     * 其他aidl中就可以使用该对象了。传参数 in  out
     */

//----------------------------------------------------------------------------------------------------------------------------
    /**
     * 跨进程 bind service
     * service中 onbind方法中返回的IBinder对象==com.testservice.bindservice.CountService$MyBinder（extends DataService.Stub）
     * activity中ServiceConnection传入的IBinder对象==android.os.BinderProxy
     * DataService.Stub.asInterface(IBinder对象)==com.testservice.bindservice.DataService$Stub$Proxy (拿到的是service中MyBinder对象的代理)
     * <p>
     * activity给service发消息
     * 通过aidl接口传参数给service
     * aidl接口中可以设置set get方法 这样activity可以传入或get值
     * <p>
     * <p>
     * service给activity发消息
     * 通过另外一个aidl接口ServiceDataListener并将这个接口设置给远程service
     * ServiceDataListener serviceDataListener = new ServiceDataListener.Stub() {
     *
     * @Override public int getCountFromService(int i) {
     * return i;
     * }
     * };
     * <p>
     * <p>
     * public interface DataService extends IInterface {
     * public abstract static class Stub extends Binder implements DataService {
     * public static DataService asInterface(IBinder obj) {
     * if (obj == null) {
     * return null;
     * } else {
     * IInterface iin = obj.queryLocalInterface("com.testservice.bindservice.DataService");
     * return (DataService)(iin != null && iin instanceof DataService ? (DataService)iin : new DataService.Stub.Proxy(obj));//返回的代理对象
     * }
     * }
     * public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {}//重要
     * private static class Proxy implements DataService {这里面是要调用的远程的业务方法里调用
     * mRemote.transact(Stub.TRANSACTION_getConuntAIDL, _data, _reply, 0);}//mRemote就是上面的obj
     * }
     * }
     */

//----------------------------------------------------------------------------------------------------------------------------
    CountService countService;

    private Handler mHandler = new InternalHandler(this);

    private static class InternalHandler extends Handler {
        private WeakReference<Activity> weakRefActivity;

        /**
         * A constructor that gets a weak reference to the enclosing class. We
         * do this to avoid memory leaks during Java Garbage Collection.
         */
        public InternalHandler(Activity activity) {
            weakRefActivity = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity activity = weakRefActivity.get();
            if (activity != null) {
                Book i = (Book) msg.obj;
                Toast.makeText(activity, "service主动传回的数据是： " + i.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    DataService dataService;//这不是个service
    private ServiceConnection conn = new ServiceConnection() {
        /** 获取服务对象时的操作 */
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service == null) {
                Log.d("MainActivity", "返回IBinder=======null");
                return;
            }

            Log.d("MainActivity", "pid=" + Process.myPid() + " onServiceConnected返回");


            dataService = DataService.Stub.asInterface(service);
            try {
                service.linkToDeath(md, 0);
                dataService.setListener(serviceDataListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            System.out.println("ServiceConnection中的Binder" + service.getClass());


            //  System.out.println("ServiceConnection中的dataService" + dataService.getClass());
            // TODO Auto-generated method stub
//            countService = ((CountService.ServiceBinder) service).getService();
//            countService.setServiceListener(new ServiceListener() {
//                @Override
//                public void sendToActivity(int i) {
//                    Log.d("MainActivity", "i=" + i);
//
//                    // Toast.makeText(MainActivity.this, "service主动传回的数据是： " + i, Toast.LENGTH_SHORT).show();
//                }
//            });
        }

        /** 无法获取到服务对象时的操作 */
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            countService = null;
            Log.d("MainActivity", "onServiceDisconnected=============");//此处在UI线程中执行
        }

    };


    ServiceDataListener serviceDataListener = new ServiceDataListener.Stub() {

        @Override
        public void getCountFromService(Book book) {
            Log.d("MainActivity", "pid=" + Process.myPid() + "  service主动传回的数据是： " + book.getName());
            //Toast.makeText(MainActivity.this, "service主动传回的数据是： " + i, Toast.LENGTH_SHORT).show();


            Message msg = new Message();
            msg.what = 11;
            msg.obj = book;
            mHandler.sendMessage(msg);
        }

    };

    private IBinder.DeathRecipient md = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {

            if (dataService == null) {
                return;
            }
            dataService.asBinder().unlinkToDeath(md, 0);
            dataService = null;
            //logcat选择remote进程，点击x号模拟此功能  客户端的binder线程中执行
            Log.d("MainActivity", "binderDied=============");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TextView) findViewById(R.id.text)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Book count = null;
                try {
                    count = dataService.getConuntAIDL(0);
                    System.out.println("MainActivity count=" + count);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                // Toast.makeText(MainActivity.this, "activity主动获取的数据是： " + countService.getConunt(), Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "activity主动获取的数据是： " + count.getName(), Toast.LENGTH_SHORT).show();

            }
        });


        findViewById(R.id.ddd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dataService.addBook(new Book(88, "三国"));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                // Toast.makeText(MainActivity.this, "activity主动获取的数据是： " + countService.getConunt(), Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "activity主动添加的数据是： " + "三国", Toast.LENGTH_SHORT).show();

            }
        });


        //绑定服务
        Intent intent = new Intent(MainActivity.this, CountService.class);
        //startService(intent);
        /** 进入Activity开始服务 */
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity.this.unbindService(conn);
                    // String s = dataService.stopService();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unbindService(conn);
        Log.v("MainStadyServics", "out");
    }
}
