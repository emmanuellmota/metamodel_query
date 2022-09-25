package com.emmanuellmota.metamodel_query;

import com.squareup.javapoet.*;
import lombok.Data;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Arrays;

/**
 * Create metamodel class for given Type.
 */
class OrderableClassWriter {

    private static final String SUFFIX           = "__Order";

    private final TypeElement beanType;
    private final ClassModel  classModel;
    private final String      metaClassName;
    private final ClassName   className;

    /**
     Initialize class with {@link TypeElement}, {@link ClassModel} containing attributes and {@link String} packageName.
     * @param beanType the bean class.
     * @param classModel attribute information about the bean class.
     */
    OrderableClassWriter(TypeElement beanType, ClassModel classModel, String packageName) {
        this.beanType = beanType;
        this.classModel = classModel;

        String[] classArr = packageName.split("\\.");
        this.className = ClassName.get(String.join(".", Arrays.copyOf(classArr, classArr.length - 1)), classArr[classArr.length - 1]);

        metaClassName = beanType.getSimpleName() + SUFFIX;
    }

    /**
     * Create the Metamodel class.
     * @throws IOException if source file cannot be written.
     */
    void invoke() throws IOException {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(metaClassName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Data.class)
                .addAnnotation(AnnotationSpec.builder(Generated.class).addMember("value", "\"$L\"", OrderableProcessor.class.getCanonicalName()).build());
        classModel.attributes().forEach((name, type) -> classBuilder.addField(createFieldSpec(name, type)));

        JavaFile javaFile = JavaFile.builder(ClassName.get(beanType).packageName(), classBuilder.build()).indent("    ")
                .addFileComment("Generated code. Do not modify!")
                .build();
        javaFile.writeTo(classModel.getEnvironment().getFiler());
    }

    private FieldSpec createFieldSpec(String attributeName, AttributeInfo info) {
        return FieldSpec
                .builder(className, attributeName)
                .addModifiers(Modifier.PRIVATE).build();
    }

}
