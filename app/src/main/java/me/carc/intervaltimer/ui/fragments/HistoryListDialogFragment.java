package me.carc.intervaltimer.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import me.carc.intervaltimer.App;
import me.carc.intervaltimer.BuildConfig;
import me.carc.intervaltimer.R;
import me.carc.intervaltimer.data.local.AppDatabase;
import me.carc.intervaltimer.data.local.HistoryItem;
import me.carc.intervaltimer.ui.activities.GoogleMapsActivity;
import me.carc.intervaltimer.ui.listeners.HistoryListListener;
import me.carc.intervaltimer.utils.Commons;
import me.carc.intervaltimer.utils.MapUtils;
import me.carc.intervaltimer.utils.ViewUtil;
import me.carc.intervaltimer.widgets.DividerItemDecoration;

/**
 * Show the favorites list
 * Created by bamptonm on 31/08/2017.
 */
public class HistoryListDialogFragment extends DialogFragment {

    public static final String TAG = HistoryListDialogFragment.class.getName();
    public static final String ID_TAG = "HistoryListDialogFragment";

    private List<HistoryItem> historyItems;
    private Unbinder unbinder;

    @BindView(R.id.historyListCollapsingToolbar)
    CollapsingToolbarLayout collapsingToolbar;

    @BindView(R.id.historyListToolbar)
    Toolbar toolbar;

    @BindView(R.id.historyListHeader)
    ImageView imageHeader;

    @BindView(R.id.historyListRecyclerview)
    RecyclerView recyclerView;

    @BindView(R.id.emptyListView)
    TextView emptyListView;

    @BindView(R.id.fabClose)
    FloatingActionButton fabClose;

    private Context getAppCtx() {
        return getActivity().getApplicationContext();
    }

    public static void showInstance(final Context appContext) {

        AppCompatActivity activity = ((App) appContext).getCurrentActivity();

        try {
            Bundle bundle = new Bundle();

            bundle.putBoolean("DEBUG", BuildConfig.DEBUG);

            HistoryListDialogFragment fragment = new HistoryListDialogFragment();
            fragment.setArguments(bundle);
            fragment.show(activity.getSupportFragmentManager(), ID_TAG);
        } catch (RuntimeException e) {
            Log.d(TAG, "showInstance: ");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(STYLE_NO_FRAME, R.style.Dialog_Fullscreen);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.xxxx, container, false);
        View view = inflater.inflate(R.layout.history_list_recyclerview_layout, container, false);
        unbinder = ButterKnife.bind(this, view);

        Bundle args = getArguments();
        if (args != null) {

            HistoryListAdapter adapter = new HistoryListAdapter(listListener);

            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            layoutManager.setReverseLayout(true);
            layoutManager.setStackFromEnd(true);

            RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
            recyclerView.addItemDecoration(itemDecoration);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            recyclerView.addOnScrollListener(scrollListener);

            Drawable drawable = ViewUtil.changeIconColor(getContext(), R.drawable.ic_arrow_back, R.color.white);
            toolbar.setNavigationIcon(drawable);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            collapsingToolbar.setTitleEnabled(false);
            collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(getActivity(), R.color.white));
        }

        loadDatabase();

        return view;
    }


    private void loadDatabase() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = ((App) getAppCtx()).getDB();
                historyItems = db.historyDao().getAllEntries();

                for (HistoryItem item : historyItems) {
                    if (item.getLocations().size() > 1 && TextUtils.isEmpty(item.getDistanceFmt())) {
                        item.setDistance(MapUtils.getDistance(item.getLocations()));
                        item.setDistanceFmt(MapUtils.getFormattedDistance(item.getDistance()));
                    }
                }

                if(getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (historyItems.size() == 0)
                                emptyListView.setVisibility(View.VISIBLE);
                            ((HistoryListAdapter) recyclerView.getAdapter()).addItems(historyItems);
                        }
                    });
                }
            }
        });
    }

    private void updateItemToDatabase(final HistoryItem historyItem) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = ((App) getAppCtx()).getDB();
                db.historyDao().update(historyItem);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((HistoryListAdapter) recyclerView.getAdapter()).updateItem(historyItem);
                    }
                });
            }
        });
    }

    private void removeFromDatabase(final int itemIndex) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final AppDatabase db = ((App) getAppCtx()).getDB();
                final HistoryItem item = db.historyDao().findByIndex(itemIndex);

                if (Commons.isNotNull(item))
                    db.historyDao().delete(item);
            }
        });
    }
    private void removeHistoryItem(HistoryItem item) {
        removeFromDatabase(item.getKeyID());
        HistoryListAdapter adapter = (HistoryListAdapter) recyclerView.getAdapter();
        int index = adapter.getItemPosition(item);
        if (index >= 0 && adapter.removeItem(index)) {
            adapter.notifyItemRemoved(index);
            adapter.notifyItemRangeChanged(index, adapter.getItemCount());
        }
    }

    private void removeAllItems() {
        AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_delete_all)
                .setTitle("Remove Unlocked Entries?")
                .setMessage("This will delete all unlocked entries? You can not undo this")
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((HistoryListAdapter) recyclerView.getAdapter()).removeAll();
                        nukeDatabase();
                        dialog.dismiss();
                    }
                });
        dlg.show();
    }

    private void nukeDatabase() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = ((App) getAppCtx()).getDB();

                List<HistoryItem> allItems = db.historyDao().getAllEntries();

                boolean warningShown = false;
                for (HistoryItem item : allItems) {
                    if(!item.isLocked()){
                        db.historyDao().delete(item);
                    } else if(!warningShown) {
                        warningShown = true;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Locked items are not removed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                loadDatabase();
            }
        });
    }

    private final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dy > 0)
                fabClose.hide();
            else
                fabClose.show();
        }
    };


    @OnClick(R.id.fabClose)
    void exit() {
        ViewUtil.createAlphaAnimator(fabClose, false, getResources()
                .getInteger(R.integer.gallery_alpha_duration) * 2)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                }).start();
    }

    public void hide() {
        getDialog().hide();
    }

    public void show() {
        getDialog().show();
    }

    public void close() {
        dismiss();
    }

    private HistoryViewerDialogFragment getHistoryItemFragment() {
        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(HistoryViewerDialogFragment.ID_TAG);
        return fragment != null && !fragment.isDetached() && !fragment.isRemoving() ? (HistoryViewerDialogFragment) fragment : null;
    }

    private void showHistoryViewerDialog(HistoryItem historyItem) {
        HistoryViewerDialogFragment fragment = getHistoryItemFragment();
        if (Commons.isNotNull(fragment)) {
            fragment.dismiss();
        }
        HistoryViewerDialogFragment.showInstance(getAppCtx(), historyItem);
    }

    public void updateHistoryItem(HistoryItem historyItem) {
        updateItemToDatabase(historyItem);
    }


    private final HistoryListListener listListener = new HistoryListListener() {
        @Override
        public void onClick(HistoryItem historyItem) {
            showHistoryViewerDialog(historyItem);
        }

        @Override
        public void onClickImage(HistoryItem historyItem) {
            Intent mapIntent = new Intent(getActivity(), GoogleMapsActivity.class);
            mapIntent.putParcelableArrayListExtra(GoogleMapsActivity.MAP_POINTS, historyItem.getLocationsArray());
            mapIntent.putExtra(GoogleMapsActivity.MAP_TIME, historyItem.getElaspedTime());
            mapIntent.putExtra(GoogleMapsActivity.MAP_TITLE, historyItem.getTitle());
            startActivity(mapIntent);
        }

        @Override
        public void onClickOverflow(View v, int position) {
            Log.d(TAG, "onClickOverflow: ");
        }

        @Override
        public void onLongTouch(final HistoryItem historyItem) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_delete)
                    .setTitle("Delete Record?")
                    .setMessage("Do you really want to remove this record?")
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                        }
                    })
                    .setNeutralButton("Remove All", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeAllItems();
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(historyItem.isLocked()) {
                                AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity())
                                        .setIcon(R.drawable.ic_locked)
                                        .setTitle("Locked Entry")
                                        .setMessage("This entry is locked. Do you really want to remove it?")
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                removeHistoryItem(historyItem);
                                            }
                                        });
                                dlg.show();
                            } else
                                removeHistoryItem(historyItem);
                        }
                    });
            dlg.show();
        }
    };
}
