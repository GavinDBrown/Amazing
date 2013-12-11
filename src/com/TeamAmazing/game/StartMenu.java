package com.TeamAmazing.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StartMenu extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_menu);
	}

	public void start(View v) {
		Intent intent = new Intent(this, MazeGame.class);
		startActivity(intent);
	}

}
