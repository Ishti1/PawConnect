import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateTables {
    public static void main(String[] args) {
        String url = "jdbc:mysql://catconnect-db-pawconnect.j.aivencloud.com:25039/defaultdb?sslmode=require";
        String user = "avnadmin";
        String pass = "AVNS_lBZKwBnWOlCB0iYRdJK";
        
        String[] queries = {
            "CREATE TABLE IF NOT EXISTS vet_likes (vet_id BIGINT NOT NULL, user_id BIGINT);",
            "CREATE TABLE IF NOT EXISTS shop_likes (shop_id BIGINT NOT NULL, user_id BIGINT);",
            "CREATE TABLE IF NOT EXISTS shelter_likes (shelter_id BIGINT NOT NULL, user_id BIGINT);",
            "CREATE TABLE IF NOT EXISTS meme_likes (meme_id BIGINT NOT NULL, user_id BIGINT);",
            "CREATE TABLE IF NOT EXISTS adoption_likes (adoption_id BIGINT NOT NULL, user_id BIGINT);",
            "CREATE TABLE IF NOT EXISTS lostfound_likes (post_id BIGINT NOT NULL, user_id BIGINT);",
            "CREATE TABLE IF NOT EXISTS moment_likes (moment_id BIGINT NOT NULL, user_id BIGINT);",
            "CREATE TABLE IF NOT EXISTS campaign_likes (campaign_id BIGINT NOT NULL, user_id BIGINT);"
        };

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            
            for (String query : queries) {
                stmt.execute(query);
                System.out.println("Executed: " + query);
            }
            System.out.println("All tables created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
