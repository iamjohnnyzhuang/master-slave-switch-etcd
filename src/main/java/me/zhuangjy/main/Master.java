package me.zhuangjy.main;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import me.zhuangjy.etcd.EtcdClient;
import me.zhuangjy.etcd.EtcdClientException;
import me.zhuangjy.etcd.EtcdResult;
import me.zhuangjy.util.Common;
import me.zhuangjy.util.HttpClientUtils;

import java.net.URI;

/**
 * Created by zhuangjy on 2016/8/23.
 */
public class Master {
    public static void main(String[] args) throws EtcdClientException {
        //获取分布式锁
        final EtcdClient client = new EtcdClient(URI.create(Common.url[0]));
        final String key = "/watch";
        ListenableFuture<EtcdResult> listenableFuture = client.watch(key);
        Futures.addCallback(listenableFuture, new FutureCallback<EtcdResult>() {
            public void onSuccess(EtcdResult etcdResult) {
                System.out.println(etcdResult);
            }

            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

//        EtcdClient client2  = new EtcdClient(URI.create(Common.url[0]));
//        EtcdResult result = client2.set(key, "hello2");
//        System.out.println(result);
//        EtcdResult result2 = client2.set(key, "hello3");
    }
}
