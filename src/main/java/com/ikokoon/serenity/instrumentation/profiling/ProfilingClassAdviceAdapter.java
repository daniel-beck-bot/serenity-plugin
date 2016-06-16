package com.ikokoon.serenity.instrumentation.profiling;

import com.ikokoon.toolkit.Toolkit;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 06.06.10
 */
public class ProfilingClassAdviceAdapter extends ClassVisitor {

    private Logger logger;
    private String className;

    /**
     * Constructor takes the class that will be profiled and the original visitor.
     *
     * @param visitor   the visitor from ASM
     * @param className the name of the class to be profiled
     */
    public ProfilingClassAdviceAdapter(final ClassVisitor visitor, final String className) {
        super(Opcodes.ASM5, visitor);
        this.className = Toolkit.slashToDot(className);
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * {@inheritDoc}
     */
    public MethodVisitor visitMethod(final int access, final String methodName, final String methodDescription,
                                     final String methodSignature, final String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, methodName, methodDescription, methodSignature, exceptions);
        // LOGGER.warn("Access : " + access + ", " + Opcodes.ACC_ABSTRACT + ", " + Opcodes.ACC_INTERFACE);
        // We test for interfaces and abstract classes, of course these methods do
        // not have bodies so we can't add instructions to these methods or the Jvm
        // will not like it, class format exceptions
        switch (access) {
            case Opcodes.ACC_ABSTRACT:
            case Opcodes.ACC_ABSTRACT + Opcodes.ACC_PUBLIC:
            case Opcodes.ACC_ABSTRACT + Opcodes.ACC_PRIVATE:
            case Opcodes.ACC_ABSTRACT + Opcodes.ACC_PROTECTED:
            case Opcodes.ACC_INTERFACE:
            case Opcodes.ACC_INTERFACE + Opcodes.ACC_PUBLIC:
            case Opcodes.ACC_INTERFACE + Opcodes.ACC_PRIVATE:
            case Opcodes.ACC_INTERFACE + Opcodes.ACC_PROTECTED:
                // logger.info("Abstract method : " + access + " : " + methodName);
                return methodVisitor;
            default:
                return new ProfilingMethodAdviceAdapter(methodVisitor, access, className, methodName, methodDescription);
        }
    }

}