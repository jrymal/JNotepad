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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import com.android.demo.jnotepad.R;
import java.lang.Thread.UncaughtExceptionHandler;

public class JNotepad extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private final static String LogName = "@string/app_name";
    
    private NotesDbAdapter mDbHelper;
    private Cursor notesCursor;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        Thread.setDefaultUncaughtExceptionHandler(new myUncaught());
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.notes_list);
        
        
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        
        fillData();
        
        registerForContextMenu(getListView());
    }

    private void fillData() {
    	notesCursor = mDbHelper.fetchAllNotes();

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{NotesDbAdapter.KEY_BODY, NotesDbAdapter.KEY_DATETIME};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.preview, R.id.date};

        if (notesCursor == null) {
        	Log.e("null", "notesCursor");
        	return;
        }
        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = 
            new SimpleCursorAdapter(this, R.layout.notes_row, notesCursor, from, to);
        
        setListAdapter(notes);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
                createNote();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createNote() {
        Intent i = new Intent(this, EditNote.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent i = new Intent(this, EditNote.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);

        startActivityForResult(i, ACTIVITY_EDIT);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        fillData();
    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		notesCursor.close();
		mDbHelper.close();
	}
    

	private static class myUncaught implements UncaughtExceptionHandler{

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

        private void logStackTraceArray(StackTraceElement[] stackTrace, StringBuffer msg) {
            for(int i=0; i < stackTrace.length; i++) {
                resetStringBuffer(msg);

                msg.append(i);
                msg.append(':');
                msg.append(stackTrace[i].toString());
                Log.e(LogName, msg.toString());
            }

        }

        private void resetStringBuffer(StringBuffer msg) {
            msg.delete(0, msg.length());
            msg.trimToSize();
        }
        
    }

	
	
}
