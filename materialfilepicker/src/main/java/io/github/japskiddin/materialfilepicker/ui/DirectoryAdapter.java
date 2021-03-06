package io.github.japskiddin.materialfilepicker.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import io.github.japskiddin.materialfilepicker.FileItem;
import io.github.japskiddin.materialfilepicker.R;
import io.github.japskiddin.materialfilepicker.utils.FileTypeUtils;
import java.io.File;
import java.util.List;

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.DirectoryViewHolder> {
  public interface OnItemClickListener {
    void onItemClick(View view, int position);
  }

  public static class DirectoryViewHolder extends RecyclerView.ViewHolder {
    private ImageView mFileImage;
    private TextView mFileTitle;
    private TextView mFileSubtitle;

    public DirectoryViewHolder(View itemView, final OnItemClickListener clickListener) {
      super(itemView);

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          clickListener.onItemClick(v, getAdapterPosition());
        }
      });

      mFileImage = itemView.findViewById(R.id.item_file_image);
      mFileTitle = itemView.findViewById(R.id.item_file_title);
      mFileSubtitle = itemView.findViewById(R.id.item_file_subtitle);
    }
  }

  private List<FileItem> mFiles;
  private Context mContext;
  private OnItemClickListener mOnItemClickListener;
  private boolean isHome;

  public DirectoryAdapter(Context context, List<FileItem> files, boolean isHome) {
    mContext = context;
    mFiles = files;
    this.isHome = isHome;
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    mOnItemClickListener = listener;
  }

  @NonNull
  @Override public DirectoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);

    return new DirectoryViewHolder(view, mOnItemClickListener);
  }

  @Override public void onBindViewHolder(@NonNull DirectoryViewHolder holder, int position) {
    FileItem currentFile = mFiles.get(position);

    FileTypeUtils.FileType fileType =
        FileTypeUtils.getFileType(new File(currentFile.getFilePath()));
    int drawableId;
    if (isHome) {
      if (currentFile.getFilePath()
          .equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
        drawableId = R.drawable.ic_hard_disk;
      } else {
        drawableId = R.drawable.ic_sd_storage_black;
      }
    } else {
      drawableId = fileType.getIcon();
    }
    Drawable drawable = VectorDrawableCompat.create(mContext.getResources(),
        drawableId,
        mContext.getTheme());
    holder.mFileImage.setImageDrawable(drawable);
    holder.mFileSubtitle.setText(fileType.getDescription());
    holder.mFileTitle.setText(currentFile.getFileName());
  }

  @Override public int getItemCount() {
    return mFiles.size();
  }

  public File getModel(int index) {
    return new File(mFiles.get(index).getFilePath());
  }
}