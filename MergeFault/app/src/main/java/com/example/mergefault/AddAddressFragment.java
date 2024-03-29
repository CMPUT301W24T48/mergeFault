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
 * This is a fragment class for adding address
 */
public class AddAddressFragment extends DialogFragment {
    /**
     * this is a listener interface which the activity that calls this class implements
     */
    interface AddAddressDialogListener {
        /**
         * This is the abstract method addAddress
         * @param Address
         * This is a String Address
         */
        void addAddress(String Address);
    }
    private AddAddressDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AddAddressDialogListener) {
            listener = (AddAddressDialogListener) context;
        }
        else {
            throw new RuntimeException(context + "must implement AddAddressDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_add_text,null);
        EditText editAddress = view.findViewById(R.id.editTextBox);
        CharSequence hint = "Add Address";
        editAddress.setHint(hint);
        AlertDialog.Builder builder = new AlertDialog.Builder((getContext()));
        return builder
                .setView(view)
                .setTitle("Add a Address")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String address = editAddress.getText().toString();
                        listener.addAddress(address);
                    }
                })
                .create();
    }
}
