package com.ikokoon.serenity.persistence;

import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.toolkit.Toolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is the in memory database. Several options were explored including DB4O, Neodatis, JPA, SQL, and finally none
 * were performant enough. As well as that several hybrids were investigated like including a l1 cache and a l2 cache,
 * but the actual persistence in the case of JPA and SQL was just
 * too slow. This class does everything in memory and commits the data finally to the under lying database once the processing is finished.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 11.10.09
 */
public final class DataBaseRam extends DataBase {

    private Logger logger;
    /**
     * The database file for access.
     */
    private String dataBaseFile;
    /**
     * The underlying persistence database to the file system.
     */
    private IDataBase dataBase;
    /**
     * The closed flag.
     */
    private boolean closed;

    /**
     * This is the index of packages, classes, methods and lines.
     */
    private transient volatile List<Composite<?, ?>> index = new ArrayList<>();

    /**
     * Constructor takes the underlying database that will commit the data to the file system, the listener for when the database closes we can
     * release the resources and whether to create a new database or open an old one.
     *
     * @param dataBaseFile file fo open the database on
     * @param dataBase     the underlying database that will actually persist the objects to the file system
     */
    DataBaseRam(final String dataBaseFile, final IDataBase dataBase) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.logger.info("Opening RAM database with " + dataBase + " underneath.");
        this.dataBaseFile = dataBaseFile;
        this.dataBase = dataBase;
        this.index.clear();
        this.closed = false;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized final <E extends Composite<?, ?>> E persist(final E composite) {
        setIds(composite);
        return composite;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public synchronized final <E extends Composite<?, ?>> E find(final Class<E> klass, final Long id) {
        return (E) search(klass, index, id);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public synchronized final <E extends Composite<?, ?>> E find(final Class<E> klass, final List<?> parameters) {
        Long id = Toolkit.hash(parameters.toArray());
        return (E) search(klass, index, id);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized final <E extends Composite<?, ?>> List<E> find(final Class<E> klass) {
        return find(klass, 0, Integer.MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E extends Composite<?, ?>> List<E> find(final Class<E> klass, final int start, final int end) {
        List<E> list = new ArrayList<>();
        int counter = 0;
        for (final Composite<?, ?> composite : index) {
            if (klass.isInstance(composite)) {
                list.add((E) composite);
                if (counter++ >= end) {
                    break;
                }
            }
        }
        if (list.size() == 0) {
            // Try to find some in the underlying database
            if (dataBase != null) {
                list = dataBase.find(klass, start, end);
                if (list != null && list.size() > 0) {
                    for (final Composite composite : list) {
                        insert(index, composite);
                    }
                }
            }
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public synchronized final <E extends Composite<?, ?>> E remove(final Class<E> klass, final Long id) {
        Composite<?, ?> composite = find(klass, id);
        if (composite != null) {
            Composite<?, ?> parent = composite.getParent();
            if (parent != null) {
                List<?> children = parent.getChildren();
                if (children != null) {
                    children.remove(composite);
                }
            }
            composite.setParent(null);
            if (!index.remove(composite)) {
                logger.warn("Didn't remove composite with id : " + id + ", because it wasn't in the index.");
            }
        }
        if (dataBase != null) {
            dataBase.remove(klass, id);
        }
        return (E) composite;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized final boolean isClosed() {
        if (dataBase != null) {
            if (!closed && dataBase.isClosed()) {
                closed = true;
            }
        }
        return closed;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized final void close() {
        if (closed) {
            logger.info("User tried to close the database again");
            return;
        }
        logger.info("Committing and closing the database");

        try {
            logger.info("Persisting index : " + dataBase);
            if (dataBase != null) {
                for (final Composite<?, ?> composite : index) {
                    if (Package.class.isInstance(composite) || Project.class.isInstance(composite)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Persisting : " + composite);
                        }
                        dataBase.persist(composite);
                    }
                }
                dataBase.close();
            } else {
                logger.warn("Persistence database was null : " + this);
            }
            index.clear();

            IDataBaseEvent dataBaseEvent = new DataBaseEvent(this, IDataBaseEvent.Type.DATABASE_CLOSE);
            IDataBase.DataBaseManager.fireDataBaseEvent(dataBaseFile, dataBaseEvent);
        } catch (final Exception e) {
            logger.error("Exception committing and closing the database", e);
        }
        closed = true;
    }

    /**
     * This method sets the ids in a graph of objects. The objects need to be stored, perhaps using the top level object in the hierarchy, then the
     * database is consulted for it's uid for the object. The uid is set in the field that has the Identifier annotation on the setter method for the
     * field.
     *
     * @param composite the object to set the id for
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    synchronized final void setIds(final Composite<?, ?> composite) {
        if (composite == null) {
            return;
        }
        super.setId(composite);
        // Insert the object into the index
        insert(index, composite);
        if (composite instanceof com.ikokoon.serenity.model.Class) {
            String name = ((com.ikokoon.serenity.model.Class) composite).getName();
            if (name.indexOf('/') > -1) {
                logger.warn("Invalid class name : " + name);
                Thread.dumpStack();
            }
        }
        List<Composite<?, ?>> children = (List<Composite<?, ?>>) composite.getChildren();
        for (final Composite<?, ?> child : children) {
            setIds(child);
        }
    }

    /**
     * A binary search through the index of composites.
     *
     * @param <E>   the type of composite
     * @param klass the class to search for
     * @param index the index of composites
     * @param id    the id of the composite to get
     * @return the composite from the index, or the underlying database, or null if no such composites exists
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    final <E extends Composite<?, ?>> E search(final Class klass, final List<Composite<?, ?>> index, final long id) {
        int low = 0;
        int high = index.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            Composite<?, ?> composite = index.get(mid);
            long midVal = composite.getId();
            if (midVal < id) {
                low = mid + 1;
            } else if (midVal > id) {
                high = mid - 1;
            } else {
                return (E) composite;
            }
        }
        // Look for the object in the underlying database
        if (this.dataBase != null) {
            Composite composite = this.dataBase.find(klass, id);
            if (composite != null) {
                insert(index, composite);
            }
            return (E) composite;
        }
        return null;
    }

    /**
     * Insert the composite into the index at the correct index.
     *
     * @param index    the index of composites
     * @param toInsert the composite to insert into the index
     */
    final void insert(final List<Composite<?, ?>> index, final Composite<?, ?> toInsert) {
        boolean inserted = false;
        if (index.size() == 0) {
            index.add(toInsert);
            inserted = true;
        } else {
            long key = toInsert.getId();
            int low = 0;
            int high = index.size();
            while (low <= high) {
                int mid = (low + high) >>> 1;
                if (mid >= index.size()) {
                    index.add(mid, toInsert);
                    inserted = true;
                    break;
                }
                Composite<?, ?> composite = index.get(mid);
                long midVal = composite.getId();
                if (midVal < key) {
                    int next = mid + 1;
                    if (index.size() > next) {
                        Composite<?, ?> nextComposite = index.get(next);
                        long nextVal = nextComposite.getId();
                        if (nextVal > key) {
                            index.add(next, toInsert);
                            inserted = true;
                            break;
                        }
                    }
                    low = mid + 1;
                } else if (midVal > key) {
                    int previous = mid - 1;
                    if (previous >= 0) {
                        Composite<?, ?> previousComposite = index.get(previous);
                        long previousVal = previousComposite.getId();
                        if (previousVal < key) {
                            index.add(mid, toInsert);
                            inserted = true;
                            break;
                        }
                    } else {
                        index.add(0, toInsert);
                        inserted = true;
                        break;
                    }
                    high = mid - 1;
                } else {
                    break;
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Inserted : " + toInsert + " - " + inserted);
        }
    }

    public <E extends Composite<?, ?>> List<E> find(final Class<E> klass, final Map<String, ?> parameters) {
        throw new RuntimeException("Not implempented.");
    }

}