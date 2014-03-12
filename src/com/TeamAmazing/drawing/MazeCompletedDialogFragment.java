package com.TeamAmazing.drawing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class MazeCompletedDialogFragment extends DialogFragment {
	OnDialogClosedListener mCallback;

	// Interface the container activity must implement
	public interface OnDialogClosedListener {
		public void onDialogClosed();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnDialogClosedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnDialogClosedListener");
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		mCallback.onDialogClosed();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		int time = args.getInt("time");
		String message = mMessageFormater(time);

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
				AlertDialog.THEME_HOLO_DARK);
		builder.setMessage(message);
		// Create the AlertDialog object and return it
		return builder.create();
	}

	@SuppressLint("DefaultLocale")
	private String mMessageFormater(int time) {
		int millis = (time % 1000)/100; 
		int second = (time / 1000) % 60;
		int minute = (time / (1000 * 60)) % 60;
		int hour = (time / (1000 * 60 * 60)) % 24;
		String message;

		if (hour > 0) {
			message = "Nice, you made it to the end. See how fast you can do it next time.";
		} else if (minute > 1) {
			message = String
					.format("Good job, you completed the maze in %d minutes and %d.%d seconds.",
							minute, second, millis);
		} else if (minute > 0) {
			message = String
					.format("Good job, you completed the maze in 1 minute and %d.%d seconds.",
							second, millis);
		} else {
			message = String
					.format("Congratulations, you completed the maze in %d.%d seconds!",
							second, millis);
		}

		return message;
	}

}
