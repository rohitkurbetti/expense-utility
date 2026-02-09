package com.example.expenseutility.entityadapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.expenseutility.R;
import com.example.expenseutility.dto.NoteContent;
import com.example.expenseutility.dto.NoteType;

import java.util.List;

public class CanvasAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<NoteContent> items;
    private OnContentClickListener listener;

    public CanvasAdapter(Context context, List<NoteContent> items) {
        this.context = context;
        this.items = items;
    }

    public void setOnContentClickListener(OnContentClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= 0 && position < items.size()) {
            NoteContent item = items.get(position);
            return item.getType().ordinal();
        }
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        NoteType type = NoteType.values()[viewType];
        switch (type) {
            case IMAGE:
                view = inflater.inflate(R.layout.layout_canvas_image, parent, false);
                return new ImageViewHolder(view);
            case LINK:
                view = inflater.inflate(R.layout.layout_canvas_link, parent, false);
                return new LinkViewHolder(view);
            case DOCUMENT:
                view = inflater.inflate(R.layout.layout_canvas_document, parent, false);
                return new DocumentViewHolder(view);
            case LIST:
                view = inflater.inflate(R.layout.layout_canvas_checklist, parent, false);
                return new ChecklistViewHolder(view);
            case DRAWING:
                view = inflater.inflate(R.layout.layout_canvas_drawing, parent, false);
                return new DrawingViewHolder(view);
            default: // TEXT
                view = inflater.inflate(R.layout.layout_canvas_text, parent, false);
                return new TextViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= 0 && position < items.size()) {
            NoteContent item = items.get(position);

            switch (holder.getItemViewType()) {
                case 0: // TEXT
                    bindTextViewHolder((TextViewHolder) holder, item, position);
                    break;
                case 1: // IMAGE
                    bindImageViewHolder((ImageViewHolder) holder, item, position);
                    break;
                case 2: // LINK
                    bindLinkViewHolder((LinkViewHolder) holder, item, position);
                    break;
                case 3: // DOCUMENT
                    bindDocumentViewHolder((DocumentViewHolder) holder, item, position);
                    break;
                case 4: // LIST
                    bindChecklistViewHolder((ChecklistViewHolder) holder, item, position);
                    break;
                case 5: // DRAWING
                    bindDrawingViewHolder((DrawingViewHolder) holder, item, position);
                    break;
            }
        }
    }

    private void bindTextViewHolder(TextViewHolder holder, NoteContent item, int position) {
        if (item.getContent() != null) {
            holder.textContent.setText(item.getContent());
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null && position >= 0 && position < items.size()) {
                listener.onContentDeleted(position);
            }
        });
    }

    private void bindImageViewHolder(ImageViewHolder holder, NoteContent item, int position) {
        try {
            if (item.getFilePath() != null && !item.getFilePath().isEmpty()) {
                Uri imageUri = Uri.parse(item.getFilePath());
                Glide.with(context)
                        .load(imageUri)
                        .placeholder(android.R.drawable.ic_menu_camera)
                        .into(holder.imageContent);
            } else {
                holder.imageContent.setImageResource(android.R.drawable.ic_menu_camera);
            }
        } catch (Exception e) {
            e.printStackTrace();
            holder.imageContent.setImageResource(android.R.drawable.ic_menu_camera);
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null && position >= 0 && position < items.size()) {
                listener.onContentDeleted(position);
            }
        });
    }

    private void bindLinkViewHolder(LinkViewHolder holder, NoteContent item, int position) {
        if (item.getContent() != null) {
            holder.linkText.setText(item.getContent());

            View.OnClickListener openUrlListener = v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getContent()));
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            holder.linkText.setOnClickListener(openUrlListener);
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null && position >= 0 && position < items.size()) {
                listener.onContentDeleted(position);
            }
        });
    }

    private void bindDocumentViewHolder(DocumentViewHolder holder, NoteContent item, int position) {
        if (item.getContent() != null) {
            holder.documentName.setText(item.getContent());
        }

        holder.openButton.setOnClickListener(v -> {
            if (item.getFilePath() != null) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(item.getFilePath()), "application/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null && position >= 0 && position < items.size()) {
                listener.onContentDeleted(position);
            }
        });
    }

    private void bindChecklistViewHolder(ChecklistViewHolder holder, NoteContent item, int position) {
        // Simple checklist implementation - just show the text
        if (item.getContent() != null) {
            holder.checklistText.setText(item.getContent());
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null && position >= 0 && position < items.size()) {
                listener.onContentDeleted(position);
            }
        });
    }

    private void bindDrawingViewHolder(DrawingViewHolder holder, NoteContent item, int position) {
        holder.drawingTitle.setText("Drawing");

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null && position >= 0 && position < items.size()) {
                listener.onContentDeleted(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // Update data method
    public void updateData(List<NoteContent> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    // Add item method
    public void addItem(NoteContent item) {
        if (items != null) {
            items.add(item);
            notifyItemInserted(items.size() - 1);
        }
    }

    // Remove item method
    public void removeItem(int position) {
        if (items != null && position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public interface OnContentClickListener {
        void onContentDeleted(int position);

        void onContentUpdated(int position, NoteContent content);

        void onAddChecklistItem(int position, String item);
    }

    // ViewHolder classes - SIMPLIFIED VERSION
    public static class TextViewHolder extends RecyclerView.ViewHolder {
        TextView textContent;
        ImageButton deleteButton;

        public TextViewHolder(View itemView) {
            super(itemView);
            textContent = itemView.findViewById(R.id.text_content);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageContent;
        ImageButton deleteButton;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageContent = itemView.findViewById(R.id.image_content);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }

    public static class LinkViewHolder extends RecyclerView.ViewHolder {
        TextView linkText;
        ImageButton deleteButton;

        public LinkViewHolder(View itemView) {
            super(itemView);
            linkText = itemView.findViewById(R.id.link_text);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }

    public static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView documentName;
        Button openButton;
        ImageButton deleteButton;

        public DocumentViewHolder(View itemView) {
            super(itemView);
            documentName = itemView.findViewById(R.id.document_name);
            openButton = itemView.findViewById(R.id.btn_open);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }

    public static class ChecklistViewHolder extends RecyclerView.ViewHolder {
        TextView checklistText;
        ImageButton deleteButton;

        public ChecklistViewHolder(View itemView) {
            super(itemView);
            checklistText = itemView.findViewById(R.id.checklist_text);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }

    public static class DrawingViewHolder extends RecyclerView.ViewHolder {
        TextView drawingTitle;
        ImageButton deleteButton;

        public DrawingViewHolder(View itemView) {
            super(itemView);
            drawingTitle = itemView.findViewById(R.id.drawing_title);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}