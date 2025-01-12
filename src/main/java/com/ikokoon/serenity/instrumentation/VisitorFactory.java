package com.ikokoon.serenity.instrumentation;

import com.ikokoon.serenity.instrumentation.dependency.DependencyAnnotationAdapter;
import com.ikokoon.serenity.instrumentation.dependency.DependencySignatureAdapter;
import com.ikokoon.toolkit.ObjectFactory;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.io.ByteArrayOutputStream;

/**
 * This class instantiates visitors for classes, methods, field, signature and annotations.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09.12.09
 */
public class VisitorFactory {

    /**
     * Instantiates a chain of class visitors. Each visitor can modify or add code to the class as it is parsed and
     * the writer will output the new class byte
     * code.
     *
     * @param classAdapterClasses the class visitor classes
     * @param className           the name of the class to be visited
     * @param classBytes          the byte array of the byte code
     * @param source              the output stream of the source code for the class
     * @return the class visitor/writer
     */
    public static ClassVisitor getClassVisitor(final Class<ClassVisitor>[] classAdapterClasses, final String className,
                                               final byte[] classBytes, final ByteArrayOutputStream source) {
        ClassReader reader = new ClassReader(classBytes);
        // 'true' for ASM 2.2, 'ClassWriter.COMPUTE_MAXS' for ASM 3++
        // ClassWriter writer = new ClassWriter(reader, true);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = writer;
        for (Class<ClassVisitor> klass : classAdapterClasses) {
            Object[] parameters = new Object[]{visitor, className, classBytes, source};
            visitor = ObjectFactory.getObject(klass, parameters);
        }
        // 'false' for ASM 2.2, '0' for ASM 3++
        reader.accept(visitor, ClassReader.SKIP_FRAMES);
        return writer;
    }

    /**
     * This method constructs a method visitor chain based on the class of the method adapter passed as a parameter.
     *
     * @param visitor           the parent method visitor
     * @param klass             the class of the method visitor to initialise
     * @param access            the type of access to the method
     * @param className         the name of the class that the method visitor will visit
     * @param methodName        the name of the method
     * @param methodDescription the description or signature of the method in byte code format
     * @return the method visitor
     */
    public static MethodVisitor getMethodVisitor(final MethodVisitor visitor, final Class<?> klass, final int access,
                                                 final String className, final String methodName, final String methodDescription) {
        Object[] parameters = new Object[]{visitor, access, className, methodName, methodDescription};
        return (MethodVisitor) ObjectFactory.getObject(klass, parameters);
    }

    /**
     * This method constructs a field visitor chain that will visit the byte code for a field in a class.
     *
     * @param visitor     the parent field visitor
     * @param klass       the field visitor class type
     * @param className   the name of the class the field is in
     * @param description the description of the field in byte code format
     * @param signature   the signature of the field in byte code
     * @return the field visitor
     */
    public static FieldVisitor getFieldVisitor(final FieldVisitor visitor, final Class<?> klass, final String className,
                                               final String description, final String signature) {
        Object[] parameters = new Object[]{visitor, className, description, signature};
        return (FieldVisitor) ObjectFactory.getObject(klass, parameters);
    }

    /**
     * This method constructs a signature visitor that will visit a signature for something, could be an annotation or a method.
     *
     * @param className the name of the class the signature is in
     * @param signature the signature in byte code to visit
     * @return the signature to be visited
     */
    public static SignatureVisitor getSignatureVisitor(final String className, final String signature) {
        SignatureReader reader = new SignatureReader(signature);
        SignatureVisitor adapter = new DependencySignatureAdapter(className);
        reader.accept(adapter);
        return adapter;
    }

    /**
     * This method constructs an annotation visitor to visit annotations.
     *
     * @param visitor     the parent annotation visitor
     * @param className   the name of the class with the annotation
     * @param description the description or signature of the annotation
     * @return the annotation visitor
     */
    public static AnnotationVisitor getAnnotationVisitor(final AnnotationVisitor visitor, final String className, final String description) {
        return new DependencyAnnotationAdapter(visitor, className, description);
    }

}
