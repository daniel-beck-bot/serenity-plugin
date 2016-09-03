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
    @SuppressWarnings("unchecked")
    public void dump() {
        IDataBase dataBase = getDataBase(DataBaseOdb.class,
                "/home/laptop/Workspace/serenity/work/jobs/deploy/builds/7/serenity/serenity.odb",
                // "/home/laptop/Workspace/deploy/serenity/serenity.odb",
                // "/home/laptop/Workspace/deploy/serenity/serenity.odb",
                Boolean.FALSE,
                null);
        DataBaseToolkit.dump(dataBase, null, "Database dump : ");

        /*Class clazz = dataBase.find(Class.class, 1953713242666473466L);
        ClassAggregator classAggregator = new ClassAggregator(dataBase, clazz);
        classAggregator.aggregate();*/

        dataBase.close();
    }

}
