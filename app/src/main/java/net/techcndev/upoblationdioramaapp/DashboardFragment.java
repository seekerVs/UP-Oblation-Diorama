package net.techcndev.upoblationdioramaapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nordan.dialog.Animation;
import com.nordan.dialog.DialogType;
import com.nordan.dialog.NordanAlertDialog;
import com.nordan.dialog.NordanAlertDialogListener;

import net.techcndev.upoblationdioramaapp.databinding.FragmentDashboardBinding;

import org.apache.commons.text.WordUtils;

import java.util.Objects;


public class DashboardFragment extends Fragment {

    public static final String LOG_TAG = DashboardFragment.class.getSimpleName();

    FragmentDashboardBinding binding;
    GlobalObject globalObject;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        globalObject.batteryPercentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.batteryText.setText(String.valueOf(snapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(binding.mainLayout, "Error: " + error.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });

        globalObject.deviceModeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.modeText.setText(snapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(binding.mainLayout, "Error: " + error.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });

        globalObject.powerSourceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.powerText.setText(snapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(binding.mainLayout, "Error: " + error.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });

        globalObject.waterLevelRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.waterText.setText(snapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(binding.mainLayout, "Error: " + error.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });

        binding.controlsBtn.setOnClickListener(v -> {
            if (globalObject.isReliableInternetAvailable()) {
                Navigation.findNavController(v).navigate(R.id.action_dashboardFragment_to_controlsFragment3);
            } else {
                Snackbar.make(binding.mainLayout, "No Internet Connection", Snackbar.LENGTH_SHORT).show();
            }
        });

        binding.settingsBtn.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_dashboardFragment_to_settingsFragment2));

        checkAuthenticatedUser();
//        tryFirebase();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        globalObject = new GlobalObject(context.getApplicationContext());
        sharedPreferences = context.getSharedPreferences("PREFS_DATA", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, LOG_TAG + " onResume");
        if (!sharedPreferences.getBoolean("isWelcomed", false)) {
            editor.putBoolean("isWelcomed", true);
            editor.commit();
            showDialog(DialogType.SUCCESS, "Sign-in Success", "Welcome to UP Oblation Diorama App!", Animation.POP, true, "NICE");
        }
    }

    private void checkDevice() {
        String current_email = mAuth.getCurrentUser().getEmail();
        if (current_email != null) {
            globalObject.rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String value = childSnapshot.child("registeredUser").getValue(String.class);

                        if (Objects.equals(value, current_email)) {
                            // If the value matches, do something
                            editor.putString("user_device", childSnapshot.getKey());
                            editor.commit();
                            Log.d("FirebaseData", "Node with specific value found: " + childSnapshot.getKey());
                            break;
                        }
                    }
                    String user_device = sharedPreferences.getString("user_device", "");
                    if (user_device.isBlank()) {
                        showDialog(DialogType.INFORMATION, "No Linked Device", "In Settings, scan your device QR code to link your device to UP Oblation Diorama App.", Animation.POP, true, "OK");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("FirebaseData", "Database error: " + databaseError.getMessage());
                }
            });
        }
    }

    private void checkInternetConnectivity() {
        if (!globalObject.isReliableInternetAvailable()) {
            showDialog(DialogType.WARNING, "No Internet Connection", "Please check your internet connection.", Animation.POP, true, "OK");
        }
    }

    private void checkAuthenticatedUser() {
        try {
            currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Log.d(LOG_TAG, "JUST A NORMAL DAY CAPTAIN!");
                Navigation.findNavController(requireView()).navigate(R.id.action_dashboardFragment_to_signinFragment);
            } else {
                Log.d(LOG_TAG, "This is an existing user.");
                String current_name = WordUtils.capitalizeFully(mAuth.getCurrentUser().getDisplayName());
                binding.usernameText.setText(current_name);
                checkInternetConnectivity();
                checkDevice();
            }
        } catch (Exception e){
            Log.d(LOG_TAG, "Catch Error in DashboardFragment OnCreate: " + e);
        }
    }

    private void showDialog (com.nordan.dialog.DialogType dialogType, String title,
                            String message, Animation animation, boolean cancellable, String btnPos) {
        new NordanAlertDialog.Builder(requireActivity())
                .setDialogType(dialogType)
                .setAnimation(animation)
                .isCancellable(cancellable)
                .setTitle(title)
                .setMessage(message)
                .setPositiveBtnText(btnPos)
                .onPositiveClicked(() -> Toast.makeText(requireContext(), btnPos, Toast.LENGTH_SHORT).show())
                .build().show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        globalObject.unregisterListener();
        binding = null;
    }
}