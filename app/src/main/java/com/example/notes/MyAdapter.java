package com.example.notes;


import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private final String TAG = "MyAdapter";
    private CardSource dataSource;
    private Activity activity;
    private OnItemClickListener itemClickListener;  // Слушатель будет устанавливаться извне


    private int menuPosition;

    public int getMenuPosition() {
        return menuPosition;
    }

    // Передаём в конструктор источник данных
    // В нашем случае это массив, но может быть и запрос к БД
    public MyAdapter(CardSource dataSource, Activity activity) {
        this.dataSource = dataSource;
        this.activity = activity;
           }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Создаём новый элемент пользовательского интерфейса
        // Через Inflater
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        Log.d(TAG, "onBindViewHolder");
        // Здесь можно установить всякие параметры
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
// Получить элемент из источника данных (БД, интернет...)
        // Вынести на экран, используя ViewHolder
        holder.setData(dataSource.getCardData(position));
        Log.d(TAG, "onBindViewHolder");
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    // Сеттер слушателя нажатий
    public void SetOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    // Интерфейс для обработки нажатий, как в ListView
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    // Этот класс хранит связь между данными и элементами View
    // Сложные данные могут потребовать несколько View на
    // один пункт списка
    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView description;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);

            registerContextMenu(itemView);

            // Обработчик нажатий на этом ViewHolder
            description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(v, getAdapterPosition());
                    }
                }
            });

            // Обработчик нажатий на картинке
            description.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    menuPosition = getLayoutPosition();
                    itemView.showContextMenu(10, 10);
                    return true;
                }
            });
        }

        private void registerContextMenu(@NonNull View itemView) {
            if (activity != null) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        menuPosition = getLayoutPosition();
                        return false;
                    }
                });
                activity.registerForContextMenu(itemView);
            }
        }

        public void setData(CardData cardData) {
            title.setText(cardData.getTitle());
            description.setText(cardData.getDescription());
        }
    }

}
