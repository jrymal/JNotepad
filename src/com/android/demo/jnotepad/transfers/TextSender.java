package com.android.demo.jnotepad.transfers;

import android.content.Intent;

public class TextSender {
	
	private String body;
	private String title;
	
	/**
	 * sets the body string
	 * @param bod the string to set as the body of the data set.
	 */
	public void setBody(String bod) {
		body = bod;
	}

	/**
	 * sets the subject/title string
	 * @param ttl the title or subject of the email or message
	 */
	public void setTitle(String ttl) {
		title = ttl;
	}	
	
	/**
	 * Builds an intent with all the data packed into it
	 * 
	 * @return the intent configured for any text type message
	 */
	public Intent getIntent(){
		Intent txt = new Intent(Intent.ACTION_SEND);
		
		/* 
		 * It was noted in a site I found that we should use "message/rfc822" and not "text/plain" 
		 * http://stackoverflow.com/questions/2197741/how-to-send-email-from-my-android-application
		 * 
		 * emailNote.setType("text/plain");
		 */
		txt.setType("text/plain");
 
		/* If we have a title... */
		if (title != null && title.length() > 0) {
			/* ... use the title */
			txt.putExtra(Intent.EXTRA_SUBJECT, title);
		}
		
		/* The body is set */
        txt.putExtra(Intent.EXTRA_TEXT, body);

        return txt;
		
	}

}
