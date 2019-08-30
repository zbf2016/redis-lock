package cn.test.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext.xml"})
public class TestRedisLockCluster {

    @Autowired
    private RedisLockCluster redisLock;

    @Autowired
    private RedisLockSingle redisLockSingle;

    @Test
    public void testRedisLock(){
        System.out.println("第一次加锁");
        Boolean b = redisLock.tryGetDistributedLock("1111","222",100000000);
        //Boolean b = redisLockSingle.tryGetDistributedLock("1111","nihao",1000000);
        System.out.println("加锁："+(b==true?"成功":"失败"));
    }

    @Test
    public void testRedisLock1(){
        System.out.println("第二次加锁");
        Boolean b = redisLock.tryGetDistributedLock("1111","ni hao",100000000);
        //Boolean b = redisLockSingle.tryGetDistributedLock("1111","hello world",100000);
        System.out.println("加锁："+(b==true?"成功":"失败"));
    }

    @Test
    public void testReleaseRedisLock(){
        System.out.println("----------------------");
        System.out.println(redisLock.releaseDistributedLock("1111"));
        System.out.println(redisLockSingle.releaseDistributedLock("1111","nihao"));
    }




}
