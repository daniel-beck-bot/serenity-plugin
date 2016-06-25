package com.ikokoon.serenity.hudson;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Profiler;
import com.ikokoon.toolkit.Thread;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Spy;

@Ignore
public class SerenityPluginTest extends ATest {

    // @Spy
    private SerenityPlugin serenityPlugin;

    @Before
    public void before() {
        Thread.initialize();
    }

    @After
    public void after() {
        Thread.destroy();
    }

    @Test
    @Ignore
    public void startServerAndPostDataFromProfiler() {
        serenityPlugin.start();
        Profiler.initialize(null);
        Thread.sleep(15000);
    }

}
