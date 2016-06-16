package com.ikokoon.serenity.instrumentation.profiling;

import com.ikokoon.serenity.ATest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 13-06-2016
 */
public class ProfilingMethodAdviceAdapterTest extends ATest {

    @Mock
    private MethodVisitor methodVisitor;
    private ProfilingMethodAdviceAdapter profilingMethodAdviceAdapter;

    @Before
    public void before() {
        profilingMethodAdviceAdapter = new ProfilingMethodAdviceAdapter(methodVisitor, 1, className, methodName, methodDescription);
    }

    @Test
    public void isWaitInsn() {
        byte[] classBytes = getClassBytes(this.className);
        byte[] sourceBytes = getSourceBytes(this.className);
        profilingMethodAdviceAdapter.isWaitInsn(Opcodes.INVOKESTATIC, className, methodName, methodDescription);
    }

}
