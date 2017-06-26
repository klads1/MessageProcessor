package com.test.businessobjects;

import java.math.BigDecimal;

public class Adjustment {
	public static final String ADD = "Add";
	public static final String SUBTRACT = "Subtract";
	public static final String MULTIPLY = "Multiply";
	private BigDecimal value;
	private String operation;
	
	public Adjustment(String operation, BigDecimal value) {
		this.operation = operation;
		this.value = value;
	}
	
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	
}
