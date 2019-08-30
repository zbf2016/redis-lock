package cn.test.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RedisLockCluster {

    @Autowired
    private JedisPoolConfig jedisPoolConfig;

    private static String pre="REDIS_LOCK:";

    @Value("${redis.maxIdle}")
    private int maxIdle;

    @Value("${redis.maxActive}")
    private int maxTotal;

    @Value("${redis.maxWaitMillis}")
    private int maxWaitMillis;

    @Value("${redis.hosts}")
    private String hosts;

    @Value("${redis.password}")
    private String password;

    @Value("${redis.timeout}")
    private int connectionTimeout;

    @Value("${redis.maxWait}")
    private int soTimeOut;

    @Value("${redis.maxAttempts}")
    private int maxAttempts;

    @Value("${redis.testOnBorrow}")
    private boolean testOnBorrow;



    /**
     * 集群版
     * @return
     */
    private JedisCluster getJedisCluster(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        String[] hostArry = hosts.split(",");
        Set<HostAndPort> nodes = new HashSet<>();
        if(hostArry != null && hostArry.length > 0){
            Arrays.stream(hostArry).forEach(p->{
                String[] detail = p.split(":");
                nodes.add(new HostAndPort(detail[0],Integer.parseInt(detail[1])));
            });
        }
        return new JedisCluster(nodes,connectionTimeout,soTimeOut,maxAttempts,password,jedisPoolConfig);
    }

    /**
     * 尝试获取分布式锁，注释的为单机版
     * @param seriNo 全局流水号
     * @param value  key的value值
     * @param expireTime 过期时间
     * @return true成功，false失败
     */
    public boolean tryGetDistributedLock(String seriNo,String value,int expireTime){
        JedisCluster jedisPool = getJedisCluster();
        try{
            String result = jedisPool.set(pre+seriNo,value,"NX","PX",expireTime);
            if("OK".equals(result)){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            try {
                jedisPool.close();
            }catch (IOException ioException){
                ioException.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 释放锁，注释的为单机版
     * @param seriNo 全局流水号
     * @return true成功，false失败
     */
    public boolean releaseDistributedLock(String seriNo){
        JedisCluster jedisPool = getJedisCluster();
        try{
            long result = jedisPool.del(pre+seriNo);
            if(1L==result){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            try {
                jedisPool.close();
            }catch (IOException ioException){
                ioException.printStackTrace();
            }
        }
        return false;
    }




}
