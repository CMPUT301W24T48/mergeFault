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

public class AddLimitFragment extends DialogFragment {
    interface AddLimitDialogListener {
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
