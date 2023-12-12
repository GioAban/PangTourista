package pangtourista.project.Adapters;

import android.app.Activity;
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import pangtourista.project.Models.FiestaFestivalEvent;
import pangtourista.project.R;
import pangtourista.project.databinding.ItemFiestaFestivalEventLayoutBinding;



public class FiestaFestivalEventAdapter extends RecyclerView.Adapter<FiestaFestivalEventAdapter.FiestaFestivalEventViewHolder> {

    private final Context context;
    private final ArrayList<FiestaFestivalEvent> fiestaFestivalEvents;

    public FiestaFestivalEventAdapter(Context context, ArrayList<FiestaFestivalEvent> fiestaFestivalEvents) {
        this.context = context;
        this.fiestaFestivalEvents = fiestaFestivalEvents;
    }

    @NonNull
    @Override
    public FiestaFestivalEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FiestaFestivalEventViewHolder(LayoutInflater.from(context).inflate(R.layout.item_fiesta_festival_event_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FiestaFestivalEventViewHolder holder, int position) {
        FiestaFestivalEvent fiestaFestivalEvent = fiestaFestivalEvents.get(position);
        String description = fiestaFestivalEvent.getDescription();
        Spanned description_html = Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY);
        // Format and display event date
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        inputDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

        try {
            Date startDate = inputDateFormat.parse(fiestaFestivalEvent.getEventDateStart());
            Date endDate = inputDateFormat.parse(fiestaFestivalEvent.getEventDateEnd());

            SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
            dayFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

            String dayStart = dayFormat.format(startDate);
            String dayEnd = dayFormat.format(endDate);

            holder.binding.dateStart.setText(dayStart);
            holder.binding.dateEnd.setText(dayEnd);

            SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM");
            outputDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

            String monthName = outputDateFormat.format(startDate);
            holder.binding.dateMonth.setText(monthName);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Populate the item view with the FiestaFestivalEvent data
        holder.binding.title.setText(fiestaFestivalEvent.getEventTitle());
        holder.binding.municipality.setText(fiestaFestivalEvent.getMunicipalityName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!description_html.toString().isEmpty()) {
                    // Inflate the custom layout
                    View popupView = LayoutInflater.from(v.getContext()).inflate(R.layout.modal_fiesta_festival, null);
                    int widthInDp = 340;
                    float density = v.getResources().getDisplayMetrics().density;
                    int widthInPx = (int) (widthInDp * density);
                    PopupWindow popupWindow = new PopupWindow(popupView, widthInPx, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                    popupWindow.setElevation(20); // Set the elevation to simulate margins
                    // Set the content of the PopupWindow (customize this based on your needs)
                    TextView detailsTextView = popupView.findViewById(R.id.description);
                    Button close_btn = popupView.findViewById(R.id.close_festival);
                    close_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }
                    });

                    detailsTextView.setText(description_html);
                    popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

                    // Set focusable true to enable touch events outside of the PopupWindow
                    popupWindow.setTouchable(true);
                    popupWindow.setFocusable(true);
                    popupWindow.setOutsideTouchable(true);

                    // Show the PopupWindow
                    popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                    // You can also dismiss the popup when clicking on it if needed
                    popupView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }
                    });
                }
            }
        });
    }




    @Override
    public int getItemCount() {
        return fiestaFestivalEvents.size();
    }
    public class FiestaFestivalEventViewHolder extends RecyclerView.ViewHolder {
        ItemFiestaFestivalEventLayoutBinding binding;
        public FiestaFestivalEventViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemFiestaFestivalEventLayoutBinding.bind(itemView);
        }
    }
}
