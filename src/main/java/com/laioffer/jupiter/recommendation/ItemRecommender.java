package com.laioffer.jupiter.recommendation;
import com.laioffer.jupiter.entity.Game;
import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.entity.ItemType;
import com.laioffer.jupiter.external.TwitchClient;
import com.laioffer.jupiter.external.TwitchException;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.db.MySQLException;
import java.util.*;

public class ItemRecommender {
    private static final int DEFAULT_GAME_LIMIT = 3; //新用户/没有user interaction的推荐3个
    private static final int DEFAULT_PER_GAME_RECOMMENDATION_LIMIT = 10;
    private static final int DEFAULT_TOTAL_RECOMMENDATION_LIMIT = 20;

    //helper方法：The recommendation is purely based-on top games returned by Twitch。topgames应该有20个
    private List<Item> recommendByTopGames(ItemType type, List<Game> topGames) throws RecommendationException {
        List<Item> recommendedItems = new ArrayList<>();
        TwitchClient client = new TwitchClient();

        outerloop:
        for (Game game : topGames) {
            List<Item> items;
            try {
                //对应的一个game id，同一类型只返回10个item
                items = client.searchByType(game.getId(), type, DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
            } catch (TwitchException e) {
                throw new RecommendationException("Failed to get recommendation result");
            }
            for (Item item : items) {
                //一共只推荐20个item，reach limit则跳出loop
                if (recommendedItems.size() == DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
                    break outerloop;
                }
                recommendedItems.add(item);
            }
        }
        return recommendedItems;
    }

    //helper方法：调用了MySQLConnection里面的方法，拿到的favoriteGameIds，favoritedItemIds。
    //把拿到的favoriteGameIds按照hashmap形式，并且count从大到小sort一下。根据game id来推荐
    private List<Item> recommendByFavoriteHistory(
            Set<String> favoritedItemIds, List<String> favoriteGameIds, ItemType type) throws RecommendationException {
        //favoritedGameIds -> [gam， game， game] -> { 1234:3, 2345:1} 因为GameId可能会重复，需要去重,并按照counting的顺序从大到小排序。因为我们想要按照gameid出现的次数最多的那个game 来匹配相似度，进而推荐
        // Collectors.groupingBy：把array里面相同的元素变成一个组， key就是这个元素，value就是出现的次数
        //Function.identity()：key /也可以写str -> str（lambda func）。Collectors.counting()：value
        Map<String, Long> favoriteGameIdByCount = favoriteGameIds.parallelStream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        //把favoriteGameIdByCount（上面的hashmap）转换回一个list，用list自带的sort方法，按照count的大->小顺序排序。此处的sort方法有很多种方式，是algorithm，只是本项目sean用了这种方法
        List<Map.Entry<String, Long>> sortedFavoriteGameIdListByCount = new ArrayList<>(
                favoriteGameIdByCount.entrySet());
        sortedFavoriteGameIdListByCount.sort((Map.Entry<String, Long> e1, Map.Entry<String, Long> e2) -> Long
                .compare(e2.getValue(), e1.getValue()));

        //如果返回的{ 1234:3, 2345:1 。。。。。}特别多，就返回3个
        if (sortedFavoriteGameIdListByCount.size() > DEFAULT_GAME_LIMIT) {
            sortedFavoriteGameIdListByCount = sortedFavoriteGameIdListByCount.subList(0, DEFAULT_GAME_LIMIT);
        }

        //最后要返回的List<Item> 推荐内容
        List<Item> recommendedItems = new ArrayList<>();
        TwitchClient client = new TwitchClient();

        outerloop:
        for (Map.Entry<String, Long> favoriteGame : sortedFavoriteGameIdListByCount) {
            List<Item> items;
            try {//还是要从twitch backend来调取game id 相似度所提供的item推荐，一个game id和类型只推荐10个item
                items = client.searchByType(favoriteGame.getKey(), type, DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
            } catch (TwitchException e) {
                throw new RecommendationException("Failed to get recommendation result");
            }

            for (Item item : items) {
                //一共只推荐20个item，reach limit则跳出loop
                if (recommendedItems.size() == DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
                    break outerloop;
                }
                //如果用户已经favorite这个item了，就不推荐了。此处优化一下
                if (!favoritedItemIds.contains(item.getId())) {
                    recommendedItems.add(item);
                }
            }
        }
        return recommendedItems;
    }

    //主要方法：根据用户favorite的内容（gameid）来分析要推荐的item。
    //用户只给了userid，但是可以调用了MySQLConnection里传回来的用户favorite信息
    public Map<String, List<Item>> recommendItemsByUser(String userId) throws RecommendationException {
        Map<String, List<Item>> recommendedItemMap = new HashMap<>();
        Set<String> favoriteItemIds;
        Map<String, List<String>> favoriteGameIds;//{"VIDEO":"1234,1234,3456", "STREAM":"3456"....} -> {"1234(gameid)": "VIDEO（type）", "3456": "VIDEO, STREAM"}
        MySQLConnection connection = null;
        try {
            connection = new MySQLConnection();
            favoriteItemIds = connection.getFavoriteItemIds(userId);
            favoriteGameIds = connection.getFavoriteGameIds(favoriteItemIds);
        } catch (MySQLException e) {
            throw new RecommendationException("Failed to get user favorite history for recommendation");
        } finally {
            connection.close();
        }

        for (Map.Entry<String, List<String>> entry : favoriteGameIds.entrySet()) {
            if (entry.getValue().size() == 0) {//如果用户虽然登陆了，但是却没有favorite过任何东西，就从twitch调取top games
                TwitchClient client = new TwitchClient();
                List<Game> topGames;
                try {
                    topGames = client.topGames(DEFAULT_GAME_LIMIT);
                } catch (TwitchException e) {
                    throw new RecommendationException("Failed to get game data for recommendation");
                }//类似下面的方法，详情看下面
                recommendedItemMap.put(entry.getKey(), recommendByTopGames(ItemType.valueOf(entry.getKey()), topGames));
            } else {//如果用户登陆，就调用本文的helper，从MySQLConnection查看favorited game ID来推荐
                recommendedItemMap.put(entry.getKey(), recommendByFavoriteHistory(favoriteItemIds, entry.getValue(), ItemType.valueOf(entry.getKey())));
            }
        }
        return recommendedItemMap;
    }

    //主要方法：调用了TwitchClient里的topGames方法拿到了List<Game>，然后传给本文中的recommendByTopGames方法（新用户/没有user interaction）
    public Map<String, List<Item>> recommendItemsByDefault() throws RecommendationException {
        Map<String, List<Item>> recommendedItemMap = new HashMap<>();
        TwitchClient client = new TwitchClient();
        List<Game> topGames;
        try {
            topGames = client.topGames(DEFAULT_GAME_LIMIT);
        } catch (TwitchException e) {
            throw new RecommendationException("Failed to get game data for recommendation");
        }

        for (ItemType type : ItemType.values()) {
            recommendedItemMap.put(type.toString(), recommendByTopGames(type, topGames));
        }
        //最后返回的格式： {video：List<Item>}, {stream：List<Item>},{clips：List<Item>}
        return recommendedItemMap;
    }
}
