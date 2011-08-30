package com.android.demo.jnotepad.transfers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;

public class SDWriter {

	/**
	 * The writer object
	 */
	private FileWriter writer;
	
	/**
	 * Builds a writer to the SD card's file system
	 * 
	 * @param rootDir the core directory to write new files into
	 * @param fileName the new file name to create (can over write old files)
	 * @throws IOException if we cannot open the sd card or the FileWriter class throws one on open
	 */
	//TODO: fix so overwriting is protected
	public SDWriter(String rootDir, String fileName) throws IOException {
		
		if (!canOpenSDCard()) {
			throw new IOException("Cannot write to SD Card!");
		}
		
        File root = new File(Environment.getExternalStorageDirectory(), rootDir);
        if (!root.exists()) {
            root.mkdirs();
        }
        File gpxfile = new File(root, fileName);
        writer = new FileWriter(gpxfile);
	}
	
	/**
	 * Test to see if we can Open the SD card
	 * @return
	 */
	private boolean canOpenSDCard() {
	    String auxSDCardStatus = Environment.getExternalStorageState();

	    if (auxSDCardStatus.equals(Environment.MEDIA_MOUNTED))
	        return true;
	    else if (auxSDCardStatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
	        return true;
	    }
	    else if(auxSDCardStatus.equals(Environment.MEDIA_NOFS)){
	        return false;
	    }

	    else if(auxSDCardStatus.equals(Environment.MEDIA_REMOVED)){
	        return false;
	    }
	    else if(auxSDCardStatus.equals(Environment.MEDIA_SHARED)){
	        return false;
	    }
	    else if (auxSDCardStatus.equals(Environment.MEDIA_UNMOUNTABLE)){
	        return false;
	    }
	    else if (auxSDCardStatus.equals(Environment.MEDIA_UNMOUNTED)){
	        return false;
	    }


	    return true;
	}

	/**
	 * Adds new data to the FileWriter
	 * 
	 * @param body the new text to add
	 * @throws IOException thrown if the add fails of if flush fails.
	 */
	public void addData(String body) throws IOException {
		writer.append(body);
		writer.flush();
	}

	/**
	 * Closes the FileWriter
	 * @throws IOException thrown if the close fails
	 */
	public void close() throws IOException {
        writer.close();
	}

}