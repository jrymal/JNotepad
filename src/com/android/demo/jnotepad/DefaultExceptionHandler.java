package com.android.demo.jnotepad;

import java.lang.Thread.UncaughtExceptionHandler;

import android.util.Log;

/**
 * Note that thought this class LOOKS useful. When debugging in Android there 
 * are many application processes that are just being called and even though 
 * you are seeing an exception in one of the Android packages, chances are 
 * that you stored a bad value or your functions aren't in the stack...
 * 
 * Maybe i got a bug in here?
 *  
 * @author jrymal
 *
 */
public class DefaultExceptionHandler implements UncaughtExceptionHandler {

	private final static String LogName = "@string/app_name";	
	
	/**
	 * This is the function that gets called when an unhandled exception 
	 * occurs.
	 * 
	 * @param thread the thread that the exception occured in
	 * @param thrwbl the actual exception that happened
	 */
    public void uncaughtException(Thread thread, Throwable thrwbl) {
        /* First message */
        StringBuffer msg = new StringBuffer();
        msg.append(thread.getName());
        msg.append('(');
        msg.append(thread.getId());
        msg.append(')');
        msg.append(" has died: ");
        msg.append(thrwbl.getMessage());
        msg.append('(');
        msg.append(thrwbl.toString());
        msg.append(')');
        Log.e(LogName, msg.toString());
        
        /*Thread stack*/
        logStackTraceArray(thread.getStackTrace(), msg);
        
        logStackTraceArray(thrwbl.getStackTrace(), msg);
    }

    /**
     * this iterates through the stack and prints out the messages 
     * (showing where in the stack the application crashed at)
     * 
     * @param stackTrace an array of stack information
     * @param msg a shared instance of StringBuffer
     */
    private void logStackTraceArray(StackTraceElement[] stackTrace, StringBuffer msg) {
        for(int i=0; i < stackTrace.length; i++) {
            resetStringBuffer(msg);

            msg.append(i);
            msg.append(':');
            msg.append(stackTrace[i].toString());
            Log.e(LogName, msg.toString());
        }

    }

    /**
     * Resets the string buffer as needed
     * 
     * @param msg the string buffer to reset
     */
    private void resetStringBuffer(StringBuffer msg) {
        msg.delete(0, msg.length());
        msg.trimToSize();
    }

}
