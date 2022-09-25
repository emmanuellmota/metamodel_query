package com.emmanuellmota.metamodel_query;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.squareup.javapoet.*;

import lombok.Data;

/**
 * Create metamodel class for given Type.
 */
class FilterableClassWriter {

    private static final String SUFFIX = "__Filter";

    private final TypeElement beanType;
    private final ClassModel classModel;
    private final String metaClassName;
    private final ClassName className;

    /**
     * Initialize class with {@link TypeElement}, {@link ClassModel} containing attributes and {@link String} packageName.
     *
     * @param beanType   the bean class.
     * @param classModel attribute information about the bean class.
     */
    FilterableClassWriter(TypeElement beanType, ClassModel classModel, String packageName) {
        this.beanType = beanType;
        this.classModel = classModel;

        String[] classArr = packageName.split("\\.");
        this.className = ClassName.get(String.join(".", Arrays.copyOf(classArr, classArr.length - 1)), classArr[classArr.length - 1]);

        metaClassName = beanType.getSimpleName() + SUFFIX;
    }

    /**
     * Create the Metamodel class.
     *
     * @throws IOException if source file cannot be written.
     */
    void invoke() throws IOException {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(metaClassName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Data.class)
                .addAnnotation(AnnotationSpec.builder(Generated.class).addMember("value", "\"$L\"", FilterableProcessor.class.getCanonicalName()).build());
        classModel.attributes().forEach((name, type) -> classBuilder.addField(createFieldSpec(name, type)));

        classBuilder.addField(FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get("", metaClassName)), "and")
                .addModifiers(Modifier.PUBLIC)
                .build());

        classBuilder.addField(FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get("", metaClassName)), "or")
                .addModifiers(Modifier.PUBLIC)
                .build());

        JavaFile javaFile = JavaFile.builder(ClassName.get(beanType).packageName(), classBuilder.build()).indent("    ")
                .addFileComment("Generated code. Do not modify!")
                .build();
        javaFile.writeTo(classModel.getEnvironment().getFiler());
    }

    private FieldSpec createFieldSpec(String attributeName, AttributeInfo info) {
        final ParameterizedTypeName fieldType = declarationTypeName(info);
        return FieldSpec.builder(fieldType, attributeName)
                .addModifiers(Modifier.PRIVATE).build();
    }

    private ParameterizedTypeName declarationTypeName(AttributeInfo info) {
        final TypeName attributeTypeName = Utils.getAttributeTypeName(info);
        return ParameterizedTypeName.get(className, attributeTypeName);
    }

}
