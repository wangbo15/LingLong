/*
 * Copyright (C) 2013 INRIA
 *
 * This software is governed by the CeCILL-C License under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/or redistribute the software under the terms of the CeCILL-C license as
 * circulated by CEA, CNRS and INRIA at http://www.cecill.info.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the CeCILL-C License for more details.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package cn.edu.pku.sei.plde.hanabi.fl.common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sacha.finder.classes.impl.ClassloaderFinder;
import sacha.finder.filters.impl.TestFilter;
import xxl.java.container.classic.MetaList;
import xxl.java.junit.CustomClassLoaderThreadFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Favio D. DeMarco
 */
public final class TestClassesFinder implements Callable<Collection<Class<?>>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Collection<Class<?>> call() throws Exception {
        URLClassLoader classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        ClassloaderFinder finder = new ClassloaderFinder(classLoader);
        TestFilter testFilter = new TestFilter(false);
        testFilter.acceptInnerClass();
        TestFinder processor = new TestFinder(finder, testFilter);
        Class<?>[] classes = processor.process();
        return MetaList.newArrayList(classes);
    }

    protected String[] namesFrom(Collection<Class<?>> classes) {
        String[] names = new String[classes.size()];
        int index = 0;
        for (Class<?> aClass : classes) {
            names[index] = aClass.getName();
            index += 1;
        }
        return names;
    }

    public String[] findIn(ClassLoader dumpedToClassLoader,
                           boolean acceptTestSuite) {
        ExecutorService executor = Executors.newSingleThreadExecutor(new CustomClassLoaderThreadFactory(dumpedToClassLoader));
        String[] testClasses;
        try {
            TestClassesFinder finder = new TestClassesFinder();
            Collection<Class<?>> classes = executor.submit(finder).get();
            testClasses = namesFrom(classes);
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        } catch (ExecutionException ee) {
            throw new RuntimeException(ee);
        } finally {
            executor.shutdown();
        }

        if (!acceptTestSuite) {
            testClasses = removeTestSuite(testClasses);
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Test clasess:");
            for (String testClass : testClasses) {
                this.logger.debug(testClass);
            }
        }

        return testClasses;
    }

    public String[] findIn(final URL[] classpath, boolean acceptTestSuite) {
        return findIn(new URLClassLoader(classpath), acceptTestSuite);
    }

    public String[] removeTestSuite(String[] totalTest) {
        List<String> tests = new ArrayList<String>();
        for (int i = 0; i < totalTest.length; i++) {
            if (!totalTest[i].endsWith("Suite")) {
                tests.add(totalTest[i]);
            }
        }
        return tests.toArray(new String[tests.size()]);
    }

}
