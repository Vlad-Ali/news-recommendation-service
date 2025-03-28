package org.hsse.news.util;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

final public class ReflectionUtil {
    private ReflectionUtil() {
    }

    public interface AnnotatedMethodVisitor<AnnotationT> {
        void visit(AnnotationT annotation, Object object, Method method);
    }

    public static <AnnotationT extends Annotation> void forEachAnnotatedMethod(
            final ApplicationContext applicationContext, final List<String> skipBeans,
            final Class<AnnotationT> annotationClass,
            final AnnotatedMethodVisitor<AnnotationT> visitor) {
        for (final String beanName : applicationContext.getBeanDefinitionNames()) {
            if (skipBeans.contains(beanName)) {
                continue;
            }

            final Object bean = applicationContext.getBean(beanName);
            final Class<?> beanClass = AopUtils.getTargetClass(bean);
            ReflectionUtils.doWithMethods(beanClass, (method) -> {
                final AnnotationT annotation = AnnotationUtils.findAnnotation(method, annotationClass);
                if (annotation == null) {
                    return;
                }

                visitor.visit(annotation, bean, method);
            });
        }
    }

    public static Object parseArg(final String arg, final Class<?> type) {
        if (type == String.class) {
            return arg;
        } else if (type == int.class) {
            return Integer.parseInt(arg);
        } else if (type == long.class) {
            return Long.parseLong(arg);
        } else {
            try {
                return type.getMethod("fromString", String.class)
                        .invoke(null, arg);
            } catch (ReflectiveOperationException ignored) {
            }

            try {
                return type.getMethod("valueOf", String.class)
                        .invoke(null, arg);
            } catch (ReflectiveOperationException ignored) {
            }

            throw new IllegalArgumentException("Can't create a parameter of type " + type);
        }
    }

    public static Class<?> parameterType(final Method method, final int parameterIndex) {
        return new MethodParameter(method, parameterIndex).getParameterType();
    }
}
