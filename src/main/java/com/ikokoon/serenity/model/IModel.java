package com.ikokoon.serenity.model;

import java.io.Serializable;

public interface IModel extends Serializable {
	
	public String getName();

	public String[] getLegend();

	public double[][] getLimits();

	public double[][] getMetrics();
}