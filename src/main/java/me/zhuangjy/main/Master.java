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
 * 通过设置等待时间不断去获取分布式锁，如果获取了在正常服务时应该更新心跳防止因为服务挂掉没有释放锁
 */
public class Master {
    private static final Logger LOGGER = LoggerFactory.getLogger(Master.class);

    public static void main(String[] args) throws InterruptedException {
        int index = 0;
        EtcdClient client = new EtcdClient(URI.create(Common.URL[index]));
        while (true) {
            LOGGER.info("Master服务正在注册锁,当前访问服务器: " + client.getBaseUri());
            try {
                client.casExist(Common.LOCK, "Master", "false", Common.WAIT);
            } catch (EtcdClientException e) {
                if (e.isNetError()) {
                    LOGGER.info("当前服务器访问不上,切换下一个服务器,重试...");
                    client = new EtcdClient(URI.create(Common.URL[(++index) % Common.URL.length]));
                    continue;
                } else {
                    LOGGER.info("注册失败...Master服务进入等待时间");
                    TimeUnit.SECONDS.sleep(Common.WAIT);
                    continue;
                }
            }

            new Thread(new HeartBeatThread("Master")).start();

            //模拟正常工作
            LOGGER.info("Master服务成功获取锁，正在进行服务...");
            TimeUnit.HOURS.sleep(1);
            //工作结束后可以释放锁动态分配任务
            LOGGER.info("Master服务结束,正在释放锁...");
            try {
                client.delete("lock");
            } catch (EtcdClientException e) {
                LOGGER.error("", e);
            }
        }
    }
}
