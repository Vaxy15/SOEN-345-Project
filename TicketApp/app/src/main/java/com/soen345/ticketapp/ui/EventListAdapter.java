package com.soen345.ticketapp.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.soen345.ticketapp.databinding.RowEventBinding;
import com.soen345.ticketapp.model.Event;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.VH> {

    public interface OnEventClick {
        void onClick(Event event);
    }

    private final List<Event> items;
    private final OnEventClick onClick;

    public EventListAdapter(List<Event> items, OnEventClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowEventBinding b = RowEventBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Event e = items.get(position);

        holder.binding.tvTitle.setText(e.getTitle() != null ? e.getTitle() : "Untitled event");
        holder.binding.tvLocation.setText("Location: " + (e.getLocation() != null ? e.getLocation() : "Not specified"));
        holder.binding.tvSeats.setText("Seats: " + e.getAvailableSeats());

        if (e.getEventDateTime() != null) {
            Date eventDate = e.getEventDateTime().toDate();
            holder.binding.tvTime.setText("Date: " + DateFormat.getDateTimeInstance().format(eventDate));
        } else {
            holder.binding.tvTime.setText("Date: No date");
        }

        holder.binding.getRoot().setOnClickListener(v -> onClick.onClick(e));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final RowEventBinding binding;

        VH(RowEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}