package com.example.notes;


import android.content.res.Resources;
import java.util.ArrayList;
import java.util.List;


public class CardSourceImpl implements CardSource {


    private List<CardData> dataSource;
    private Resources resources;

    CardSourceImpl(Resources resources) {
        dataSource = new ArrayList<>(200);
        this.resources = resources;
    }

    public CardSourceImpl init() {
        String[] titles = resources.getStringArray(R.array.titles);
        String[] descriptions = resources.getStringArray(R.array.descriptions);

        for (int i = 0; i < descriptions.length; i++) {
            dataSource.add(new CardData(titles[i], descriptions[i]));
        }
        return this;
    }


    @Override
    public CardData getCardData(int position) {
        return dataSource.get(position);
    }

    @Override
    public int size() {
        return dataSource.size();
    }

    @Override
    public void deleteCardData(int position) {
        dataSource.remove(position);
    }

    @Override
    public void updateCardData(int position, CardData cardData) {
        dataSource.set(position, cardData);
    }

    @Override
    public void addCardData(CardData cardData) {
        dataSource.add(cardData);
    }

    @Override
    public void clearCardData() {
        dataSource.clear();
    }


}
