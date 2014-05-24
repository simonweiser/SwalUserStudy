package com.example.swaluserstudy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class TextTimeStampSaver {
	private Context context;
	private String text;
	private String csv_line_timestamp = "";
	private String csv_line_characters = "";
	private int id;
	private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMM yyyy  HH:mm:ss.SSS", Locale.US);
	private String FILENAME;
	private int textID;
	private FileWriter fw;
	private BufferedWriter bw;
	private int counter = 1;
	private String CHARACTERS_INPUT = "";
	private String csv_line_unixTime = "";
	private String csv_line_difference = "";
	private long lastTimeStamp;
	private boolean firstTimeStamp = true;
	private File file;

	public TextTimeStampSaver(Context context, String text, int id, int textID) {
		this.context = context;
		this.text = text;
		this.id = id;
		this.textID = textID;
	}

	public void setID(int id) {
		this.id = id;
	}

	private void openFile() {
		String root = Environment.getExternalStorageDirectory().toString();
		File path = new File(root + "/swal_study");
		path.mkdirs();
		FILENAME = id + "_textID_" + textID + ".csv";
		file = new File(path, FILENAME);
		try {
			fw = new FileWriter(file, true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // true to append to existing
			// file
		bw = new BufferedWriter(fw);
		String CHARACTERS = "";
		String COLUMN_NAMES = "";

		for (int i = 0; i < text.length(); i++) {
			String separator = ";";

			if (i == text.length() - 1)
				separator = "\n";

			COLUMN_NAMES = COLUMN_NAMES + i + separator;
			CHARACTERS = CHARACTERS + text.charAt(i) + separator;
		}

		try {
			bw.append(COLUMN_NAMES);
			bw.append(CHARACTERS);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void addCharacter(String character) {
		String separator = ";";
		long unixTime = System.currentTimeMillis();
		String time = DATE_FORMAT.format(new Date(unixTime));
		long difference;

		if (firstTimeStamp) {
			difference = 0;
			firstTimeStamp = false;
		} else {
			difference = unixTime - lastTimeStamp;
		}

		lastTimeStamp = unixTime;

		Log.i("counter", Integer.toString(counter));
		Log.i("textl", Integer.toString(text.length()));

		if (counter == text.length())
			separator = "\n";

		csv_line_timestamp = csv_line_timestamp + time + separator;
		csv_line_characters = csv_line_characters + character + separator;
		csv_line_unixTime = csv_line_unixTime + unixTime + separator;
		csv_line_difference = csv_line_difference + difference + separator;
		counter++;

	}

	public void addUserInput(String input) {
		for (int i = 0; i < text.length(); i++) {
			String separator = ";";

			if (i == text.length() - 1)
				separator = "\n";

			CHARACTERS_INPUT = CHARACTERS_INPUT + input.charAt(i) + separator;
		}
		csv_line_characters = CHARACTERS_INPUT;
	}

	public void writeCSVLineToFile() {
		openFile();

		try {

			bw.append(csv_line_characters);
			bw.append(csv_line_timestamp);
			bw.append(csv_line_unixTime);
			bw.append(csv_line_difference);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MediaScannerHelp mediaScannerHelpOverviewFile = new MediaScannerHelp(context);
		mediaScannerHelpOverviewFile.addFile(file.getAbsolutePath());
	}
}
