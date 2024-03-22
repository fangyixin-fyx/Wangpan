package com.wangpan;

import com.wangpan.constants.Constants;
import com.wangpan.service.UserService;
import com.wangpan.utils.RedisUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author fangyixin
 * @date 2024/3/22 16:46
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtils redisUtils;

    @Test
    public void registerTest(){
        // 创建一个具有固定线程数量的线程池
        int coreSize=50;
        int maxSize=100;
        BlockingQueue<Runnable> queue=new LinkedBlockingQueue<>(20);
        ExecutorService executorService=new ThreadPoolExecutor(coreSize,maxSize,0L, TimeUnit.SECONDS,queue);

        //交任务到线程池，并获取 Future 对象
        List<Future<Long>> futures=new ArrayList<>();
        for(int i=0;i<100;i++){
            //提前配好环境
            String name="email"+i;
            String redisKey= Constants.EMAIL_CODE+name;
            redisUtils.set(redisKey,"12345");
            // 定义要执行的任务
            Callable<Long> task = () -> {
                // 这里编写需要执行的任务代码，并返回执行结果
                long startTime=System.currentTimeMillis();
                //执行并发注册
                userService.register(name,"test",name,"12345");
                long endTime = System.currentTimeMillis();
                return endTime-startTime;
            };
            Future<Long> future= executorService.submit(task);
            futures.add(future);

        }
        //获取并发任务的平均执行时间和最大执行时间
        Long maxTime=Long.MIN_VALUE;
        Long sum=0L;
        for(Future<Long> future:futures){
            try {
                Long threadTime=future.get();
                sum+=threadTime;
                if(threadTime>maxTime) maxTime=threadTime;

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }
        System.out.println("-----------max:"+maxTime+" average:"+(sum/100));

        //关闭线程池
        executorService.shutdown();
    }
}
