package com.ikokoon.serenity.persistence;

import com.ikokoon.serenity.ATest;
import com.ikokoon.toolkit.LoggingConfigurator;
import com.ikokoon.toolkit.Toolkit;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;

public class DataBaseManagerTest extends ATest {

    @BeforeClass
    public static void setup() {
        LoggingConfigurator.configure();
    }

    @Test
    public void getDataBase() {
        File dataBaseFile = new File("./src/test/resources/dummy.odb");
        IDataBase dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile.getAbsolutePath(), Boolean.FALSE, mockInternalDataBase);
        assertNotNull(dataBase);
        dataBase.close();

        dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, dataBaseFile.getAbsolutePath(), Boolean.FALSE, dataBase);
        assertNotNull(dataBase);
        dataBase.close();

        Toolkit.deleteFile(dataBaseFile, 3);
    }
}