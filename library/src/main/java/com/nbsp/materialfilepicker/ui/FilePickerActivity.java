package com.nbsp.materialfilepicker.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nbsp.materialfilepicker.R;
import com.nbsp.materialfilepicker.filter.CompositeFilter;
import com.nbsp.materialfilepicker.filter.PatternFilter;
import com.nbsp.materialfilepicker.utils.FileUtils;
import com.nbsp.materialfilepicker.widget.EmptyRecyclerView;
import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * Created by Dimorinny on 24.10.15.
 */
public class FilePickerActivity extends AppCompatActivity {
  public static final String ARG_START_PATH = "arg_start_path";
  public static final String ARG_CURRENT_PATH = "arg_current_path";

  public static final String ARG_FILTER = "arg_filter";
  public static final String ARG_CLOSEABLE = "arg_closeable";
  public static final String ARG_TITLE = "arg_title";

  public static final String STATE_START_PATH = "state_start_path";
  private static final String STATE_CURRENT_PATH = "state_current_path";

  public static final String RESULT_FILE_PATH = "result_file_path";
  private static final int HANDLE_CLICK_DELAY = 150;

  private Toolbar mToolbar;
  private EmptyRecyclerView mDirectoryRecyclerView;
  private DirectoryAdapter mDirectoryAdapter;
  private View mEmptyView;
  private String mStartPath = Environment.getExternalStorageDirectory().getAbsolutePath();
  private String mCurrentPath = mStartPath;
  private CharSequence mTitle;

  private Boolean mCloseable;

  private CompositeFilter mFilter;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_file_picker);

    initArguments(savedInstanceState);
    initViews();
    initToolbar();
    initFilesList();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem menuItem) {
    if (menuItem.getItemId() == R.id.action_select) {
      setResultAndFinish(mCurrentPath);
    } else if (menuItem.getItemId() == R.id.action_add_dir) {
      showNewFolderDialog();
    } else if (menuItem.getItemId() == android.R.id.home) {
      backClick();
    }

    return super.onOptionsItemSelected(menuItem);
  }

  @Override public void onBackPressed() {
    backClick();
  }

  @Override public void onSaveInstanceState(Bundle outState) {
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
      updateTitle();
    } else {
      if (getIntent().hasExtra(ARG_START_PATH)) {
        mStartPath = getIntent().getStringExtra(ARG_START_PATH);
        mCurrentPath = mStartPath;
      }

      if (getIntent().hasExtra(ARG_CURRENT_PATH)) {
        String currentPath = getIntent().getStringExtra(ARG_CURRENT_PATH);

        if (currentPath.startsWith(mStartPath)) {
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
  }

  private void initToolbar() {
    setSupportActionBar(mToolbar);

    // Show back button
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // Truncate start of path
    try {
      Field f;
      if (TextUtils.isEmpty(mTitle)) {
        f = mToolbar.getClass().getDeclaredField("mTitleTextView");
      } else {
        f = mToolbar.getClass().getDeclaredField("mSubtitleTextView");
      }

      f.setAccessible(true);
      TextView textView = (TextView) f.get(mToolbar);
      textView.setEllipsize(TextUtils.TruncateAt.START);
    } catch (Exception ignored) {
    }

    if (!TextUtils.isEmpty(mTitle)) {
      setTitle(mTitle);
    }
    updateTitle();
  }

  private void initViews() {
    mToolbar = (Toolbar) findViewById(R.id.toolbar);
    mDirectoryRecyclerView = (EmptyRecyclerView) findViewById(R.id.directory_recycler_view);
    mEmptyView = findViewById(R.id.directory_empty_view);
  }

  private void updateTitle() {
    if (getSupportActionBar() != null) {
      String titlePath = mCurrentPath.isEmpty() ? "/" : mCurrentPath;
      if (TextUtils.isEmpty(mTitle)) {
        getSupportActionBar().setTitle(titlePath);
      } else {
        getSupportActionBar().setSubtitle(titlePath);
      }
    }
  }

  private void initFilesList() {
    mDirectoryAdapter =
        new DirectoryAdapter(this, FileUtils.getFileListByDirPath(mCurrentPath, mFilter));

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

  private void backClick() {
    if (!mCurrentPath.equals(mStartPath)) {
      mCurrentPath = FileUtils.cutLastSegmentOfPath(mCurrentPath);
      updateTitle();
      initFilesList();
    } else {
      setResult(RESULT_CANCELED);
      finish();
    }
  }

  private void showNewFolderDialog() {
    final LayoutInflater layoutInflater = this.getLayoutInflater();
    LinearLayout linearLayout =
        (LinearLayout) layoutInflater.inflate(R.layout.dialog_new_dir, null);
    final EditText dirName = (EditText) linearLayout.findViewById(R.id.et_dirName);
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
    if (!dirName.equals("")) {
      File dir;

      if (Build.VERSION.SDK_INT
          > Build.VERSION_CODES.N_MR1) { //TODO: разобраться, почему папка не создаётся
        dir = new File(new File(FileUtils.cutLastSegmentOfPath(mCurrentPath),
            FileUtils.getCurrentDir(mCurrentPath)), dirName);
      } else {
        dir = new File(mCurrentPath + "/" + dirName);
      }

      dir.mkdirs();
      initFilesList();
    }
  }

  private void handleFileClicked(final File clickedFile) {
    if (clickedFile.isDirectory()) {
      mCurrentPath = clickedFile.getPath();
      // If the user wanna go to the emulated directory, he will be taken to the
      // corresponding user emulated folder.
      if (mCurrentPath.equals("/storage/emulated")) {
        mCurrentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
      }

      updateTitle();
      initFilesList();
    }
  }

  private void setResultAndFinish(String filePath) {
    Intent data = new Intent();
    data.putExtra(RESULT_FILE_PATH, filePath);
    setResult(RESULT_OK, data);
    finish();
  }
}
