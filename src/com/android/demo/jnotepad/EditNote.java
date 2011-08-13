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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.android.demo.jnotepad.R;

public class EditNote extends Activity {
		
    private NotesDbAdapter mDbHelper;
    
    private EditText mBodyText;
    private Long mRowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (mDbHelper == null) {
            mDbHelper = new NotesDbAdapter(this);

            mDbHelper.open();
        }
        
        setContentView(R.layout.note_edit);

        mBodyText = (EditText) findViewById(R.id.body);

        Button confirmButton = (Button) findViewById(R.id.confirm);
        Button cancelButton = (Button) findViewById(R.id.cancel);
        Button deleteButton = (Button) findViewById(R.id.delete);

        mRowId = null;
        
        if (savedInstanceState != null) {
            mRowId = (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        }
        
        if (mRowId == null || mRowId == 0) {
            Bundle extras = getIntent().getExtras();
            
            if (extras != null) {
                mRowId = extras.getLong(NotesDbAdapter.KEY_ROWID);
            }
        }

        populateFields();

        if (mRowId == null || mRowId == 0){
        	deleteButton.setEnabled(false);
        }
        
        deleteButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

            	if (mRowId != null && mRowId != 0) {
            	    mDbHelper.deleteNote(mRowId);
            	}
            	
            	if (mDbHelper != null){
                    mDbHelper.close();
                    mDbHelper = null;
            	}
                finish();
            }

        });        
        
        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	saveState();
            	
            	if (mDbHelper != null){
                    mDbHelper.close();
                    mDbHelper = null;
            	}
                finish();
            }

        });
        
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	if (mDbHelper != null){
                    mDbHelper.close();
                    mDbHelper = null;
            	}
                finish();
            }

        });
    }
    
    private void populateFields() {
    	if (mDbHelper == null){
    		Log.e("NoteEdit","db is closed");
    		return;
    	}
    	
        if (mRowId != null && mRowId != 0) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            if (note != null)
            {

                mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
            }
        } else {
        	Button deleteButton = (Button) findViewById(R.id.delete);
            deleteButton.setFocusable(false);
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        if (mRowId != null && mRowId != 0) {
            outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
        }
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	saveState();
    }
    
    
    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }
    
    
    private void saveState() {
    	
    	if (mDbHelper == null){
    		Log.e("NoteEdit","db is closed");
    		return;
    	}    	
    	
        String body = mBodyText.getText().toString().trim();
        
        String datetime = DateFormat.getDateTimeInstance().format(new Date());
        

        /* note that android version 8 does not (apparently) have isEmpty() 
         * on the string class */
        if (body.length() == 0) {
        
        	if (mRowId != null || mRowId != 0) {
        		mDbHelper.deleteNote(mRowId);
        	}
        	
        } else if (mRowId == null || mRowId == 0) {
            long id = mDbHelper.createNote(datetime, body);
            if (id > 0) {
                mRowId = id;
            }
        } else {
        	
        	Cursor note = mDbHelper.fetchNote(mRowId);
            if (note != null)
            {

                String origStr = note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));
                
                if (!origStr.equals(body)){
                	mDbHelper.updateNote(mRowId, datetime, body);
                }
                
            }
        }
    }    
     
}
