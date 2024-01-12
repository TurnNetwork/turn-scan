package com.turn.browser.response;

import com.github.pagehelper.Page;
import com.turn.browser.exception.BeanCreateOrUpdateException;
import com.turn.browser.response.token.*;
import com.turn.browser.utils.ClassUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ResTest {

    private List<Class<?>> target = new ArrayList<>();

    /**
     * 测试开始前，设置相关行为属性
     * 
     * @throws IOException
     * @throws BeanCreateOrUpdateException
     */
    @Before
    public void setup() {
        String packageName = ResTest.class.getPackage().getName();
        Set<Class<?>> classSet = ClassUtil.getClasses(packageName);
        classSet.stream()
            .filter(clazz -> !clazz.getName().endsWith("Test") && !clazz.getName().endsWith("Column")
                && !clazz.getName().endsWith("Criterion") && !clazz.getName().endsWith("GeneratedCriteria"))
            .forEach(this.target::add);
    }

    @Test
    public void test() {
        for (Class<?> clazz : this.target) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (Modifier.isStatic(method.getModifiers()))
                    continue;
                if (Modifier.isProtected(method.getModifiers()))
                    continue;
                if (Modifier.isPrivate(method.getModifiers()))
                    continue;
                if (method.getName().equals("init"))
                    continue;
                if (clazz.isEnum())
                    continue;
                Class<?>[] types = method.getParameterTypes();

                Object instance = null;
                try {
                    instance = clazz.newInstance();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    continue;
                }
                if (types.length != 0) {
                    Object[] args = new Object[types.length];
                    for (int i = 0; i < types.length; i++) {
                        if (Boolean.class == types[i] || boolean.class == types[i]) {
                            args[i] = Boolean.TRUE;
                            continue;
                        }
                        if (Double.class == types[i] || double.class == types[i]) {
                            args[i] = 11.3;
                            continue;
                        }
                        if (String.class == types[i]) {
                            args[i] = "333";
                            continue;
                        }
                        if (Integer.class == types[i] || int.class == types[i]) {
                            args[i] = 333;
                            continue;
                        }
                        if (Long.class == types[i] || long.class == types[i]) {
                            args[i] = 333L;
                            continue;
                        }
                        if (types[i].getTypeName().equals("byte[]")) {
                            args[i] = new byte[] {1};
                            continue;
                        }
                        if (types[i].getTypeName().equals("java.lang.Double[]")) {
                            args[i] = new Double[] {};
                            continue;
                        }
                        if (types[i].getTypeName().equals("java.lang.Long[]")) {
                            args[i] = new Long[] {};
                            continue;
                        }
                        args[i] = mock(types[i]);
                    }
                    method.setAccessible(true);
                    try {
                        method.invoke(instance, args);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                method.setAccessible(true);
                try {
                    method.invoke(instance);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        assertTrue(true);
    }


}
