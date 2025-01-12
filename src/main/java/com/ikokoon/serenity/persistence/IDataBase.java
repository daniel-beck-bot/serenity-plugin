package com.ikokoon.serenity.persistence;

import com.ikokoon.serenity.model.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the persistence interface for the Serenity code coverage and dependency functionality.
 *
 * @author Michael Couck
 * @version 01.3 <br>
 *          Added a find method that can be implemented as a fuzzy search method by the implementation with
 *          multiple search criteria. For example 'where x like n and y in m'.
 * @since 12-08-2009
 */
public interface IDataBase {

    /**
     * This class manages the databases if there are more than one. It also listens to events that the databases might
     * throw like a close and so on.
     *
     * @author Michael Couck
     * @version 01.00
     * @since 12-08-2009
     */
    class DataBaseManager {

        private static Logger LOGGER = LoggerFactory.getLogger(DataBaseManager.class);
        /**
         * The map of open databases keyed on the database file name.
         */
        private static Map<String, IDataBase> DATABASES = new HashMap<>();
        /**
         * The map of database listeners keyed on the database file name.
         */
        private static Map<String, List<IDataBaseListener>> DATABASE_LISTENERS = new HashMap<>();

        /**
         * Access to all the databases in the current VM.
         *
         * @return all the databases
         */
        public static synchronized Map<String, IDataBase> getDataBases() {
            return DATABASES;
        }

        /**
         * Accesses a database. In the case the database is not open one will be instantiated on the database file specified.
         * In the case the database is open but not the right type of database, the old one will be closed and the new one will
         * be opened on the database file, otherwise the database is returned.
         *
         * @param <E>              the database type
         * @param klass            the class of database to initialise
         * @param dataBaseFile     the database file to open the database on
         * @param server           whether the database should be opened as a server
         * @param internalDataBase the underlying database
         * @return the database
         */
        public static synchronized <E extends IDataBase> IDataBase getDataBase(
                final Class<E> klass,
                final String dataBaseFile,
                final boolean server,
                final IDataBase internalDataBase) {
            IDataBase dataBase = DATABASES.get(dataBaseFile);
            if (dataBase == null || dataBase.isClosed()) {
                getDataBaseListener(dataBaseFile);
                if (DataBaseRam.class.isAssignableFrom(klass)) {
                    dataBase = new DataBaseRam(dataBaseFile, internalDataBase);
                } else if (DataBaseOdb.class.isAssignableFrom(klass)) {
                    dataBase = new DataBaseOdb(dataBaseFile, server);
                }
                LOGGER.debug("Adding database : " + dataBase);
                DATABASES.put(dataBaseFile, dataBase);
            }
            LOGGER.debug("Returned database : " + klass + ", data base : " + dataBase + ", file : " + dataBaseFile);
            return dataBase;
        }

        static synchronized void addDataBaseListener(final String dataBaseFile, final IDataBaseListener dataBaseListener) {
            List<IDataBaseListener> dataBaseListeners = DataBaseManager.DATABASE_LISTENERS.get(dataBaseFile);
            if (dataBaseListeners == null) {
                dataBaseListeners = new ArrayList<>();
                DataBaseManager.DATABASE_LISTENERS.put(dataBaseFile, dataBaseListeners);
            }
            dataBaseListeners.add(dataBaseListener);
        }

        static synchronized void removeDataBaseListener(final String dataBaseFile, final IDataBaseListener dataBaseListener) {
            List<IDataBaseListener> dataBaseListeners = DataBaseManager.DATABASE_LISTENERS.get(dataBaseFile);
            if (dataBaseListeners != null) {
                dataBaseListeners.remove(dataBaseListener);
            }
        }

        static synchronized void fireDataBaseEvent(final String dataBaseFile, final IDataBaseEvent dataBaseEvent) {
            List<IDataBaseListener> dataBaseListeners = DataBaseManager.DATABASE_LISTENERS.get(dataBaseFile);
            if (dataBaseListeners != null) {
                IDataBaseListener[] array = dataBaseListeners.toArray(new IDataBaseListener[dataBaseListeners.size()]);
                for (IDataBaseListener dataBaseListener : array) {
                    dataBaseListener.fireDataBaseEvent(dataBaseEvent);
                }
            }
        }

        private synchronized static IDataBaseListener getDataBaseListener(final String dataBaseFile) {
            return new IDataBaseListener() {

                {
                    DataBaseManager.addDataBaseListener(dataBaseFile, this);
                }

                public void fireDataBaseEvent(IDataBaseEvent dataBaseEvent) {
                    if (dataBaseEvent.getEventType().equals(IDataBaseEvent.Type.DATABASE_CLOSE)) {
                        IDataBase dataBase = dataBaseEvent.getDataBase();
                        if (!DATABASES.values().remove(dataBase)) {
                            LOGGER.debug("Database not removed : " + dataBase);
                        } else {
                            LOGGER.debug("Removed database : " + dataBase);
                        }
                        DataBaseManager.removeDataBaseListener(dataBaseFile, this);
                    }
                }
            };
        }

    }

    /**
     * Persists an object to a persistent store.
     *
     * @param composite the composite to persist
     * @param <E>       the type
     * @return the composite that is persisted, typically the id will be set by the underlying implementation
     */
    <E extends Composite<?, ?>> E persist(final E composite);

    /**
     * Selects a composite based on the class type and the id of the class.
     *
     * @param <E>   the return type of the class
     * @param klass the type of class to select
     * @param id    the unique id of the class
     * @return the composite with the specified id
     */
    <E extends Composite<?, ?>> E find(final Class<E> klass, final Long id);

    /**
     * Selects a class based on the combination of field values in the parameter list.
     *
     * @param <E>        the return type of the class
     * @param klass      the type of class to select
     * @param parameters the unique combination of field values to select the class with
     * @return the composite with the specified unique field combination
     */
    <E extends Composite<?, ?>> E find(final Class<E> klass, final List<?> parameters);

    /**
     * Selects a list of objects based on the values in the objects and the class of the object. The implementations can implement multiple search
     * criteria including fuzzy selection etc.
     *
     * @param <E>        the return type of the class
     * @param klass      the type of class to select
     * @param parameters the parameters to do the selection with. These are the fields in the objects and the values
     * @return the list of composites that match the selection criteria
     */
    <E extends Composite<?, ?>> List<E> find(final Class<E> klass, final Map<String, ?> parameters);

    /**
     * Selects all the classes of a particular type. Note this could potentially return the whole database.
     *
     * @param <E>   the return type of the class
     * @param klass the type of class to select
     * @return a list of all the objects in the database that have the specified class type
     */
    <E extends Composite<?, ?>> List<E> find(final Class<E> klass);

    /**
     * Selects all the classes of a particular type starting from an index and going to an index.
     *
     * @param <E>   the return type of the class
     * @param klass the type of class to select
     * @param start the beginning index to retrieve objects from
     * @param end   the end index for the list of objects
     * @return a list of all the objects in the database that have the specified class type
     */
    <E extends Composite<?, ?>> List<E> find(final Class<E> klass, final int start, final int end);

    /**
     * Removes an object from the database and returns the removed object as a convenience.
     *
     * @param <E>   the return type of the class
     * @param klass the type of class to select
     * @param id    the unique id of the class
     * @return the composite with the specified id
     */
    <E extends Composite<?, ?>> E remove(final Class<E> klass, final Long id);

    /**
     * Checks the open status of the database.
     *
     * @return whether the database is open or closed
     */
    boolean isClosed();

    /**
     * Closes the database.
     */
    void close();

}