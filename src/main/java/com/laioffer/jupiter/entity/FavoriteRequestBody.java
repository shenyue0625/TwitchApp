package com.laioffer.jupiter.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;


public class FavoriteRequestBody {
    private final Item favoriteItem;

    @JsonCreator//JSON ->  REQUEST BODY的对象(OBJECT)
    //只是单向convert，不需要返回favorite信息
    public FavoriteRequestBody(@JsonProperty("favorite") Item favoriteItem) {//@JsonProperty("favorite")是把前端的favorite读取进来，只读取favorite里面的值
        this.favoriteItem = favoriteItem;
    }

    public Item getFavoriteItem() {
        return favoriteItem;
    }
}
