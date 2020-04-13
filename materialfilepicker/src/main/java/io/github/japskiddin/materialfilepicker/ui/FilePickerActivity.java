package io.github.japskiddin.materialfilepicker.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import io.github.japskiddin.materialfilepicker.R;
import io.github.japskiddin.materialfilepicker.filter.CompositeFilter;
import io.github.japskiddin.materialfilepicker.filter.PatternFilter;
import io.github.japskiddin.materialfilepicker.storage.StorageBean;
import io.github.japskiddin.materialfilepicker.storage.StorageUtils;
import io.github.japskiddin.materialfilepicker.utils.FileUtils;
import io.github.japskiddin.materialfilepicker.widget.EmptyRecyclerView;
import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FilePickerActivity extends Activity {
  public static final String ARG_START_PATH = "arg_start_path";
  public static final String ARG_CURRENT_PATH = "arg_current_path";

  public static final String ARG_FILTER = "arg_filter";
  public static final String ARG_CLOSEABLE = "arg_closeable";
  public static final String ARG_TITLE = "arg_title";
  public static final String ARG_FILE_PICK = "arg_file_pick";
  public static final String ARG_ADD_DIRS = "arg_add_dirs";

  public static final String STATE_START_PATH = "state_start_path";
  private static final String STATE_CURRENT_PATH = "state_current_path";

  public static final String RESULT_FILE_PATH = "result_file_path";
  private static final int HANDLE_CLICK_DELAY = 150;

  private EmptyRecyclerView mDirectoryRecyclerView;
  private DirectoryAdapter mDirectoryAdapter;
  private View mEmptyView;
  private String mStartPath = Environment.getExternalStorageDirectory().getAbsolutePath();
  private String mCurrentPath = mStartPath;
  private CharSequence mTitle;
  private TextView tvToolbarTitle;
  private boolean mCloseable, isFilePick, addDirs, isHome = false;
  private CompositeFilter mFilter;
  private List<File> storages = new ArrayList<>();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_file_picker);

    initArguments(savedInstanceState);
    initViews();
    initToolbar();
    initFilesList();
  }

  @Override public void onBackPressed() {
    backClick(true);
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(STATE_CURRENT_PATH, mCurrentPath);
    outState.putString(STATE_START_PATH, mStartPath);
  }

  @SuppressWarnings("unchecked") private void initArguments(Bundle savedInstanceState) {
    if (getIntent().hasExtra(ARG_FILTER)) {
      Serializable filter = getIntent().getSerializableExtra(ARG_FILTER);

      if (filter instanceof Pattern) {
        ArrayList<FileFilter> filters = new ArrayList<>();
        filters.add(new PatternFilter((Pattern) filter, false));
        mFilter = new CompositeFilter(filters);
      } else {
        mFilter = (CompositeFilter) filter;
      }
    }

    if (savedInstanceState != null) {
      mStartPath = savedInstanceState.getString(STATE_START_PATH);
      mCurrentPath = savedInstanceState.getString(STATE_CURRENT_PATH);
    } else {
      if (getIntent().hasExtra(ARG_START_PATH)) {
        mStartPath = getIntent().getStringExtra(ARG_START_PATH);
        mCurrentPath = mStartPath;
      }

      if (getIntent().hasExtra(ARG_CURRENT_PATH)) {
        String currentPath = getIntent().getStringExtra(ARG_CURRENT_PATH);

        if (currentPath != null && currentPath.startsWith(mStartPath)) {
          mCurrentPath = currentPath;
        }
      }
    }

    if (getIntent().hasExtra(ARG_TITLE)) {
      mTitle = getIntent().getCharSequenceExtra(ARG_TITLE);
    }

    if (getIntent().hasExtra(ARG_CLOSEABLE)) {
      mCloseable = getIntent().getBooleanExtra(ARG_CLOSEABLE, true);
    }

    if (getIntent().hasExtra(ARG_FILE_PICK)) {
      isFilePick = getIntent().getBooleanExtra(ARG_FILE_PICK, false);
    }

    if (getIntent().hasExtra(ARG_ADD_DIRS)) {
      addDirs = getIntent().getBooleanExtra(ARG_ADD_DIRS, true);
    }

    List<StorageBean> storageBeans = StorageUtils.getStorageData(this);
    storages = new ArrayList<>();
    if (storageBeans != null) {
      for (StorageBean storageBean : storageBeans) {
        storages.add(new File(storageBean.getPath()));
      }
    } else {
      String path = Environment.getExternalStorageDirectory().getAbsolutePath();
      storages.add(new File(path));
    }
  }

  private void initToolbar() {
    // Truncate start of path
    tvToolbarTitle.setSingleLine();
    tvToolbarTitle.setHorizontallyScrolling(true);
    tvToolbarTitle.setEllipsize(TextUtils.TruncateAt.START);
    ImageView ivToolbarAdd = findViewById(R.id.iv_toolbar_add);
    ImageView ivToolbarBack = findViewById(R.id.iv_filepicker_toolbar_back);
    ImageView ivToolbarCheck = findViewById(R.id.iv_toolbar_check);
    ivToolbarBack.setImageDrawable(
        VectorDrawableCompat.create(getResources(), R.drawable.ic_arrow_back_filepicker,
            getTheme()));
    ivToolbarCheck.setImageDrawable(
        VectorDrawableCompat.create(getResources(), R.drawable.ic_check_circle_black_24dp,
            getTheme()));
    ivToolbarAdd.setImageDrawable(
        VectorDrawableCompat.create(getResources(), R.drawable.ic_add_box_black_24dp, getTheme()));

    if (!TextUtils.isEmpty(mTitle)) {
      tvToolbarTitle.setText(mTitle);
    }
    if (!addDirs) {
      ivToolbarAdd.setVisibility(View.GONE);
    }
    if (isFilePick) {
      ivToolbarCheck.setVisibility(View.GONE);
    }
    ivToolbarAdd.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        showNewFolderDialog();
      }
    });
    ivToolbarCheck.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        setResultAndFinish(mCurrentPath);
      }
    });
    ivToolbarBack.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
      }
    });
    updateTitle();
  }

  private void initViews() {
    tvToolbarTitle = findViewById(R.id.tv_filepicker_toolbar_title);
    mDirectoryRecyclerView = findViewById(R.id.directory_recycler_view);
    mEmptyView = findViewById(R.id.directory_empty_view);
    RelativeLayout btnHome = findViewById(R.id.btn_home);
    ImageView ivHome = findViewById(R.id.iv_home);
    RelativeLayout btnUp = findViewById(R.id.btn_back);
    ImageView ivPlaceholder = findViewById(R.id.iv_placeholder);
    ImageView ivUp = findViewById(R.id.iv_up);
    ivUp.setImageDrawable(
        VectorDrawableCompat.create(getResources(), R.drawable.ic_up, getTheme()));
    ivHome.setImageDrawable(
        VectorDrawableCompat.create(getResources(), R.drawable.ic_home_black, getTheme()));
    ivPlaceholder.setImageDrawable(
        VectorDrawableCompat.create(getResources(), R.drawable.ic_file_placeholder, getTheme()));

    btnUp.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        backClick(false);
      }
    });
    btnHome.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        isHome = true;
        updateTitle();
        initFilesList();
      }
    });
  }

  private void updateTitle() {
    if (isHome) {
      tvToolbarTitle.setText(R.string.file_picker_app_name);
    } else {
      String titlePath = mCurrentPath.isEmpty() ? "/" : mCurrentPath;
      tvToolbarTitle.setText(titlePath);
    }
  }

  private void initFilesList() {
    mDirectoryAdapter =
        new DirectoryAdapter(this,
            isHome ? storages : FileUtils.getFileListByDirPath(mCurrentPath, mFilter), isHome);

    mDirectoryAdapter.setOnItemClickListener(new DirectoryAdapter.OnItemClickListener() {
      @Override public void onItemClick(View view, int position) {
        onFileClicked(mDirectoryAdapter.getModel(position));
      }
    });

    mDirectoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mDirectoryRecyclerView.setAdapter(mDirectoryAdapter);
    mDirectoryRecyclerView.setEmptyView(mEmptyView);
  }

  private void onFileClicked(final File clickedFile) {
    new Handler().postDelayed(new Runnable() {
      @Override public void run() {
        handleFileClicked(clickedFile);
      }
    }, HANDLE_CLICK_DELAY);
  }

  private void backClick(boolean isBackPressed) {
    if (!isHome) {
      if (!mCurrentPath.equals(mStartPath)) {
        mCurrentPath = FileUtils.cutLastSegmentOfPath(mCurrentPath);
        updateTitle();
        initFilesList();
      } else {
        isHome = true;
        updateTitle();
        initFilesList();
      }
    } else {
      if (isBackPressed) {
        setResult(RESULT_CANCELED);
        finish();
      }
    }
  }

  private void showNewFolderDialog() {
    final LayoutInflater layoutInflater = this.getLayoutInflater();
    LinearLayout linearLayout =
        (LinearLayout) layoutInflater.inflate(R.layout.dialog_new_dir,
            (ViewGroup) findViewById(R.id.main_layout), false);
    final EditText dirName = linearLayout.findViewById(R.id.et_dirName);
    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
    alertDialog.setView(linearLayout);
    alertDialog.setTitle(R.string.dialog_title);
    alertDialog.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
      }
    });
    alertDialog.setPositiveButton(R.string.dialog_create, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int i) {
        createNewFolder(dirName.getText().toString());
      }
    });
    alertDialog.create().show();
  }

  private void createNewFolder(String dirName) {
    if (!TextUtils.isEmpty(dirName)) {
      File dir = new File(mCurrentPath, dirName);
      if (!dir.exists()) {
        boolean created = dir.mkdir();
        if (created) {
          initFilesList();
        } else {
          Toast.makeText(getApplicationContext(), getString(R.string.error_folder_created),
              Toast.LENGTH_LONG).show();
        }
      } else {
        Toast.makeText(getApplicationContext(), getString(R.string.error_folder_exists),
            Toast.LENGTH_LONG).show();
      }
    } else {
      Toast.makeText(getApplicationContext(), getString(R.string.error_folder_name),
          Toast.LENGTH_LONG).show();
    }
  }

  private void handleFileClicked(final File clickedFile) {
    if (clickedFile.isDirectory()) {
      if (isHome) {
        mStartPath = clickedFile.getPath();
        isHome = false;
      }

      mCurrentPath = clickedFile.getPath();
      // If the user wanna go to the emulated directory, he will be taken to the
      // corresponding user emulated folder.
      if (mCurrentPath.equals("/storage/emulated")) {
        mCurrentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
      }

      updateTitle();
      initFilesList();
    }

    if (isFilePick && clickedFile.isFile()) {
      mCurrentPath = clickedFile.getPath();
      setResultAndFinish(mCurrentPath);
    }
  }

  private void setResultAndFinish(String filePath) {
    Intent data = new Intent();
    data.putExtra(RESULT_FILE_PATH, filePath);
    setResult(RESULT_OK, data);
    finish();
  }
}