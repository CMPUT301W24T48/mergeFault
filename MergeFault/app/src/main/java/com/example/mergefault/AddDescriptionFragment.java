package com.example.mergefault;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
/**
 * This is a fragment class for adding description
 */
public class AddDescriptionFragment extends DialogFragment {
    /**
     * this is a listener interface which the activity that calls this class implements
     */
    interface AddDescriptionDialogListener {
        /**
         * This is the abstract method addDescription
         * @param Description
         * This is a String Description
         */
        void addDescription(String Description);
    }
    private AddDescriptionDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AddDescriptionDialogListener) {
            listener = (AddDescriptionDialogListener) context;
        }
        else {
            throw new RuntimeException(context + "must implement AddDescriptionDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_add_text,null);
        EditText editDescription = view.findViewById(R.id.editTextBox);
        CharSequence hint = "Add Description";
        editDescription.setHint(hint);
        AlertDialog.Builder builder = new AlertDialog.Builder((getContext()));
        return builder
                .setView(view)
                .setTitle("Add a Description")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editDescription.getText().toString().equals("")) {
                            String description = editDescription.getText().toString();
                            listener.addDescription(description);
                        } else {
                            Toast.makeText(getContext(), "Invalid Description", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .create();
    }
}
