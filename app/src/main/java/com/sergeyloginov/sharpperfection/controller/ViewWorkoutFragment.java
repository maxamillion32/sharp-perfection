package com.sergeyloginov.sharpperfection.controller;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sergeyloginov.sharpperfection.R;
import com.sergeyloginov.sharpperfection.model.Exercise;
import com.sergeyloginov.sharpperfection.model.Set;
import com.sergeyloginov.sharpperfection.model.Workout;

import java.util.ArrayList;

public class ViewWorkoutFragment extends Fragment {

    private static final String ARG_JSON_WORKOUT = "json_workout";
    private Workout workout;
    private String jsonWorkout;

    public static ViewWorkoutFragment newInstance(String jsonWorkout) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_JSON_WORKOUT, jsonWorkout);
        ViewWorkoutFragment fragment = new ViewWorkoutFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_delete:
                getActivity().setResult(Activity.RESULT_OK, MainActivity
                        .newIntent(getActivity(), new Gson().toJson(workout)));
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        jsonWorkout = (String) getArguments().getSerializable(ARG_JSON_WORKOUT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.workout);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        workout = new Gson().fromJson(jsonWorkout, new TypeToken<Workout>(){}.getType());
        ArrayList<Exercise> exercises = new Gson().fromJson(
                workout.getExercises(),
                new TypeToken<ArrayList<Exercise>>(){}.getType());

        ArrayList<String> list = new ArrayList<>();
        for (Exercise exercise : exercises) {
            list.add(exercise.getTitle());
            for (Set set : exercise.getSets()) {
                String builder = getResources().getString(R.string.set)
                        + ": "
                        + set.getWeight()
                        + " x "
                        + set.getReps();
                list.add(builder);
            }
        }

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        ViewRecyclerViewAdapter adapter = new ViewRecyclerViewAdapter(list);
        rv.setAdapter(adapter);
        return view;
    }

    private class ViewRecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private View divider;

        ViewRecyclerViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            divider = itemView.findViewById(R.id.top_divider);
        }
    }

    private class ViewRecyclerViewAdapter extends RecyclerView.Adapter<ViewRecyclerViewHolder> {

        private ArrayList<String> list;

        ViewRecyclerViewAdapter(ArrayList<String> list) {
            this.list = list;
        }

        @Override
        public ViewRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.rv_view_item, parent, false);
            return new ViewRecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewRecyclerViewHolder holder, int position) {
            holder.title.setText(list.get(position));
            if (!list.get(position).startsWith(getResources().getString(R.string.set))) {
                holder.title.setTextColor(
                        ContextCompat.getColor(getActivity(), android.R.color.black));
                if (position != 0) {
                    holder.divider.setVisibility(View.VISIBLE);
                }
            } else {
                holder.title.setTextColor(
                        ContextCompat.getColor(getActivity(), android.R.color.black));
                holder.divider.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}
