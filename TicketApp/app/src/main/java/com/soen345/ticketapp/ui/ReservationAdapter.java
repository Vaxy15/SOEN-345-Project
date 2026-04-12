package com.soen345.ticketapp.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.soen345.ticketapp.databinding.RowReservationBinding;
import com.soen345.ticketapp.model.Reservation;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.VH> {

    public interface Listener {
        void onCancel(@NonNull Reservation reservation);
    }

    private final List<Reservation> items;
    private final Listener listener;

    public ReservationAdapter(List<Reservation> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowReservationBinding b = RowReservationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Reservation r = items.get(position);
        String title = r.getEventTitle();
        if (title == null || title.isEmpty()) {
            title = "Event";
        }
        holder.binding.tvTitle.setText(title);
        holder.binding.tvReservedAt.setText(DateFormat.getDateTimeInstance().format(new Date(r.getCreatedAt())));        holder.binding.btnCancel.setOnClickListener(v -> listener.onCancel(r));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final RowReservationBinding binding;

        VH(RowReservationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
