package com.example.expenseutility;

import static android.app.Activity.RESULT_OK;

import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.expenseutility.database.DatabaseHelper;
import com.example.expenseutility.databinding.FragmentFirstBinding;
import com.example.expenseutility.entityadapter.ExpenseItem;
import com.example.expenseutility.entityadapter.Suggestion;
import com.example.expenseutility.utility.CsvImportWorker;
import com.example.expenseutility.utility.CustomSpinnerAdapter;
import com.example.expenseutility.utility.SpinnerItem;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirstFragment extends Fragment {

    public static FragmentFirstBinding binding;
    private int val = 0;
    private String dateVal;
    private String dateTimeVal;
    private static final int PICK_PDF_REQUEST = 1;
    private byte[] pdfBytes;
    private String fileName;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 2MB in bytes
    private static final int REQUEST_CODE_PHOTO = 200;

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private Button startListeningButton;

    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn = false;
    private Handler handler;
    private boolean isBlinking = false;
    private static final int CAMERA_REQUEST = 50;
    private static int BLINK_DURATION = 50;  // 100ms blink
    private static int INTERVAL_DURATION = 4000;  // 2 seconds interval
    private SharedPreferences sharedPreferences;
    private static ArrayAdapter<String> adapter;
    private static String[] suggestionsList;
    private static DatabaseHelper db2;
    private static Set<String> partSet;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DatabaseHelper db = new DatabaseHelper(getContext());
        db2 = new DatabaseHelper(getContext());
        sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        populateSpinnerListItems();
        try {
            monthlySpendingView(false, false);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        binding.triggerImport.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setCancelable(false);
            builder.setIcon(R.drawable.csv_document_svgrepo_com);
            builder.setTitle("Data Import");
            builder.setMessage("Ready to import the CSV file?");
            builder.setPositiveButton("Ok", (dialog, which) -> scheduleCsvImport(getContext()))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).create().show();


        });

        binding.previousMonthData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    monthlySpendingView(true, false);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        binding.nextMonthData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    monthlySpendingView(false, true);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        handler = new Handler(Looper.getMainLooper());

        partSet = sharedPreferences.getStringSet("partSuggestionsList", new HashSet<>());


        List<Suggestion> suggestionList = db2.getAllSuggestions();

        partSet.clear();

        suggestionList.stream().forEach(i -> {
            partSet.add(i.getDescription());
        });


        suggestionsList = new String[partSet.size()];
        AtomicInteger i= new AtomicInteger();
        partSet.stream().forEachOrdered(element -> {
            suggestionsList[i.getAndIncrement()] = element;
        });
        // Set up the ArrayAdapter with suggestion data
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, suggestionsList);
        binding.etParticulars.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        binding.readPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    // Input PDF file from Downloads folder
                    File inputPdf = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "statement3.pdf");

                    // Output CSV file to Downloads folder
                    File outputCsv = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "output.csv");

                    PdfReader reader = new PdfReader(inputPdf.getAbsolutePath());
                    BufferedWriter writer = new BufferedWriter(new FileWriter(outputCsv));

                    for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                        String pageContent = PdfTextExtractor.getTextFromPage(reader, i);

                        String[] lines = pageContent.split("\\r?\\n");

                        for (String line : lines) {
                            // Replace multiple spaces/tabs with a comma
                            String csvLine = line.trim().replaceAll("\\s{2,}", ",");
                            writer.write(csvLine);
                            writer.newLine();
                        }
                    }

                    writer.close();
                    reader.close();

                    System.out.println("CSV written to: " + outputCsv.getAbsolutePath());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Optional: Set the minimum number of characters to show suggestions
//        binding.etParticulars.setThreshold(1);  // Start showing suggestions after 1 character is typed


        binding.etParticulars.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getContext(), "Suggestions list updated", Toast.LENGTH_SHORT).show();
                return true;
            }
        });


        binding.btnGetAllData.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Show the Snackbar with details
                float income = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getFloat("monthlyIncome", 87000.0f);

                double todaysExp = db.getTotalExpenseForToday();
                double todPer = (todaysExp/(income/30))*100;
                Snackbar.make(v, "Todays expense ", 4000)
                        .setAction("\u20B9"+(int) todaysExp +" ("+todPer+"%)", v1 -> {})
                        .show();
                return true;
            }
        });



        // Check if camera permission is granted
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }

        // Initialize CameraManager
        cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0]; // Usually 0 is the back camera
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // Initialize the SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(getContext(), "Language not supported", Toast.LENGTH_SHORT).show();
                    } else {
                        setVoice(); // Set the desired voice
                        textToSpeech.setPitch(1.0f);  // Set pitch, 1.0 is normal
                        textToSpeech.setSpeechRate(0.7f);  // Set speech rate, 1.0 is normal
                    }
                }
            }
        });

        // Check for Audio recording permission
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        }

        binding.etDateTime.setOnClickListener(dateTimeView -> showDateTimeDialog());

        binding.etParticulars.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(before > 0 && s.toString().length() <= 0){
                    binding.etParticulars.setError("Particulars field is empty");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(before > 0 && s.toString().length() <= 0){
                    binding.etAmount.setError("Amount field is empty");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.etDateTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(before > 0 && s.toString().length() <= 0){
                    binding.etDateTime.setError("Date Time field is empty");
                } else {
                    binding.etDateTime.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        binding.chooseFileText.setOnClickListener(v -> openFileChooser());

        binding.btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String particulars = binding.etParticulars.getText().toString();
                String amount = binding.etAmount.getText().toString();
                SpinnerItem selectedItem = (SpinnerItem) binding.spinnerOptions.getSelectedItem();
                String expenseCategory = selectedItem.getText();

                particulars = capitalizeFirstLetter(particulars);

                if(validateFields(expenseCategory,particulars, amount, dateTimeVal, dateVal)){
                    boolean res = false;
                    try {
                        res = db.insertExpense(expenseCategory,particulars, amount, dateTimeVal, dateVal, fileName, pdfBytes, null);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                    if(res) {
                        //save data now into firebase

                        try {
                            saveToFirebase(expenseCategory,particulars, amount, dateTimeVal, dateVal, fileName, pdfBytes);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }

                        resetFields();
                        Toast.makeText(getContext(), "Expense added", Toast.LENGTH_SHORT).show();
                                NavHostFragment.findNavController(FirstFragment.this)
                                        .navigate(R.id.action_FirstFragment_to_SecondFragment);

                    } else {
                        Toast.makeText(getContext(), "Expense save failed !", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Please provide valid input", Toast.LENGTH_SHORT).show();
                }

            }

            private void resetFields() {
                binding.etParticulars.setText("");
                binding.etAmount.setText("");
                binding.etDateTime.setText("");
                binding.spinnerOptions.setSelection(0);
                binding.chooseFileText.setText("");
                clearErrors();
            }





            private boolean validateFields(String spinnerItem, String particulars, String amount, String dateTimeVal, String dateVal) {
                boolean isValid = true;

                if(spinnerItem == null || spinnerItem.equalsIgnoreCase("") ||
                        spinnerItem.equalsIgnoreCase("Select Options") || spinnerItem.equalsIgnoreCase("0") ){
                    isValid = false;
                }

                if(particulars == null || particulars.equalsIgnoreCase("")){
                    binding.etParticulars.setError("Particulars field is empty");
                    isValid = false;
                }

                if(amount == null || amount.equalsIgnoreCase("") || amount.equalsIgnoreCase("0")){
                    binding.etAmount.setError("Amount field is empty");
                    isValid = false;
                }

                if(dateTimeVal == null || dateTimeVal.equalsIgnoreCase("")){
                    binding.etDateTime.setError("Date Time field is empty");
                    isValid = false;
                }

                if(dateVal == null || dateVal.equalsIgnoreCase("")){
                    isValid = false;
                }


                if(dateVal == null || dateVal.equalsIgnoreCase("")){
                    isValid = false;
                }

                return isValid;
            }
        });

        binding.btnGetAllData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor data = db.getAllExpenseData();
                if(data.getCount() <= 0) {
                    Toast.makeText(getContext(), "No data to show", Toast.LENGTH_SHORT).show();
                } else {
                    NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
                }
            }
        });



        binding.flashLightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isBlinking) {
                    isBlinking = true;
                    startBlinkingPattern();
                    Toast.makeText(getContext(), "Flash started", Toast.LENGTH_SHORT).show();
                } else {
                    isBlinking = false;
                    stopBlinking();
                    Toast.makeText(getContext(), "Flash stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });




        binding.launchCaptureActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE_PHOTO);


            }
        });


        binding.voiceAssistantBtn.setOnClickListener(v -> launchVoiceAssistant());

    }

    private void animateProgressBar(int start, int end, int duration) {
        ObjectAnimator animation = ObjectAnimator.ofInt(binding.spendingProgressBar, "progress", start, end);
        animation.setDuration(duration);
        animation.start();
    }

    private void monthlySpendingView(boolean prev, boolean next) throws ParseException {
        String month = new SimpleDateFormat("yyyy-MM").format(new Date());
        String monthYearText = new SimpleDateFormat("MMMM yyyy").format(new Date());

        List<ExpenseItem> items;
        if(prev) {
            String monthStr = sharedPreferences.getString("dateStr", month);
            Date d = new SimpleDateFormat("yyyy-MM").parse(monthStr);
            d.setMonth(d.getMonth()-1);
            month = new SimpleDateFormat("yyyy-MM").format(d);
            sharedPreferences.edit().putString("dateStr", month).apply();

            monthYearText = new SimpleDateFormat("MMMM yyyy").format(d);
            items = db2.getMonthData(month);
            binding.monthYearText.setText(String.valueOf(monthYearText));

        } else if(next) {
            String monthStr = sharedPreferences.getString("dateStr", month);
            Date d = new SimpleDateFormat("yyyy-MM").parse(monthStr);
            if(!d.before(new Date())) {
                d.setMonth(d.getMonth()+1);
            } else {
                d.setMonth(new Date().getMonth());
            }
            month = new SimpleDateFormat("yyyy-MM").format(d);
            sharedPreferences.edit().putString("dateStr", month).apply();

            monthYearText = new SimpleDateFormat("MMMM yyyy").format(d);
            items = db2.getMonthData(month);
            binding.monthYearText.setText(String.valueOf(monthYearText));
        } else {
            sharedPreferences.edit().putString("dateStr", month).apply();
            items = db2.getMonthData(month);
            binding.monthYearText.setText(String.valueOf(monthYearText));

        }
//        List<ExpenseItem> items = db2.getMonthData(month);
        binding.monthYearText.setText(String.valueOf(monthYearText));
        long totalExpAmount = items.stream().mapToLong(ExpenseItem::getExpenseAmount).sum();
        double per = ((double) totalExpAmount / 86000) * 100;
        binding.spendingProgressBar.setProgress((int) per, true);
        binding.spendingPercentageText.setText(String.format("%.2f",per)+"%");
        binding.totalSpentText.setText(String.valueOf("\u20B9"+totalExpAmount));
        binding.monthlySalaryText.setText(String.valueOf("100%"));
        binding.remainingBudgetText.setText(String.format("%.2f",(100-per))+"%");
        animateProgressBar(0, (int) per, 1500); // duration in ms

    }

    public void scheduleCsvImport(Context context) {

        // One-time immediate execution
        WorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(CsvImportWorker.class).build();
        WorkManager.getInstance(requireContext()).enqueue(oneTimeRequest);

        UUID workId = oneTimeRequest.getId();

        WorkManager.getInstance(context).getWorkInfoByIdLiveData(workId)
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getProgress() != null) {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        int progress = workInfo.getProgress().getInt("progress", 0);
                        int total = workInfo.getProgress().getInt("total", 0);
                        int per = workInfo.getProgress().getInt("per",0 );


                        binding.importProgressTextView.setText("Imported " + progress + " of " + total + " rows "+ per+"% done");
                        binding.progressBar.setProgress(per);
                        if (workInfo.getState().isFinished()) {
                            binding.importProgressTextView.setText("✅ Import finished!");
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    }
                });



//        *** Repeats on specific time

//        PeriodicWorkRequest csvWork = new PeriodicWorkRequest.Builder(
//                CsvImportWorker.class,
//                10, TimeUnit.SECONDS // or whatever period you need
//        ).build();
//
//        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//                "CsvImportWork",
//                ExistingPeriodicWorkPolicy.KEEP,
//                csvWork
//        );

        // Show a toast notification when the scheduling is done
        Toast.makeText(context, "CSV Import scheduler started", Toast.LENGTH_SHORT).show();

    }

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Capitalize the first letter and concatenate with the rest of the string
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public static void refreshSuggList() {
        List<Suggestion> suggestionList = db2.getAllSuggestions();
        partSet.clear();
        suggestionList.stream().forEach(i -> {
            partSet.add(i.getDescription());
        });

        suggestionsList = new String[partSet.size()];
        AtomicInteger i= new AtomicInteger();
        partSet.stream().forEachOrdered(element -> {
            suggestionsList[i.getAndIncrement()] = element;
        });
        adapter.notifyDataSetChanged();
    }

    private void startBlinkingPattern() {
        BLINK_DURATION = sharedPreferences.getInt("blinkTime", 50);
        INTERVAL_DURATION = sharedPreferences.getInt("intervalTime", 2000);

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isBlinking) {
                    toggleFlashlight(true);  // Turn on the flashlight for <n> millis
                    handler.postDelayed(() -> {
                        toggleFlashlight(false);  // Turn off the flashlight
                        handler.postDelayed(this, INTERVAL_DURATION);  // Wait for n seconds before the next blink
                    }, BLINK_DURATION);
                }
            }
        });
    }

    private void stopBlinking() {
        handler.removeCallbacksAndMessages(null); // Stop handler
        if (isFlashOn) {
            toggleFlashlight(false); // Turn off the flashlight if it's on
        }
    }

    private void toggleFlashlight(boolean turnOn) {
        try {
            cameraManager.setTorchMode(cameraId, turnOn);
            isFlashOn = turnOn;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void saveToFirebase(String expenseCategory, String particulars, String amount, String dateTimeVal, String dateVal, String fileName, byte[] pdfBytes) throws ParseException {

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String expenseId = database.push().getKey();
        expenseId = dateVal+"/"+expenseId;
        // Create Expense object
        ExpenseItem expense = new ExpenseItem(expenseCategory, particulars, amount, dateTimeVal, dateVal, null, null);



        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date parsedDate = sdf.parse(dateVal);

        SimpleDateFormat sdf1 = new SimpleDateFormat("MMM");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy");

        int year = Integer.parseInt(sdf2.format(parsedDate));
        String month = sdf1.format(parsedDate);

        String subDir = month+"-"+year;

        // Save data in "expenses" node with a unique id
        if (expenseId != null) {
            database.child("expenses").child(String.valueOf(year+"/"+subDir)).child(expenseId).setValue(expense)
            .addOnSuccessListener(aVoid -> {
                // Success message
                Toast.makeText(FirstFragment.adapter.getContext(), "Expense saved in cloud.", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                // Error handling
                Toast.makeText(FirstFragment.adapter.getContext(), "Failed to save on cloud.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setVoice() {
        // Get available voices
        Set<Voice> voices = textToSpeech.getVoices();
        for (Voice voice : voices) {
//            Log.i("VoiceInfo", "Available voice: " + voice.getName());

            // You can select a specific voice by checking voice name or other characteristics
            if (voice.getName().contains("en-us-x-sfg#female_4-local")) { // Example for English (US) female voice
                textToSpeech.setVoice(voice);
                break;
            }
        }
    }

    private void launchVoiceAssistant() {
        // Create an Intent to recognize speech input
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...");


        // Set up SpeechRecognizer listener
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {
                binding.resultTextView.setText("Listening...");
            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {
                binding.resultTextView.setText("");
                Toast.makeText(getContext(), "Error recognizing speech: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                // Called when speech recognition is complete
                ArrayList<String> resultList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (resultList != null && !resultList.isEmpty()) {
                    String recognizedText = resultList.get(0);
                    binding.resultTextView.setText(recognizedText);


//                    String inputText = "save the expense for fuel 200 rupees on todays date";

                    // Extract expense category
                    String categoryPattern = "expense for (\\w+)";
                    String category = extractValue(recognizedText, categoryPattern);

                    // Extract expense amount
                    String amountPattern = "(\\d+) rupees";
                    String amount = extractValue(recognizedText, amountPattern);

                    // Extract expense date (In this example, today's date)
                    String datePattern = "todays date";
                    String date = extractTodayDate();

                    System.out.println("Expense Category: " + category);
                    System.out.println("Expense Amount: " + amount);
                    System.out.println("Expense Date: " + date);



                    // Reply via Text-to-Speech
                    String response = generateReply(recognizedText);
                    try {
                        speak(response, category, amount, date);
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }


                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Called when partial results are available (optional)
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Called when a special event occurs (optional)
            }
        });
        // Start listening
        speechRecognizer.startListening(intent);
    }

    // Function to extract values based on the pattern
    private static String extractValue(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // Function to get today's date in a desired format
    private static String extractTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    // Generate a response based on recognized speech
    private String generateReply(String recognizedText) {
        if (recognizedText.toLowerCase().contains("rohit")) {
            return "Hey Rohit, welcome to AI assistant";
        }
        return "Hello, how can I assist you today?";
    }

    // Speak out the response using TextToSpeech
    private void speak(String message, String category, String amount, String date) throws NoSuchFieldException, IllegalAccessException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(category !=null && amount !=null && date !=null){
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Expense category: "+category+ "\nExpense Amount: "+amount+"\nExpense Date: "+date);

                DatabaseHelper db = new DatabaseHelper(getContext());

                boolean res = db.insertExpense(capitalize(category),category,amount,date,date,null,null,null);
                if(res) {
                    Toast.makeText(getContext(), "Expense added", Toast.LENGTH_SHORT).show();
//                    NavHostFragment.findNavController(FirstFragment.this)
//                            .navigate(R.id.action_FirstFragment_to_SecondFragment);
                }
                textToSpeech.speak("Expense has been saved", TextToSpeech.QUEUE_FLUSH, null, null);
                binding.resultTextView.setText(stringBuilder.toString());
            } else {
                textToSpeech.speak("No data received", TextToSpeech.QUEUE_FLUSH, null);
            }
        } else {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public static String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;  // Return as it is if the string is null or empty
        }

        // Capitalize the first character and make the rest lowercase
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    // Handle the permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_PDF_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri pdfUri = data.getData();
            try {

                InputStream inputStream = getContext().getContentResolver().openInputStream(pdfUri);
                if(inputStream != null) {
                    int fileSize = inputStream.available();
                    inputStream.close();
                    if (fileSize <= MAX_FILE_SIZE) {
                        pdfBytes = readPdfFromUri(pdfUri);
                        fileName = getFileName(pdfUri);
                        String fileNameUI = fileName;
                        fileNameUI = fileNameUI.length()>20? fileNameUI.substring(0,20)+".."+fileNameUI.substring(fileNameUI.lastIndexOf(".")) :fileNameUI;
                        binding.chooseFileText.setText(fileNameUI);
                    } else {
                        Toast.makeText(getContext(), "File size exceeds 2MB limit.", Toast.LENGTH_SHORT).show();
                        pdfBytes= null;
                        fileName = "";
                        binding.chooseFileText.setText("");
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to read PDF", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_CODE_PHOTO && resultCode == RESULT_OK) {
            String photoFileName = data.getStringExtra("photoFileName"); // Get the file name
            if (photoFileName != null) {
                File file = new File(photoFileName); // filePath is the absolute path of the file

                Uri fileUri = Uri.fromFile(file);
                try {
                    InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
                    if(inputStream != null) {
                        int fileSize = 0;

                        fileSize = inputStream.available();
                        inputStream.close();
                        if (fileSize <= MAX_FILE_SIZE) {
                            pdfBytes = readPdfFromUri(fileUri);
                            fileName = getFileName(fileUri);
                            String fileNameUI = fileName;
                            fileNameUI = fileNameUI.length()>20? fileNameUI.substring(0,20)+".."+fileNameUI.substring(fileNameUI.lastIndexOf(".")) :fileNameUI;
                            binding.chooseFileText.setText(fileNameUI);
                        } else {
                            Toast.makeText(getContext(), "File size exceeds 2MB limit.", Toast.LENGTH_SHORT).show();
                            pdfBytes= null;
                            fileName = "";
                            binding.chooseFileText.setText("");
                        }

                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    private String getFileName(Uri pdfUri) {
        String result = null;
        if (pdfUri.getScheme().equals("content")) {
            try (Cursor cursor = getContext().getContentResolver().query(pdfUri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = pdfUri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private byte[] readPdfFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    private void clearErrors() {
            binding.etParticulars.setError(null);
            binding.etAmount.setError(null);
            binding.etDateTime.setError(null);
    }

    private void populateSpinnerListItems() {
        List<SpinnerItem> items = new ArrayList<>();

        fetchAllSpinnerOptions(items);



        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getContext(), items);
        binding.spinnerOptions.setAdapter(adapter);
    }

    public static List<SpinnerItem> fetchAllSpinnerOptions(List<SpinnerItem> items) {
        items.add(new SpinnerItem("Select Options", R.drawable.arrow_next_right_icon));
        items.add(new SpinnerItem("Housing Expenses", R.drawable.house_to_rent_svgrepo_com));
        items.add(new SpinnerItem("Transportation", R.drawable.ground_transportation_svgrepo_com));
        items.add(new SpinnerItem("Food", R.drawable.meal_easter_svgrepo_com));
        items.add(new SpinnerItem("Healthcare", R.drawable.healthcare_hospital_medical_9_svgrepo_com));
        items.add(new SpinnerItem("Fuel", R.drawable.fuel_station));
        items.add(new SpinnerItem("Recharge", R.drawable.mobile_phone_recharge_svgrepo_com));
        items.add(new SpinnerItem("Shopping", R.drawable.shopping_cart_svgrepo_com));
        items.add(new SpinnerItem("Subscriptions", R.drawable.youtube_svgrepo_com));
        items.add(new SpinnerItem("Debt Payments", R.drawable.money_svgrepo_com__1_));
        items.add(new SpinnerItem("Entertainment", R.drawable.entertainment_svgrepo_com));
        items.add(new SpinnerItem("Savings and Investments", R.drawable.piggybank_pig_svgrepo_com));
        items.add(new SpinnerItem("Grocery", R.drawable.shopping_basket));
        items.add(new SpinnerItem("Clothing and Personal Care", R.drawable.clothes_clothing_formal_wear_svgrepo_com));
        items.add(new SpinnerItem("Education", R.drawable.education_graduation_learning_school_study_svgrepo_com));
        items.add(new SpinnerItem("Charity and Gifts", R.drawable.loving_charity_svgrepo_com));
        items.add(new SpinnerItem("Travel", R.drawable.travel_svgrepo_com__1_));
        items.add(new SpinnerItem("Insurance", R.drawable.employee_svgrepo_com));
        items.add(new SpinnerItem("Childcare and Education", R.drawable.woman_pushing_stroller_svgrepo_com));
        items.add(new SpinnerItem("Miscellaneous", R.drawable.healthcare_hospital_medical_9_svgrepo_com));
        return items;
    }

    private void showDateTimeDialog() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    new TimePickerDialog(
                            getContext(),
                            (TimePicker view1, int hourOfDay, int minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                String dateTime = new SimpleDateFormat("dd").format(calendar.getTime()) + "-" + (String.format("%02d",month + 1) ) + "-" + year + " " + new SimpleDateFormat("HH").format(calendar.getTime()) + ":" + new SimpleDateFormat("mm").format(calendar.getTime());
                                dateVal = year + "-" + new SimpleDateFormat("MM").format(calendar.getTime()) + "-" + new SimpleDateFormat("dd").format(calendar.getTime());
                                dateTimeVal = year + "-" + (String.format("%02d",month + 1) ) + "-" + new SimpleDateFormat("dd").format(calendar.getTime()) + " " + new SimpleDateFormat("HH").format(calendar.getTime()) + ":" + new SimpleDateFormat("mm").format(calendar.getTime());
                                binding.etDateTime.setText(dateTime);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    ).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }




}