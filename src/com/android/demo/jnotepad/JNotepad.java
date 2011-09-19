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

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class JNotepad extends ListActivity {
    
    private NotesDbAdapter mDbHelper;
    private Cursor mNotesCursor;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	/* Add a exception handler for everything that isn't handled so we 
    	 * can look at it */
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

        /* Set the view panel for the initial application */
        setContentView(R.layout.list_layout);
        
        /* Open the db helper object*/
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        
        /* Perform the initial fill of data from the DB*/
        fillData();
        
        /* We have a Context menu. This is only needed due to the context 
         * based menu (for deleting a note) */
        registerForContextMenu(getListView());
    }

    /**
     * handles reading from the DB and placing the notes into the list pane
     */
    private void fillData() {
    	/* retrieves all the notes from the DB */
    	mNotesCursor = mDbHelper.fetchAllNotes();

        /* Create an array to specify the fields we want to display in the 
         * list (only TITLE) */
        String[] from = new String[]{NotesDbAdapter.KEY_BODY, 
        		NotesDbAdapter.KEY_DATETIME};

        /* and an array of the fields we want to bind those fields to (in 
         * this case just text1) */
        int[] to = new int[]{R.id.list_layout_row_preview, R.id.list_layout_row_date};

        /* Ensure that we are not running into a null object */
        if (mNotesCursor == null) {
        	Log.e("null", "notesCursor");
        	return;
        }
        
        /* Now create a simple cursor adapter and set it to display */
        SimpleCursorAdapter notes = 
            new SimpleCursorAdapter(this, R.layout.list_layout_row, mNotesCursor, from, to);
        
        /* This places the cursor data into the list (Note that we are in a 
         * List-based activity. */
        setListAdapter(notes);
        
        /* I attempted to keep the notesCursor here as a limited scope 
         * variable. I attempted to close it (close()) but if I did that, 
         * we ended up with an empty list. I don't quite understand this. */
        
    }

    /**
     * The options menu is the menu at the bottom of the screen when the menu 
     * button is pressed. This handles the opening of that menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);
        
        return true;
    }

    /**
     * When a menu item is selected from the Options Menu, this is called to 
     * handle the event.
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch(item.getItemId()) {
            case R.id.list_menu_insert:
                createNote();
                break;
            default:
                Log.e("jnotepad", "unhandled menu item:"+Integer.toHexString(item.getItemId())+
                		" is not "+Integer.toHexString(R.id.list_menu_insert)+" item string:'"+
                		item.getTitle()+"'");
                return super.onMenuItemSelected(featureId, item);
                	
        }
        return true;
    }

    /**
     * This is called when the user holds down on a item in the list. This 
     * creates the Context menu that appears at that moment.  
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
    	
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);
    }

    /**
     * This is the handler for the Context menu configured above
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	
        switch(item.getItemId()) {
            case R.id.list_context_menu_delete:
            	/* retrieve that current context information */
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                
                /* delete the note */
                mDbHelper.deleteNote(info.id);
                
                /* reload after the delete */
                fillData();
                break;
            default:
                Log.e("jnotepad", "unhandled context menu item:"+Integer.toHexString(item.getItemId())+
                		" is not "+Integer.toHexString(R.id.list_context_menu_delete)+" item string:'"+
                		item.getTitle()+"'");
                return super.onContextItemSelected(item);
        }
        
        return true;
    }

    /**
     * Kicks off the edit intent
     */
    private void createNote() {
        Intent i = new Intent(this, EditNote.class);
        
        /* We need a call back to update the list so we need the ForResult 
         * version */
        startActivityForResult(i, 0);
    }

    /**
     * Callback to handle the "click" on a object in the list.
     * 
     * Kicks off the edit intent with an extra value that indicates that this 
     * is an edit.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent i = new Intent(this, EditNote.class);
        
        /* this line indicates to the EditNote that this is an edit of an 
         * item in the DB */
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);

        /* We need a call back to update the list so we need the ForResult 
         * version */

        startActivityForResult(i, 0);
    }
    
    /**
     * Called when the activity that we launched (the EditNote) completed.
     * 
     * We just refill the list of notes.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        fillData();
    }

    /**
     * When we are ready to be finished off, we should try to do the right 
     * thing and clean up our objects.
     */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		mNotesCursor.close();
		mDbHelper.close();
	}
    
}
