/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Log class used for performing loggin of the simulation process. It provides the ability to
 * substitute the output stream by any OutputStream subclass.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */

/**
 *  extended by @author Marcuspeng
 *
 * */
public class Log {

	/** The Constant LINE_SEPARATOR. */
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/** The output. */
	private static OutputStream output;

	/** The disable output flag. */
	private static boolean disabled;

	private static boolean location_flag = true;

	public static String getClassAndLine(){

		String methodName = getCurrentMethod();
		String fileName = getCurrentFile();
		String line = getCurrentLine();
		String dateTime = getTime();

		String exHead = getExternal();

		return exHead + dateTime + " " + "IN " + methodName + " AT " + fileName + ":" + line + " ";
	}

	private static String getCurrentFile() {
		StackTraceElement[] eles = Thread.currentThread().getStackTrace();
		int stackOffset = getStackOffset(eles);
		if(stackOffset == -1){
			Log.printLine("IN Log.java AT line: 61, 获取打印日志的更详细信息失败");
		}else {
			return eles[stackOffset].getFileName();
		}
		return null;
	}

	private static String getExternal() {
		return "[INFO]";
	}

	private static String getCurrentLine() {

		StackTraceElement[] eles = Thread.currentThread().getStackTrace();
		int stackOffset = getStackOffset(eles);
		if(stackOffset == -1){
			Log.printLine("IN Log.java AT line: 61, 获取打印日志的更详细信息失败");
		}else {
			return eles[stackOffset].getLineNumber()+"";
		}
		return null;
	}

	private static String getCurrentMethod() {
		StackTraceElement[] eles = Thread.currentThread().getStackTrace();
		//
		int stackOffset = getStackOffset(eles);
		if(stackOffset == -1){
			Log.printLine("IN Log.java AT line: 73, 获取打印日志的更详细信息失败");
		}else {
			return eles[stackOffset].getMethodName();
		}

		return null;
	}

	private static String getTime() {
		DateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");

		return format.format(new Date());
	}

	private static int getStackOffset(StackTraceElement[] eles) {
		int target = -1;
		for(int i =1; i< eles.length; i++){

			StackTraceElement e = eles[i];
			if(!e.getClassName().equals(Log.class.getName())){
				target = i;
				break;
			}

		}

		return target;
	}


	/**
	 * Prints the message.
	 * 
	 * @param message the message
	 */
	public static void print(String message) {
		if (!isDisabled()) {
			try {
				if (location_flag) {
					//getOutput().write(getClassAndLine().getBytes());
				}
				getOutput().write(message.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Prints the message passed as a non-String object.
	 * 
	 * @param message the message
	 */
	public static void print(Object message) {
		if (!isDisabled()) {
			print(String.valueOf(message));
		}
	}

	/**
	 * Prints the line.
	 * 
	 * @param message the message
	 */
	public static void printLine(String message) {
		if (!isDisabled()) {
			print(message + LINE_SEPARATOR);
		}
	}

	/**
	 * Prints the empty line.
	 */
	public static void printLine() {
		if (!isDisabled()) {
			print(LINE_SEPARATOR);
		}
	}

	/**
	 * Prints the line passed as a non-String object.
	 * 
	 * @param message the message
	 */
	public static void printLine(Object message) {
		if (!isDisabled()) {
			printLine(String.valueOf(message));
		}
	}

	/**
	 * Prints a string formated as in String.format().
	 * 
	 * @param format the format
	 * @param args the args
	 */
	public static void format(String format, Object... args) {
		if (!isDisabled()) {
			print(String.format(format, args));
		}
	}

	/**
	 * Prints a line formated as in String.format().
	 * 
	 * @param format the format
	 * @param args the args
	 */
	public static void formatLine(String format, Object... args) {
		if (!isDisabled()) {
			printLine(String.format(format, args));
		}
	}

	/**
	 * Sets the output.
	 * 
	 * @param _output the new output
	 */
	public static void setOutput(OutputStream _output) {
		output = _output;
	}

	/**
	 * Gets the output.
	 * 
	 * @return the output
	 */
	public static OutputStream getOutput() {
		if (output == null) {
			setOutput(System.out);
		}
		return output;
	}

	/**
	 * Sets the disable output flag.
	 * 
	 * @param _disabled the new disabled
	 */
	public static void setDisabled(boolean _disabled) {
		disabled = _disabled;
	}

	/**
	 * Checks if the output is disabled.
	 * 
	 * @return true, if is disable
	 */
	public static boolean isDisabled() {
		return disabled;
	}

	/**
	 * Disables the output.
	 */
	public static void disable() {
		setDisabled(true);
	}

	/**
	 * Enables the output.
	 */
	public static void enable() {
		setDisabled(false);
	}

	public static String getLineSeparator() {
		return LINE_SEPARATOR;
	}

	public static boolean isLocation_flag() {
		return location_flag;
	}

	public static void setLocation_flag(boolean location_flag) {
		Log.location_flag = location_flag;
	}
}
