package com.example.mergefault;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
/**
 * This is a fragment class for adding description
 */
public class EditDescriptionFragment extends DialogFragment {
    /**
     * this is a listener interface which the activity that calls this class implements
     */
    interface EditDescriptionDialogListener {
        /**
         * This is the abstract method addDescription
         * @param Description
         * This is a String Description
         */
        void addDescription(String Description);
    }
    private EditDescriptionDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof EditDescriptionDialogListener) {
            listener = (EditDescriptionDialogListener) context;
        }
        else {
            throw new RuntimeException(context + "must implement EditDescriptionDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String description = getArguments().getString("description");
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_add_text,null);
        EditText editDescription = view.findViewById(R.id.editTextBox);
        editDescription.setText(description);
        AlertDialog.Builder builder = new AlertDialog.Builder((getContext()));
        return builder
                .setView(view)
                .setTitle("Edit Description")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String description = editDescription.getText().toString();
                        listener.addDescription(description);
                    }
                })
                .create();
    }
}
