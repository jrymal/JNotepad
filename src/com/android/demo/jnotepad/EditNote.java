/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.demo.jnotepad;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.android.demo.jnotepad.R;

public class EditNote extends Activity {
	
    private NotesDbAdapter mDbHelper;
    
    private EditText mBodyText;
    private Long mRowId;

    /**
     * Called on the creation of the Intent
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (mDbHelper == null) {
            mDbHelper = new NotesDbAdapter(this);

            mDbHelper.open();
        }
        
        /* set the layout */
        setContentView(R.layout.note_edit);

        /* store the text component */
        mBodyText = (EditText) findViewById(R.id.body);

        /* retrieve the buttons for configuration and tying to events */
        Button confirmButton = (Button) findViewById(R.id.confirm);
        Button cancelButton = (Button) findViewById(R.id.cancel);
        Button deleteButton = (Button) findViewById(R.id.delete);

        mRowId = null;
        
        /*  see if we have a old message stored (usually from a back or home 
         * button press) */
        if (savedInstanceState != null) {
            mRowId = (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        }
        
        /* No message there */
        if (mRowId == null || mRowId == 0) {
        	/* check to see if we are editing an old message */
            Bundle extras = getIntent().getExtras();
            
            if (extras != null) {
                mRowId = extras.getLong(NotesDbAdapter.KEY_ROWID);
            }
        }

        /* Populate the body object with old test (if there is any) */
        populateFields();
        
        /* Configures what happens on a "click" event on the delete button */
        deleteButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

            	deleteNote();
            }

        });        
        
        /* Configures what happens on a "click" event on the confirm button */
        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	doneEdit();
            }

        });
        
        /* Configures what happens on a "click" event on the cancel button */
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	cancelEdit();
            }

        });
    }

    /**
     * Clean up any objects the need to be cleaned
     */
    private void cleanUp() {
    	if (mDbHelper != null){
            mDbHelper.close();
            mDbHelper = null;
    	}
    }
    
    /**
     * what to do when the intent is told to delete (either by button press or 
     * menu press)
     */
    private void deleteNote(){
    	if (mRowId != null && mRowId != 0) {
    	    mDbHelper.deleteNote(mRowId);
    	}
    	
    	/* be a good citizen and clean up */
    	cleanUp();
    	
    	/* intent is done */
        finish();
    }

    /**
     * what to do when the intent is told to commit the data (either by button 
     * press or menu press)
     */    
    private void doneEdit(){
    	saveState();
    	
    	/* be a good citizen and clean up */
    	cleanUp();
    	
        finish();
    }

    /**
     * what to do when the intent is told to ignore the new data (either by 
     * button press or menu press)
     */    
    private void cancelEdit(){
    	/* be a good citizen and clean up */
    	cleanUp();
    	
        finish();
    	
    }
  
    /**
     * Pulls the data from the DB and places it on screen (in the text box)
     */
    private void populateFields() {
    	/* Ensure that we are still ok to do stuff with the db*/
    	if (mDbHelper == null){
    		Log.e("NoteEdit","db is closed");
    		return;
    	}
    	
        if (mRowId != null && mRowId != 0) {
        	/* This is an edit request */
        	
            Cursor note = mDbHelper.fetchNote(mRowId);
            
            if (note != null)
            {

            	/* Set the text in the message body object*/
                mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
                
                /* Set the delete button to enabled (As this message is in the 
                 * DB, we CAN delete it) */
                Button deleteButton = (Button) findViewById(R.id.delete);
                setButtonPressable(deleteButton, true);
            }
        } else {
        	
            /* Set the delete button to disabled (As this message is not in the 
             * DB, we CANNOT delete it) */
        	Button deleteButton = (Button) findViewById(R.id.delete);
        	setButtonPressable(deleteButton, false);
        }
    }

    /**
     * Configures a button to look and behave like its unavailable
     * @param button the button object to set
     * @param pressable the enable/disable value to set
     */
    private void setButtonPressable(Button button, boolean pressable) {
    	button.setFocusable(pressable);
    	button.setClickable(pressable);
    	button.setEnabled(pressable);
	}

    
    /**
     * Called when told to save the state (usually when the home button is 
     * pressed).
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        /* Save the data in the DB */
        saveState();
        
        /* If this is a edited message, store the rowId in the state object */
        if (mRowId != null && mRowId != 0) {
            outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
        }
    }
    
    /**
     * Called when the application is paused. This seems to happen quite 
     * often and in cases where you wouldn't expect.
     */
    @Override
    protected void onPause() {
    	super.onPause();
    	saveState();
    }
    
    /**
     * Called when the application is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }
    
    /* Saves the current text to the DB */
    private void saveState() {
    	
    	if (mDbHelper == null){
    		Log.e("NoteEdit","db is closed");
    		return;
    	}    	
    	
    	/* Pulls the text out and removes leading and following whitespace */
        String body = mBodyText.getText().toString().trim();
        
        /* Gets the save time */
        String datetime = DateFormat.getDateTimeInstance().format(new Date());
        

        /* note that android version 8 does not (apparently) have isEmpty() 
         * in the string class. Later versions do. */
        if (body.length() == 0) {
        	
            /* They've committed an empty string, we assume they want to 
             * delete the message */
        	if (mRowId != null || mRowId != 0) {
        		mDbHelper.deleteNote(mRowId);
        	}
        	
        } else if (mRowId == null || mRowId == 0) {
        	
        	/* This is a new note and we need an ID number for the row*/
            long id = mDbHelper.createNote(datetime, body);
            
            if (id > 0) {
            	/* Keep the id around */
                mRowId = id;
            }
        } else {
        	
        	/* This was an edit, just save the data */
        	Cursor note = mDbHelper.fetchNote(mRowId);
        	
            if (note != null)
            {
            	/* Get the string from the DB for comparison */
                String origStr = note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));

                /* If the two strings are the same, there is no need to edit 
                 * anything or change the time stamp */
                if (!origStr.equals(body)){
                	mDbHelper.updateNote(mRowId, datetime, body);
                }
                
            }
        }
    } 
    
    /**
     * Handles creating the option menu (the one resulting from a menu button 
     * press)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
       
        /* Standard pull from menu code */
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);
        
        /* Enable or disable the delete item*/
        MenuItem delete = menu.findItem(R.id.menu_delete);
        
        if (delete != null){
        	delete.setEnabled(mRowId != null && mRowId != 0);
        }
        
        return true;        
    }

    /**
     * Called when a menu item is selected
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_done:
            	doneEdit();
                break;
            case R.id.menu_cancel:
            	cancelEdit();
                break;
            case R.id.menu_delete:
                deleteNote();
                break;
            default:
                return super.onMenuItemSelected(featureId, item);
                
        }

        return true;
    }
     
}
