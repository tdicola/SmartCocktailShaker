package com.tonydicola.smartshaker;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tonydicola.smartshaker.interfaces.PreparationStep;

import java.text.DecimalFormat;
import java.util.List;

public class StepListAdapter extends BaseAdapter {

    private Context context;
    private List<PreparationStep> steps;
    private PrepareDrinkModel model;
    private View.OnClickListener nextClick;
    private DecimalFormat formatter = new DecimalFormat("#,##0.00");

    public StepListAdapter(Context context, List<PreparationStep> steps, PrepareDrinkModel model, View.OnClickListener nextClick) {
        this.context = context;
        this.steps = steps;
        this.model = model;
        this.nextClick = nextClick;
    }

    @Override
    public int getCount() {
        // Return the number of steps.
        if (steps != null) {
            return steps.size();
        }
        else return 0;
    }

    @Override
    public Object getItem(int i) {
        // Return the requested step.
        return steps.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // Inflate a view if necessary.
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.step_list_item, null);
            // Add a view holder to reference UI elements later.
            ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
            // Set view field state which will never change.
            holder.next.setOnClickListener(nextClick);
            holder.measureProgress.setMax(100);
        }
        // Get the step for this view.
        PreparationStep step = steps.get(i);
        // Get holder which has reference to view fields.
        ViewHolder holder = (ViewHolder) view.getTag();
        // Set instruction text.
        holder.instructions.setText(step.getInstructions());
        // Hide the next button if this is the last step.
        holder.next.setVisibility(i == (steps.size() - 1) ? View.GONE : View.VISIBLE);
        // Hide measure views if this step has no measurement.
        holder.measureViews.setVisibility(model.stepHasMeasurement(i) ? View.VISIBLE : View.GONE);
        // Enable view and show the active measure fields if this step is active.
        if (model.isCurrentStep(i)) {
            holder.activeViews.setVisibility(View.VISIBLE);
            holder.instructions.setEnabled(true);
            holder.instructions.setTypeface(null, Typeface.BOLD);
            // Update the measure and progress bar if this step has a measurement.
            if (model.stepHasMeasurement(i)) {
                holder.measureValue.setText(formatter.format(model.getMeasureOz()));
                holder.measureProgress.setProgress(model.getMeasureProgress());
            }
            view.setPadding(0, 20, 0, 20);
        }
        else {
            holder.activeViews.setVisibility(View.GONE);
            holder.instructions.setEnabled(false);
            holder.instructions.setTypeface(null, Typeface.NORMAL);
            view.setPadding(0, 0, 0, 0);
        }
        return view;
    }

    private class ViewHolder {
        public TextView instructions;
        public TextView measureLabel;
        public TextView measureValue;
        public ProgressBar measureProgress;
        public Button next;
        public RelativeLayout activeViews;
        public RelativeLayout measureViews;

        public ViewHolder(View view) {
            instructions = (TextView) view.findViewById(R.id.step_list_instruction);
            measureLabel = (TextView) view.findViewById(R.id.step_list_measure_label);
            measureValue = (TextView) view.findViewById(R.id.step_list_measure_value);
            measureProgress = (ProgressBar) view.findViewById(R.id.step_list_measure_progress);
            next = (Button) view.findViewById(R.id.step_list_next);
            activeViews = (RelativeLayout) view.findViewById(R.id.step_list_active_views);
            measureViews = (RelativeLayout) view.findViewById(R.id.step_list_measure_views);
        }
    }
}
