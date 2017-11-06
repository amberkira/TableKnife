package com.example.sleepeanuty.myapplication;

import android.app.IntentService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.ColumnLayoutHelper;
import com.alibaba.android.vlayout.layout.StaggeredGridLayoutHelper;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "main";
    public static final int BANNER=1;
    public static final int HEADER=BANNER<<1;
    public static final int SERVICE=HEADER<<1;
    public static final int TOPIC=SERVICE<<1;


    public RecyclerView mRecyclerview;
    public RecyclerView.RecycledViewPool pool;
    public Context mContext;
    public VirtualLayoutManager mManager;
    public DelegateAdapter mDelegateAdapter;
    public Handler h = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==1) {
                mDelegateAdapter.removeAdapter(2);
                StaggeredGridLayoutHelper mHelper = new StaggeredGridLayoutHelper(2,4);
                mDelegateAdapter.addAdapter(new ItemAdapter(mContext,mHelper,8,TOPIC));
            }
        }
    };

    public ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("test","connected");
            IMyAidlInterface itf = IMyAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

public TextView tv;
    @Override
    protected void onStart() {
        super.onStart();

        new Thread(new Runnable() {
            @Override
            public void run() {
                tv.setText("hahaha");
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentService s = new IntentService("spa") {
            @Override
            protected void onHandleIntent(@Nullable Intent intent) {

            }
        };
        mContext = this;
        tv = (TextView) findViewById(R.id.tv);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceConnection m = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        Log.e("service","connected");
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {

                    }
                };
                Intent it = new Intent(MainActivity.this,TestService.class);
                bindService(it,m,BIND_AUTO_CREATE);
                startService(it);
                //bindService(it,m,BIND_AUTO_CREATE);
            }
        });

        client c = new client();
        c.init();
        Intent it = new Intent(this,AidlService.class);
        bindService(it,mServiceConnection,BIND_AUTO_CREATE);
        initRv();

        ConcreteObserverable ober = new ConcreteObserverable();
        ConcreteObserver c1 = new ConcreteObserver();
        ConcreteObserver c2 = new ConcreteObserver();
        ConcreteObserver c3 = new ConcreteObserver();
        ober.register(c1);
        ober.register(c2);
        ober.register(c3);
        ober.notifyAll("sb" );

    }

    private void initRv() {
        mManager = new VirtualLayoutManager(this);
        mRecyclerview = (RecyclerView) findViewById(R.id.rv);
        mRecyclerview.setLayoutManager(mManager);

        pool = new RecyclerView.RecycledViewPool();
        mRecyclerview.setRecycledViewPool(pool);
        pool.setMaxRecycledViews(TOPIC,10);

        mDelegateAdapter = new DelegateAdapter(mManager);
        mRecyclerview.setAdapter(mDelegateAdapter);

        StaggeredGridLayoutHelper mHelper = new StaggeredGridLayoutHelper(2,4);
        mDelegateAdapter.addAdapter(new ItemAdapter(this,mHelper,8,TOPIC));

        ColumnLayoutHelper helper2 = new ColumnLayoutHelper();
        mDelegateAdapter.addAdapter(new ItemAdapter(this,helper2,4,TOPIC));
        ColumnLayoutHelper helper3 = new ColumnLayoutHelper();
        mDelegateAdapter.addAdapter(new ItemAdapter(this,helper3,4,SERVICE));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    h.sendEmptyMessage(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    class ItemAdapter extends DelegateAdapter.Adapter<BaseViewHolder>{

        public Context mContext;
        public LayoutHelper mHelper;
        public int mCount;
        public int mViewType;

        public ItemAdapter(Context mContext, LayoutHelper mHelper,int mCount,int mViewType) {
            this.mContext = mContext;
            this.mHelper = mHelper;
            this.mCount = mCount;
            this.mViewType = mViewType;
        }

        @Override
        public LayoutHelper onCreateLayoutHelper() {
            return mHelper;
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == SERVICE) {
                View v = LayoutInflater.from(mContext).inflate(R.layout.item2,parent,false);
                return  new Item2ViewHolder(v);
            }else if(viewType == TOPIC){
                View v = LayoutInflater.from(mContext).inflate(R.layout.item1,parent,false);
                return  new Item1ViewHolder(v);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {
            if (holder instanceof Item1ViewHolder) {
                ((Item1ViewHolder) holder).img.setBackgroundColor(Color.BLACK);
                ((Item1ViewHolder) holder).txt.setText(position+"");
            }
            if (holder instanceof Item2ViewHolder) {
                ((Item2ViewHolder) holder).img.setBackgroundColor(Color.BLUE);
                ((Item2ViewHolder) holder).txt1.setText("第"+position+"项");
                ((Item2ViewHolder) holder).txt2.setText(position+"");
            }
        }

        @Override
        public int getItemViewType(int position) {
            return mViewType;
        }

        @Override
        public int getItemCount() {
            return mCount;
        }
    }

    class Item1ViewHolder extends BaseViewHolder{
        public ImageView img;
        public TextView txt;

        public Item1ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.item_pic);
            txt = (TextView) itemView.findViewById(R.id.item_title);
        }
    }

    class Item2ViewHolder extends BaseViewHolder{
        public ImageView img;
        public TextView txt1;
        public TextView txt2;


        public Item2ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.item2_pic);
            txt1 = (TextView) itemView.findViewById(R.id.item2_title);
            txt2 = (TextView) itemView.findViewById(R.id.item2_detail);

        }
    }

    class BaseViewHolder extends RecyclerView.ViewHolder{

        public BaseViewHolder(View itemView) {
            super(itemView);
        }
    }
}
