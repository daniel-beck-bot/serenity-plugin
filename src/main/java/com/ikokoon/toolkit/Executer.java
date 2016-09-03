package com.ikokoon.toolkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Executer {

    private static Logger logger = LoggerFactory.getLogger(Executer.class);

    public interface IPerform {
        void execute();
    }

    public static double execute(final IPerform perform, final String type, final long iterations) {
        double start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            perform.execute();
        }
        double end = System.currentTimeMillis();
        double duration = (end - start) / 1000d;
        double executionsPerSecond = (iterations / duration);
        logger.info("Duration : " + duration + ", " + type + " per second : " + executionsPerSecond);
        return executionsPerSecond;
    }

}
