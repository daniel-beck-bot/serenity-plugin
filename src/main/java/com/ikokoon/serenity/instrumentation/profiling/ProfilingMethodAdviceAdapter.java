package com.ikokoon.serenity.instrumentation.profiling;

import com.ikokoon.serenity.IConstants;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10.06.10
 */
public class ProfilingMethodAdviceAdapter extends AdviceAdapter {

    private static final String OBJECT_CLASS_NAME = Type.getInternalName(Object.class);
    private static final String THREAD_CLASS_NAME = Type.getInternalName(Thread.class);

    private static final String WAIT = "wait";
    private static final String JOIN = "join";
    private static final String SLEEP = "sleep";
    private static final String YIELD = "yield";

    private Logger logger;

    private String className;
    private String methodName;

    private Label[] catchBlockLabels = new Label[0];

    public ProfilingMethodAdviceAdapter(final MethodVisitor methodVisitor, final int access, final String className,
                                        final String methodName, final String methodDescription) {
        super(Opcodes.ASM5, methodVisitor, access, methodName, methodDescription);
        this.className = className;
        this.methodName = methodName;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    protected void onMethodEnter() {
        if (methodName.equals("<init>") || methodName.equals("<clinit>")) {
            insertInstruction(IConstants.COLLECTOR_CLASS_NAME, IConstants.COLLECT_ALLOCATION, IConstants.PROFILING_METHOD_DESCRIPTION);
        }
        insertInstruction(IConstants.COLLECTOR_CLASS_NAME, IConstants.COLLECT_START, IConstants.PROFILING_METHOD_DESCRIPTION);
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean inf) {
        if (isWaitInsn(opcode, owner, name, desc)) {
            insertInstruction(IConstants.COLLECTOR_CLASS_NAME, IConstants.COLLECT_START_WAIT, IConstants.PROFILING_METHOD_DESCRIPTION);
            super.visitMethodInsn(opcode, owner, name, desc, inf);
            insertInstruction(IConstants.COLLECTOR_CLASS_NAME, IConstants.COLLECT_END_WAIT, IConstants.PROFILING_METHOD_DESCRIPTION);
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, inf);
        }
    }

    @Override
    public void visitLabel(final Label label) {
        super.visitLabel(label);
        for (final Label interruptedCatchBlockLabel : catchBlockLabels) {
            if (label == interruptedCatchBlockLabel) {
                // This is an exception label, handler, so stop the wait, we don't know what the
                // caught exception was so to be safe just call the stop wait
                insertInstruction(IConstants.COLLECTOR_CLASS_NAME, IConstants.COLLECT_END_WAIT, IConstants.PROFILING_METHOD_DESCRIPTION);
            }
        }
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        super.visitTryCatchBlock(start, end, handler, type);
        if (type != null) {
            // We have to end the wait regardless of the exception type
            Label[] copyCatchBlockLabels = new Label[catchBlockLabels.length + 1];
            System.arraycopy(catchBlockLabels, 0, copyCatchBlockLabels, 0, catchBlockLabels.length);
            copyCatchBlockLabels[copyCatchBlockLabels.length - 1] = handler;
            catchBlockLabels = copyCatchBlockLabels;
        }
    }

    @Override
    protected void onMethodExit(final int inst) {
        insertInstruction(IConstants.COLLECTOR_CLASS_NAME, IConstants.COLLECT_END, IConstants.PROFILING_METHOD_DESCRIPTION);
    }

    boolean isWaitInsn(final int opcode, final String owner, final String methodName, final String methodDescription) {
        // logger.info("Opcode : " + opcode + ", owner : " + owner + ", method name : " + methodName + ", method description : " + methodDescription);
        switch (opcode) {
            case Opcodes.INVOKESTATIC: {
                if (THREAD_CLASS_NAME.equals(owner)) {
                    // "(J)V", "(JI)V"
                    if (SLEEP.equals(methodName)
                            && (IConstants.sleepLongMethodDescriptor.equals(methodDescription) || IConstants.sleepLongIntMethodDescriptor
                            .equals(methodDescription))) {
                        return true;
                    }
                    // "()V"
                    if (YIELD.equals(methodName) && IConstants.yieldMethodDescriptor.equals(methodDescription)) {
                        return true;
                    }
                }
            }
            case Opcodes.INVOKEVIRTUAL: {
                if (OBJECT_CLASS_NAME.equals(owner)) {
                    if (WAIT.equals(methodName)
                            && (IConstants.waitMethodDescriptor.equals(methodDescription)
                            || IConstants.waitLongMethodDescriptor.equals(methodDescription) || IConstants.waitLongIntMethodDescriptor
                            .equals(methodDescription))) {
                        // "()V", "(J)V", "(JI)V"
                        return true;
                    }
                } else if (THREAD_CLASS_NAME.equals(owner)) {
                    // "()V", "(J)V", "(JI)V"
                    if (JOIN.equals(methodName)
                            && (IConstants.joinMethodDescriptor.equals(methodDescription)
                            || IConstants.joinLongMethodDescriptor.equals(methodDescription) || IConstants.joinLongIntMethodDescriptor
                            .equals(methodDescription))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    void insertInstruction(final String collectorClassName, final String collectorMethodName, final String collectorMethodDescription) {
        super.visitLdcInsn(className);
        super.visitLdcInsn(methodName);
        super.visitLdcInsn(methodDesc);
        super.visitMethodInsn(Opcodes.INVOKESTATIC, collectorClassName, collectorMethodName, collectorMethodDescription, Boolean.FALSE);
    }

}