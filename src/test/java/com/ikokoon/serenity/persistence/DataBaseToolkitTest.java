package com.ikokoon.serenity.persistence;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.toolkit.FileUtilities;
import com.ikokoon.toolkit.Toolkit;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.ikokoon.serenity.persistence.IDataBase.DataBaseManager.getDataBase;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DataBaseToolkitTest extends ATest {

    @Test
    public void copy() {
        File odbDataBaseFile = new File("./src/test/resources/isearch/merge/serenity.odb");
        File ramDataBaseFile = new File("./src/test/resources/isearch/merge/serenity.ram");
        File jarDataBaseFile = new File("./src/test/resources/isearch/merge/jar.odb");
        File tagDataBaseFile = new File("./src/test/resources/isearch/merge/tag.odb");

        Toolkit.deleteFile(odbDataBaseFile, 3);
        Toolkit.deleteFile(ramDataBaseFile, 3);
        IDataBase odbDataBase = getDataBase(DataBaseOdb.class, odbDataBaseFile.getAbsolutePath(), Boolean.FALSE, null);
        DataBaseToolkit.clear(odbDataBase);
        odbDataBase.close();

        IDataBase sourceDataBase = getDataBase(DataBaseOdb.class, jarDataBaseFile.getAbsolutePath(), Boolean.FALSE, null);
        odbDataBase = getDataBase(DataBaseOdb.class, odbDataBaseFile.getAbsolutePath(), Boolean.FALSE, null);
        IDataBase ramDataBase = getDataBase(DataBaseRam.class, ramDataBaseFile.getAbsolutePath(), Boolean.FALSE, odbDataBase);

        Class<?, ?> klass = ramDataBase.find(Class.class, Toolkit.hash("com.ikokoon.search.action.Index"));
        assertNull(klass);
        klass = ramDataBase.find(Class.class, Toolkit.hash("com.ikokoon.search.tag.SpellingTag"));
        assertNull(klass);

        DataBaseToolkit.copyDataBase(sourceDataBase, ramDataBase);

        sourceDataBase.close();
        // odbDataBase.close();
        ramDataBase.close();

        sourceDataBase = getDataBase(DataBaseOdb.class, tagDataBaseFile.getAbsolutePath(), Boolean.FALSE, null);
        odbDataBase = getDataBase(DataBaseOdb.class, odbDataBaseFile.getAbsolutePath(), Boolean.FALSE, null);
        ramDataBase = getDataBase(DataBaseRam.class, ramDataBaseFile.getAbsolutePath(), Boolean.FALSE, odbDataBase);

        klass = odbDataBase.find(Class.class, Toolkit.hash("com.ikokoon.search.action.Index"));
        assertNotNull(klass);
        klass = odbDataBase.find(Class.class, Toolkit.hash("com.ikokoon.search.tag.SpellingTag"));
        assertNull(klass);

        DataBaseToolkit.copyDataBase(sourceDataBase, ramDataBase);

        sourceDataBase.close();
        // odbDataBase.close();
        ramDataBase.close();

        odbDataBase = getDataBase(DataBaseOdb.class, odbDataBaseFile.getAbsolutePath(), Boolean.FALSE, null);

        klass = odbDataBase.find(Class.class, Toolkit.hash("com.ikokoon.search.action.Index"));
        assertNotNull(klass);
        klass = odbDataBase.find(Class.class, Toolkit.hash("com.ikokoon.search.tag.SpellingTag"));
        assertNotNull(klass);

        odbDataBase.close();
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

    @Test
    public void dump() {
        // String targetPath = "/usr/share/eclipse/workspace/serenity/work/jobs/ikube/builds/2013-12-08_12-50-04/serenity/serenity.odb";
        String targetPath = "/tmp/serenity.odb";
        IDataBase odbDataBase = getDataBase(DataBaseOdb.class, targetPath, Boolean.FALSE, null);
        DataBaseToolkit.dump(odbDataBase, null, null);
    }

    @SuppressWarnings({"rawtypes", "unused"})
    void addSource(final File targetDataBaseFile) {
        String targetPath = targetDataBaseFile.getAbsolutePath();
        IDataBase odbDataBase = getDataBase(DataBaseOdb.class, targetPath, Boolean.FALSE, null);
        File findbugsDirectory = FileUtilities.findDirectoryRecursively(new File("."), 1, "serenity");
        List<Class> classes = odbDataBase.find(Class.class);
        for (final Class clazz : classes) {
            String className = clazz.getName();
            if (clazz.getSource() == null) {
                String simpleName = className.substring(className.lastIndexOf('.') + 1, className.length()) + ".java";
                File sourceFile = FileUtilities.findFileRecursively(findbugsDirectory, simpleName);
                if (sourceFile != null) {
                    clazz.setSource(FileUtilities.getContent(sourceFile));
                }
                odbDataBase.persist(clazz);
            }
        }
    }

}
