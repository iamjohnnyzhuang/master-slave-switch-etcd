package me.zhuangjy.main;

import me.zhuangjy.etcd.EtcdClient;
import me.zhuangjy.etcd.EtcdClientException;
import me.zhuangjy.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhuangjy on 2016/8/24.
 */
public class HeartBeatThread implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(Slave.class);
    private EtcdClient client;
    private String name;
    private int index = 0;

    public HeartBeatThread(String name) {
        this.name = name;
        this.client = new EtcdClient(URI.create(Common.URL[index % Common.URL.length]));
    }

    @Override
    public void run() {
        //超时时间过半后就更新心跳
        while (true) {
            try {
                client.casVal(Common.LOCK, name, name,Common.WAIT);
                LOGGER.info(name + "发送心跳成功");
                TimeUnit.SECONDS.sleep(Common.WAIT / 2);
            } catch (InterruptedException e) {
                LOGGER.error("", e);
            } catch (EtcdClientException e) {
                LOGGER.warn("心跳发送失败,尝试切换节点发送...");
                if (e.isNetError()) {
                    client = new EtcdClient(URI.create(Common.URL[(++index) % Common.URL.length]));
                    LOGGER.info("切换节点成功,当前节点为:" + client.getBaseUri());
                    continue;
                } else {
                    LOGGER.error("", e);
                    //TODO 心跳发送过去却出现节点不存在的情况
                    LOGGER.error("当前节点已经不存在可能是因为心跳超时时间过长,需要关闭当前服务...");
                }
            }
        }
    }
}
