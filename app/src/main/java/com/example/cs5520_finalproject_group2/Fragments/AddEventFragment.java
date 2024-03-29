package com.example.cs5520_finalproject_group2.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cs5520_finalproject_group2.Models.Event;
import com.example.cs5520_finalproject_group2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class AddEventFragment extends Fragment {
    private static final String ARG_DAY = "day";
    TextView addEventName, addStartTime, addEndTime, addEventLocation;
    Button addSaveButton, addBack;
    Event event;
    IAddEventActivity addEventActivity;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private ArrayList<Event> events = new ArrayList<>();
    public AddEventFragment() {
        // Required empty public constructor
    }
    public static AddEventFragment newInstance(String day) {
        AddEventFragment fragment = new AddEventFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DAY, day);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        event = new Event();
        if (getArguments() != null) {
            if (args.containsKey(ARG_DAY)) {
                event.setDay(args.getString(ARG_DAY));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_event, container, false);
        Bundle args = getArguments();
        String day = args.getString(ARG_DAY);
        addEventName = view.findViewById(R.id.addEventName);
        addStartTime = view.findViewById(R.id.addStartTime);
        addEndTime = view.findViewById(R.id.addEndTime);
        addEventLocation = view.findViewById(R.id.addEventLocation);
        addSaveButton = view.findViewById(R.id.addSaveButton);
        addBack = view.findViewById(R.id.addBack);

        db.collection("user")
                .document(firebaseAuth.getCurrentUser().getEmail())
                .collection(event.getDay())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                Event event = queryDocumentSnapshot.toObject(Event.class);
                                events.add(event);
                            }
                        }
                    }
                });


        addSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = addEventName.getText().toString();
                String startTime = addStartTime.getText().toString();
                String endTime = addEndTime.getText().toString();
                String location = addEventLocation.getText().toString();
                if (name.equals("")) {
                    addEventName.setError("Event Name Cannot Be Empty!");
                } else if (startTime.equals("")) {
                    addStartTime.setError("Event Start Time Cannot Be Empty!");
                } else if (endTime.equals("")) {
                    addEndTime.setError("Event End Time Cannot Be Empty!");
                } else if (!validTime(startTime, endTime)) {
                    addEndTime.setError("Start time should be earlier than end time!");
                } else if (location.equals("")) {
                    addEventLocation.setError("Event Location Cannot Be Empty!");
                } else if (!checkName(name)) {
                    addEventName.setError("Event name already exists!");
                } else {
                    event.setName(name);
                    event.setStartTime(startTime);
                    event.setEndTime(endTime);
                    event.setLocation(location);
                    addEventToDb(event);
                    addEventActivity.addEvent(event.getDay());
                }
            }
        });

        addBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEventActivity.addEvent(day);
            }
        });

        return view;
    }

    public Boolean checkName(String name) {
        Boolean notDuplicate = true;
        for (Event e : events) {
            if (e.getName().equals(name)) {
                notDuplicate = false;
                break;
            }
        }
        return notDuplicate;
    }

    private void addEventToDb(Event event) {
        db.collection("user")
                .document(firebaseAuth.getCurrentUser().getEmail())
                .collection(event.getDay())
                .document(event.getName())
                .set(event);
    }

    private Boolean validTime(String startTime, String endTime) {
        try {
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);
            return (start.compareTo(end) < 0);
        } catch (DateTimeParseException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IAddEventActivity) {
            addEventActivity = (IAddEventActivity) context;
        } else {
            throw new RuntimeException(context.toString() + "must implement IAddEventActivity");
        }
    }

    public interface IAddEventActivity {
        void addEvent(String day);
    }
}