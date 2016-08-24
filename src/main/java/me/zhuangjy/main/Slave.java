package me.zhuangjy.main;

import me.zhuangjy.etcd.EtcdClient;
import me.zhuangjy.etcd.EtcdClientException;
import me.zhuangjy.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhuangjy on 2016/8/23.
 */
public class Slave {
    private static final Logger LOGGER = LoggerFactory.getLogger(Slave.class);

    public static void main(String[] args) throws InterruptedException {
        while (true) {
            LOGGER.info("Slave服务正在注册锁...");
            final EtcdClient client = new EtcdClient(URI.create(Common.URL[0]));
            try {
                client.cas(Common.LOCK, "Slave", "false", Common.WAIT);
            } catch (EtcdClientException e) {
                LOGGER.info("注册失败...Slave服务进入等待时间");
                TimeUnit.SECONDS.sleep(Common.WAIT);
                continue;
            }
            LOGGER.info("Slave服务成功获取锁，正在进行服务...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //超时时间过半后就更新心跳
                    try {
                        TimeUnit.SECONDS.sleep(Common.WAIT / 2);
                        client.cas(Common.LOCK, "Slave", "true", Common.WAIT);
                        LOGGER.info("Slave发送心跳...");
                    } catch (InterruptedException e) {
                        LOGGER.error("", e);
                    } catch (EtcdClientException e) {
                        LOGGER.error("", e);
                    }
                }
            }).start();
            //模拟正常工作
            TimeUnit.HOURS.sleep(1);
            //工作结束后可以释放锁动态分配任务
            LOGGER.info("Slave服务结束,正在释放锁...");
            try {
                client.delete("lock");
            } catch (EtcdClientException e) {
                LOGGER.error("", e);
            }
        }
    }
}
