package com.ikokoon.serenity.persistence;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.toolkit.Toolkit;
import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.ODBServer;
import org.neodatis.odb.Objects;
import org.neodatis.odb.core.query.IQuery;
import org.neodatis.odb.core.query.criteria.Where;
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.ServerSocket;
import java.util.*;

/**
 * This is the database class using Neodatis as the persistence tool.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 01-12-2009
 */
public class DataBaseOdb extends DataBase {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * The server for the database for remote access to the running data.
     */
    private ODBServer odbServer;
    /**
     * The Neodatis object database for persistence.
     */
    private ODB odb = null;
    /**
     * The database file for Neodatis.
     */
    private String dataBaseFile;
    /**
     * The closed flag.
     */
    private boolean closed = Boolean.TRUE;

    /**
     * Constructor initialises a {@link DataBaseOdb} object.
     *
     * @param dataBaseFile the file to open the database with
     * @param server       whether to start the database server of not
     */
    public DataBaseOdb(final String dataBaseFile, final boolean server) {
        synchronized (DataBaseOdb.class) {
            this.dataBaseFile = dataBaseFile;
            logger.debug("Opening ODB database on file : " + new File(dataBaseFile).getAbsolutePath());
            try {
                if (!server) {
                    odb = ODBFactory.open(this.dataBaseFile);
                } else {
                    odbServer = ODBFactory.openServer(findOpenPort(IConstants.DATABASE_PORT));
                    odbServer.addBase(IConstants.DATABASE_FILE_ODB, this.dataBaseFile);
                    odbServer.setAutomaticallyCreateDatabase(Boolean.TRUE);
                    odbServer.startServer(Boolean.TRUE);

                    odb = odbServer.openClient(IConstants.DATABASE_FILE_ODB);
                }
                closed = false;
            } catch (final Exception e) {
                logger.error("Exception initialising the database : " + dataBaseFile + ", " + this, e);
            }
        }
    }

    private int findOpenPort(final int startingAtPort) {
        int openPort = startingAtPort;
        while (openPort < ((Short.MAX_VALUE * 2) - 1)) {
            try {
                new ServerSocket(openPort).close();
                return openPort;
            } catch (final Exception e) {
                logger.info("Port occupied, multiple instances of Serenity perhaps : ", e);
            } finally {
                openPort++;
            }
        }
        // Default is to get the system to choose a port
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public synchronized <E extends Composite<?, ?>> E find(final Class<E> klass, final Long id) {
        IQuery query = new CriteriaQuery(klass, Where.equal("id", id));
        return (E) find(query);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized <E extends Composite<?, ?>> E find(final Class<E> klass, final List<?> parameters) {
        Long id = Toolkit.hash(parameters.toArray());
        return find(klass, id);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized <E extends Composite<?, ?>> List<E> find(final Class<E> klass) {
        return find(klass, 0, Integer.MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <E extends Composite<?, ?>> List<E> find(final Class<E> klass, final int start, final int end) {
        if (isClosed()) {
            return Collections.EMPTY_LIST;
        }
        List<E> list = new ArrayList<>();
        try {
            Objects objects = odb.getObjects(klass, false, start, end);
            while (objects.hasNext()) {
                Object object = objects.next();
                list.add((E) object);
            }
        } catch (final Exception e) {
            logger.error("Exception selecting objects with class : " + klass + ", " + this, e);
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <E extends Composite<?, ?>> List<E> find(final Class<E> klass, final Map<String, ?> parameters) {
        if (isClosed()) {
            return Collections.EMPTY_LIST;
        }
        Set<E> set = new TreeSet<>();
        for (final Map.Entry<String, ?> mapEntry : parameters.entrySet()) {
            Object value = mapEntry.getValue();
            logger.debug("Field : " + mapEntry.getKey() + ", " + value);
            IQuery query = new CriteriaQuery(klass, Where.like(mapEntry.getKey(), "%" + value.toString() + "%"));
            try {
                Objects objects = odb.getObjects(query);
                logger.debug("Objects : " + objects);
                if (set.size() == 0) {
                    set.addAll(objects);
                }
                set.retainAll(objects);
                logger.debug("Set : " + set);
            } catch (Exception e) {
                logger.error("Exception selecting objects with class : " + klass + ", parameters : " + parameters + ", " + this, e);
            }
        }
        List<E> list = new ArrayList<>();
        list.addAll(set);
        logger.debug("List : " + list);
        return list;
    }

    private synchronized void commit() {
        try {
            if (!isClosed()) {
                odb.commit();
            }
        } catch (final Exception e) {
            logger.error("Exception committing the ODB database : " + this, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public synchronized <E extends Composite<?, ?>> E persist(final E composite) {
        if (isClosed()) {
            return null;
        }
        try {
            setIds(composite);
            E duplicate = (E) find(composite.getClass(), composite.getId());
            if (duplicate != null) {
                if (duplicate != composite) {
                    logger.warn("Attempted to persist a duplicate composite : " + composite + ", " + this);
                    return composite;
                }
            }
            logger.debug("Persisting composite : " + composite);
            odb.store(composite);
        } catch (final Exception e) {
            logger.error("Exception persisting object : " + composite + ", " + this, e);
        }
        commit();
        return composite;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized <E extends Composite<?, ?>> E remove(final Class<E> klass, final Long id) {
        E composite = find(klass, id);
        try {
            if (composite != null) {
                odb.delete(composite);
            }
        } catch (final Exception e) {
            logger.error("Exception deleting object : " + id + ", " + this, e);
        }
        commit();
        return composite;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized boolean isClosed() {
        return closed;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void close() {
        try {
            if (isClosed()) {
                logger.warn("Attempted to close the database again : " + this);
                return;
            }

            commit();
            odb.close();

            if (odbServer != null) {
                odbServer.close();
            }

            IDataBaseEvent dataBaseEvent = new DataBaseEvent(this, IDataBaseEvent.Type.DATABASE_CLOSE);
            IDataBase.DataBaseManager.fireDataBaseEvent(dataBaseFile, dataBaseEvent);

            logger.debug("Closed database on file : " + new File(dataBaseFile).getAbsolutePath());
        } catch (final Exception e) {
            logger.error("Exception closing the ODB database : " + this, e);
        } finally {
            closed = true;
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized <E extends Composite<?, ?>> E find(final IQuery query) {
        if (isClosed()) {
            return null;
        }
        E e = null;
        try {
            Objects objects = odb.getObjects(query);
            if (objects.size() == 1) {
                e = (E) objects.getFirst();
            } else if (objects.size() > 1) {
                logger.warn("Id for object must be unique : " + query);
            }
        } catch (final Exception ex) {
            logger.error("Exception selecting object on ODB database : " + query + ", " + this.dataBaseFile + ", " + this, ex);
        }
        return e;
    }

    /**
     * This method sets the ids in a graph of objects.
     *
     * @param composite the object to set the ids for
     */
    @SuppressWarnings("unchecked")
    synchronized final void setIds(final Composite<?, ?> composite) {
        if (isClosed()) {
            return;
        }
        if (composite == null) {
            return;
        }
        super.setId(composite);
        logger.debug("Persisted object : " + composite);
        List<Composite<?, ?>> children = (List<Composite<?, ?>>) composite.getChildren();
        if (children != null) {
            for (final Composite<?, ?> child : children) {
                setIds(child);
            }
        }
    }

}
