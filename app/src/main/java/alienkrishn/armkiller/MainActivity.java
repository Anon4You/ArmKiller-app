package alienkrishn.armkiller;

import android.Manifest;
import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.database.Cursor;
import android.content.pm.PackageManager;
import android.content.res.*;
import android.graphics.*;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.*;
import org.json.*;
import com.apk.axml.*;
import java.io.*;
import android.util.*;
import org.xml.sax.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import com.android.apksig.apk.ApkFormatException;
import java.security.GeneralSecurityException;
import java.io.IOException;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Enumeration;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.provider.Settings;
import android.content.pm.ApplicationInfo;

public class MainActivity extends Activity {

	private Timer _timer = new Timer();

	private String apkpath = "";
	private String outapkpath = "";
	private String axmltext = "";
	private String input = "";
	private String output = "";
	private String value = "";

	private LinearLayout linear1;
	private LinearLayout linear10;
	private LinearLayout headerLayout;
	private LinearLayout cardPicker;
	private LinearLayout cardOptions;
	private LinearLayout cardLog;
	private LinearLayout pickerLayout;
	private Button button1;
	private Button btn_pick_apk;
	private TextView tv_log;
	private TextView tv_apk_path;
	private ImageView imageview1;
	private LinearLayout linear11;
	private TextView textview4;
	private TextView textview5;
	private TextView textview6;
	private TextView textview7;
	private TextView textview8;
	private RadioButton radiobutton1;
	private RadioButton radiobutton2;
	private RadioGroup radioGroup;
	private ScrollView scrollView1;

	private TimerTask t;

	private static final int PICK_APK_REQUEST = 1001;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		initialize(_savedInstanceState);

		if (Build.VERSION.SDK_INT >= 23) {
			if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
			||checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
				requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
			} else {
				initializeLogic();
			}
		} else {
			initializeLogic();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PICK_APK_REQUEST && resultCode == RESULT_OK && data != null) {
			Uri apkUri = data.getData();
			if (apkUri != null) {
				String path = getRealPathFromUri(apkUri);
				if (path != null) {
					apkpath = path;
					tv_apk_path.setText(path);
					loadApkInfo(path);
					log("APK selected: " + path);
				}
			}
		}
	}

	private String getRealPathFromUri(Uri uri) {
		String path = null;

		// Handle file:// URIs directly
		if ("file".equals(uri.getScheme())) {
			return uri.getPath();
		}

		// Handle content:// URIs - try to get real path
		if ("content".equals(uri.getScheme())) {
			// Try MediaStore DATA column first (works for some file managers)
			Cursor cursor = getContentResolver().query(uri, new String[] { android.provider.MediaStore.Files.FileColumns.DATA }, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					int columnIndex = cursor.getColumnIndex(android.provider.MediaStore.Files.FileColumns.DATA);
					if (columnIndex != -1) {
						path = cursor.getString(columnIndex);
						if (path != null && !path.isEmpty()) {
							cursor.close();
							return path;
						}
					}
				}
				cursor.close();
			}

			// For Document URIs, try to extract the document ID and construct path
			if (uri.getAuthority().equals("com.android.externalstorage.documents") ||
				uri.getAuthority().equals("com.android.providers.media.documents")) {
				String docId = android.provider.DocumentsContract.getDocumentId(uri);
				if (docId != null && docId.contains(":")) {
					String[] parts = docId.split(":");
					String type = parts[0];
					String name = parts[1];

					if ("primary".equalsIgnoreCase(type)) {
						path = "/storage/emulated/0/" + name;
						if (new java.io.File(path).exists()) {
							return path;
						}
					} else {
						// Try /storage/{type}/
						path = "/storage/" + type + "/" + name;
						if (new java.io.File(path).exists()) {
							return path;
						}
					}
				}
			}

			// Try to get display name and search for it
			String displayName = getFileNameFromUri(uri);
			if (displayName != null) {
				// Search in common locations
				String[] searchPaths = {
					"/storage/emulated/0/",
					"/storage/emulated/0/Download/",
					"/storage/emulated/0/MT2/apks/",
					"/sdcard/",
					"/sdcard/Download/",
					"/sdcard/MT2/apks/"
				};

				for (String searchPath : searchPaths) {
					java.io.File file = new java.io.File(searchPath + displayName);
					if (file.exists()) {
						return file.getAbsolutePath();
					}
				}
			}
		}

		// Last resort - return URI path (may not work)
		return uri.getPath();
	}

	private String getFileNameFromUri(Uri uri) {
		String fileName = null;
		Cursor cursor = getContentResolver().query(uri, new String[] { android.provider.OpenableColumns.DISPLAY_NAME }, null, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int columnIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
				if (columnIndex != -1) {
					fileName = cursor.getString(columnIndex);
				}
			}
			cursor.close();
		}
		return fileName;
	}

	private void loadApkInfo(String apkPath) {
		try {
			PackageManager pm = getPackageManager();
			PackageInfo pi = pm.getPackageArchiveInfo(apkPath, 0);

			if (pi != null) {
				ApplicationInfo ai = pi.applicationInfo;
				ai.sourceDir = apkPath;
				ai.publicSourceDir = apkPath;

				Drawable icon = pm.getApplicationIcon(ai);
				imageview1.setImageDrawable(icon);

				String packageName = pi.packageName;
				textview5.setText(packageName);

				String appName = pm.getApplicationLabel(ai).toString();
				textview4.setText(appName);
			}
		} catch (Exception e) {
			textview4.setText("Unknown App");
			textview5.setText("Error loading info");
		}
	}

	private void initialize(Bundle _savedInstanceState) {
		linear1 = findViewById(R.id.linear1);
		headerLayout = findViewById(R.id.headerLayout);
		cardPicker = findViewById(R.id.cardPicker);
		cardOptions = findViewById(R.id.cardOptions);
		cardLog = findViewById(R.id.cardLog);
		pickerLayout = findViewById(R.id.pickerLayout);
		btn_pick_apk = findViewById(R.id.btn_pick_apk);
		tv_apk_path = findViewById(R.id.tv_apk_path);
		radiobutton1 = findViewById(R.id.radiobutton1);
		radiobutton2 = findViewById(R.id.radiobutton2);
		radioGroup = findViewById(R.id.radioGroup);
		button1 = findViewById(R.id.button1);
		tv_log = findViewById(R.id.tv_log);
		imageview1 = findViewById(R.id.imageview1);
		linear11 = findViewById(R.id.linear11);
		textview4 = findViewById(R.id.textview4);
		textview5 = findViewById(R.id.textview5);
		textview6 = findViewById(R.id.textview6);
		textview7 = findViewById(R.id.textview7);
		textview8 = findViewById(R.id.textview8);
		scrollView1 = findViewById(R.id.scrollView1);

		btn_pick_apk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				openFilePicker();
			}
		});

		radiobutton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2) {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					radiobutton2.setChecked(false);
				}
			}
		});

		radiobutton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2) {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					radiobutton1.setChecked(false);
				}
			}
		});

		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (apkpath == null || apkpath.isEmpty()) {
					log("Error: Please select an APK file first");
					Toast.makeText(getApplicationContext(), "Please select an APK file", Toast.LENGTH_SHORT).show();
					return;
				}

				outapkpath = apkpath.replace(".apk", ".kill.apk");
				log("Output path: " + outapkpath);

				if (radiobutton1.isChecked()) {
					processWithConfigSo();
				} else {
					processWithoutConfigSo();
				}
			}
		});

		tv_log.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View _view) {
				return true;
			}
		});
	}

	private void openFilePicker() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("application/vnd.android.package-archive");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			startActivityForResult(Intent.createChooser(intent, "Select APK"), PICK_APK_REQUEST);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Please install a file manager", Toast.LENGTH_SHORT).show();
		}
	}

	private void log(final String message) {
		runOnUiThread(() -> {
			String currentLog = tv_log.getText().toString();
			if (!"Ready to process...".equals(currentLog)) {
				tv_log.setText(currentLog + "\n" + message);
			} else {
				tv_log.setText(message);
			}
			scrollView1.post(() -> scrollView1.fullScroll(View.FOCUS_DOWN));
		});
	}

	private void clearLog() {
		runOnUiThread(() -> tv_log.setText(""));
	}

	private void processWithConfigSo() {
		clearLog();
		log("Starting ARM Killer with Config.so...");

		Handler handler = new Handler(Looper.getMainLooper());

		new Thread(() -> {
			try {
				handler.post(() -> {
					_Show(true, 5, "Preparing...");
					log("Preparing...");
				});

				String apkPath = apkpath;
				java.io.File tempDir = getFilesDir();
				String unsignedApk = new java.io.File(tempDir, "unsigned.apk").getAbsolutePath();
				String finalApk = outapkpath;

				byte[] buffer = new byte[4096];
				int len;

				handler.post(() -> {
					_Show(true, 10, "Extracting DEX and Assets");
					log("Extracting DEX and Assets...");
				});
				java.util.zip.ZipFile zip = new java.util.zip.ZipFile(apkPath);
				java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zip.entries();
				java.util.ArrayList<java.io.File> dexFiles = new java.util.ArrayList<>();
				java.io.File manifestFile = new java.io.File(tempDir, "AndroidManifest.xml");
				java.io.File configFile = new java.io.File(tempDir, "config.so");

				while (entries.hasMoreElements()) {
					java.util.zip.ZipEntry entry = entries.nextElement();
					String name = entry.getName();
					if (entry.isDirectory()) continue;

					if (name.matches("assets/classes(\\d*)?\\.dex")) {
						java.io.File outFile = new java.io.File(tempDir, name.substring(name.lastIndexOf("/") + 1));
						java.io.InputStream is = zip.getInputStream(entry);
						java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile);
						while ((len = is.read(buffer)) != -1) fos.write(buffer, 0, len);
						is.close(); fos.close();
						dexFiles.add(outFile);
					} else if (name.equals("AndroidManifest.xml")) {
						java.io.InputStream is = zip.getInputStream(entry);
						java.io.FileOutputStream fos = new java.io.FileOutputStream(manifestFile);
						while ((len = is.read(buffer)) != -1) fos.write(buffer, 0, len);
						is.close(); fos.close();
					} else if (name.equals("assets/config.so")) {
						java.io.InputStream is = zip.getInputStream(entry);
						java.io.FileOutputStream fos = new java.io.FileOutputStream(configFile);
						while ((len = is.read(buffer)) != -1) fos.write(buffer, 0, len);
						is.close(); fos.close();
					}
				}
				zip.close();

				handler.post(() -> {
					_Show(true, 30, "Decoding DEX files");
					log("Decoding DEX files...");
				});
				for (java.io.File dex : dexFiles) {
					byte[] data = new byte[(int) dex.length()];
					java.io.FileInputStream fis = new java.io.FileInputStream(dex);
					fis.read(data); fis.close();
					for (int i = 0; i < data.length; i++) data[i] ^= 0xFF;
					java.io.FileOutputStream fos = new java.io.FileOutputStream(dex);
					fos.write(data); fos.close();
				}

				handler.post(() -> {
					_Show(true, 50, "Modifying Manifest");
					log("Modifying AndroidManifest.xml...");
				});
				java.io.FileInputStream fis = new java.io.FileInputStream(manifestFile);
				String manifestXml = new aXMLDecoder().decode(fis).trim();
				fis.close();
				String appName = FileUtil.readFile(configFile.getAbsolutePath()).trim();
				manifestXml = manifestXml.replace("arm.StubApp", appName);
				java.io.FileOutputStream fos = new java.io.FileOutputStream(manifestFile);
				byte[] encodedXml = new aXMLEncoder().encodeString(MainActivity.this, manifestXml);
				fos.write(encodedXml); fos.close();

				handler.post(() -> {
					_Show(true, 70, "Rebuilding APK");
					log("Rebuilding APK...");
				});
				zip = new java.util.zip.ZipFile(apkPath);
				java.util.zip.ZipOutputStream outZip = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(unsignedApk));
				entries = zip.entries();
				while (entries.hasMoreElements()) {
					java.util.zip.ZipEntry entry = entries.nextElement();
					String name = entry.getName();
					if (name.equals("AndroidManifest.xml") ||
					name.equals("assets/config.so") ||
					name.endsWith("libarm_protect.so") ||
					name.matches("assets/classes(\\d*)?\\.dex") ||
					name.matches("classes(\\d*)?\\.dex")) continue;

					outZip.putNextEntry(new java.util.zip.ZipEntry(name));
					java.io.InputStream is = zip.getInputStream(entry);
					while ((len = is.read(buffer)) != -1) outZip.write(buffer, 0, len);
					is.close(); outZip.closeEntry();
				}
				zip.close();

				outZip.putNextEntry(new java.util.zip.ZipEntry("AndroidManifest.xml"));
				fis = new java.io.FileInputStream(manifestFile);
				while ((len = fis.read(buffer)) != -1) outZip.write(buffer, 0, len);
				fis.close(); outZip.closeEntry();

				for (java.io.File dex : dexFiles) {
					outZip.putNextEntry(new java.util.zip.ZipEntry(dex.getName()));
					fis = new java.io.FileInputStream(dex);
					while ((len = fis.read(buffer)) != -1) outZip.write(buffer, 0, len);
					fis.close(); outZip.closeEntry();
				}
				outZip.close();

				handler.post(() -> {
					_Show(true, 85, "Signing APK...");
					log("Signing APK...");
				});
				_sign(unsignedApk, finalApk, true, true, true, false);

				manifestFile.delete();
				configFile.delete();
				new java.io.File(unsignedApk).delete();
				for (java.io.File dex : dexFiles) dex.delete();

				handler.postDelayed(() -> {
					_Show(false, 100, "Done");
					log("=================================");
					log("SUCCESS!");
					log("Output APK: " + finalApk);
					log("=================================");
					Toast.makeText(getApplicationContext(), "APK processed successfully!", Toast.LENGTH_LONG).show();
				}, 300);

			} catch (Exception e) {
				handler.post(() -> {
					_Show(false, 0, "Error");
					log("ERROR: " + e.toString());
				});
			}
		}).start();
	}

	private void processWithoutConfigSo() {
		clearLog();
		log("Starting ARM Killer without Config.so...");

		try {
			String outputPath = getFilesDir() + "/classes.dex";

			java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(new java.io.File(apkpath));
			java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zipFile.entries();

			log("Extracting classes.dex...");
			while (entries.hasMoreElements()) {
				java.util.zip.ZipEntry entry = entries.nextElement();
				if (entry.getName().equals("classes.dex")) {
					java.io.InputStream is = zipFile.getInputStream(entry);
					java.io.FileOutputStream fos = new java.io.FileOutputStream(outputPath);

					byte[] buffer = new byte[1024];
					int len;
					while ((len = is.read(buffer)) != -1) {
						fos.write(buffer, 0, len);
					}

					fos.close();
					is.close();

					log("Running baksmali...");
					_Run("baksmali");
					break;
				}
			}

			zipFile.close();
		} catch (Exception e) {
			log("ERROR: " + e.toString());
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	private void initializeLogic() {
		button1.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)20, 0xFF4CAF50));
		btn_pick_apk.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)8, 0xFF4CAF50));
		button1.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/front.ttf"), 0);
		btn_pick_apk.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/front.ttf"), 0);
		_checkSDK();
		radiobutton1.performClick();
		log("Ready to process...");
	}

	private void _Show(final boolean _showi, final double _por, final String _dd) {
		if (_showi) {
			if (coreprog == null){
				coreprog = new ProgressDialog(this);
				coreprog.setCancelable(false);
				coreprog.setCanceledOnTouchOutside(false);
				coreprog.requestWindowFeature(Window.FEATURE_NO_TITLE); coreprog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
			}
			coreprog.show();
			coreprog.setContentView(R.layout.ccc);
			LinearLayout linear = (LinearLayout)coreprog.findViewById(R.id.linear);
			TextView t = (TextView)
			coreprog.findViewById(R.id.t);
			TextView te = (TextView)
			coreprog.findViewById(R.id.te);
			ProgressBar p = (ProgressBar)
			coreprog.findViewById(R.id.p);
			android.graphics.drawable.GradientDrawable JCBCJFG = new android.graphics.drawable.GradientDrawable();
			JCBCJFG.setColor(Color.parseColor("#ffffff"));
			JCBCJFG.setCornerRadius(15);
			linear.setBackground(JCBCJFG);
			p.setProgress((int)_por);
			t.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/gfx.ttf"), 0);
			t.setText(_dd);
			te.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/gfx.ttf"), 1);
		} else {
			if (coreprog != null){
				coreprog.dismiss();
			}
		}
	}
	private ProgressDialog coreprog;
	{
	}


	public void _checkSDK() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if (!Environment.isExternalStorageManager()) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
				startActivity(intent);
			}
		}
	}


	public void underscore() {
	}
	private ProgressDialog progress;
	public static class BaksmaliAdapter {
		public static void run(java.io.File input, java.io.File output) throws Exception {
			org.jf.baksmali.BaksmaliOptions options = new org.jf.baksmali.BaksmaliOptions();
			options.deodex = false;
			options.implicitReferences = false;
			options.parameterRegisters = true;
			options.localsDirective = true;
			options.sequentialLabels = true;
			options.debugInfo = true;
			options.codeOffsets = false;
			options.accessorComments = false;
			options.registerInfo = 0;
			options.inlineResolver = null;
			int jobs = Runtime.getRuntime().availableProcessors();
			if (jobs > 6) {
				jobs = 6;
			}
			java.io.InputStream is = new java.io.BufferedInputStream(new java.io.FileInputStream(input.toString()));
			org.jf.dexlib2.iface.DexFile dexFile = org.jf.dexlib2.dexbacked.DexBackedDexFile.fromInputStream(org.jf.dexlib2.Opcodes.forApi(15), is);
			org.jf.baksmali.Baksmali.disassembleDexFile(dexFile, output, jobs, options, null);
		}
		public static void run(String input, String output) throws Exception {
			run(new java.io.File(input), new java.io.File(output));
		}
	}

	{
	}


	public void _Run(final String _type) {
		if (_type.equals("baksmali")) {
			input = getFilesDir().getAbsolutePath() + "/classes.dex";;
			output = getFilesDir().getAbsolutePath();;
		} else {

		}
		input = new java.io.File(input).toString();
		output = new java.io.File(output).toString();
		if (input.equals("")) {
			Toast.makeText(getApplicationContext(), "Enter input", Toast.LENGTH_SHORT).show();
		} else {
			if (output.equals("")) {

			} else {
				if (!FileUtil.isExistFile(input)) {
					Toast.makeText(getApplicationContext(), "Enter output", Toast.LENGTH_SHORT).show();
				} else {
					if (!FileUtil.isExistFile(new java.io.File(output).getParent())) {
						Toast.makeText(getApplicationContext(), "Output parent not exists", Toast.LENGTH_SHORT).show();
					} else {
						new AsyncTask<String, String, String>() {
							@Override
							protected void onPreExecute() {
								super.onPreExecute();
								log("Processing...");
							}
							@Override
							protected String doInBackground(String... arg0) {
								String debug = "";
								while(true) {
									try {
										if (_type.equals("baksmali")) {
											BaksmaliAdapter.run(input ,output);
										} else {
											if (_type.equals("")) {

											}
										}
										debug = "S";
									}
									catch (Exception e) {
										debug = e.getMessage();
									}
									break;
								}
								return debug;
							}
							@Override
							protected void onPostExecute(String result) {
								super.onPostExecute(result);
								String fileContent = FileUtil.readFile(getFilesDir().getAbsolutePath().concat("/arm/StubApp.smali"));

								Pattern pattern = Pattern.compile("\"([^\"]+)\"");
								Matcher matcher = pattern.matcher(fileContent);

								if (matcher.find()) {
									value = matcher.group(1);
								}
								try {
									String apkPath = apkpath;
									String outputApk = outapkpath;
									String unsignedApk = getFilesDir() + "/unsigned.apk";
									java.io.File tempDir = getFilesDir();
									byte[] buffer = new byte[4096];
									int len;

									log("Extracting DEX...");
									_Show(true, 10, "Extracting Dex...");

									java.util.zip.ZipFile zip = new java.util.zip.ZipFile(apkPath);
									java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zip.entries();
									java.util.ArrayList<java.io.File> dexFiles = new java.util.ArrayList<>();
									java.io.File manifestFile = new java.io.File(tempDir, "AndroidManifest.xml");

									while (entries.hasMoreElements()) {
										java.util.zip.ZipEntry entry = entries.nextElement();
										String name = entry.getName();
										if (entry.isDirectory()) continue;

										if (name.matches("assets/classes(\\d*)?\\.dex")) {
											java.io.File outFile = new java.io.File(tempDir, name.substring(name.lastIndexOf("/") + 1));
											java.io.InputStream is = zip.getInputStream(entry);
											java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile);
											while ((len = is.read(buffer)) != -1) fos.write(buffer, 0, len);
											is.close(); fos.close();
											dexFiles.add(outFile);
										} else if (name.equals("AndroidManifest.xml")) {
											java.io.InputStream is = zip.getInputStream(entry);
											java.io.FileOutputStream fos = new java.io.FileOutputStream(manifestFile);
											while ((len = is.read(buffer)) != -1) fos.write(buffer, 0, len);
											is.close(); fos.close();
										}
									}
									zip.close();

									log("Decoding DEX...");
									_Show(true, 20, "Decoding Dex...");

									for (java.io.File dex : dexFiles) {
										byte[] data = new byte[(int) dex.length()];
										java.io.FileInputStream fis = new java.io.FileInputStream(dex);
										fis.read(data); fis.close();
										for (int i = 0; i < data.length; i++) data[i] ^= 0xFF;
										java.io.FileOutputStream fos = new java.io.FileOutputStream(dex);
										fos.write(data); fos.close();
									}

									log("Fixing Manifest...");
									_Show(true, 40, "Fixing Manifest...");

									java.io.FileInputStream fis = new java.io.FileInputStream(manifestFile);
									String manifestXml = new aXMLDecoder().decode(fis).trim();
									fis.close();

									manifestXml = manifestXml.replace("arm.StubApp", value);

									java.io.FileOutputStream fos = new java.io.FileOutputStream(manifestFile);
									byte[] encodedXml = new aXMLEncoder().encodeString(MainActivity.this, manifestXml);
									fos.write(encodedXml); fos.close();

									log("Rebuilding unsigned APK...");
									_Show(true, 60, "Rebuilding unsigned APK...");

									zip = new java.util.zip.ZipFile(apkPath);
									java.util.zip.ZipOutputStream outZip = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(unsignedApk));
									entries = zip.entries();

									while (entries.hasMoreElements()) {
										java.util.zip.ZipEntry entry = entries.nextElement();
										String name = entry.getName();
										if (name.equals("AndroidManifest.xml") ||
										name.endsWith("libarm_protect.so") ||
										name.matches("assets/classes(\\d*)?\\.dex") ||
										name.matches("classes(\\d*)?\\.dex")) continue;

										outZip.putNextEntry(new java.util.zip.ZipEntry(name));
										java.io.InputStream is = zip.getInputStream(entry);
										while ((len = is.read(buffer)) != -1) outZip.write(buffer, 0, len);
										is.close(); outZip.closeEntry();
									}
									zip.close();

									outZip.putNextEntry(new java.util.zip.ZipEntry("AndroidManifest.xml"));
									fis = new java.io.FileInputStream(manifestFile);
									while ((len = fis.read(buffer)) != -1) outZip.write(buffer, 0, len);
									fis.close(); outZip.closeEntry();

									for (java.io.File dex : dexFiles) {
										outZip.putNextEntry(new java.util.zip.ZipEntry(dex.getName()));
										fis = new java.io.FileInputStream(dex);
										while ((len = fis.read(buffer)) != -1) outZip.write(buffer, 0, len);
										fis.close(); outZip.closeEntry();
									}
									outZip.close();

									log("Signing APK...");
									_Show(true, 80, "Signing APK...");

									_sign(unsignedApk, outputApk, true, true, true, false);

									new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
										java.io.File tmp = new java.io.File(unsignedApk);
										for (int i = 0; i < 3; i++) {
											if (tmp.exists()) {
												if (tmp.delete()) break;
												try { Thread.sleep(100); } catch (Exception ignored) {}
											} else break;
										}

										log("=================================");
										log("SUCCESS!");
										log("Output: " + outputApk);
										log("=================================");
										_Show(false, 0, "Signed APK saved to: " + outputApk);
									}, 300);

									manifestFile.delete();
									for (java.io.File dex : dexFiles) dex.delete();

								} catch (Exception e) {
									log("ERROR: " + e.toString());
									_Show(false, 0, "Error: " + e.toString());
								}
							}
						}.execute();
					}
				}
			}
		}
	}


	public void _sign(final String _input, final String _output, final boolean _v1, final boolean _v2, final boolean _v3, final boolean _v4) {
		try {
			new Signer().calculateSignature(_input, _output, _v1, _v2, _v3, _v4);

		} catch (ApkFormatException e) {

		} catch (GeneralSecurityException e) {

		} catch (IOException e) {

		}
	}


	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}

	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}

	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}

	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}

	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}

	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}

	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}

	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}
}
