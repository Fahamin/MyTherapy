package medication.smartpatient.mytherapy;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import medication.smartpatient.mytherapy.adapter.MedListAdapter;
import medication.smartpatient.mytherapy.model.ReminderItem;
import medication.smartpatient.mytherapy.utils.AlarmReceiver;
import medication.smartpatient.mytherapy.utils.DateTimeSorter;
import medication.smartpatient.mytherapy.utils.Reminder;
import medication.smartpatient.mytherapy.utils.ReminderDatabase;

public class MainActivity extends AppCompatActivity implements ItemClickListener {

    private RecyclerView mList;
    private MedListAdapter mAdapter;
    //    private Toolbar mToolbar;
    private TextView mNoReminderView;
    private FloatingActionButton mAddReminderButton;
    private int mTempPost;
    private LinkedHashMap<Integer, Integer> IDmap = new LinkedHashMap<>();
    private ReminderDatabase rb;
    private AlarmReceiver mAlarmReceiver;

    List<ReminderItem> medicineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        // Initialize reminder database
        rb = new ReminderDatabase(getApplicationContext());


        mAddReminderButton = (FloatingActionButton) findViewById(R.id.add_reminder);
        mList = (RecyclerView) findViewById(R.id.reminder_list);
        mNoReminderView = (TextView) findViewById(R.id.no_reminder_text);

        // To check is there are saved reminders
        // If there are no reminders display a message asking the user to create reminders
        List<Reminder> mTest = rb.getAllReminders();

        if (mTest.isEmpty()) {
            mNoReminderView.setVisibility(View.VISIBLE);
        }
        medicineList = new ArrayList<>();
        medicineList = generateData();
        // Create recycler view
        mList.setLayoutManager(new LinearLayoutManager(this));
        registerForContextMenu(mList);
        mAdapter = new MedListAdapter(medicineList, this, this);
        mList.setAdapter(mAdapter);

        // On clicking the floating action button
        mAddReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ReminderAddActivity.class);
                startActivity(intent);
            }
        });

        // Initialize alarm
        mAlarmReceiver = new AlarmReceiver();
    }

    public List<ReminderItem> generateData() {
        ArrayList<ReminderItem> items = new ArrayList<>();

        // Get all reminders from the database
        List<Reminder> reminders = rb.getAllReminders();

        // Initialize lists
        List<String> Titles = new ArrayList<>();
        List<String> Repeats = new ArrayList<>();
        List<String> RepeatNos = new ArrayList<>();
        List<String> RepeatTypes = new ArrayList<>();
        List<String> Actives = new ArrayList<>();
        List<String> DateAndTime = new ArrayList<>();
        List<Integer> IDList = new ArrayList<>();
        List<DateTimeSorter> DateTimeSortList = new ArrayList<>();

        // Add details of all reminders in their respective lists
        for (Reminder r : reminders) {
            Titles.add(r.getTitle());
            DateAndTime.add(r.getDate() + " " + r.getTime());
            Repeats.add(r.getRepeat());
            RepeatNos.add(r.getRepeatNo());
            RepeatTypes.add(r.getRepeatType());
            Actives.add(r.getActive());
            IDList.add(r.getID());
        }

        int key = 0;

        // Add date and time as DateTimeSorter objects
        for (int k = 0; k < Titles.size(); k++) {
            DateTimeSortList.add(new DateTimeSorter(key, DateAndTime.get(k)));
            key++;
        }

        // Sort items according to date and time in ascending order
        Collections.sort(DateTimeSortList, new DateTimeComparator());

        int k = 0;

        // Add data to each recycler view item
        for (DateTimeSorter item : DateTimeSortList) {
            int i = item.getIndex();

            items.add(new ReminderItem(Titles.get(i), DateAndTime.get(i), Repeats.get(i),
                    RepeatNos.get(i), RepeatTypes.get(i), Actives.get(i)));
            IDmap.put(k, IDList.get(i));
            k++;
        }
        return items;
    }

    @Override
    public void onResume() {
        super.onResume();

        // To check is there are saved reminders
        // If there are no reminders display a message asking the user to create reminders
        List<Reminder> mTest = rb.getAllReminders();

        if (mTest.isEmpty()) {
            mNoReminderView.setVisibility(View.VISIBLE);
        } else {
            mNoReminderView.setVisibility(View.GONE);
        }

        medicineList = new ArrayList<>();
        medicineList = generateData();
        mList.setLayoutManager(new LinearLayoutManager(this));
        registerForContextMenu(mList);
        mAdapter = new MedListAdapter(medicineList, this, this);
        mList.setAdapter(mAdapter);
    }


    private void selectReminder(int pos) {

        String mStringClickID = Integer.toString(pos);
        Intent i = new Intent(this, ReminderEditActivity.class);
        i.putExtra(ReminderEditActivity.EXTRA_REMINDER_ID, mStringClickID);
        startActivity(i);

    }


    @Override
    public void clickListener(int pos) {

        int mReminderClickID = IDmap.get(pos);
        selectReminder(mReminderClickID);
      //  selectReminder(pos);

    }

}