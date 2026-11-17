package sumdu.edu.ua.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sumdu.edu.ua.persistence.entity.CommentEntity;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @Query("SELECT c FROM CommentEntity c WHERE c.user.id = :userId")
    List<CommentEntity> findByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM CommentEntity c WHERE c.book.id = :bookId")
    List<CommentEntity> findByBookId(@Param("bookId") Long bookId);

    @Query("SELECT c FROM CommentEntity c WHERE c.book.id = :bookId")
    Page<CommentEntity> findByBookId(@Param("bookId") Long bookId, Pageable pageable);
}
