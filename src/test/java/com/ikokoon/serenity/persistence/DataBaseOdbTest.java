package com.ikokoon.serenity.persistence;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.*;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.toolkit.Toolkit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ikokoon.serenity.persistence.IDataBase.DataBaseManager.getDataBase;
import static org.junit.Assert.*;

public class DataBaseOdbTest extends ATest {

    private IDataBase dataBase;
    private File dataBaseFile = new File("./src/test/resources/DataBaseOdbTest.odb");

    @Before
    public void clear() {
        dataBase = getDataBase(DataBaseOdb.class, dataBaseFile.getAbsolutePath(), Boolean.FALSE, mockInternalDataBase);
        DataBaseToolkit.clear(dataBase);
    }

    @After
    public void close() {
        dataBase.close();
        Toolkit.deleteFile(dataBaseFile, 3);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void persist() {
        Package pakkage = getPackage();
        dataBase.persist(pakkage);
        pakkage = dataBase.find(Package.class, pakkage.getId());
        assertNotNull(pakkage);

        Long classId = ((Class) pakkage.getChildren().get(0)).getId();
        Class klass = dataBase.find(Class.class, classId);
        assertNotNull(klass);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findId() {
        Package<Project, Class> pakkage = (Package<Project, Class>) getPackage();
        dataBase.persist(pakkage);
        Line line = dataBase.find(Line.class, 2527758816159558051l);
        assertNotNull(line);
    }

    @Test
    public void findParameters() {
        Package pakkage = getPackage();
        dataBase.persist(pakkage);

        List<Object> parameters = new ArrayList<>();
        parameters.add(packageName);
        pakkage = dataBase.find(Package.class, parameters);
        assertNotNull(pakkage);

        parameters.clear();
        parameters.add(className);
        Class klass = dataBase.find(Class.class, parameters);
        assertNotNull(klass);

        parameters.clear();
        parameters.add(klass.getName());
        parameters.add(methodName);
        parameters.add(methodDescription);
        Method method = dataBase.find(Method.class, parameters);
        assertNotNull(method);

        parameters.clear();
        parameters.add(klass.getName());
        parameters.add(method.getName());
        parameters.add(lineNumber);
        Line line = dataBase.find(Line.class, parameters);
        assertNotNull(line);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void removeId() throws Exception {
        // java.lang.Class<T> klass, Long id
        Package pakkage = getPackage();
        dataBase.persist(pakkage);
        Class klass = (Class) pakkage.getChildren().iterator().next();
        klass = dataBase.find(Class.class, klass.getId());
        assertNotNull(klass);
        dataBase.remove(Class.class, klass.getId());
        klass = dataBase.find(Class.class, klass.getId());
        assertNull(klass);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void find() {
        Package pakkage = getPackage();
        dataBase.persist(pakkage);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", pakkage.getName());
        List<Class> classes = dataBase.find(Class.class, parameters);
        assertEquals(1, classes.size());
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void findStartEnd() {
        Package pakkage = getPackage();
        dataBase.persist(pakkage);
        List<Class> classes = dataBase.find(Class.class, 0, Integer.MAX_VALUE);
        assertEquals(1, classes.size());

        pakkage = getPackage();
        dataBase.persist(pakkage);

        classes = dataBase.find(Class.class, 0, 1);
        assertEquals(1, classes.size());
    }

}