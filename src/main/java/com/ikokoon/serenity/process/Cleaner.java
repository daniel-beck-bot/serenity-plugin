package com.ikokoon.serenity.process;

import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.IDataBase;

import java.util.List;

/**
 * During the collection of the data packages are collected along with the data so we have references to the
 * packages. For example if a class relies on 'org.logj4' then this package will be added to the database but is
 * not included in the packages that the user wants. This class will clean the unwanted packages from the database
 * when the processing is finished.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12.08.09
 */
public class Cleaner extends AProcess implements IConstants {

    private IDataBase dataBase;

    /**
     * Constructor takes the parent.
     *
     * @param parent   the parent process that will chain this process
     * @param dataBase the database to aggregate the statistics for
     */
    public Cleaner(final IProcess parent, final IDataBase dataBase) {
        super(parent);
        this.dataBase = dataBase;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    public void execute() {
        super.execute();
        // Clean all the packages that got in the database along the processing
        // that were not included in the packages required
        List<Package> packages = dataBase.find(Package.class);
        for (final Package<?, ?> pakkage : packages.toArray(new Package[packages.size()])) {
            // Remove the packages that are not included in the list to process
            String packageName = pakkage.getName();
            if (Configuration.getConfiguration().excluded(packageName)) {
                dataBase.remove(Package.class, pakkage.getId());
            } else {
                if (!Configuration.getConfiguration().included(packageName)) {
                    dataBase.remove(Package.class, pakkage.getId());
                }
            }
        }
        // Remove all the classes that are excluded too
        List<Class> classes = dataBase.find(Class.class);
        for (final Class clazz : classes) {
            String className = clazz.getName();
            if (Configuration.getConfiguration().excluded(className)) {
                dataBase.remove(Class.class, clazz.getId());
            } else {
                if (!Configuration.getConfiguration().included(className)) {
                    dataBase.remove(Class.class, clazz.getId());
                }
            }
        }
    }

}