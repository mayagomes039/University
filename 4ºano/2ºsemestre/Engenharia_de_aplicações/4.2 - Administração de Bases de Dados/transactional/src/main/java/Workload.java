import model.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Map.entry;

public class Workload {

    private final Random rand = new Random();
    private final Connection conn;
    private PreparedStatement addPlaytime, addReview, addToLibrary, addFriendship, addGame, addUser,
            getGameInfo, getGameDevelopers, getGamePublishers, getGameCategories, getGameGenres, getGameTags,
            getGameScore, getGameRecentReviews, getUserInfo, getUserTopGames, getRecentGamesPerTag, getGamesByTitle,
            getGamesSemantic;
    // considered ids for this client
    private final Map<String, List<Integer>> ids = Map.of(
            "game", new ArrayList<>(),
            "users", new ArrayList<>(),
            "developer", new ArrayList<>(),
            "publisher", new ArrayList<>(),
            "category", new ArrayList<>(),
            "tag", new ArrayList<>(),
            "genre", new ArrayList<>(),
            "query_embedding_sample", new ArrayList<>()
    );
    private final Map<Integer, List<Integer>> gamesPerUser = new HashMap<>();
    private final List<String> searchExactTokens = List.of(
            "age & of", "alien", "assassin", "battle:*", "battlefield", "borderlands", "call & of & duty",
            "civilization", "counter & strike", "cyberpunk", "dark & souls",  "dark & souls", "doom:*", "dungeon",
            "elder & scrolls", "escape", "fear", "final & fantasy", "forest",  "god & of & war", "grand & theft & auto",
            "half-life", "halo", "hero", "horizon", "left & 4 & dead",  "monster", "mortal & kombat",
            "need & for & speed", "ninja", "pirate", "rainbow & six", "red & dead",  "resident:*", "simulator",
            "stars & wars", "tomb & raider", "war", "witch", "zombie"
    );

    // weights for each transaction type
    private final List<Map.Entry<TransactionType, Integer>> transactionWeights = List.of(
        entry(TransactionType.AddPlaytime, 20),
        entry(TransactionType.ReviewGame, 5),
        entry(TransactionType.BuyGame, 10),
        entry(TransactionType.NewFriendship, 5),
        entry(TransactionType.NewGame, 1),
        entry(TransactionType.NewUser, 4),
        entry(TransactionType.GameInfo, 25),
        entry(TransactionType.GameReviews, 5),
        entry(TransactionType.UserInfo, 10),
        entry(TransactionType.RecentGamesPerTag, 10),
        entry(TransactionType.SearchGames, 5)
    );
    private final int totalWeight = transactionWeights.stream().mapToInt(Map.Entry::getValue).sum();


    public Workload(Connection c) throws SQLException {
        conn = c;
        conn.setAutoCommit(false); // autocommit = off to execute operations inside a transaction
        prepareStatements();
        populateIds();
    }


    /**
     *  Prepares the statements used in this workload
     */
    private void prepareStatements() throws SQLException {
        addPlaytime = conn.prepareStatement("""
            update library
            set playtime = playtime + ?
            where user_id = ? and game_id = ?
        """);

        addReview = conn.prepareStatement("""
            insert into review (user_id, game_id, created_date, recommend, text)
            values (?, ?, now(), ?, ?)
        """);

        addToLibrary = conn.prepareStatement("""
            insert into library (user_id, game_id, added_date, buy_price, playtime, achievements)
            select ?, ?, now(), price, 0, 0
            from game
            where id = ?
        """);

        addFriendship = conn.prepareStatement("""
            insert into friendship values (?, ?)
        """);

        addGame = conn.prepareStatement("""
            with i_game as (
                insert into game (
                    id, name, release_date, required_age, price, short_description, long_description, support_url,
                    platforms, metacritic_score, metacritic_url, achievements, languages
                ) values (default, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                returning id
            ), i_developer as (
                insert into games_developers (game_id, developer_id)
                select id, ?
                from i_game
                returning true
            ), i_publisher as (
                insert into games_publishers (game_id, publisher_id)
                select id, ?
                from i_game
                returning true
            ), i_category as (
                insert into games_categories (game_id, category_id)
                select id, ?
                from i_game
                returning true
            ), i_genre as (
                insert into games_genres (game_id, genre_id)
                select id, ?
                from i_game
                returning true
            ), i_tag as (
                insert into games_tags (game_id, tag_id)
                select id, ?
                from i_game
                returning true
            )
            select *
            from i_game, i_developer, i_publisher, i_category, i_genre, i_tag
        """);

        addUser = conn.prepareStatement("""
            insert into users (id, username, email, created_date, vac_banned, profile_description, country)
            values (default, ?, ?, ?, ?, ?, ?)
        """);

        getGameInfo = conn.prepareStatement("""
            select name, release_date, price, long_description, platforms, languages
            from game
            where id = ?
        """);

        getGameDevelopers = conn.prepareStatement("""
            select coalesce(array_agg(name), '{}')
            from developer
            join games_developers on developer_id = id
            where game_id = ?
        """);

        getGamePublishers = conn.prepareStatement("""
            select coalesce(array_agg(name), '{}')
            from publisher
            join games_publishers on publisher_id = id
            where game_id = ?
        """);

        getGameCategories = conn.prepareStatement("""
              select coalesce(array_agg(name), '{}')
              from category
              join games_categories on category_id = id
              where game_id = ?
        """);

        getGameGenres = conn.prepareStatement("""
              select coalesce(array_agg(name), '{}')
              from genre
              join games_genres on genre_id = id
              where game_id = ?
        """);

        getGameTags = conn.prepareStatement("""
            select coalesce(array_agg(name), '{}')
            from tag
            join games_tags on tag_id = id
            where game_id = ?
        """);

        getGameScore = conn.prepareStatement("""
            select round(sum(recommend::int) / count(*)::decimal * 100, 3)
            from review
            where game_id = ?
        """);

        getGameRecentReviews = conn.prepareStatement("""
            select u.id, u.username, r.created_date, r.recommend, r.text, l.playtime
            from review r
            join users u on u.id = r.user_id
            join library l on l.user_id = r.user_id and l.game_id = r.game_id
            where r.game_id = ?
            order by r.created_date desc
            limit 25
        """);

        getUserInfo = conn.prepareStatement("""
            select username, created_date, vac_banned, profile_description, country
            from users
            where id = ?;
        """);

        getUserTopGames = conn.prepareStatement("""
            select id, name, playtime
            from library
            join game on id = game_id
            where user_id = ?
            order by 3 desc
            limit 5
        """);

        getRecentGamesPerTag = conn.prepareStatement("""
            select g.id, g.name, g.release_date
            from tag t
            join games_tags gt on gt.tag_id = t.id
            join game g on g.id = gt.game_id
            where t.id = ?
            order by g.release_date desc
            limit 25
        """);

        getGamesByTitle = conn.prepareStatement("""
            select id, name, release_date
            from game
            where to_tsvector('english', name) @@ to_tsquery('english', ?)
            limit 25
        """);

        getGamesSemantic = conn.prepareStatement("""
            select g.id, g.name, g.release_date
            from game g
            join game_search_embedding gse on gse.game_id = g.id
            join query_embedding_sample qes on true
            where qes.id = ?
            order by gse.embedding <-> qes.embedding
            limit 25;
        """);
    }


    /**
     * Populates each entity with random identifiers
     */
    private void populateIds() throws SQLException {
        Statement s = this.conn.createStatement();

        for (String table: ids.keySet()) {
            if (table.equals("users")) {
                continue;
            }

            ResultSet rs = s.executeQuery("select id from " + table + " order by random() limit 100000");
            while (rs.next()) {
                ids.get(table).add(rs.getInt(1));
            }
        }

        ResultSet rs = s.executeQuery("""
            select id, game_id
            from (
                select id
                from users
                where not vac_banned
                order by random()
                limit 100000
            ) t
            left join library on user_id = id
        """);

        Set<Integer> usersIds = new HashSet<>();
        while (rs.next()) {
            int userId = rs.getInt(1);
            int gameId = rs.getInt(2);

            if (!usersIds.contains(userId)) {
                ids.get("users").add(userId);
                usersIds.add(userId);
            }

            if (!gamesPerUser.containsKey(userId)) {
                gamesPerUser.put(userId, new ArrayList<>());
            }

            gamesPerUser.get(userId).add(gameId);
        }
    }


    /**
     * Adds playtime to some user/game pair
     */
    private void addPlaytime() throws SQLException {
        int userId = Utils.randomElement(ids.get("users"));
        int gameId = Utils.randomElement(gamesPerUser.get(userId));
        int playtime = rand.nextInt(180);

        Utils.setPreparedStatementArgs(addPlaytime, playtime, userId, gameId);
        addPlaytime.executeUpdate();
        conn.commit();
    }


    /**
     * Adds a new review to a game
     */
    private void reviewGame() throws SQLException {
        int userId = Utils.randomElement(ids.get("users"));
        int gameId = Utils.randomElement(gamesPerUser.get(userId));
        boolean recommend = rand.nextBoolean();
        String text = Utils.randomString(50);

        Utils.setPreparedStatementArgs(addReview, userId, gameId, recommend, text);
        addReview.executeUpdate();
        conn.commit();
    }


    /**
     * Adds a new game to a user's library
     */
    private void buyGame() throws SQLException {
        int userId = Utils.randomElement(ids.get("users"));
        int gameId = Utils.randomElement(ids.get("game"));

        Utils.setPreparedStatementArgs(addToLibrary, userId, gameId, gameId);
        addToLibrary.executeUpdate();
        conn.commit();
    }


    /**
     * Creates a friendship between two users
     */
    private void newFriendship() throws SQLException {
        int userId1 = Utils.randomElement(ids.get("users"));
        int userId2 = Utils.randomElement(ids.get("users"));
        while (userId1 == userId2) {
            userId2 = Utils.randomElement(ids.get("users"));
        }

        int min = Math.min(userId1, userId2);
        int max = Math.max(userId1, userId2);
        Utils.setPreparedStatementArgs(addFriendship, min, max);
        addFriendship.executeUpdate();
        conn.commit();
    }


    /**
     * Registers a new game
     */
    private void newGame() throws SQLException {
        String name = Utils.randomString(10);
        LocalDate releaseDate = LocalDate.now();
        int requiredAge = 12;
        double price = rand.nextInt(70);
        String shortDescription = Utils.randomString(50);
        String longDescription = Utils.randomString(200);
        String supportUrl = Utils.randomString(30);
        List<String> platforms = List.of("windows", "mac", "linux");
        int metacriticScore = rand.nextInt(100);
        String metacriticUrl = "http://metacritic.com/game/" + name;
        int achievements = rand.nextInt(100);
        List<String> languages = List.of("English");
        int developerId = Utils.randomElement(ids.get("developer"));
        int publisherId = Utils.randomElement(ids.get("publisher"));
        int categoryId = Utils.randomElement(ids.get("category"));
        int genreId = Utils.randomElement(ids.get("genre"));
        int tagId = Utils.randomElement(ids.get("tag"));

        Utils.setPreparedStatementArgs(
                addGame, name, releaseDate, requiredAge, price, shortDescription, longDescription, supportUrl,
                platforms, metacriticScore, metacriticUrl, achievements, languages, developerId, publisherId,
                categoryId, genreId, tagId
        );
        addGame.execute();
        conn.commit();
    }


    /**
     * Registers a new user
     */
    private void newUser() throws SQLException {
        String username = Utils.randomString(10);
        String email = username + "@email.com";
        LocalDate createdDate = LocalDate.now();
        boolean vacBanned = false;
        String profileDescription = Utils.randomString(100);
        String country = "PT";

        Utils.setPreparedStatementArgs(addUser, username, email, createdDate, vacBanned, profileDescription, country);
        addUser.executeUpdate();
        conn.commit();
    }


    /**
     * Returns information about a game
     */
    private Game gameInfo() throws SQLException {
        int gameId = Utils.randomElement(ids.get("game"));
        Utils.setPreparedStatementArgs(getGameInfo, gameId);
        Utils.setPreparedStatementArgs(getGameDevelopers, gameId);
        Utils.setPreparedStatementArgs(getGamePublishers, gameId);
        Utils.setPreparedStatementArgs(getGameCategories, gameId);
        Utils.setPreparedStatementArgs(getGameGenres, gameId);
        Utils.setPreparedStatementArgs(getGameTags, gameId);

        ResultSet rs = getGameInfo.executeQuery();
        rs.next();
        String name = rs.getString(1);
        LocalDate releaseDate = rs.getDate(2).toLocalDate();
        double price = rs.getDouble(3);
        String description = rs.getString(4);
        String platforms = rs.getString(5);
        String languages = rs.getString(6);

        List<String> developers = new ArrayList<>();
        rs = getGameDevelopers.executeQuery();
        if (rs.next()) {
            developers = Arrays.stream(((String[]) rs.getArray(1).getArray())).toList();
        }

        List<String> publishers = new ArrayList<>();
        rs = getGamePublishers.executeQuery();
        if (rs.next()) {
            publishers = Arrays.stream(((String[]) rs.getArray(1).getArray())).toList();
        }

        List<String> categories = new ArrayList<>();
        rs = getGameCategories.executeQuery();
        if (rs.next()) {
            categories = Arrays.stream(((String[]) rs.getArray(1).getArray())).toList();
        }

        List<String> genres = new ArrayList<>();
        rs = getGameGenres.executeQuery();
        if (rs.next()) {
            genres = Arrays.stream(((String[]) rs.getArray(1).getArray())).toList();
        }

        List<String> tags = new ArrayList<>();
        rs = getGameTags.executeQuery();
        if (rs.next()) {
            tags = Arrays.stream(((String[]) rs.getArray(1).getArray())).toList();
        }

        conn.commit();
        return new Game(
                gameId, name, releaseDate, price, description, platforms, languages, developers, publishers, categories,
                genres, tags
        );
    }


    /**
     * Returns the overall score and recent reviews about a game
     */
    private GameReviews gameReviews() throws SQLException {
        int gameId = Utils.randomElement(ids.get("game"));
        Utils.setPreparedStatementArgs(getGameScore, gameId);
        Utils.setPreparedStatementArgs(getGameRecentReviews, gameId);

        ResultSet rs = getGameScore.executeQuery();
        rs.next();
        double overallScore = rs.getDouble(1);

        rs = getGameRecentReviews.executeQuery();
        List<Review> recentReviews = new ArrayList<>();
        while (rs.next()) {
            int userId = rs.getInt(1);
            String username = rs.getString(2);
            LocalDateTime createdDate = rs.getTimestamp(3).toLocalDateTime();
            boolean recommend = rs.getBoolean(4);
            String text = rs.getString(5);
            int playtime = rs.getInt(6);
            recentReviews.add(new Review(userId, username, createdDate, recommend, text, playtime));
        }

        conn.commit();
        return new GameReviews(overallScore, recentReviews);
    }


    /**
     * Returns information about a user
     */
    private User userInfo() throws SQLException {
        int userId = Utils.randomElement(ids.get("users"));
        Utils.setPreparedStatementArgs(getUserInfo, userId);
        Utils.setPreparedStatementArgs(getUserTopGames, userId);

        ResultSet rs = getUserInfo.executeQuery();
        rs.next();
        String username = rs.getString(1);
        LocalDate createdDate = rs.getDate(2).toLocalDate();
        boolean vacBanned = rs.getBoolean(3);
        String profileDescription = rs.getString(4);
        String country = rs.getString(5);

        rs = getUserTopGames.executeQuery();
        List<UserProfileGame> topGames = new ArrayList<>();
        while (rs.next()) {
            int gameId = rs.getInt(1);
            String name = rs.getString(2);
            int playtime = rs.getInt(3);
            topGames.add(new UserProfileGame(gameId, name, playtime));
        }

        conn.commit();
        return new User(userId, username, createdDate, vacBanned, profileDescription, country, topGames);
    }


    /**
     * Returns the most recent games for some tag
     */
    private List<GameLite> recentGamesPerTag() throws SQLException {
        int tagId = Utils.randomElement(ids.get("tag"));
        Utils.setPreparedStatementArgs(getRecentGamesPerTag, tagId);

        ResultSet rs = getRecentGamesPerTag.executeQuery();
        List<GameLite> games = new ArrayList<>();
        while (rs.next()) {
            int gameId = rs.getInt(1);
            String name = rs.getString(2);
            LocalDate releaseDate = rs.getDate(3).toLocalDate();
            games.add(new GameLite(gameId, name, releaseDate));
        }

        conn.commit();
        return games;
    }


    /**
     * Searches games based on an exact title match (exact=true) or based on semantic information (exact=false)
     */
    private List<GameLite> searchGames(boolean exact) throws SQLException {
        ResultSet rs;
        if (exact) {
            String token = Utils.randomElement(searchExactTokens);
            Utils.setPreparedStatementArgs(getGamesByTitle, token);
            rs = getGamesByTitle.executeQuery();
        } else {
            int semanticQueryId = Utils.randomElement(ids.get("query_embedding_sample"));
            Utils.setPreparedStatementArgs(getGamesSemantic, semanticQueryId);
            rs = getGamesSemantic.executeQuery();
        }

        List<GameLite> games = new ArrayList<>();
        while (rs.next()) {
            int gameId = rs.getInt(1);
            String name = rs.getString(2);
            LocalDate releaseDate = rs.getDate(3).toLocalDate();
            games.add(new GameLite(gameId, name, releaseDate));
        }

        conn.commit();
        return games;
    }


    /**
     * Randomly selects a TransactionType, based on the weights in this.transactionWeights
     */
    private TransactionType getRandomTransaction() throws NullPointerException {
        int r = rand.nextInt(0, totalWeight);
        int curr = 0;

        for (var entry: transactionWeights) {
            if (r < entry.getValue() + curr) {
                return entry.getKey();
            }
            curr += entry.getValue();
        }

        throw new NullPointerException();
    }


    /**
     * Executes a random transaction
     */
    public TransactionType transaction() throws Exception {
        var type = getRandomTransaction();

        switch (type) {
            case AddPlaytime -> addPlaytime();
            case ReviewGame -> reviewGame();
            case BuyGame -> buyGame();
            case NewFriendship -> newFriendship();
            case NewGame -> newGame();
            case NewUser -> newUser();
            case GameInfo -> gameInfo();
            case GameReviews -> gameReviews();
            case UserInfo -> userInfo();
            case RecentGamesPerTag -> recentGamesPerTag();
            case SearchGames -> searchGames(rand.nextBoolean());
        }

        return type;
    }
}
