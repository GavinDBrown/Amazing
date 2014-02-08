package com.TeamAmazing.drawing;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class MazeCompletedDialogFragment extends DialogFragment {


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
				AlertDialog.THEME_HOLO_DARK);
		builder.setMessage("Congratulations!").setPositiveButton("OK", null);
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int id) {
		//
		// }
		// });
		// Create the AlertDialog object and return it
		return builder.create();
	}

}
