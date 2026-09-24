package org.example.db;

public class CommentDao {
    public void init() throws java.sql.SQLException {
        try (var c = Db.get(); var st = c.createStatement()) {
            st.execute("""
        create table if not exists comments(
          id identity primary key,
          author varchar(64) not null,
          text   varchar(1000) not null,
          created_at timestamp not null default current_timestamp
        )""");
        }
    }

    public void add(String author, String text) throws java.sql.SQLException {
        try (var c = Db.get();
             var ps = c.prepareStatement("insert into comments(author,text) values(?,?)")) {
            ps.setString(1, author);
            ps.setString(2, text);
            ps.executeUpdate();
        }
    }

    public java.util.List<java.util.Map<String,Object>> list() throws java.sql.SQLException {
        try (var c = Db.get();
             var ps = c.prepareStatement("select id,author,text,created_at from comments order by id desc");
             var rs = ps.executeQuery()) {
            var out = new java.util.ArrayList<java.util.Map<String,Object>>();
            while (rs.next()) {
                out.add(java.util.Map.of(
                        "id", rs.getLong("id"),
                        "author", rs.getString("author"),
                        "text", rs.getString("text"),
                        "createdAt", rs.getTimestamp("created_at").toInstant().toString()
                ));
            }
            return out;
        }
    }
}
