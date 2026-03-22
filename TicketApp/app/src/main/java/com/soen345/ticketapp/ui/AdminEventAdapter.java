package com.soen345.ticketapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.soen345.ticketapp.databinding.RowAdminEventBinding;
import com.soen345.ticketapp.model.Event;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.VH> {

    public interface Listener {
        void onEdit(@NonNull Event event);

        void onCancelEvent(@NonNull Event event);
    }

    private final List<Event> items;
    private final Listener listener;

    public AdminEventAdapter(List<Event> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowAdminEventBinding b = RowAdminEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Event e = items.get(position);
        holder.binding.tvTitle.setText(e.getTitle());
        String sub = e.getCategory() + " · " + e.getLocation() + " · "
            + DateFormat.getDateTimeInstance().format(new Date(e.getDateTimeMillis()))
            + " · seats " + e.getAvailableSeats();
        holder.binding.tvSubtitle.setText(sub);
        holder.binding.tvCancelled.setVisibility(e.getCancelled() ? View.VISIBLE : View.GONE);
        holder.binding.btnCancelEvent.setEnabled(!e.getCancelled());
        holder.binding.btnEdit.setOnClickListener(v -> listener.onEdit(e));
        holder.binding.btnCancelEvent.setOnClickListener(v -> listener.onCancelEvent(e));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final RowAdminEventBinding binding;

        VH(RowAdminEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
