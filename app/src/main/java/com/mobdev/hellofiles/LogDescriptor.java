package com.mobdev.hellofiles;

/**
 * Created by Marco Picone (picone.m@gmail.com) 20/03/2020
 * Simple Data Structure to describe collected Logs
 */
public class LogDescriptor {

	private long timestamp = 0;
	private double value = 0.0;
	
	public LogDescriptor() {
	}
	
	public LogDescriptor(long timestamp, double value) {
		super();
		this.timestamp = timestamp;
		this.value = value;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "LogDescriptor{" +
				"timestamp=" + timestamp +
				", value=" + value +
				'}';
	}
}
