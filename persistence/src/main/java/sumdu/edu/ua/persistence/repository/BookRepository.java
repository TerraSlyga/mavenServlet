package sumdu.edu.ua.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sumdu.edu.ua.persistence.entity.BookEntity;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {

    @Query("SELECT b FROM BookEntity b WHERE :q IS NULL OR :q = '' " +
           "OR LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<BookEntity> search(@Param("q") String q, Pageable pageable);

    @Query("SELECT b FROM BookEntity b WHERE :q IS NULL OR :q = '' " +
           "OR LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<BookEntity> search(@Param("q") String q);
}
