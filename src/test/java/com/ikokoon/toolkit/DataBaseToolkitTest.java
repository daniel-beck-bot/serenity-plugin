package com.ikokoon.toolkit;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import org.junit.Ignore;
import org.junit.Test;

import static com.ikokoon.serenity.persistence.IDataBase.DataBaseManager.getDataBase;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 12-06-2016
 */
public class DataBaseToolkitTest extends ATest {

    @Test
    @Ignore
    public void dump() {
        IDataBase dataBase = getDataBase(DataBaseOdb.class,
                "/home/laptop/Workspace/serenity/serenity/serenity-clean-two.odb",
                Boolean.FALSE,
                null);
        DataBaseToolkit.dump(dataBase, null, "Database dump : ");
        dataBase.close();
    }

}
