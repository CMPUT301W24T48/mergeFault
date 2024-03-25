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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
/**
 * This is a fragment class for adding limit
 */
public class AddLimitFragment extends DialogFragment {
    /**
     * this is a listener interface which the activity that calls this class implements
     */
    interface AddLimitDialogListener {
        /**
         * This is the abstract method addLimit
         * @param Limit
         * This is a Integer Limit
         */
        void addLimit(Integer Limit);
    }
    private AddLimitDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AddLimitDialogListener) {
            listener = (AddLimitDialogListener) context;
        }
        else {
            throw new RuntimeException(context + "must implement AddLimitDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_add_number, null);
        EditText editLimit = view.findViewById(R.id.editNumberText);
        editLimit.setInputType(InputType.TYPE_CLASS_NUMBER);
        CharSequence hint = "Add Limit";
        editLimit.setHint(hint);
        AlertDialog.Builder builder = new AlertDialog.Builder((getContext()));
        return builder
                .setView(view)
                .setTitle("Add a Limit")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Integer Limit = Integer.parseInt(editLimit.getText().toString());
                        listener.addLimit(Limit);
                    }
                })
                .create();
    }
}
