package io.github.japskiddin.materialfilepicker.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
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

  private List<File> mFiles;
  private Context mContext;
  private OnItemClickListener mOnItemClickListener;

  public DirectoryAdapter(Context context, List<File> files) {
    mContext = context;
    mFiles = files;
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
    File currentFile = mFiles.get(position);

    FileTypeUtils.FileType fileType = FileTypeUtils.getFileType(currentFile);
    Drawable drawable = VectorDrawableCompat.create(mContext.getResources(), fileType.getIcon(),
        mContext.getTheme());
    holder.mFileImage.setImageDrawable(drawable);
    holder.mFileSubtitle.setText(fileType.getDescription());
    holder.mFileTitle.setText(currentFile.getName());
  }

  @Override public int getItemCount() {
    return mFiles.size();
  }

  public File getModel(int index) {
    return mFiles.get(index);
  }
}