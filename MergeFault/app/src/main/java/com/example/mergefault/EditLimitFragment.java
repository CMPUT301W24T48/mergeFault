package com.example.mergefault;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * This is a fragment class for adding limit
 */
public class EditLimitFragment extends DialogFragment {
    /**
     * this is a listener interface which the activity that calls this class implements
     */
    interface EditLimitDialogListener {
        /**
         * This is the abstract method addLimit
         * @param Limit
         * This is a Integer Limit
         */
        void addLimit(Integer Limit);
    }
    private EditLimitDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof EditLimitDialogListener) {
            listener = (EditLimitDialogListener) context;
        }
        else {
            throw new RuntimeException(context + "must implement EditLimitDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_add_number, null);
        EditText editLimit = view.findViewById(R.id.editNumberText);
        editLimit.setInputType(InputType.TYPE_CLASS_NUMBER);

        Bundle bundle = getArguments();
        if (!bundle.isEmpty()){
            String attendeeLimitString = getArguments().getString("attendeeLimit");
            editLimit.setText(attendeeLimitString);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder((getContext()));
        return builder
                .setView(view)
                .setTitle("Edit Limit")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editLimit.getText().toString().equals("")) {
                            Integer Limit = Integer.parseInt(editLimit.getText().toString());
                            listener.addLimit(Limit);
                        } else {
                            Toast.makeText(getContext(), "Invalid Limit", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .create();
    }
}
