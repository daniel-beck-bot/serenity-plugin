package com.ikokoon.serenity.hudson.source;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Executer;
import org.junit.Test;

import java.util.List;

import static com.ikokoon.serenity.persistence.IDataBase.DataBaseManager.getDataBase;
import static org.junit.Assert.assertTrue;

public class CoverageSourceCodeTest extends ATest {

    @SuppressWarnings("FieldCanBeLocal")
    private String source = "class Dummy {\n\tprivate String name;\n\n\tpublic Dummy() {\n\t}\n\tpublic String getName() {\n\t\treturn name;\n\t}\n}";

    @Test
    public void getSource() {
        Package<?, ?> pakkage = getPackage();
        IDataBase dataBase = getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, Boolean.FALSE, mockInternalDataBase);
        dataBase.persist(pakkage);

        @SuppressWarnings("unchecked")
        Class<Package, Method> klass = dataBase.find(Class.class, pakkage.getChildren().get(0).getId());
        setCovered(klass);
        final CoverageSourceCode coverageSourceCode = new CoverageSourceCode(klass, source);
        String html = coverageSourceCode.getSource();
        logger.info(html);

        double executionsPerSecond = Executer.execute(new Executer.IPerform() {
            public void execute() {
                coverageSourceCode.getSource();
            }
        }, "highlight source", 10);
        assertTrue(executionsPerSecond > 10);
        dataBase.remove(pakkage.getClass(), pakkage.getId());
        dataBase.close();
    }

    private void setCovered(Class<Package, Method> klass) {
        List<Method> methods = klass.getChildren();
        //noinspection unchecked
        for (final Method<Class, Line> method : methods) {
            List<Line> lines = method.getChildren();
            for (Line<?, ?> line : lines) {
                line.setCounter(1);
            }
        }
    }

}
