package me.carc.intervaltimer.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import me.carc.intervaltimer.App;
import me.carc.intervaltimer.ui.activities.GoogleMapsActivity;
import me.carc.intervaltimer.R;
import me.carc.intervaltimer.data.local.HistoryItem;
import me.carc.intervaltimer.data.local.prefs.Preferences;
import me.carc.intervaltimer.widgets.listeners.HistoryItemViewerListener;

/**
 * Viewer for history item
 * Created by bamptonm on 15/02/2018.
 */

public class HistoryViewerDialogFragment extends DialogFragment {
    HistoryItemViewerListener cbHistoryItemViewerListener;

    private static final String TAG = HistoryViewerDialogFragment.class.getName();
    public static final String ID_TAG = TAG;

    private static final String HISTORY_ITEM = "HISTORY_ITEM";
/*

    public static final String MAP_POINTS    = "MAP_POINTS";
    public static final String MAP_TIME      = "MAP_TIME";
    public static final String MAP_TITLE     = "MAP_TITLE";
*/

    private Unbinder unbinder;
    private HistoryItem mHistoryItem;
    private String mTitleAcceptValue;
    private boolean mOriginalLockState, mTitleChanged, mLockAcceptValue;
    private ArrayList<String> prefArray;
    private ArrayAdapter<String> spinnerAdapter;

    @BindView(R.id.mapBtn)       Button mapBtn;
    @BindView(R.id.setBtn)       Button setBtn;
    @BindView(R.id.itemLock)     ImageView itemLock;

    @BindView(R.id.historyViewerTitle)  EditText historyViewerTitle;
    @BindView(R.id.titleSpinner)        Spinner titleSpinner;

    @OnTextChanged(value = R.id.historyViewerTitle, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterTitleInput(Editable editable) {
        if (!mHistoryItem.getTitle().equals(editable.toString())) {
            mTitleAcceptValue = editable.toString();
            mTitleChanged = true;
            setBtn.setEnabled(true);
        } else
            setBtn.setEnabled(false);
    }


    @OnClick(R.id.mapBtn)
    void showMap() {

        Intent mapIntent = new Intent(getActivity(), GoogleMapsActivity.class);
        mapIntent.putParcelableArrayListExtra(GoogleMapsActivity.MAP_POINTS, mHistoryItem.getLocationsArray());
        mapIntent.putExtra(GoogleMapsActivity.MAP_TIME, mHistoryItem.getElaspedTime());
        mapIntent.putExtra(GoogleMapsActivity.MAP_TITLE, TextUtils.isEmpty(mTitleAcceptValue) ? mHistoryItem.getTitle() : mTitleAcceptValue);
        startActivity(mapIntent);

        if(mTitleChanged)   onSetBtn();
        else                dismiss();
    }


    @OnItemSelected(R.id.titleSpinner)
    void onItemSelected(int position) {
        if(position != 0 && !prefArray.get(position).equals("")) {
            historyViewerTitle.setText(prefArray.get(position));
            mTitleChanged = true;
            setBtn.setEnabled(true);
        }
    }

    @OnClick(R.id.itemLock)
    void lockIconTouch() {
        if (mLockAcceptValue) {
            mLockAcceptValue = false;
            itemLock.setImageResource(R.drawable.ic_unlocked);
        } else {
            mLockAcceptValue = true;
            itemLock.setImageResource(R.drawable.ic_locked);
        }
        if (!mTitleChanged) {
            if (mOriginalLockState == mLockAcceptValue)
                setBtn.setEnabled(false);
            else
                setBtn.setEnabled(true);
        }
    }

    @OnClick(R.id.setBtn)
    void onSetBtn() {
        prefArray.add(mTitleAcceptValue);
        Preferences.putHistoryTitleArray(getActivity(), prefArray);

        mHistoryItem.setTitle(mTitleAcceptValue);
        mHistoryItem.setLocked(mLockAcceptValue);
        cbHistoryItemViewerListener.onUpdateHistoryItem(mHistoryItem);
        dismiss();
    }

    @OnClick(R.id.cancelBtn)
    void onCancelBtn() {
        dismiss();
    }


    public static boolean showInstance(final Context appContext, HistoryItem historyItem) {
        AppCompatActivity activity = ((App) appContext).getCurrentActivity();

        try {
            Bundle bundle = new Bundle();
            bundle.putParcelable(HISTORY_ITEM, historyItem);

            HistoryViewerDialogFragment fragment = new HistoryViewerDialogFragment();
            fragment.setArguments(bundle);
            fragment.show(activity.getSupportFragmentManager(), ID_TAG);
            return true;

        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            cbHistoryItemViewerListener = (HistoryItemViewerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement HistoryItemViewerListener callbacks");
        }
    }

    @Override
    public void onDetach() {
        cbHistoryItemViewerListener = null;
        super.onDetach();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setShowsDialog(true);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.history_viewer_dialog, container, false);
        unbinder = ButterKnife.bind(this, view);

        Bundle args = getArguments();
        if (args != null) {
            mHistoryItem = args.getParcelable(HISTORY_ITEM);
        } else
            dismiss();

        if(mHistoryItem == null)
            dismiss();

        if(mHistoryItem.getLocations().size() > 1)
            mapBtn.setVisibility(View.VISIBLE);
        else
            mapBtn.setVisibility(View.INVISIBLE);

        int timeDelimit = mHistoryItem.getDate().lastIndexOf(":");
        String date;
        String time;
        if (-1 == timeDelimit) {
            date = mHistoryItem.getDate();
            time = "N/A";
        } else {
            date = mHistoryItem.getDate().substring(0, timeDelimit);
            time = mHistoryItem.getDate().substring(timeDelimit + 1, mHistoryItem.getDate().length());
        }

        TextView dateTV = view.findViewById(R.id.date);
        TextView timeTV = view.findViewById(R.id.time);
        TextView durationTV = view.findViewById(R.id.duration);
        TextView distanceTV = view.findViewById(R.id.distance);
        TextView stepsTV = view.findViewById(R.id.steps);
        TextView roundsTV = view.findViewById(R.id.rounds);
        TextView workRestTime = view.findViewById(R.id.workRestTime);

        if (TextUtils.isEmpty(mHistoryItem.getTitle())) {
            historyViewerTitle.setHint("Rename Workout");
            mTitleAcceptValue = "";
        } else {
            mTitleAcceptValue = mHistoryItem.getTitle();
            historyViewerTitle.setText(mHistoryItem.getTitle());
        }

        dateTV.setText(date);
        timeTV.setText(time);

        String duration = mHistoryItem.getElaspedTime();
        if(!TextUtils.isEmpty(mHistoryItem.getDistanceFmt()))
            duration = duration + " / " + mHistoryItem.getDistanceFmt();

        distanceTV.setText(mHistoryItem.getDistance() > 0 ? mHistoryItem.getDistanceFmt() : "---");
        stepsTV.setText(mHistoryItem.getSteps() > 0 ? String.valueOf(mHistoryItem.getSteps()) : "---");

        durationTV.setText(duration);
        roundsTV.setText(String.format(getActivity().getString(R.string.roundsOf), mHistoryItem.getRoundsCompleted(), mHistoryItem.getRoundsTotal()));
        workRestTime.setText(String.format("%s / %s", mHistoryItem.getWorkTime(), mHistoryItem.getRestTime()));

        mLockAcceptValue = mOriginalLockState = mHistoryItem.isLocked();
        if (mOriginalLockState)
            itemLock.setImageResource(R.drawable.ic_locked);
        else
            itemLock.setImageResource(R.drawable.ic_unlocked);

        setTitleSpinner();

        return view;
    }

    private void setTitleSpinner() {
        prefArray = Preferences.getHistoryTitleArray(getActivity());
        prefArray.add(0, "");
        spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, prefArray) /*{

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == 0) {
                    ((TextView)v.findViewById(android.R.id.text1)).setVisibility(View.GONE);
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount()-1;            // you don't display last item. It is used as hint.
            }
        }*/;

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        titleSpinner.setAdapter(spinnerAdapter);
    }

    private void showNewTitleDlg() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        final EditText input = new EditText(getActivity());
        input.setHint("Add Title");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        input.setPadding((int)getResources().getDimension(R.dimen.margin_standard),
                (int)getResources().getDimension(R.dimen.margin_standard),
                (int)getResources().getDimension(R.dimen.margin_standard),
                (int)getResources().getDimension(R.dimen.margin_standard));

        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_icon_none);

        alertDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String title = input.getText().toString();
                        int index = prefArray.size();
                        prefArray.add(title);
                        onItemSelected(index);
                    }
                });

        alertDialog.show();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }
}
