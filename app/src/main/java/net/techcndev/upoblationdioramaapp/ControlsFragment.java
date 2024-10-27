package net.techcndev.upoblationdioramaapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import net.techcndev.upoblationdioramaapp.databinding.FragmentControlsBinding;

import java.util.Objects;

public class ControlsFragment extends Fragment {

    FragmentControlsBinding binding;
    private PopupWindow popupWindow;
    private TextView tooltipText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentControlsBinding.inflate(inflater, container, false);

        //// Volume seekbar tooltip value
        // Inflate the tooltip layout
        View tooltipView = inflater.inflate(R.layout.tooltip_layout, null);
        tooltipText = tooltipView.findViewById(R.id.tooltip_text);

        // Create the PopupWindow for the tooltip
        popupWindow = new PopupWindow(tooltipView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);

        // Set up SeekBar listener to show tooltip on thumb movement
        binding.volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // Update tooltip with the current progress value
                    tooltipText.setText(String.valueOf(progress));

                    // Get the position of the thumb
                    int[] location = new int[2];
                    seekBar.getLocationOnScreen(location);

                    // Get thumb bounds to determine its position
                    int thumbX = location[0] + seekBar.getPaddingLeft() + (seekBar.getWidth() - seekBar.getPaddingLeft() - seekBar.getPaddingRight()) * progress / seekBar.getMax() - (seekBar.getThumb().getIntrinsicWidth() / 2);
                    int thumbY = location[1] - tooltipView.getHeight() - 20; // Position tooltip above the thumb

                    // Update the PopupWindow's position as the thumb moves
                    popupWindow.update(thumbX, thumbY, popupWindow.getWidth(), popupWindow.getHeight());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Get the initial position of the thumb
                int[] location = new int[2];
                seekBar.getLocationOnScreen(location);

                int thumbX = location[0] + seekBar.getPaddingLeft() + (seekBar.getWidth() - seekBar.getPaddingLeft() - seekBar.getPaddingRight()) * seekBar.getProgress() / seekBar.getMax() - (seekBar.getThumb().getIntrinsicWidth() / 2);
                int thumbY = location[1] - tooltipView.getHeight() - 20;

                // Show the tooltip at the initial position
                popupWindow.showAtLocation(seekBar, Gravity.NO_GRAVITY, thumbX, thumbY);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Dismiss the tooltip when the user stops sliding the thumb
                popupWindow.dismiss();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] modes = getResources().getStringArray(R.array.device_modes);
        String[] states = getResources().getStringArray(R.array.component_states);
        String[] musicNames = getResources().getStringArray(R.array.music_names);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, modes);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, states);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, musicNames);

        binding.modeTextView.setAdapter(adapter1);
        binding.waterLeftTextView.setAdapter(adapter2);
        binding.waterRightTextView.setAdapter(adapter2);
        binding.laserLeftTextView.setAdapter(adapter2);
        binding.laserRightTextView.setAdapter(adapter2);
        binding.lightspotLeftTextView.setAdapter(adapter2);
        binding.lightspotRightTextView.setAdapter(adapter2);
        binding.musicTextView.setAdapter(adapter3);

        // Listeners
        binding.modeTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedValue = adapter1.getItem(position);
                if (Objects.equals(selectedValue, "Custom Mode")) {
                    enableCustomControls();
                } else {
                    disableCustomControls();
                }
            }
        });

        binding.controlsBackBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_controlsFragment_to_dashboardFragment3));
    }

    private void enableCustomControls() {
        binding.volumeSeekbar.setEnabled(true);
        binding.musicTextView.setEnabled(true);
        binding.waterLeftTextView.setEnabled(true);
        binding.waterRightTextView.setEnabled(true);
        binding.laserLeftTextView.setEnabled(true);
        binding.laserRightTextView.setEnabled(true);
        binding.lightspotLeftTextView.setEnabled(true);
        binding.lightspotRightTextView.setEnabled(true);
    }

    private void disableCustomControls() {
        binding.volumeSeekbar.setEnabled(false);
        binding.musicTextView.setEnabled(false);
        binding.waterLeftTextView.setEnabled(false);
        binding.waterRightTextView.setEnabled(false);
        binding.laserLeftTextView.setEnabled(false);
        binding.laserRightTextView.setEnabled(false);
        binding.lightspotLeftTextView.setEnabled(false);
        binding.lightspotRightTextView.setEnabled(false);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        globalObject.unregisterListener();
    }
}
