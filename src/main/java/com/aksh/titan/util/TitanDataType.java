package com.aksh.titan.util;

import java.util.UUID;

import com.thinkaurelius.titan.core.attribute.Geoshape;

public enum TitanDataType {

	String, Character, Boolean, Byte, Short, Integer, Long, Float, Double, Geoshape, UUID, Date;

	public static Class getDataTypeClass(String dataType) {
		Class elementClass = null;
		if (dataType.equalsIgnoreCase(String.name())) {
			elementClass = String.class;
		} else if (dataType.equalsIgnoreCase(Character.name())) {
			elementClass = Character.class;
		} else if (dataType.equalsIgnoreCase(Boolean.name())) {
			elementClass = Boolean.class;
		} else if (dataType.equalsIgnoreCase(Byte.name())) {
			elementClass = Byte.class;
		} else if (dataType.equalsIgnoreCase(Short.name())) {
			elementClass = Short.class;
		} else if (dataType.equalsIgnoreCase(Integer.name())) {
			elementClass = Integer.class;
		} else if (dataType.equalsIgnoreCase(Long.name())) {
			elementClass = Long.class;
		} else if (dataType.equalsIgnoreCase(Float.name())) {
			elementClass = Float.class;
		} else if (dataType.equalsIgnoreCase(Double.name())) {
			elementClass = Double.class;
		} else if (dataType.equalsIgnoreCase(Geoshape.name())) {
			elementClass = Geoshape.class;
		} else if (dataType.equalsIgnoreCase(UUID.name())) {
			elementClass = UUID.class;
		} else if (dataType.equalsIgnoreCase(Date.name())) {
			elementClass = java.util.Date.class;
		}
		return elementClass;
	}

}
