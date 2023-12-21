package com.nowcoder.community;

import com.nowcoder.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTests {

    private static Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);

    // JDK fixed thread pool
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    @Autowired
    private AlphaService alphaService;
    // Spring thread pool
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testExecutorService() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ExecutorService");
            }
        };

        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }

        sleep(10000);
    }

    @Test
    public void testScheduledExecutorService() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ScheduledExecutorService");
            }
        };

        scheduledExecutorService.scheduleAtFixedRate(task, 10000, 1000, TimeUnit.MILLISECONDS);

        sleep(30000);
    }

    @Test
    public void testTaskExecutor() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello taskExecutor");
            }
        };

        for (int i = 0; i <= 10; i++) {
            taskExecutor.submit(task);
        }

        sleep(10000);
    }

    @Test
    public void testTaskScheduler() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello TaskScheduler");
            }
        };

        Date start = new Date(System.currentTimeMillis() + 10000);
        taskScheduler.scheduleAtFixedRate(task, start, 1000);
        sleep(30000);
    }

    // Spring thread-pool executor simplified
    @Test
    public void testTaskExecutorSimple() {
        for (int i = 0; i <= 10; i++) {
            alphaService.execute1();
        }

        sleep(100000);
    }

    // Spring thread-pool scheduler simplified
    @Test
    public void testTaskSchedulerSimple() {
        sleep(30000);
    }
}
