package com.ikokoon.serenity.persistence;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.toolkit.Toolkit;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.util.List;

import static com.ikokoon.serenity.persistence.IDataBase.DataBaseManager.getDataBase;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DataBaseToolkitTest extends ATest {

    private IDataBase dataBaseOne;
    private IDataBase dataBaseTwo;

    @Test
    @SuppressWarnings("unchecked")
    public void copy() throws NoSuchFieldException, IllegalAccessException, IllegalClassFormatException, IOException {
        // Create a database with some classes
        // Split the database into two databases
        // Copy the one database into the other
        // Check that the target database contains all the entities from the source database
        String dataBaseFileOne = "./serenity/serenity-one.odb";
        String dataBaseFileTwo = "./serenity/serenity-two.odb";
        loadDatabase(dataBaseFileOne, dataBaseFileTwo);
        dataBaseOne = getDataBase(DataBaseRam.class, dataBaseFileOne, Boolean.FALSE, null);
        dataBaseTwo = getDataBase(DataBaseRam.class, dataBaseFileTwo, Boolean.FALSE, null);

        // Verify that the files have no elements in common
        assertContents(Boolean.FALSE);

        DataBaseToolkit.copyDataBase(dataBaseOne, dataBaseTwo);

        // Verify that the files have all the elements from database one in database two
        assertContents(Boolean.TRUE);
    }

    void assertContents(final boolean contains) {
        List<Package> packages = dataBaseOne.find(Package.class);
        for (final Package aPackage : packages) {
            if (contains) {
                assertNotNull(dataBaseTwo.find(Package.class, aPackage.getId()));
            } else {
                assertNull(dataBaseTwo.find(Package.class, aPackage.getId()));
            }
            for (final Object clazz : aPackage.getChildren()) {
                if (contains) {
                    assertNotNull(dataBaseTwo.find(Class.class, ((Class) clazz).getId()));
                } else {
                    assertNull(dataBaseTwo.find(Class.class, ((Class) clazz).getId()));
                }
                for (final Object method : ((Class) clazz).getChildren()) {
                    if (contains) {
                        assertNotNull(dataBaseTwo.find(Method.class, ((Method) method).getId()));
                    } else {
                        assertNull(dataBaseTwo.find(Method.class, ((Method) method).getId()));
                    }
                }
            }
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void clear() {
        String dataBaseFile = "./src/test/resources/dummy.odb";
        IDataBase dataBase = getDataBase(DataBaseOdb.class, dataBaseFile, Boolean.FALSE, null);
        Class<?, ?> klass = new Class<Package, Method>();
        klass.setId(Long.MAX_VALUE);
        dataBase.persist(klass);
        klass = dataBase.find(Class.class, klass.getId());
        assertNotNull(klass);

        DataBaseToolkit.clear(dataBase);
        dataBase.close();

        dataBase = getDataBase(DataBaseOdb.class, dataBaseFile, Boolean.FALSE, null);

        klass = dataBase.find(Class.class, klass.getId());
        assertNull(klass);

        dataBase.close();

        Toolkit.deleteFile(new File(dataBaseFile), 3);
    }

}
