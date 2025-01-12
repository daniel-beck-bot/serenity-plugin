package com.ikokoon.serenity.process;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.persistence.IDataBase;

import java.util.List;

/**
 * This class removes the lines and the efferent and afferent from the model as we will not need them further
 * and they form a very large part of the model which hogs memory.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-01-2010
 */
public class Pruner extends AProcess implements IConstants {

    /**
     * The database to prune.
     */
    private IDataBase dataBase;

    /**
     * Constructor takes the parent.
     *
     * @param parent   the parent process that will chain this process
     * @param dataBase the database to aggregate the statistics for
     */
    public Pruner(IProcess parent, IDataBase dataBase) {
        super(parent);
        this.dataBase = dataBase;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    public void execute() {
        super.execute();
        List<Line> lines = dataBase.find(Line.class);
        for (Line line : lines) {
            dataBase.remove(line.getClass(), line.getId());
        }
        List<Afferent> afferents = dataBase.find(Afferent.class);
        for (Afferent afferent : afferents) {
            // dataBase.remove(afferent.getClass(), afferent.getId());
        }
        List<Efferent> efferents = dataBase.find(Efferent.class);
        for (Efferent efferent : efferents) {
            // dataBase.remove(efferent.getClass(), efferent.getId());
        }
    }
}