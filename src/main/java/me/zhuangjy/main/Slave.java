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
    private final static Logger LOGGER = LoggerFactory.getLogger(Slave.class);

    public static void main(String[] args) throws InterruptedException {
        int index = 0;
        EtcdClient client = new EtcdClient(URI.create(Common.URL[index]));
        while (true) {
            LOGGER.info("Slave服务正在注册锁,当前访问服务器: " + client.getBaseUri());
            try {
                client.casExist(Common.LOCK, "Slave", "false", Common.WAIT);
            } catch (EtcdClientException e) {
                if (e.isNetError()) {
                    LOGGER.info("当前服务器访问不上,切换下一个服务器,重试...");
                    client = new EtcdClient(URI.create(Common.URL[(++index) % Common.URL.length]));
                } else {
                    LOGGER.info("注册失败...Slave服务进入等待时间");
                    TimeUnit.SECONDS.sleep(Common.WAIT);
                }
                continue;
            }

            new Thread(new HeartBeatThread("Slave")).start();

            //模拟正常工作
            LOGGER.info("Slave服务成功获取锁，正在进行服务...");
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
