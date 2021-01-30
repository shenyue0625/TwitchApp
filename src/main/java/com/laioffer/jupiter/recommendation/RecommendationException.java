package com.laioffer.jupiter.recommendation;

//由于recommend的时候（用itemRecommender）调用了TwitchClient和MySQLConnection，防止出错
public class RecommendationException extends RuntimeException {
    public RecommendationException(String errorMessage) {
        super(errorMessage);
    }
}
