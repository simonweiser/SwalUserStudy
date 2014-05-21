package com.example.swaluserstudy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class MainActivity extends Activity implements OnKeyListener, TextWatcher {
	/*
	 * Layout komponenten
	 */
	TextView textToEnter;
	EditText textEntered;
	String userText;
	/*
	 * variablen zum messen der benötigten Zeit
	 */
	long timestampStart;
	long timestampEnd;
	long duration;

	// firstKeystroke?
	boolean firstKeystroke = true;

	int counter;// counter ist zeigt an welcher buchstabe aktuell getippt werden
				// soll

	int lastSpace = 0;// an welcher position befindet sich das letzte
						// lererzeichen, dient dazu wörter zu markieren
	int nextSpace = 0;// an welcher position befindet sich das nächste
						// lererzeichen, dient dazu wörter zu markieren

	String studyText; // Variable speichert den text an dem die testperson
						// gemessen wird

	int selected = 0; // ausgewählter index des textes
	int selectedKeyboard = 0;// ausgewählter index des eyboards
	int id = 99; // standard id
	String keyboard = ""; // Name des ausgewäääählätenä keyboäards
	String[] fileList; // array der mölichen use case texte
	String[] keyboards; // array der möglichen keyboard layouts im moment DVORA
						// und NEO

	static final int DEFAULT_ID = 99;
	static final String DEFAULT_KEYBOARD = "None";

	SharedPreferences sharedPref;
	SharedPreferences.Editor editor;
	FileWriter fw;
	BufferedWriter bw;

	private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMM yyyy  HH:mm:ss.SSS", Locale.US);

	String FILENAME;
	String COLUMN_NAMES = "ID;Keyboard;Timestamp;Duration;Accuracy;Mistakes;TextToEnter;EnteredText\n";

	public MainActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		AssetManager manager = getAssets();
		try {
			fileList = manager.list("UseCaseTexts");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		keyboards = getResources().getStringArray(R.array.keyboards);

		sharedPref = getPreferences(Context.MODE_PRIVATE);
		editor = sharedPref.edit();

		int sp_id = sharedPref.getInt("ID", DEFAULT_ID);
		Log.i("SP", Integer.toString(sp_id));

		String sp_keyboard = sharedPref.getString("Keyboard", DEFAULT_KEYBOARD);
		Log.i("SP", sp_keyboard);

		if (sp_id == DEFAULT_ID && sp_keyboard.equals(DEFAULT_KEYBOARD)) {
			buildKeyboardDialog();
			buildIdDialog();
		} else if (sp_keyboard.equals("QWERTZ")) {
			id = sp_id;
			buildKeyboardDialog();
		} else {
			id = sp_id;
			keyboard = sp_keyboard;
		}

		buildStartDialog();

		textToEnter = (TextView) findViewById(R.id.textToEnter);

		textEntered = (EditText) findViewById(R.id.textEntered);
		textToEnter.setMovementMethod(new ScrollingMovementMethod());// erlaubt
																		// das
																		// scrollen
																		// der
																		// textview

		textEntered.setOnKeyListener(this);

		textEntered.addTextChangedListener(this);

	}

	/*
	 * nachdem die Dialoge ausgefüllt wurden wird der usecase text initialisiert
	 */
	private void initialize() {
		lastSpace = 0;
		nextSpace = findNextSpace(studyText, 0);

		SpannableString text = new SpannableString(studyText);
		// make text (characters 0 to counter) red

		text.setSpan(new UnderlineSpan(), 0, 1, 0); // unterstreiche
													// aktiven
													// Buchstabben
													// zur
													// Orientierung

		text.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), lastSpace, nextSpace, 0);// Färbe
																								// zu
																								// schreibendes
																								// Wort
																								// FETT
		// text.setSpan(new ForegroundColorSpan(Color.GREEN), lastSpace,
		// nextSpace, 0);
		textToEnter.setText(text, BufferType.SPANNABLE);

	}

	/*
	 * ermittelt ob der Text vollständig eingegeben wurde
	 */
	protected boolean isLastKey(int textLength, int inputLength) {
		if (textLength == inputLength)
			return true;
		return false;
	}

	/*
	 * Funktion läd text datei aus dem assetts ordner; hier kommen unsere
	 * Beispieltexte rein
	 */

	private String loadText(String filename) throws Exception {
		AssetManager assetManager = getAssets();
		InputStream input;

		input = assetManager.open("UseCaseTexts/" + filename);

		int size = input.available();
		byte[] buffer = new byte[size];

		input.read(buffer);
		input.close();

		String result = new String(buffer);
		return result;

	}

	// speichern von: -text der eingegeben wurde - zeit - fehlerrate

	// blockieren des delete keys
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		switch (keyCode) {

		case KeyEvent.KEYCODE_DEL:

			return true;
		default:
			break;
		}

		return false;

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// TODO Auto-generated method stub

	}

	// funktion findet immer nächstes leerzeichen um dadurch ein woärt äzu
	// markiäeren
	public int findNextSpace(String string, int start) {

		for (int i = start; i < string.length(); i++) {
			if (string.charAt(i) == ' ') {
				return i;

			}

		}

		return string.length() - 1;
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// if (textToEnter.length() <= textEntered.getText().length()) {
		// Log.i("text", textToEnter.getText().charAt(counter) + "");
		Log.i("textLength", textToEnter.getText().length() + ";" + textEntered.getText().length());
		if (nextSpace == counter)
			lastSpace = counter; // sobald nächstes wort erreicht ist setze
									// lastSpace auf das alte nextSpace
		nextSpace = findNextSpace(studyText, counter + 1);// ermittle neues
															// nextSpace
		Log.i("nextSpace", nextSpace + "");
		// SpannableString text = new
		// SpannableString(getResources().getString(R.string.source_text));
		SpannableString text = new SpannableString(studyText);
		// make text (characters 0 to counter) red
		if (!isLastKey(textToEnter.length(), textEntered.getText().length())) {
			text.setSpan(new UnderlineSpan(), counter + 1, counter + 2, 0); // unterstreiche
																			// aktiven
																			// Buchstabben
																			// zur
																			// Orientierung
			text.setSpan(new ForegroundColorSpan(Color.GRAY), 0, counter + 1, 0);// Färbe
																					// bereits
																					// geschriebene
																					// buchstaben
																					// grau
			text.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), lastSpace, nextSpace, 0);// Färbe
																									// zu
																									// schreibendes
																									// Wort
																									// FETT
			// text.setSpan(new ForegroundColorSpan(Color.GREEN), lastSpace,
			// nextSpace, 0);
			textToEnter.setText(text, BufferType.SPANNABLE); // Formatierungen
																// übernehmen
			counter++;
		}
		// }

		// TimerStart beim ersten Tastendruck
		if (firstKeystroke) {
			timestampStart = System.currentTimeMillis();
			Log.i("timestampstart", timestampStart + "");
			firstKeystroke = false;

		}

		// sobald letzter keystroke getätigt wurde startet der EndDialog mit den
		// Ergebnissen
		if (isLastKey(textToEnter.length(), textEntered.getText().length())) {
			timestampEnd = System.currentTimeMillis();
			Log.i("timestampend", timestampEnd + "");
			duration = timestampEnd - timestampStart;
			double accuracy = compareStrings(textEntered.getText().toString(), textToEnter.getText().toString());
			int mistakes = calculateMistakes(textEntered.getText().toString(), textToEnter.getText().toString());

			// in CSV datei schreiben
			try {
				FILENAME = "user_" + id + ".csv";

				/** für normales device */
				String root = Environment.getExternalStorageDirectory().toString();
				File path = new File(root + "/swal_study");

				/** für emulator (code für device auskommentieren) */
				// File path = new
				// File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
				// + "/swal_study");

				path.mkdirs();
				File file = new File(path, FILENAME);
				fw = new FileWriter(file, true); // true to append to existing
													// file
				bw = new BufferedWriter(fw);
				bw.append(COLUMN_NAMES);
				// java.util.Date time=new java.util.Date((long)timestampStart);

				String time = DATE_FORMAT.format(new Date(timestampStart));

				String stringToWrite = id + ";" + keyboard + ";" + time + ";" + duration + ";" + accuracy + ";" + mistakes + ";" + fileList[selected] + ";" + textEntered.getText().toString() + "\n";

				bw.append(stringToWrite);
				bw.close();
				fw.close();
				MediaScannerHelp mediaScannerHelp = new MediaScannerHelp(this);
				mediaScannerHelp.addFile(file.getAbsolutePath());
			} catch (Exception e) {
				Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			}

			buildEndDialog(id, duration, accuracy);

			textEntered.setFilters(new InputFilter[] { new InputFilter() {
				public CharSequence filter(CharSequence src, int start, int end, Spanned dst, int dstart, int dend) {
					return src.length() < 1 ? dst.subSequence(dstart, dend) : "";
				}
			} });

			Log.i("time", duration / 1000.0 + " Sek. von Marko");
			Log.i("time", duration + " Sek. von Marko");
		}

	}

	/*
	 * Erstellen der verschiednenen Dialoge für den Anfang und das Ende
	 */

	/*
	 * Am anfang kann man die ID, die gewählte Tastatur eingeben und einene
	 * Beispieltext auswählen, am Ende wird das Ergebnis angezeigt. mit klick
	 * auf ok startet die activity neu
	 */

	/*
	 * Dialog zum eingeben der ID der Testperson
	 */
	public void buildIdDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("ID");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		input.setText("0");
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				id = Integer.parseInt(input.getEditableText().toString());
				editor.putInt("ID", id);
				editor.commit();
			}
		});
		alert.setCancelable(false);
		alert.show();

	}

	/*
	 * Dialog zum auswählen des benutzten keyboards gespeichert sind die
	 * möglichen Keyboards in res-> values-> keyboards als String array
	 */
	public void buildKeyboardDialog() {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Keyboard");

		// singlechoice item zum anzeigen der möglichen keyboards in einer Liste
		alert.setSingleChoiceItems(keyboards, 0, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				selectedKeyboard = which;
			}
		});
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				keyboard = keyboards[selectedKeyboard];
				editor.putString("Keyboard", keyboard);
				editor.commit();
				dialog.dismiss();
				// Do something with value!
			}
		});

		alert.setCancelable(false);
		alert.show();

	}

	/*
	 * Dialog zum anzeigen der Beispieltexte im arodner assets-> UseCaseTexts Im
	 * moment gibt es 3 verschiedne text3 ist sehr kurz text2 ist relativ kurz
	 * und text1 ist extrem lang
	 */
	public void buildStartDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setSingleChoiceItems(fileList, 0, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				selected = which;
			}
		});
		builder.setPositiveButton("OK", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					studyText = loadText(fileList[selected]);
					textToEnter.setText(studyText);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				initialize();
				dialog.cancel();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	/*
	 * Dialog zum anzeigen der ergebnisse Activity startet neu nachdem auf
	 * restart gedrückt wird; ergebnisse werden verworfen
	 */
	public void buildEndDialog(int id, long duration, double mistakes) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();

			}
		});

		// DecimalFormat df = new DecimalFormat(",##0.00");
		// String formated = df.format(mistakes);
		// builder.setMessage("ID:" + id + ";" + "Time: " + duration +
		// "; Accuracy: " + formated + "; Keyboard: " + keyboard);
		builder.setMessage("You're done!");
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub

	}

	/*
	 * Activity neustart am ende
	 */
	public void restartActivity() {
		Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);

	}

	/*
	 * Berechnung der Levensthein Distanz Nutzt die Simmetrics library im Ordner
	 * libs
	 */
	public double compareStrings(String a, String b) {
		Levenshtein levensthein = new Levenshtein();
		return levensthein.getSimilarity(a, b);

	}

	public int calculateMistakes(String a, String b) {

		Double accuracy = compareStrings(a, b);
		Double calcMistakes = textToEnter.getText().length() - (accuracy * textToEnter.getText().length());

		return calcMistakes.intValue();
	}

}