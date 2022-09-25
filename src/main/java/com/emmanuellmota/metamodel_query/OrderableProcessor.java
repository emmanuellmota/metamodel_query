package com.emmanuellmota.metamodel_query;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

@SupportedAnnotationTypes("com.emmanuellmota.metamodel_query.Orderable")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class OrderableProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(Orderable.class).forEach(this::generateMetamodel);

        return true;
    }

    private void generateMetamodel(Element element) {
        TypeElement typeElement = (TypeElement) element;
        messager().printMessage(Diagnostic.Kind.NOTE, "processing " + element);
        final ClassModel classModel = new ClassHandler(typeElement, processingEnv).invoke();
        var annotation = element.getAnnotation(Orderable.class);
        try {
            String className = annotation.toString().replaceAll(".class", "").replaceAll("value = ", "").split("[\\(\\)]")[1];
            writeMetaClass(typeElement, classModel, className);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeMetaClass(TypeElement element, ClassModel classModel, String className) {
        try {
            new OrderableClassWriter(element, classModel, className).invoke();
        } catch (IOException e) {
            messager().printMessage(Diagnostic.Kind.ERROR, "Writing metaclass failed - " + e.toString(), element);
        }
    }

    private Messager messager() {
        return processingEnv.getMessager();
    }

}
