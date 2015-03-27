// DetailsFragment.java
// Displays one contact's details
package com.deitel.movieapp;

import com.deitel.movieapp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailsFragment extends Fragment
{
   // callback methods implemented by MainActivity  
   public interface DetailsFragmentListener
   {
      // called when a contact is deleted
      public void onMovieDeleted();
      
      // called to pass Bundle of contact's info for editing
      public void onEditMovie(Bundle arguments);
   }
   
   private DetailsFragmentListener listener;
   
   private long rowID = -1; // selected contact's rowID
   private TextView titleEditText; // displays contact's name 
   private TextView directorEditText; // displays contact's phone
   private TextView writerEditText; // displays contact's email
   private TextView starsEditText; // displays contact's street
   private TextView yearEditText; // displays contact's city
   private TextView raitingEditText; // displays contact's state
   private TextView durationEditText; // displays contact's zip
   
   // set DetailsFragmentListener when fragment attached   
   @Override
   public void onAttach(Activity activity)
   {
      super.onAttach(activity);
      listener = (DetailsFragmentListener) activity;
   }
   
   // remove DetailsFragmentListener when fragment detached
   @Override
   public void onDetach()
   {
      super.onDetach();
      listener = null;
   }

   // called when DetailsFragmentListener's view needs to be created
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);  
      setRetainInstance(true); // save fragment across config changes

      // if DetailsFragment is being restored, get saved row ID
      if (savedInstanceState != null) 
         rowID = savedInstanceState.getLong(MainActivity.ROW_ID);
      else 
      {
         // get Bundle of arguments then extract the contact's row ID
         Bundle arguments = getArguments(); 
         
         if (arguments != null)
            rowID = arguments.getLong(MainActivity.ROW_ID);
      }
         
      // inflate DetailsFragment's layout
      View view = 
         inflater.inflate(R.layout.fragment_details, container, false);               
      setHasOptionsMenu(true); // this fragment has menu items to display

      // get the EditTexts
      titleEditText = (TextView) view.findViewById(R.id.titleTextView);
      directorEditText = (TextView) view.findViewById(R.id.directorTextView);
      writerEditText = (TextView) view.findViewById(R.id.writerTextView);
      starsEditText = (TextView) view.findViewById(R.id.starsTextView);
      yearEditText = (TextView) view.findViewById(R.id.yearTextView);
      raitingEditText = (TextView) view.findViewById(R.id.raitingTextView);
      durationEditText = (TextView) view.findViewById(R.id.durationTextView);
      return view;
   }
   
   // called when the DetailsFragment resumes
   @Override
   public void onResume()
   {
      super.onResume();
      new LoadMoviesTask().execute(rowID); // load contact at rowID
   } 

   // save currently displayed contact's row ID
   @Override
   public void onSaveInstanceState(Bundle outState) 
   {
       super.onSaveInstanceState(outState);
       outState.putLong(MainActivity.ROW_ID, rowID);
   }

   // display this fragment's menu items
   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.fragment_details_menu, menu);
   }

   // handle menu item selections
   @Override
   public boolean onOptionsItemSelected(MenuItem item) 
   {
      switch (item.getItemId())
      {
         case R.id.action_edit: 
            // create Bundle containing contact data to edit
            Bundle arguments = new Bundle();
            arguments.putLong(MainActivity.ROW_ID, rowID);
            arguments.putCharSequence("title", titleEditText.getText());
             arguments.putCharSequence("director", directorEditText.getText());
               arguments.putCharSequence("writer", writerEditText.getText());
                arguments.putCharSequence("stars", starsEditText.getText());
            arguments.putCharSequence("year", yearEditText.getText());
            arguments.putCharSequence("raiting", raitingEditText.getText());
            arguments.putCharSequence("duration", durationEditText.getText());           
            listener.onEditMovie(arguments); // pass Bundle to listener
            return true;
         case R.id.action_delete:
        	 deleteMovie();
            return true;
      }
      
      return super.onOptionsItemSelected(item);
   } 
   
   // performs database query outside GUI thread
   private class LoadMoviesTask extends AsyncTask<Long, Object, Cursor> 
   {
      DatabaseConnector databaseConnector = 
         new DatabaseConnector(getActivity());

      // open database & get Cursor representing specified contact's data
      @Override
      protected Cursor doInBackground(Long... params)
      {
         databaseConnector.open();
         return databaseConnector.getOneContact(params[0]);
      } 

      // use the Cursor returned from the doInBackground method
      @Override
      protected void onPostExecute(Cursor result)
      {
         super.onPostExecute(result);
         result.moveToFirst(); // move to the first item 
   
         // get the column index for each data item
         int titleIndex = result.getColumnIndex("title");
         int directorIndex = result.getColumnIndex("director");
         int writerIndex = result.getColumnIndex("writer");
         int starsIndex = result.getColumnIndex("stars");
         int yearIndex = result.getColumnIndex("year");
         int raitingIndex = result.getColumnIndex("raiting");
         int durationIndex = result.getColumnIndex("duration");
   
         // fill TextViews with the retrieved data
         titleEditText.setText(result.getString(titleIndex));
        directorEditText.setText(result.getString(directorIndex));
       writerEditText.setText(result.getString(writerIndex));
          starsEditText.setText(result.getString(starsIndex));
         yearEditText.setText(result.getString(yearIndex));
         raitingEditText.setText(result.getString(raitingIndex));
         durationEditText.setText(result.getString(durationIndex)); 
   
         result.close(); // close the result cursor
         databaseConnector.close(); // close database connection
      } // end method onPostExecute
   } // end class LoadMoviesTask

   // delete a contact
   private void deleteMovie()
   {         
      // use FragmentManager to display the confirmDelete DialogFragment
      confirmDelete.show(getFragmentManager(), "confirm delete");
   } 

   // DialogFragment to confirm deletion of contact
   private DialogFragment confirmDelete = 
      new DialogFragment()
      {
         // create an AlertDialog and return it
         @Override
         public Dialog onCreateDialog(Bundle bundle)
         {
            // create a new AlertDialog Builder
            AlertDialog.Builder builder = 
               new AlertDialog.Builder(getActivity());
      
            builder.setTitle(R.string.confirm_title); 
            builder.setMessage(R.string.confirm_message);
      
            // provide an OK button that simply dismisses the dialog
            builder.setPositiveButton(R.string.button_delete,
               new DialogInterface.OnClickListener()
               {
                  @Override
                  public void onClick(
                     DialogInterface dialog, int button)
                  {
                     final DatabaseConnector databaseConnector = 
                        new DatabaseConnector(getActivity());
      
                     // AsyncTask deletes contact and notifies listener
                     AsyncTask<Long, Object, Object> deleteTask =
                        new AsyncTask<Long, Object, Object>()
                        {
                           @Override
                           protected Object doInBackground(Long... params)
                           {
                              databaseConnector.deleteMovie(params[0]); 
                              return null;
                           } 
      
                           @Override
                           protected void onPostExecute(Object result)
                           {                                 
                              listener.onMovieDeleted();
                           }
                        }; // end new AsyncTask
      
                     // execute the AsyncTask to delete contact at rowID
                     deleteTask.execute(new Long[] { rowID });               
                  } // end method onClick
               } // end anonymous inner class
            ); // end call to method setPositiveButton
            
            builder.setNegativeButton(R.string.button_cancel, null);
            return builder.create(); // return the AlertDialog
         }
      }; // end DialogFragment anonymous inner class
} // end class DetailsFragment


