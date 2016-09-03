package com.ikokoon.serenity.hudson.modeller;

import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Stability;
import com.ikokoon.serenity.process.aggregator.AAggregator;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 11-06-2016
 */
public class GoogleChartModeller implements IModeller {

    private String model;
    private Integer[] buildNumbers;

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public void visit(final java.lang.Class<?> klass, final Composite<?, ?>... composites) {
        Map<String, Object> data = new HashMap<>();
        @SuppressWarnings("ConfusingArgumentToVarargsMethod")
        List<Map<String, Object>> columns = getColumns(
                new String[]{"id", "label", "type"},
                new String[][]{
                        {"Build", "Build", "string"},
                        {"Coverage", "Coverage", "number"},
                        {"Complexity", "Complexity", "number"},
                        {"Stability", "Stability", "number"},
                        {"Abstractness", "Abstractness", "number"},
                        {"Distance", "Distance", "number"}
                });
        data.put("cols", columns);

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        JSONObject json = new JSONObject();

        List<Map<String, List<Map<String, Object>>>> rows = getRows(composites);
        data.put("rows", rows);

        json.accumulateAll(data);

        model = json.toString();
    }

    private List<Map<String, List<Map<String, Object>>>> getRows(final Composite<?, ?>... composites) {
        List<Map<String, List<Map<String, Object>>>> rows = new ArrayList<>();
        int index = 0;

        for (final Composite<?, ?> composite : composites) {
            Map<String, List<Map<String, Object>>> row = new HashMap<>();
            List<Map<String, Object>> values = new ArrayList<>();

            values.add(getValue(buildNumbers[index++]));
            values.add(getValue(composite.getCoverage()));
            values.add(getValue(composite.getComplexity()));
            if (Package.class.isAssignableFrom(composite.getClass()) || Class.class.isAssignableFrom(composite.getClass())) {
                // Note: For some obscure reason the stability is always null in the server! We need to re-calculate each time
                double stability = AAggregator.getStability(((Stability) composite).getEfferent().size(), ((Stability) composite).getAfferent().size());
                values.add(getValue(stability * 100d));
            } else {
                values.add(getValue(composite.getStability() * 100d));
            }
            values.add(getValue(composite.getAbstractness() * 100d));
            values.add(getValue(composite.getDistance() * 100d));

            row.put("c", values);
            rows.add(row);
        }

        return rows;
    }

    private Map<String, Object> getValue(final double metric) {
        Map<String, Object> value = new HashMap<>();
        value.put("v", metric);
        return value;
    }

    private List<Map<String, Object>> getColumns(final String[] keys, final Object[]... values) {
        List<Map<String, Object>> columns = new ArrayList<>();
        for (final Object[] value : values) {
            Map<String, Object> column = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                column.put(keys[i], value[i]);
            }
            columns.add(column);
        }
        return columns;
    }

    public void setBuildNumbers(final Integer... buildNumbers) {
        this.buildNumbers = buildNumbers;
    }
}