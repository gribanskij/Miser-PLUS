package com.gribanskij.miserplus.categories_screen;

/**
 * Created by sesa175711 on 25.10.2016.
 */
public class Categories {

    private String category_name;
    private float category_sum;

    public Categories(String category_name, float category_sum) {
        this.category_name = category_name;
        this.category_sum = category_sum;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String name) {
        category_name = name;
    }

    public Float getCategory_sum() {
        return category_sum;
    }

    public void setCategory_sum(float sum) {
        category_sum = sum;
    }

    public String toString() {
        return this.category_name;
    }
}
